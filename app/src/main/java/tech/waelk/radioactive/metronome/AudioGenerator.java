/*
 * Copyright (c) 2020.
 *  This file is part of Metronome.
 *
 * Metronome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metronome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Metronome.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package tech.waelk.radioactive.metronome;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

public class AudioGenerator {
	private final static double positiveRound = 1.0;
	private final static double negativeRound = -1.0;
	private int sampleRate;
	private AudioTrack audioTrack;

	public AudioGenerator(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
		double[] sample = new double[samples];
		for (int i = 0; i < samples; i++) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequencyOfTone));
		}
		return sample;
	}

	public double[] getSawtoothWave(int samples, int sampleRate, double frequencyOfTone) {
		double[] sample = new double[samples];
		for (int i = 0; i < samples; i++) {
			sample[i] = 2 * (i % (sampleRate / frequencyOfTone)) / (sampleRate / frequencyOfTone) - 1;
		}
		return sample;
	}

	public double[] getPWMWave(int samples, int sampleRate, double frequencyOfTone) {
		double[] sample = getSineWave(samples, sampleRate, frequencyOfTone);
		//turn the sine wave into a PWM wave
		for (int i = 0; i < sample.length; i++) {
			//sample[i] = Math.round(sample[i]);
			sample[i] = getSign(sample[i]);
		}
		//return the modified sample
		return sample;
	}

	public double[] getThinPWMWave(int samples, int sampleRate, double frequencyOfTone, double thinness) {
		double[] sample = getPWMWave(samples, sampleRate, frequencyOfTone);
		//if thinness is not a fraction of 1, return square (as this is an error - gracefully fail)
		if (thinness <= 0 || thinness >= 1)
			return sample;

		int length = 0;
		boolean flag = true;
		//count the width
		while(length<sample.length && flag) {
			if(length>0 && sample[length] != sample[length-1])
				flag = false;
			else
				length++;
		}

		//calculate the effective lengths
		int length_high = (int) (thinness * length);
		int length_low = length - length_high;

		//counter
		int j;

		//for debugging, zero out the array
		/*for (int i = 0; i < sample.length; i++) {
			sample[i] = 0;
		}*/

		//resynthesize the wave
		for (int i = 0; i < sample.length - 1;) {
			j = 0;
			while(j < length_high && i < sample.length){
				sample[i] = positiveRound;
				i++;
				j++;
			}
			j=0;
			while(j < length_low && i < sample.length){
				sample[i] = negativeRound;
				i++;
				j++;
			}
		}
		//return the modified sample

		return sample;
	}


	//helper function that returns 1 for positive and -1 for negative
	private double getSign(double value){
		if (value >= 0)
			return positiveRound;
		return negativeRound;
	}

	private byte[] get16BitPcm(double[] samples) {
		byte[] generatedSound = new byte[2 * samples.length];
		int index = 0;
		for (double sample : samples) {
			// scale to maximum amplitude
			short maxSample = (short) ((sample * Short.MAX_VALUE));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSound[index++] = (byte) (maxSample & 0x00ff);
			generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);
		}
		return generatedSound;
	}

	public void createPlayer() {
		//check API version and setup the AudioTrack object accordingly, anything under 26 uses first option.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, sampleRate,
				AudioTrack.MODE_STREAM);
		else {
			AudioAttributes.Builder audioAttribuitesBuilder = new AudioAttributes.Builder();
			audioAttribuitesBuilder.setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
			AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
			audioFormatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO);
			audioTrack = new AudioTrack.Builder().setAudioAttributes(audioAttribuitesBuilder.build()).setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY).setAudioFormat(audioFormatBuilder.build()).build();
		}
		audioTrack.play();
	}

	//used to avoid of an uninitialized state which lead to a crash!
	public void createPlayer(AudioTrack audioTrack) {
		this.audioTrack = audioTrack;
		if (this.audioTrack.getState() != AudioTrack.STATE_INITIALIZED)
			this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRate, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, sampleRate,
					AudioTrack.MODE_STREAM);
		this.audioTrack.play();
	}

	public void writeSound(double[] samples) {
		byte[] generatedSnd = get16BitPcm(samples);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
	}

	public void destroyAudioTrack() {
		audioTrack.stop();
		audioTrack.release();
	}

	public AudioTrack getAudioTrack() {
		return audioTrack;
	}
}
