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

package org.ode.engine.signal_processing


/**
 * Welch Power Spectral Density estimation function.
 *
 * @author Alexandre Degurse
 *
 * @param nfft The size of ftt-computation window
 * @param samplingRate The signal's sampling rate
 */
case class WelchSpectralDensity
(
  nfft: Int,
  samplingRate: Float
) extends Serializable with FrequencyConvertible {

  private val powerSpectrumSize: Int = if (nfft % 2 == 0) nfft / 2 + 1 else (nfft + 1) / 2

  /**
   * Computes Wech estimate of the Power Spectral Density out of
   * multiple periodograms on the signal
   *
   * @param periodograms The periodograms on the signal that must be one-sided
   * The returned PSD estimation has the same normalization as the given periodograms.
   * For instance power density normalized periodogram will result in a power density Welch PSD.
   *
   * @return The Welch Power Spectral Density estimation for the provided periodograms
   */
  def compute(periodograms: Array[Array[Double]]): Array[Double] = {
    if (!periodograms.forall(_.length == powerSpectrumSize)) {
      throw new IllegalArgumentException(
        s"Inconsistent periodogram lengths for Welch aggregation ($powerSpectrumSize)"
      )
    }

    val psdAgg: Array[Double] = new Array[Double](powerSpectrumSize)

    // Using while with local variables on purpose -- See performance test
    // scalastyle:off while var.local
    var i: Int = 0
    var j: Int = 0
    while (i < powerSpectrumSize){
      while(j < periodograms.length) {
        psdAgg(i) += periodograms(j)(i)
        j += 1
      }

      psdAgg(i) /= j

      j = 0
      i += 1
    }
    // scalastyle:on while var.local

    psdAgg
  }
}
