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

import mgi.tools.jagdecs2.CS2Type;

public class FunctionDatabase {

	private File file;
	private FunctionInfo[] info;
	
	public FunctionDatabase(File file) {
		this.file = file;
		this.info = new FunctionInfo[50000];
		this.readDatabase();
	}
	
	public FunctionDatabase() {
		this.info = new FunctionInfo[50000];
	}
	
	
	public void updateFile(int functionChanged, String newDescriptor) {
		if (getInfo(functionChanged) == null)
			throw new RuntimeException("Cannot delete definitions...");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String[] buffer = new String[50000];
			int linesCount = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine())
				buffer[linesCount++] = line;
			reader.close();
			
			for (int i = 0; i < linesCount; i++) {
				String line = buffer[i];
				if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
					continue;
				
				String[] split = line.split("\\ ");
				int opcode = Integer.parseInt(split[0]);
				if (opcode == functionChanged) {
					line = newDescriptor;
				}
				buffer[i] = line;
				
				{
					split = line.split("\\ ");
					opcode = Integer.parseInt(split[0]);
					String name = split[1];
					CS2Type returnType = CS2Type.forDesc(split[2]);
					CS2Type[] argTypes = new CS2Type[(split.length - 2) / 2];
					String[] argNames = new String[(split.length - 2) / 2];
					int write = 0;
					for (int x = 3; x < split.length; x += 2) {
						argTypes[write] = CS2Type.forDesc(split[x]);
						argNames[write++] = split[x + 1];
					}
					info[opcode] = new FunctionInfo(name,argTypes,returnType,argNames);
				}
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < linesCount; i++)
				writer.write(buffer[i] + "\r\n");
			writer.close();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
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
					int opcode = Integer.parseInt(split[0]);
					String name = split[1];
					CS2Type returnType = CS2Type.forDesc(split[2]);
					CS2Type[] argTypes = new CS2Type[(split.length - 2) / 2];
					String[] argNames = new String[(split.length - 2) / 2];
					int write = 0;
					for (int i = 3; i < split.length; i += 2) {
						argTypes[write] = CS2Type.forDesc(split[i]);
						argNames[write++] = split[i + 1];
					}
					info[opcode] = new FunctionInfo(name,argTypes,returnType,argNames);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					reader.close();
					throw new RuntimeException("Error parsing function database file " + this.file + " on line:" + (linesCount + 1));
				}
			}
			reader.close();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	
	
	public FunctionInfo getInfo(int opcode) {
		if (opcode < 0 || opcode >= info.length)
			return null;
		return info[opcode];
	}
	
	public int bufferSize() {
		return info.length;
	}
	
	public int size() {
		int total = 0;
		for (int i = 0; i < info.length; i++) {
			if (info[i] != null)
				total++;
		}
		return total;
	}
	
}
