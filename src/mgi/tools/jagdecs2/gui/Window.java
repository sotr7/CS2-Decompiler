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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.TextField;
import java.io.File;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class Window extends JFrame {

	private Main main;
	private JPanel mainPanel;
	private JTabbedPane tabsPanel;
	private JTextField textField;
	
	
	private BufferedImage background;
	
	/**
	 * Create the frame.
	 */
	public Window(Main _main) {
		this.main = _main;
		
		try {
			background = ImageIO.read(new File("gui/bg.png"));
		}
		catch (Throwable t) {
			background = null;
		}
		
		setTitle("CS2 Decompiler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 526, 554);
		
		JMenuBar optionsMenuBar = new JMenuBar();
		setJMenuBar(optionsMenuBar);
		
		JMenu fileMenu = new JMenu("File");
		optionsMenuBar.add(fileMenu);
		
		JMenuItem btnExit = new JMenuItem("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.exit();
			}
		});
		
		JMenuItem btnLoadScript = new JMenuItem("Load Script");
		btnLoadScript.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		btnLoadScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.loadScript();
			}
		});
		fileMenu.add(btnLoadScript);
		
		JMenuItem btnSaveScript = new JMenuItem("Save Script");
		btnSaveScript.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		btnSaveScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.saveScript(false);
			}
		});
		fileMenu.add(btnSaveScript);
		
		JMenuItem btnSaveScriptAs = new JMenuItem("Save Script As");
		btnSaveScriptAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.saveScript(true);
			}
		});
		fileMenu.add(btnSaveScriptAs);
		fileMenu.add(btnExit);
		
		JMenu decompilerMenu = new JMenu("Decompiler");
		optionsMenuBar.add(decompilerMenu);
		
		JMenuItem btnOptions = new JMenuItem("Options");
		btnOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.openOptions();
			}
		});
		decompilerMenu.add(btnOptions);
		
		JMenu helpMenu = new JMenu("Help");
		optionsMenuBar.add(helpMenu);
		
		JMenuItem btnAbout = new JMenuItem("About");
		btnAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.message("About", "Made by mgi125 (MangiS)");
			}
		});
		helpMenu.add(btnAbout);
		
		textField = new JTextField();
		optionsMenuBar.add(textField);
		textField.setColumns(10);
		
		JButton btnDecompile = new JButton("Decompile");
		btnDecompile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int scriptID = -1;
				try {
					scriptID = Integer.parseInt(textField.getText());
				}
				catch (Throwable t) {
					main.message("Error", "Invalid script ID!");
					return;
				}
				main.loadScript(scriptID);
			}
		});
		optionsMenuBar.add(btnDecompile);
		
		JButton btnCloseTab = new JButton("Close tab");
		btnCloseTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main.closeCurrentTab();
			}
		});
		optionsMenuBar.add(btnCloseTab);
		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPanel);
		
		tabsPanel = new JTabbedPane(JTabbedPane.TOP) {
			@Override
			public void paintComponent(Graphics g) {
				if (this.getTabCount() > 0 || background == null) {
					super.paintComponent(g);
					return;
				}
				
				g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
			}
		};
		mainPanel.add(tabsPanel, BorderLayout.CENTER);
		
	}

	public JTabbedPane getTabsPanel() {
		return tabsPanel;
	}
	
	public Main getMain() {
		return main;
	}
	

}
