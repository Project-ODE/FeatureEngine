/** Copyright (C) 2017 Project-ODE
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

package org.ode.engine.signal_processing;



import org.ode.utils.test.ErrorMetrics.rmse;
import org.scalatest.{FlatSpec, Matchers};
import scala.math.cos;

/**
  * Tests for FFT wrap class
  * Author: Alexandre Degurse
  */


class TestFFT extends FlatSpec with Matchers {

  val maxRMSE = 1E-13

  "FFT" should "compute the same fft as numpy on a fake signal" in {

    val signal: Array[Double] = (0.0 to 10.0 by 0.1).map(cos).toArray
    val fftClass: FFT = new FFT(signal.length)
    val fft: Array[Double] = fftClass.compute(signal)

    val expectedFFT: Array[Double] = Array(
      -5.3552126084096887e+00,  0.0000000000000000e+00,
       -9.3015054823533401e+00,  1.8079330752013401e+01,
        1.2301449694913190e+01, -4.0406477468073383e+01,
        3.4128549118994069e+00, -1.3355268892224943e+01,
        2.0994012555222104e+00, -8.4966782159106984e+00,
        1.6160505842976098e+00, -6.3380135010606864e+00,
        1.3788750523351188e+00, -5.0836662658935818e+00,
        1.2435153452976513e+00, -4.2516287525030307e+00,
        1.1585436034182008e+00, -3.6540868833151894e+00,
        1.1015481111911438e+00, -3.2014605809270416e+00,
        1.0613951988364310e+00, -2.8451644544737404e+00,
        1.0320142113996356e+00, -2.5563866683184662e+00,
        1.0098548546261650e+00, -2.3168796912439511e+00,
        9.9272366156130321e-01, -2.1144872342922332e+00,
        9.7920408060012276e-01, -1.9407790974346453e+00,
        9.6834722763998016e-01, -1.7897146985568362e+00,
        9.5949764481401911e-01, -1.6568464125136015e+00,
        9.5219052853483344e-01, -1.5388233313290691e+00,
        9.4608871769922676e-01, -1.4330705483978579e+00,
        9.4094275351850754e-01, -1.3375752777586316e+00,
        9.3656482152348874e-01, -1.2507403119730753e+00,
        9.3281131436970510e-01, -1.1712812248958895e+00,
        9.2957089881256527e-01, -1.0981527515237579e+00,
        9.2675618451978403e-01, -1.0304950858951591e+00,
        9.2429780225299962e-01, -9.6759405975813373e-01,
        9.2214012591456207e-01, -9.0885117481985833e-01,
        9.2023813639780649e-01, -8.5376074688345194e-01,
        9.1855509150774239e-01, -8.0189226069749853e-01,
        9.1706077344555270e-01, -7.5287659502617077e-01,
        9.1573015580094330e-01, -7.0639515831375854e-01,
        9.1454237909588965e-01, -6.6217123835269764e-01,
        9.1347995591849362e-01, -6.1996305379735284e-01,
        9.1252814874276333e-01, -5.7955812649735339e-01,
        9.1167447894481168e-01, -5.4076868806293366e-01,
        9.1090833643603442e-01, -5.0342790289901251e-01,
        9.1022066714692795e-01, -4.6738674065643726e-01,
        9.0960372125316868e-01, -4.3251136879567353e-01,
        9.0905084917471568e-01, -3.9868096432453826e-01,
        9.0855633543734349e-01, -3.6578586527262014e-01,
        9.0811526276633547e-01, -3.3372599889785959e-01,
        9.0772340049805234e-01, -3.0240953627202249e-01,
        9.0737711269661470e-01, -2.7175173269828956e-01,
        9.0707328235911533e-01, -2.4167392106224997e-01,
        9.0680924886089986e-01, -2.1210263121075928e-01,
        9.0658275639037378e-01, -1.8296881317278058e-01,
        9.0639191159150634e-01, -1.5420714575937369e-01,
        9.0623514900444646e-01, -1.2575541502317011e-01,
        9.0611120319198479e-01, -9.7553949379564811e-02,
        9.0601908668172404e-01, -6.9545100014207287e-02,
        9.0595807305283360e-01, -4.1672756617257486e-02,
        9.0592768466409512e-01, -1.3881889562174343e-02,
        9.0592768466409512e-01,  1.3881889562174343e-02,
        9.0595807305283360e-01,  4.1672756617257486e-02,
        9.0601908668172404e-01,  6.9545100014207287e-02,
        9.0611120319198479e-01,  9.7553949379564811e-02,
        9.0623514900444646e-01,  1.2575541502317011e-01,
        9.0639191159150634e-01,  1.5420714575937369e-01,
        9.0658275639037378e-01,  1.8296881317278058e-01,
        9.0680924886089986e-01,  2.1210263121075928e-01,
        9.0707328235911533e-01,  2.4167392106224997e-01,
        9.0737711269661470e-01,  2.7175173269828956e-01,
        9.0772340049805234e-01,  3.0240953627202249e-01,
        9.0811526276633547e-01,  3.3372599889785959e-01,
        9.0855633543734349e-01,  3.6578586527262014e-01,
        9.0905084917471568e-01,  3.9868096432453826e-01,
        9.0960372125316868e-01,  4.3251136879567353e-01,
        9.1022066714692795e-01,  4.6738674065643726e-01,
        9.1090833643603442e-01,  5.0342790289901251e-01,
        9.1167447894481168e-01,  5.4076868806293366e-01,
        9.1252814874276333e-01,  5.7955812649735339e-01,
        9.1347995591849362e-01,  6.1996305379735284e-01,
        9.1454237909588965e-01,  6.6217123835269764e-01,
        9.1573015580094330e-01,  7.0639515831375854e-01,
        9.1706077344555270e-01,  7.5287659502617077e-01,
        9.1855509150774239e-01,  8.0189226069749853e-01,
        9.2023813639780649e-01,  8.5376074688345194e-01,
        9.2214012591456207e-01,  9.0885117481985833e-01,
        9.2429780225299962e-01,  9.6759405975813373e-01,
        9.2675618451978403e-01,  1.0304950858951591e+00,
        9.2957089881256527e-01,  1.0981527515237579e+00,
        9.3281131436970510e-01,  1.1712812248958895e+00,
        9.3656482152348874e-01,  1.2507403119730753e+00,
        9.4094275351850754e-01,  1.3375752777586316e+00,
        9.4608871769922676e-01,  1.4330705483978579e+00,
        9.5219052853483344e-01,  1.5388233313290691e+00,
        9.5949764481401911e-01,  1.6568464125136015e+00,
        9.6834722763998016e-01,  1.7897146985568362e+00,
        9.7920408060012276e-01,  1.9407790974346453e+00,
        9.9272366156130321e-01,  2.1144872342922332e+00,
        1.0098548546261650e+00,  2.3168796912439511e+00,
        1.0320142113996356e+00,  2.5563866683184662e+00,
        1.0613951988364310e+00,  2.8451644544737404e+00,
        1.1015481111911438e+00,  3.2014605809270416e+00,
        1.1585436034182008e+00,  3.6540868833151894e+00,
        1.2435153452976513e+00,  4.2516287525030307e+00,
        1.3788750523351188e+00,  5.0836662658935818e+00,
        1.6160505842976098e+00,  6.3380135010606864e+00,
        2.0994012555222104e+00,  8.4966782159106984e+00,
        3.4128549118994069e+00,  1.3355268892224943e+01,
        1.2301449694913190e+01,  4.0406477468073383e+01,
       -9.3015054823533401e+00, -1.8079330752013401e+01
    )

    rmse(fft, expectedFFT) should be < maxRMSE

  }

    // The expected fft is computed with numpy
  it should "compute the same fft as Matlab on a fake signal" in {

    val signal: Array[Double] = (0.0 to 10.0 by 0.1).map(cos).toArray
    val fftClass: FFT = new FFT(signal.length)
    val fft: Array[Double] = fftClass.compute(signal)

    val expectedFFT: Array[Double] = Array(
      -5.355212608409691377,0.000000000000000000,-9.301505482353340071,
      18.079330752013444084,12.301449694913165089,-40.406477468073362047,
      3.412854911899418475,-13.355268892224975374,2.099401255522210352,
      -8.496678215910703713,1.616050584297619164,-6.338013501060702382,
      1.378875052335118312,-5.083666265893581837,1.243515345297658392,
      -4.251628752503040509,1.158543603418202395,-3.654086883315188050,
      1.101548111191150259,-3.201460580927048660,1.061395198836431630,
      -2.845164454473744886,1.032014211399641557,-2.556386668318468391,
      1.009854854626167864,-2.316879691243955097,0.992723661561308646,
      -2.114487234292240725,0.979204080600126647,-1.940779097434643496,
      0.968347227639985375,-1.789714698556838846,0.959497644814021888,
      -1.656846412513600875,0.952190528534838876,-1.538823331329073563,
      0.946088717699228088,-1.433070548397852750,0.940942753518504316,
      -1.337575277758634940,0.936564821523490521,-1.250740311973083108,
      0.932811314369713984,-1.171281224895894368,0.929570898812570046,
      -1.098152751523753246,0.926756184519785142,-1.030495085895155594,
      0.924297802253001066,-0.967594059758134173,0.922140125914564845,
      -0.908851174819858110,0.920238136397808049,-0.853760746883456156,
      0.918555091507745836,-0.801892260697498971,0.917060773445554145,
      -0.752876595026172213,0.915730155800945744,-0.706395158313760652,
      0.914542379095892422,-0.662171238352699865,0.913479955918499176,
      -0.619963053797355723,0.912528148742767664,-0.579558126497354387,
      0.911674478944816236,-0.540768688062917890,0.910908336436035637,
      -0.503427902899018065,0.910220667146932172,-0.467386740656442756,
      0.909603721253174013,-0.432511368795678142,0.909050849174723341,
      -0.398680964324537923,0.908556335437345264,-0.365785865272617305,
      0.908115262766337583,-0.333725998897863418,0.907723400498053556,
      -0.302409536272013169,0.907377112696617805,-0.271751732698298720,
      0.907073282359119770,-0.241673921062253910,0.906809248860906192,
      -0.212102631210754650,0.906582756390378774,-0.182968813172780409,
      0.906391911591506561,-0.154207145759369391,0.906235149004448681,
      -0.125755415023170669,0.906111203191987680,-0.097553949379562188,
      0.906019086681729147,-0.069545100014203470,0.905958073052837265,
      -0.041672756617243040,0.905927684664094124,-0.013881889562170360,
      0.905927684664094124,0.013881889562170360,0.905958073052837265,
      0.041672756617243040,0.906019086681729147,0.069545100014203470,
      0.906111203191987680,0.097553949379562188,0.906235149004448681,
      0.125755415023170669,0.906391911591506561,0.154207145759369391,
      0.906582756390378774,0.182968813172780409,0.906809248860906192,
      0.212102631210754650,0.907073282359119770,0.241673921062253910,
      0.907377112696617805,0.271751732698298720,0.907723400498053556,
      0.302409536272013169,0.908115262766337583,0.333725998897863418,
      0.908556335437345264,0.365785865272617305,0.909050849174723341,
      0.398680964324537923,0.909603721253174013,0.432511368795678142,
      0.910220667146932172,0.467386740656442756,0.910908336436035637,
      0.503427902899018065,0.911674478944816236,0.540768688062917890,
      0.912528148742767664,0.579558126497354387,0.913479955918499176,
      0.619963053797355723,0.914542379095892422,0.662171238352699865,
      0.915730155800945744,0.706395158313760652,0.917060773445554145,
      0.752876595026172213,0.918555091507745836,0.801892260697498971,
      0.920238136397808049,0.853760746883456156,0.922140125914564845,
      0.908851174819858110,0.924297802253001066,0.967594059758134173,
      0.926756184519785142,1.030495085895155594,0.929570898812570046,
      1.098152751523753246,0.932811314369713984,1.171281224895894368,
      0.936564821523490521,1.250740311973083108,0.940942753518504316,
      1.337575277758634940,0.946088717699228088,1.433070548397852750,
      0.952190528534838876,1.538823331329073563,0.959497644814021888,
      1.656846412513600875,0.968347227639985375,1.789714698556838846,
      0.979204080600126647,1.940779097434643496,0.992723661561308646,
      2.114487234292240725,1.009854854626167864,2.316879691243955097,
      1.032014211399641557,2.556386668318468391,1.061395198836431630,
      2.845164454473744886,1.101548111191150259,3.201460580927048660,
      1.158543603418202395,3.654086883315188050,1.243515345297658392,
      4.251628752503040509,1.378875052335118312,5.083666265893581837,
      1.616050584297619164,6.338013501060702382,2.099401255522210352,
      8.496678215910703713,3.412854911899418475,13.355268892224975374,
      12.301449694913165089,40.406477468073362047,-9.301505482353340071,
      -18.079330752013444084
    )

    rmse(fft, expectedFFT) should be < maxRMSE

  }

  it should "raise IllegalArgumentException when given a signal of the wrong length" in {
    val signal: Array[Double] = new Array[Double](100)
    val fftClass: FFT = new FFT(10)

    an [IllegalArgumentException] should be thrownBy fftClass.compute(signal)
  }

}