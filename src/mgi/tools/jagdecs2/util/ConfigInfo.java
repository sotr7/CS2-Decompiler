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

import mgi.tools.jagdecs2.CS2Type;

public class ConfigInfo {
	
	private String domainName;
	private String configName;
	private CS2Type type;
	
	public ConfigInfo(String domainName, String configName, CS2Type type) {
		this.domainName = domainName;
		this.configName = configName;
		this.type = type;
	}

	public String getDomainName() {
		return domainName;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public CS2Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "[" + domainName + "," + configName + "," + type + "]";
	}
	
}
