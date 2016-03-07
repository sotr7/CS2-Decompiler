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


package tests;

import java.io.File;
import java.io.IOException;

import mgi.tools.jagdecs2.CS2;
import mgi.tools.jagdecs2.CS2Decoder;
import mgi.tools.jagdecs2.ICS2Provider;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.InstructionsDatabase;

public class TestProvider implements ICS2Provider {


	@Override
	public CS2 getCS2(InstructionsDatabase idb, ConfigsDatabase cdb, FunctionDatabase sdb, FunctionDatabase odb, int id) {
		try {
			return CS2Decoder.readScript(idb, cdb, new File("scripts/" + id));
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
