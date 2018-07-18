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

import org.apache.spark.sql.SparkSession

import com.github.nscala_time.time.Imports._

import org.apache.spark.SparkException
import org.scalatest.{FlatSpec, Matchers}
import org.oceandataexplorer.utils.test.OdeCustomMatchers
import com.holdenkarau.spark.testing.SharedSparkContext

/**
 * Tests for PerformanceTestWorkflow that compares its computations with ScalaPerformanceTestWorkflow
 *
 * @author Alexandre Degurse, Joseph Allemandou
 */

class TestPerformanceTestWorkflow extends FlatSpec
  with Matchers with SharedSparkContext with OdeCustomMatchers {

  /**
   * Maximum error allowed for [[OdeCustomMatchers.RmseMatcher]]
   */
  val maxRMSE = 1.0E-16

  "PerformanceTestWorkflow" should "generate results of expected size" in {

    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val recordSizeInSec = 1.0f
    val soundSamplingRate = 16000.0f
    val windowSize = 16000
    val windowOverlap = 0
    val nfft = 16000

    // Sound parameters
    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime("1978-04-11T13:14:20.200Z")))
    val soundChannels = 1
    val soundSampleSizeInBits = 16
    val soundDurationInSecs = 2.5f


    val perfTestWorkflow = new PerformanceTestWorkflow(
      spark,
      recordSizeInSec,
      windowSize,
      windowOverlap,
      nfft
    )

    val results = perfTestWorkflow(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val expectedRecordNumber = (soundDurationInSecs / recordSizeInSec).toInt
    val expectedFFTSize = nfft + 2 // nfft is even


    val sparkWelchs = results.select("welchs").collect()
    val sparkSPL = results.select("spls").collect()

    sparkWelchs should have size expectedRecordNumber
    sparkSPL should have size expectedRecordNumber

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
  }

  it should "generate the same results as the pure scala workflow" in {
    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val recordSizeInSec = 1.0f
    val soundSamplingRate = 16000.0f
    val windowSize = 16000
    val windowOverlap = 0
    val nfft = 16000
    val lowFreq = Some(3000.0)
    val highFreq = Some(7000.0)

    // Sound parameters
    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI
    val soundChannels = 1
    val soundSampleSizeInBits = 16
    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime(soundStartDate)))


    val perfTestWorkflow = new PerformanceTestWorkflow(
      spark,
      recordSizeInSec,
      windowSize,
      windowOverlap,
      nfft
    )

    val sparkResults = perfTestWorkflow(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val sparkTs: Array[Long] = sparkResults
      .select("timestamp")
      .collect()
      .map{channels =>
        val javaTs = channels.getTimestamp(0)
        new DateTime(javaTs).instant.millis
      }
      .toArray

    val sparkWelchs: Array[Array[Array[Double]]] = sparkResults
      .select("welchs")
      .collect()
      .map{channels =>
        channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
          .map(_.toArray).toArray
      }
      .toArray

    val sparkSPLs: Array[Array[Array[Double]]] = sparkResults
      .select("spls")
      .collect()
      .map{channels =>
        channels.getSeq(0).asInstanceOf[Seq[Seq[Double]]]
          .map(_.toArray).toArray
      }
      .toArray

    val welchs = sparkTs.zip(sparkWelchs)
    val spls = sparkTs.zip(sparkSPLs)

    val scalaWorkflow = new ScalaSampleWorkflow(
      recordSizeInSec,
      windowSize,
      windowOverlap,
      nfft,
      lowFreq,
      highFreq
    )

    val resultsScala = scalaWorkflow(
      soundUri,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits,
      soundStartDate
    )

    val scalaWelchs = resultsScala("welchs").right.get
    val scalaSPLs = resultsScala("spls").right.get

    welchs should rmseMatch(scalaWelchs)
    spls should rmseMatch(scalaSPLs)
  }

  it should "generate the results with the right timestamps" in {
    val spark = SparkSession.builder.getOrCreate

    // Signal processing parameters
    val recordSizeInSec = 1.0f
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
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime(soundStartDate)))


    val perfTestWorkflow = new PerformanceTestWorkflow(
      spark,
      recordSizeInSec,
      windowSize,
      windowOverlap,
      nfft
    )

    val results = perfTestWorkflow(
      soundUri.toString,
      soundsNameAndStartDate,
      soundSamplingRate,
      soundChannels,
      soundSampleSizeInBits
    )

    val timestampsSpark = results.select("timestamp").collect()

    val lastRecordStartTime = timestampsSpark.toSeq.last.getTimestamp(0)
    val lastRecordStartDate = new DateTime(lastRecordStartTime)

    val startDate = new DateTime(soundStartDate)

    val duration = lastRecordStartDate.instant.millis - startDate.instant.millis
    val expectedLastRecordDate = new DateTime("1978-04-11T13:14:21.200Z")

    duration shouldEqual 1000
    lastRecordStartDate shouldEqual expectedLastRecordDate
  }

  it should "raise an IllegalArgumentException when record size is not round" in {
    val spark = SparkSession.builder.getOrCreate

    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI

    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("sin_16kHz_2.5s.wav", new DateTime(soundStartDate)))

    val perfTestworkflow = new PerformanceTestWorkflow(spark, 0.1f, 100, 0, 100)

    the[IllegalArgumentException] thrownBy {
      perfTestworkflow(soundUri.toString, soundsNameAndStartDate, 1.0f, 1, 16)
    } should have message "Computed record size (0.1) should not have a decimal part."
  }

  it should "raise an IOException/SparkException when given a wrong sample rate" in {
    val spark = SparkSession.builder.getOrCreate

    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI

    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("wrongFileName.wav", new DateTime(soundStartDate)))


    val perfTestworkflow = new PerformanceTestWorkflow(spark, 1.0f, 100, 0, 100)

    // even though test succeeds, a missive amount of log is displayed
    spark.sparkContext.setLogLevel("OFF")

    val thrown = the[SparkException] thrownBy {
      val df = perfTestworkflow(soundUri.toString, soundsNameAndStartDate, 1.0f, 1, 16)
      df.take(1)
    }

    spark.sparkContext.setLogLevel("WARN")

    thrown.getMessage should include("sample rate (16000.0) doesn't match configured one (1.0)")
  }

  it should "raise an IllegalArgumentException when given list of files with duplicates" in {
    val spark = SparkSession.builder.getOrCreate

    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI

    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(
      ("sin_16kHz_2.5s.wav", new DateTime(soundStartDate)),
      ("sin_16kHz_2.5s.wav", new DateTime(soundStartDate))
    )

    val perfTestworkflow = new PerformanceTestWorkflow(spark, 1.0f, 100, 0, 100)

    the[IllegalArgumentException] thrownBy {
      val df = perfTestworkflow(soundUri.toString, soundsNameAndStartDate, 1.0f, 1, 16)
      df.take(1)
    } should have message "Sounds list contains duplicate filename entries"
  }

  it should "raise an IllegalArgumentException/SparkException when a unexpected wav file is encountered" in {
    val spark = SparkSession.builder.getOrCreate

    val soundUri = getClass.getResource("/wav/sin_16kHz_2.5s.wav").toURI

    val soundStartDate = "1978-04-11T13:14:20.200Z"
    val soundsNameAndStartDate = List(("wrong_name.wav", new DateTime(soundStartDate)))

    val perfTestworkflow = new PerformanceTestWorkflow(spark, 1.0f, 100, 0, 100)

    // even though test succeeds, a missive amount of log is displayed
    spark.sparkContext.setLogLevel("OFF")

    val thrown = the[SparkException] thrownBy {
      val df = perfTestworkflow(soundUri.toString, soundsNameAndStartDate, 16000.0f, 1, 16)
      df.take(1)
    }

    spark.sparkContext.setLogLevel("WARN")

    thrown.getMessage should include("Read file sin_16kHz_2.5s.wav has no startDate in given list")
  }
}
