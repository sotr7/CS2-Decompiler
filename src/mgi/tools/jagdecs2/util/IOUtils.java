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

import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFileChooser;

public class IOUtils {

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
    
    
    public static File selectFolder(Component parent, boolean isOpen) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = isOpen ? chooser.showOpenDialog(parent) : chooser.showSaveDialog(parent);
		if (option != JFileChooser.APPROVE_OPTION)
			return null;
		java.io.File folder = chooser.getSelectedFile();
		if (folder == null)
			return null;
		return folder;
    }
    
    public static File selectFile(Component parent, boolean isOpen) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int option = isOpen ? chooser.showOpenDialog(parent) : chooser.showSaveDialog(parent);
		if (option != JFileChooser.APPROVE_OPTION)
			return null;
		java.io.File folder = chooser.getSelectedFile();
		if (folder == null)
			return null;
		return folder;
    }
    
    public static File selectAny(Component parent, boolean isOpen) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int option = isOpen ? chooser.showOpenDialog(parent) : chooser.showSaveDialog(parent);
		if (option != JFileChooser.APPROVE_OPTION)
			return null;
		java.io.File selected = chooser.getSelectedFile();
		if (selected == null)
			return null;
		return selected;
    }
}
