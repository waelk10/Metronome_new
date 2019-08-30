/*
 * Copyright (c) 2019.
 * This file is part of Metronome.
 *
 *      Metronome is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Metronome is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with Metronome.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.radioactivemineral.metronome;

public class SaveValues {
	String wave;
	double[] values;

	public SaveValues(String wave, double[] values) {
		this.wave = wave;
		this.values = values;
	}

	public String getWave() {
		return wave;
	}

	public void setWave(String wave) {
		this.wave = wave;
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}
}
