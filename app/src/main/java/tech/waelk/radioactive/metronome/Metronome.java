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

public class Metronome {
	public final static String WAVE_TYPE_SINE = "SINE";
	public final static String WAVE_TYPE_PWM = "PWM";
	public final static String WAVE_TYPE_PWM_THIN = "PWM_THIN";
	public final static String WAVE_TYPE_SAWTOOTH = "SAWTOOTH";
	private final int samples = 1000; // samples of tick
	private final static int sampleRate = 8000;
	private double bpm;
	private int beat;
	private int silence;
	private double beatSound;
	private double sound;
	private double thinness;
	private String wave;
	private boolean play = true;
	private AudioGenerator audioGenerator;

	public Metronome(String waveType) {
		audioGenerator = new AudioGenerator(sampleRate);
		audioGenerator.createPlayer();
		wave = waveType;
	}

	//used for thin PWM
	public Metronome(String waveType, double thinness) {
		audioGenerator = new AudioGenerator(sampleRate);
		audioGenerator.createPlayer();
		wave = waveType;
		this.thinness = thinness;
	}

	public Metronome(AudioGenerator audioGenerator, String waveType) {
		this.audioGenerator = audioGenerator;
		audioGenerator.createPlayer(audioGenerator.getAudioTrack());
		wave = waveType;
	}

	//used for thin PWM
	public Metronome(AudioGenerator audioGenerator, String waveType, double thinness) {
		this.audioGenerator = audioGenerator;
		audioGenerator.createPlayer(audioGenerator.getAudioTrack());
		//fallback to sine if called improperly
		if (!waveType.contentEquals(Metronome.WAVE_TYPE_PWM_THIN)){
			wave = waveType;
			this.thinness = thinness;
		}else{
			wave = Metronome.WAVE_TYPE_SINE;
		}
	}

	public void calcSilence() {
		silence = (int) (((60 / bpm) * sampleRate) - samples);
	}

	public void play() {
		calcSilence();
		double[] tick = audioGenerator.getSineWave(samples, sampleRate, beatSound);
		double[] tock = audioGenerator.getSineWave(samples, sampleRate, sound);
		//start according to wave type
		if (!wave.contentEquals(WAVE_TYPE_SINE))
			switch (wave) {
				//select the proper waveform
				case Metronome.WAVE_TYPE_PWM_THIN:
					tick = audioGenerator.getThinPWMWave(samples, sampleRate, beatSound, thinness);
					tock = audioGenerator.getThinPWMWave(samples, sampleRate , sound, thinness);
					break;
				case Metronome.WAVE_TYPE_PWM:
					tick = audioGenerator.getPWMWave(samples, sampleRate, beatSound);
					tock = audioGenerator.getPWMWave(samples, sampleRate, sound);
					break;
				case Metronome.WAVE_TYPE_SAWTOOTH:
					tick = audioGenerator.getSawtoothWave(samples, sampleRate, beatSound);
					tock = audioGenerator.getSawtoothWave(samples, sampleRate, sound);
					break;
			}
		double silence = 0;
		double[] sound = new double[8000];
		int t = 0, s = 0, b = 0;
		do {
			for (int i = 0; i < sound.length && play; i++) {
				if (t < this.samples) {
					if (b == 0)
						sound[i] = tock[t];
					else
						sound[i] = tick[t];
					t++;
				} else {
					sound[i] = silence;
					s++;
					if (s >= this.silence) {
						t = 0;
						s = 0;
						b++;
						if (b > (this.beat - 1))
							b = 0;
					}
				}
			}
			audioGenerator.writeSound(sound);
		} while (play);
	}

	public void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	public double getBpm() {
		return bpm;
	}

	public void setBpm(double bpm) {
		this.bpm = bpm;
	}

	public int getBeat() {
		return beat;
	}

	public void setBeat(int beat) {
		this.beat = beat;
	}

	public double getBeatSound() {
		return beatSound;
	}

	public void setBeatSound(double beatSound) {
		this.beatSound = beatSound;
	}

	public double getSound() {
		return sound;
	}

	public void setSound(double sound) {
		this.sound = sound;
	}

	public AudioGenerator getAudioGenerator() {
		return audioGenerator;
	}

	public String getWaveType() {
		return wave;
	}

	public void setWaveType(String waveType) {
		switch (waveType) {
			case Metronome.WAVE_TYPE_SINE:
				this.setWaveTypeSine();
				break;
			case Metronome.WAVE_TYPE_PWM_THIN:
				this.setWaveTypePwmThin();
				break;
			case Metronome.WAVE_TYPE_PWM:
				this.setWaveTypePwm();
				break;
			case Metronome.WAVE_TYPE_SAWTOOTH:
				this.setWaveTypeSawtooth();
				break;
		}
	}

	public void setWaveTypeSine() {
		this.wave = WAVE_TYPE_SINE;
	}

	public void setWaveTypePwm() {
		this.wave = WAVE_TYPE_PWM;
	}

	public void setWaveTypePwmThin(){
		this.wave = WAVE_TYPE_PWM_THIN;
	}

	public void setWaveTypeSawtooth() {
		this.wave = WAVE_TYPE_SAWTOOTH;
	}

	//copy maker
	public Metronome copyMetronome() {
		if(!play)
			this.stop();
		Metronome metronomeCopy;
		metronomeCopy = new Metronome(this.getAudioGenerator(), this.getWaveType());
		metronomeCopy.setSound(this.getSound());
		metronomeCopy.setBeatSound(this.getBeatSound());
		metronomeCopy.setBpm(this.getBpm());
		metronomeCopy.setBeat(this.getBeat());
		metronomeCopy.setThinness(this.getThinness());
		return metronomeCopy;
	}

	public Boolean playRes() {
		this.play();
		return Boolean.TRUE;
	}

	public void setThinness(double thinness) {
		this.thinness = thinness;
	}

	public double getThinness(){
		return this.thinness;
	}
}
