/** Copyright (C) 2017-2018 Project-ODE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.oceandataexplorer.engine.workflows

import com.github.nscala_time.time.Imports._
import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.oceandataexplorer.engine.io.HadoopWavReader
import org.oceandataexplorer.engine.signalprocessing.SoundCalibration
import org.oceandataexplorer.utils.test.OdeCustomMatchers
import org.scalatest.{FlatSpec, Matchers}


/**
 * Tests for [[WelchSplTolWorkflow]] class
 * It uses [[ScalaSampleWorkflow]] as reference to test [[WelchSplTolWorkflow]] results.
 *
 * @author Alexandre Degurse, Joseph Allemandou
 */

class TestWelchSplTolWorkflow extends FlatSpec
  with Matchers with SharedSparkContext with OdeCustomMatchers {

  /**
   * Maximum error allowed for [[OdeCustomMatchers.RmseMatcher]]
   */
  val maxRMSE = 1.0E-16

  "WelchSplTolWorkflow" should "generate results of expected size" in {

    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val segmentDuration = 1.0f
    val soundSamplingRate = 16000.0f
    val windowSize = 16000
    val windowOverlap = 0
    val nfft = 16000
    val lowFreqTOL = Some(3000.0)
    val highFreqTOL = Some(7000.0)

    // Sound parameters
    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime("1978-04-11T13:14:20.200Z")))
    val soundChannels = 1
    val soundSampleSizeInBits = 16
    val soundDurationInSecs = 2.5f

    val hadoopWavReader = new HadoopWavReader(
      spark,
      segmentDuration
    )

    val records = hadoopWavReader.readWavRecords(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val calibrationClass = SoundCalibration(0.0)

    val calibratedRecords: RDD[Record] = records.mapValues(chan => chan.map(calibrationClass.compute))

    val sampleWorkflow = new WelchSplTolWorkflow(
      spark,
      segmentDuration,
      windowSize,
      windowOverlap,
      nfft,
      lowFreqTOL,
      highFreqTOL
    )

    val results = sampleWorkflow(
      calibratedRecords,
      soundSamplingRate
    )

    val expectedRecordNumber = (soundDurationInSecs / segmentDuration).toInt
    val expectedFFTSize = nfft + 2 // nfft is even
    val expectedNumTolBand = 4


    val sparkWelchs = results.select("welch").collect()
    val sparkSPL = results.select("spl").collect()
    val sparkTOL = results.select("tol").collect()

    sparkWelchs should have size expectedRecordNumber
    sparkSPL should have size expectedRecordNumber
    sparkTOL should have size expectedRecordNumber

    sparkWelchs.foreach{channels =>
      channels should have size 1
      val chans = channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
      chans.foreach(channel => channel should have length (expectedFFTSize / 2))
    }

    sparkSPL.foreach{channels =>
      channels should have size 1
      val chans = channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
      chans.foreach(channel => channel should have length 1 )
    }

    sparkTOL.foreach{channels =>
      channels should have size 1
      val chans = channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
      chans.foreach(channel => channel should have length expectedNumTolBand )
    }
  }

  it should "generate the same results as the pure scala workflow without tol" in {
    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val segmentDuration = 1.0f
    val soundSamplingRate = 16000.0f
    val windowSize = 6000
    val windowOverlap = 3000
    val nfft = 7000
    val lowFreqTOL = Some(3000.0)
    val highFreqTOL = Some(7000.0)

    // Sound parameters
    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI
    val soundChannels = 1
    val soundSampleSizeInBits = 16
    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime(soundStartDate)))

   val hadoopWavReader = new HadoopWavReader(
      spark,
      segmentDuration
    )

    val records = hadoopWavReader.readWavRecords(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val calibrationClass = SoundCalibration(0.0)

    val calibratedRecords: RDD[Record] = records.mapValues(chan => chan.map(calibrationClass.compute))

    val sampleWorkflow = new WelchSplTolWorkflow(
      spark,
      segmentDuration,
      windowSize,
      windowOverlap,
      nfft,
      lowFreqTOL,
      highFreqTOL
    )

    val sparkResults = sampleWorkflow(
      calibratedRecords,
      soundSamplingRate
    )

    val sparkTs: Array[Long] = sparkResults
      .select("timestamp")
      .collect()
      .map{channels =>
        val javaTs = channels.getTimestamp(0)
        new DateTime(javaTs).instant.millis
      }

    val sparkWelchs: Array[Array[Array[Double]]] = sparkResults
      .select("welch")
      .collect()
      .map{channels =>
        channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
          .map(_.toArray).toArray
      }

    val sparkSPLs: Array[Array[Array[Double]]] = sparkResults
      .select("spl")
      .collect()
      .map{channels =>
        channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
          .map(_.toArray).toArray
      }

    val sparkTOLs: Array[Array[Array[Double]]] = sparkResults
      .select("tol")
      .collect()
      .map { channels =>
        channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
          .map(_.toArray).toArray
      }

    val welchs = sparkTs.zip(sparkWelchs)
    val spls = sparkTs.zip(sparkSPLs)
    val tols = sparkTs.zip(sparkTOLs)

    val scalaWorkflow = new ScalaSampleWorkflow(
      segmentDuration,
      windowSize,
      windowOverlap,
      nfft,
      lowFreqTOL,
      highFreqTOL
    )

    val resultsScala = scalaWorkflow(
      soundUri,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits,
      soundStartDate
    )

    val scalaWelchs = resultsScala("welch").right.get
    val scalaSPLs = resultsScala("spl").right.get
    val scalaTOLs = resultsScala("tol").right.get

    welchs should rmseMatch(scalaWelchs)
    spls should rmseMatch(scalaSPLs)
    tols should rmseMatch(scalaTOLs)
  }

  it should "generate the results with the right timestamps" in {
    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val segmentDuration = 1.0f
    val soundSamplingRate = 16000.0f
    val windowSize = 16000
    val windowOverlap = 0
    val nfft = 16000

    // Sound parameters
    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI
    val soundChannels = 1
    val soundSampleSizeInBits = 16

    // Usefull for testing
    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime(soundStartDate, DateTimeZone.UTC)))

    val hadoopWavReader = new HadoopWavReader(
      spark,
      segmentDuration
    )

    val records = hadoopWavReader.readWavRecords(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val calibrationClass = SoundCalibration(0.0)

    val calibratedRecords: RDD[Record] = records.mapValues(chan => chan.map(calibrationClass.compute))

    val sampleWorkflow = new WelchSplTolWorkflow(
      spark,
      segmentDuration,
      windowSize,
      windowOverlap,
      nfft
    )

    val results = sampleWorkflow(
      calibratedRecords,
      soundSamplingRate
    )

    val timestampsSpark = results.select("timestamp").collect()

    val lastRecordStartTime = timestampsSpark.toSeq.last.getTimestamp(0)
    val lastRecordStartDate = new DateTime(lastRecordStartTime, DateTimeZone.UTC)

    val startDate = new DateTime(soundStartDate, DateTimeZone.UTC)

    val duration = lastRecordStartDate.instant.millis - startDate.instant.millis
    val expectedLastRecordDate = new DateTime("1978-04-11T13:14:21.200Z", DateTimeZone.UTC)

    duration shouldEqual 1000
    lastRecordStartDate shouldEqual expectedLastRecordDate
  }

  it should "raise an IllegalArgumentException when trying to compute TOL on with recordDuration < 1.0 sec" in {
    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val segmentDuration = 0.1f
    val windowSize = 256
    val windowOverlap = 0
    val nfft = 256
    val lowFreqTOL = Some(20.0)
    val highFreqTOL = Some(40.0)

    the[IllegalArgumentException] thrownBy {
       new WelchSplTolWorkflow(
        spark,
        segmentDuration,
        windowSize,
        windowOverlap,
        nfft,
        lowFreqTOL,
        highFreqTOL
      )
    } should have message "Incorrect segmentDuration (0.1) for TOL computation"
  }
}
