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


package mgi.tools.jagdecs2.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mgi.tools.jagdecs2.CS2;
import mgi.tools.jagdecs2.CS2Decoder;
import mgi.tools.jagdecs2.CS2Decompiler;
import mgi.tools.jagdecs2.ICS2Provider;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.util.ByteBuffer;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.IOUtils;
import mgi.tools.jagdecs2.util.InstructionsDatabase;
import mgi.tools.jagdecs2.util.Options;
import mgi.tools.jagdecs2.util.UnsafeSerializer;

public class Main {

	private UnsafeSerializer serializer;
	private Options options;
	private Window window;
	
	private InstructionsDatabase instructionsDB;
	private ConfigsDatabase configsDB;
	private FunctionDatabase opcodesDB;
	private FunctionDatabase scriptsDB;
	private CS2Decompiler decompiler;
	
	public static void main(String[] args) {
		new Main().init();
	}
	
	public void init() {
		serializer = new UnsafeSerializer();
		options = new Options(new File("gui/options.ini"));
		reloadDatabases();
		window = new Window(this);
		
		window.setVisible(true);
	}
	
	public void loadScript(int scriptID) {
		if (window.getTabsPanel().getTabCount() > 10) {
			message("Error", "Too much tabs!");
			return;
		}
		
		FunctionNode function = decompile(scriptID);
		if (function == null) {
			message("Error", "Error while loading script!");	
			return;
		}
		
		window.getTabsPanel().addTab("Editor - " + function.getName(), new EditorPanel(window, scriptID, function));
		window.getTabsPanel().setSelectedIndex(window.getTabsPanel().getTabCount() - 1);
	}

	public void loadScript() {
		if (window.getTabsPanel().getTabCount() > 10) {
			message("Error", "Too much tabs!");
			return;
		}
		
		File file = IOUtils.selectFile(window, true);
		if (file == null || !file.exists())
			return;
		
		try {
			ByteBuffer buffer = new ByteBuffer((int)file.length());
			FileInputStream fis = new FileInputStream(file);
			fis.read(buffer.getBuffer());
			fis.close();
			
			if (buffer.readInt() != 0xBABECAFE)
				throw new RuntimeException("Wrong magic.");
			int scriptID = buffer.readInt();
			byte[] data = new byte[buffer.getBuffer().length - 8];
			buffer.readBytes(data, 0, data.length);
			
			Object o = serializer.readObject(new ByteBuffer(data));
			if (!(o instanceof FunctionNode))
				throw new RuntimeException("Not function node object.");
			FunctionNode function = (FunctionNode)o;
			
			EditorPanel panel = new EditorPanel(window, scriptID, function);
			panel.setSaveFile(file);
			
			window.getTabsPanel().addTab("Editor - " + function.getName(), panel);
			window.getTabsPanel().setSelectedIndex(window.getTabsPanel().getTabCount() - 1);
		}
		catch (Throwable t) {
			t.printStackTrace();
			message("Error", "Error while loading script!");	
		}
	}	

	public void saveScript(boolean forceChoose) {
		if (getCurrentEditor() == null) {
			message("Error", "Please select editor tab first!");
			return;
		}
		
		EditorPanel editor = getCurrentEditor();
		if (editor.getSaveFile() == null || forceChoose) {
			File file = IOUtils.selectFile(window, false);
			if (file == null)
				return;
			editor.setSaveFile(file);
		}
		
		try {
			byte[] buffer = serializer.writeObject(editor.getFunction()).getBuffer();
			ByteBuffer data = new ByteBuffer(buffer.length + 8);
			data.writeInt(0xBABECAFE);
			data.writeInt(editor.getScriptID());
			data.writeBytes(buffer, 0, buffer.length);
			if (editor.getSaveFile().exists())
				editor.getSaveFile().delete();
			FileOutputStream fos = new FileOutputStream(editor.getSaveFile());
			fos.write(data.getBuffer());
			fos.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
			message("Error", "Error while saving script!");
		}
	}
	
	public void saveOptions() {
		options.save();
		reloadDatabases();
	}
	
	public void reloadDatabases() {
		instructionsDB = new InstructionsDatabase(new File(options.getOption("instructions_db_path")));
		configsDB = new ConfigsDatabase(new File("configs_db.ini"), new File("bitconfigs_db.ini")); // TODO
		opcodesDB = new FunctionDatabase(new File(options.getOption("opcodes_db_path")));
		scriptsDB = new FunctionDatabase(new File(options.getOption("scripts_db_path")));

		decompiler = new CS2Decompiler(instructionsDB, configsDB, opcodesDB, scriptsDB, new ICS2Provider() {
			@Override
			public CS2 getCS2(InstructionsDatabase idb, ConfigsDatabase cdb, FunctionDatabase sdb, FunctionDatabase odb, int id) {
				try {
					return CS2Decoder.readScript(idb, cdb, new File(options.getOption("scripts_path") + id));
				}
				catch (Throwable t) {
					return null;
				}
			}
		});
		
	}

	public void openOptions() {
		for (int i = 0; i < window.getTabsPanel().getTabCount(); i++) {
			if (window.getTabsPanel().getComponentAt(i) instanceof OptionsPanel) {
				window.getTabsPanel().setSelectedIndex(i);
				return;
			}
		}
		if (window.getTabsPanel().getTabCount() > 10) {
			message("Error", "Too much tabs!");
			return;
		}
		window.getTabsPanel().addTab("Options", new OptionsPanel(window));
		window.getTabsPanel().setSelectedIndex(window.getTabsPanel().getTabCount() - 1);
	}
	
	public FunctionNode decompile(int scriptID) {
		try {
			return decompiler.decompile(scriptID);
		}
		catch (Throwable t) {
			return null;
		}
	}
	
	public EditorPanel getCurrentEditor() {
		if (window.getTabsPanel().getSelectedIndex() == -1 || !(window.getTabsPanel().getComponentAt(window.getTabsPanel().getSelectedIndex()) instanceof EditorPanel))
			return null;
		return (EditorPanel) window.getTabsPanel().getComponentAt(window.getTabsPanel().getSelectedIndex());
	}
	
	public void closeCurrentTab() {
		if (window.getTabsPanel().getSelectedIndex() == -1)
			return;
		window.getTabsPanel().removeTabAt(window.getTabsPanel().getSelectedIndex());
	}
	
	public String inputBox(String title, String message) {
		return JOptionPane.showInputDialog(window, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public String inputBox(String title, String message, String defaultValue) {
		return (String) JOptionPane.showInputDialog(window, message, title, JOptionPane.INFORMATION_MESSAGE, null, null, defaultValue);
	}
	
	public void message(String title, String message) {
		JOptionPane.showMessageDialog(window, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void exit() {
		System.exit(0);
	}
	
	
	
	public Options getOptions() {
		return options;
	}
	
	public Window getWindow() {
		return window;
	}
	
	public InstructionsDatabase getInstructionsDB() {
		return instructionsDB;
	}
	
	public FunctionDatabase getOpcodesDB() {
		return opcodesDB;
	}
	
	public FunctionDatabase getScriptsDB() {
		return scriptsDB;
	}
	
}
