/*
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package mgi.tools.jagdecs2.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Options {

	private File file;
	private HashMap<String, String> options;
	
	public Options(File file) {
		this.file = file;
		this.options = new HashMap<String, String>();
		reload();
	}
	
	/**
	 * Save's options to file.
	 */
	public void save() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("# Auto generated options - DO NOT EDIT\n\n\n\n");
			
			for (String key : options.keySet()) {
				writer.write(key + " " + options.get(key) + "\n\n");
			}
			
			writer.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Read's options from options file.
	 */
	public void reload() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			int linesCount = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine(), linesCount++) {
				if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
					continue;			
				try {
					int space = line.indexOf(' ');
					if (space <= 0) {
						reader.close();
						throw new RuntimeException("Bad syntax!");
					}
					String key = line.substring(0, space);
					String value = line.substring(space + 1);
					setOption(key, value);
				}
				catch (Exception ex) {
					reader.close();
					throw new RuntimeException("Error parsing options file " + file + " on line:" + (linesCount + 1));
				}
			}
			
			reader.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Get's option.
	 * Return's null if it's not found.
	 */
	public String getOption(String key) {
		if (options.containsKey(key))
			return options.get(key);
		return null;
	}
	
	/**
	 * Set's option.
	 */
	public void setOption(String key, String value) {
		if (options.containsKey(key))
			options.remove(key);
		options.put(key, value);
	}
	
	
	
}
