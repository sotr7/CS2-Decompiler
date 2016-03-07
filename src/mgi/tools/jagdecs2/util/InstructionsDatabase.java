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
import java.io.File;
import java.io.FileReader;

public class InstructionsDatabase {

	private File file;
	private InstructionInfo[] info1;
	private InstructionInfo[] info2;
	
	public InstructionsDatabase(File file) {
		this.file = file;
		this.info1 = new InstructionInfo[40000];
		this.info2 = new InstructionInfo[40000];
		this.readDatabase();
	}
	
	public InstructionsDatabase() {
		this.info1 = new InstructionInfo[40000];
		this.info2 = new InstructionInfo[40000];
	}
	
	

	
	private void readDatabase() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			int linesCount = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine(),linesCount++) {
				if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
					continue;
				try {
					String[] split = line.split("\\ ");
					String name = split[0];
					int scrampled = Integer.parseInt(split[1]);
					int unscrampled = Integer.parseInt(split[2]);
					boolean big = Boolean.parseBoolean(split[3]);
					if (info1[scrampled] != null || info2[unscrampled] != null)
						throw new RuntimeException("redefinition: " + scrampled + "," + unscrampled);
					info1[scrampled] = info2[unscrampled] = new InstructionInfo(name, scrampled, unscrampled, big);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					reader.close();
					throw new RuntimeException("Error parsing instructions database file " + this.file + " on line:" + (linesCount + 1));
				}
			}
			reader.close();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	
	
	public InstructionInfo getByScrampled(int opcode) {
		if (opcode < 0 || opcode >= info1.length)
			return null;
		return info1[opcode];
	}
	
	public InstructionInfo getByUnscrampled(int opcode) {
		if (opcode < 0 || opcode >= info2.length)
			return null;
		return info2[opcode];
	}
	
	
	public int size() {
		int total = 0;
		for (int i = 0; i < info1.length; i++) {
			if (info1[i] != null)
				total++;
		}
		return total;
	}
	
}
