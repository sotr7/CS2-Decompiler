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

import mgi.tools.jagdecs2.CS2Type;

public class ConfigsDatabase {

	private File configsFile;
	private File bitConfigsFile;
	private ConfigInfo[][] configsInfo;
	private BitConfigInfo[] bitConfigsInfo;
	
	public ConfigsDatabase(File configsFile, File bitConfigsFile) {
		this.configsFile = configsFile;
		this.bitConfigsFile = bitConfigsFile;
		this.configsInfo = new ConfigInfo[100][40000];
		this.bitConfigsInfo = new BitConfigInfo[40000];
		this.readConfigsDatabase();
		this.readBitConfigsDatabase();
	}
	
	public ConfigsDatabase() {
		this.configsInfo = new ConfigInfo[100][40000];
		this.bitConfigsInfo = new BitConfigInfo[40000];
	}
	
	

	
	private void readConfigsDatabase() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configsFile));
			int linesCount = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine(),linesCount++) {
				if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
					continue;
				try {
					String[] split = line.split("\\ ");
					int domainType = Integer.parseInt(split[0]);
					int id = Integer.parseInt(split[1]);
					String domainName = split[2];
					String configName = split[3];
					String tstring = split[4];
					CS2Type type;
					if (!tstring.startsWith("."))
						type = CS2Type.forJagexChar(tstring.charAt(0));
					else
						type = CS2Type.forJagexChar((char)Short.parseShort(tstring.substring(1)));
					configsInfo[domainType][id] = new ConfigInfo(domainName, configName, type);
				}
				catch (Exception ex) {
					reader.close();
					throw new RuntimeException("Error parsing configs database file " + this.configsFile + " on line:" + (linesCount + 1), ex);
				}
			}
			reader.close();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private void readBitConfigsDatabase() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(bitConfigsFile));
			int linesCount = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine(),linesCount++) {
				if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
					continue;
				try {
					String[] split = line.split("\\ ");
					int baseDomainId = Integer.parseInt(split[0]);
					int baseId = Integer.parseInt(split[1]);
					int id = Integer.parseInt(split[2]);
					String name = split[3];
					
					ConfigInfo base = getConfigInfo(baseDomainId, baseId);
					if (base == null)
						throw new RuntimeException("Can't find base config " + baseDomainId + "," + baseId);
					
					bitConfigsInfo[id] = new BitConfigInfo(base, name);
				}
				catch (Exception ex) {
					reader.close();
					throw new RuntimeException("Error parsing bitconfigs database file " + this.bitConfigsFile + " on line:" + (linesCount + 1), ex);
				}
			}
			reader.close();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	
	
	public ConfigInfo getConfigInfo(int domainType, int id) {
		if (domainType < 0 || id < 0 || domainType >= configsInfo.length || id >= configsInfo[domainType].length)
			return null;
		return configsInfo[domainType][id];
	}
	
	public BitConfigInfo getBitConfigInfo(int id) {
		if (id < 0 || id >= bitConfigsInfo.length)
			return null;
		return bitConfigsInfo[id];
	}
	
	
	public int configsSize() {
		int total = 0;
		for (int i = 0; i < configsInfo.length; i++) {
			if (configsInfo[i] != null)
				total++;
		}
		return total;
	}
	
	public int bitConfigsSize() {
		int total = 0;
		for (int i = 0; i < bitConfigsInfo.length; i++) {
			if (bitConfigsInfo[i] != null)
				total++;
		}
		return total;
	}
	
}
