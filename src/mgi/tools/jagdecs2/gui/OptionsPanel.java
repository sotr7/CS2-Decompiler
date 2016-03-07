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

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

import mgi.tools.jagdecs2.util.Options;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OptionsPanel extends JPanel {

	private Window window;
	private JTextField instrPath;
	private JTextField opcPath;
	private JTextField scrPath;
	private JTextField scrsPath;
	

	public OptionsPanel(Window window_) {
		window = window_;
		
		setLayout(null);
		
		JLabel lblInstr = new JLabel("Instructions database path:");
		lblInstr.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblInstr.setBounds(43, 11, 184, 14);
		add(lblInstr);
		
		instrPath = new JTextField();
		instrPath.setText("Loading please wait...");
		instrPath.setBounds(43, 31, 184, 20);
		add(instrPath);
		instrPath.setColumns(10);
		
		JLabel lblOpc = new JLabel("Opcodes database path:");
		lblOpc.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblOpc.setBounds(43, 69, 184, 14);
		add(lblOpc);
		
		opcPath = new JTextField();
		opcPath.setText("Loading please wait...");
		opcPath.setColumns(10);
		opcPath.setBounds(43, 89, 184, 20);
		add(opcPath);
		
		JLabel lblScr = new JLabel("Scripts database path:");
		lblScr.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblScr.setBounds(43, 135, 184, 14);
		add(lblScr);
		
		scrPath = new JTextField();
		scrPath.setText("Loading please wait...");
		scrPath.setColumns(10);
		scrPath.setBounds(43, 155, 184, 20);
		add(scrPath);
		
		JLabel lblScrs = new JLabel("Scripts path:");
		lblScrs.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblScrs.setBounds(256, 11, 184, 14);
		add(lblScrs);
		
		scrsPath = new JTextField();
		scrsPath.setText("Loading please wait...");
		scrsPath.setColumns(10);
		scrsPath.setBounds(256, 31, 184, 20);
		add(scrsPath);
		
		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Options options = window.getMain().getOptions();
				options.setOption("instructions_db_path", instrPath.getText());
				options.setOption("opcodes_db_path", opcPath.getText());
				options.setOption("scripts_db_path", scrPath.getText());
				options.setOption("scripts_path", scrsPath.getText());	
				window.getMain().saveOptions();
				window.getMain().closeCurrentTab();
			}
		});
		btnApply.setBounds(138, 252, 89, 23);
		add(btnApply);
		
		JButton btnClose = new JButton("Cancel");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.getMain().closeCurrentTab();
			}
		});
		btnClose.setBounds(237, 252, 89, 23);
		add(btnClose);
		
		
		loadOptions();
	}
	
	private void loadOptions() {
		instrPath.setText(fetchOption("instructions_db_path"));
		opcPath.setText(fetchOption("opcodes_db_path"));
		scrPath.setText(fetchOption("scripts_db_path"));
		scrsPath.setText(fetchOption("scripts_path"));
	}
	
	private String fetchOption(String key) {
		String option = window.getMain().getOptions().getOption(key);
		return option != null ? option : "Not set";
	}
}
