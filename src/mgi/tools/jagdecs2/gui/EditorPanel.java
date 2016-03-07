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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import mgi.tools.jagdecs2.ast.AbstractCodeNode;
import mgi.tools.jagdecs2.ast.BitConfigurationLoadNode;
import mgi.tools.jagdecs2.ast.BitConfigurationStoreNode;
import mgi.tools.jagdecs2.ast.CallExpressionNode;
import mgi.tools.jagdecs2.ast.ConfigurationLoadNode;
import mgi.tools.jagdecs2.ast.ConfigurationStoreNode;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.ast.LocalVariable;
import mgi.tools.jagdecs2.ast.VariableAssignationNode;
import mgi.tools.jagdecs2.ast.VariableLoadNode;
import mgi.tools.jagdecs2.util.BitConfigInfo;
import mgi.tools.jagdecs2.util.ConfigInfo;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.FunctionInfo;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EditorPanel extends JPanel {

	private Window window;
	private File saveFile;
	
	
	private int scriptID;
	private FunctionNode function;
	private JTextPane editor;
	
	private List<Object> tags;
	private String text;
	private AbstractCodeNode[] clip;
	
	private int lastWStart;
	private int lastWEnd;

	public EditorPanel(Window window, int scriptID) {
		this(window, scriptID, window.getMain().decompile(scriptID));
	}

	/**
	 * @wbp.parser.constructor
	 */
	public EditorPanel(Window window_, int scriptID_, FunctionNode function_) {
		
		this.window = window_;
		this.scriptID = scriptID_;
		this.function = function_;
		this.tags = new ArrayList<Object>();
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane editorScroll = new JScrollPane();
		add(editorScroll, BorderLayout.CENTER);
		
		JPanel noscrollPanel = new JPanel();
		editorScroll.setViewportView(noscrollPanel);
		noscrollPanel.setLayout(new BorderLayout(0, 0));
		
		editor = new JTextPane();
		editor.setContentType("text/html");
		editor.setText("<dynamic>");
		//editor.setForeground(new Color(0, 255, 0));
		//editor.setBackground(new Color(0, 0, 0));
		editor.setEditable(false);
		noscrollPanel.add(editor, BorderLayout.CENTER);
		
		JPopupMenu editorpopup = new JPopupMenu();
		addPopup(editor, editorpopup);
		
		JMenuItem menuCopy = new JMenuItem("Copy");
		menuCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Caret caret = editor.getCaret();
		        int p0 = Math.min(caret.getDot(), caret.getMark());
		        int p1 = Math.max(caret.getDot(), caret.getMark());
		        if (p0 == p1)
		        	return;
		        
		        if (editor.getSelectedText() != null)
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.substring(htmlPosToStr(p0), htmlPosToStr(p1))), null);
			}
		});
		editorpopup.add(menuCopy);
		
		JMenuItem menuGoto = new JMenuItem("Go To Definition");
		menuGoto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (lastWStart == -1 || lastWEnd == -1) {
					window.getMain().message("Error", "Unable to navigate to given target!");
					return;
				}
				
				if (clip[lastWStart] instanceof CallExpressionNode) {
					FunctionInfo info = ((CallExpressionNode)clip[lastWStart]).getInfo();
					FunctionDatabase scriptsDB = window.getMain().getScriptsDB();
					for (int i = 0; i < scriptsDB.bufferSize(); i++) {
						if (scriptsDB.getInfo(i) == info) {
							window.getMain().loadScript(i);
							return;
						}
					}
				}
				
				window.getMain().message("Error", "Unable to navigate to given target!");
				return;
			}
		});
		editorpopup.add(menuGoto);
		
		JMenuItem menuRename = new JMenuItem("Rename");
		menuRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lastWStart == -1 || lastWEnd == -1) {
					window.getMain().message("Error", "Please select valid target to refactor!");
					return;
				}
				
				if (clip[lastWStart] instanceof VariableLoadNode || clip[lastWStart] instanceof VariableAssignationNode) {
					LocalVariable var =  clip[lastWStart] instanceof VariableLoadNode ? ((VariableLoadNode)clip[lastWStart]).getVariable() : ((VariableAssignationNode)clip[lastWEnd]).getVariable();
					String newName = window.getMain().inputBox("Rename", "Enter new variable name:", var.getName());
					if (newName != null && !newName.equals(var.getName())) {
						var.setName(newName);
						display();
					}
					return;
				}
				else if (clip[lastWStart] instanceof CallExpressionNode) {
					CallExpressionNode call = (CallExpressionNode)clip[lastWStart];
					for (int i = 0; i < 2; i++) {
						FunctionDatabase db = i == 0 ? window.getMain().getOpcodesDB() : window.getMain().getScriptsDB();
						for (int a = 0; a < db.bufferSize(); a++) {
							if (db.getInfo(a) == call.getInfo()) {
								FunctionInfo info = db.getInfo(a);
								StringBuilder descriptor = new StringBuilder();
								descriptor.append(info.getName() + " " + info.getReturnType());
								for (int x = 0; x < info.getArgumentTypes().length; x++)
									descriptor.append(" " + info.getArgumentTypes()[x] + " " + info.getArgumentNames()[x]);
								String newDescriptor = window.getMain().inputBox("Rename", "Enter new method signature:", descriptor.toString());
								if (newDescriptor != null && !newDescriptor.equals(descriptor.toString())) {
									db.updateFile(a, a + " " + newDescriptor);
									function = window.getMain().decompile(scriptID);
									display();
								}
								return;
							}
						}
					}
					
					window.getMain().message("Error", "Cannot refactor this method!");
					return;
				}
				else if (clip[lastWStart] instanceof FunctionNode) {
					FunctionNode func = (FunctionNode) clip[lastWStart];
					FunctionDatabase db = window.getMain().getScriptsDB();
					FunctionInfo info = db.getInfo(func.getId());
					if (info == null) {
						window.getMain().message("Error", "Cannot rename this function");
						return;
					}
					
					StringBuilder descriptor = new StringBuilder();
					descriptor.append(info.getName() + " " + info.getReturnType());
					for (int x = 0; x < info.getArgumentTypes().length; x++)
						descriptor.append(" " + info.getArgumentTypes()[x] + " " + info.getArgumentNames()[x]);
					String newDescriptor = window.getMain().inputBox("Rename", "Enter new method signature:", descriptor.toString());
					if (newDescriptor != null && !newDescriptor.equals(descriptor.toString())) {
						db.updateFile(func.getId(), func.getId() + " " + newDescriptor);
						function = window.getMain().decompile(scriptID);
						display();
					}
					return;
				}
				else {
					System.err.println("Unknown type selected: " + clip[lastWStart].getClass().getName());
				}
				
				window.getMain().message("Error", "Please select valid target to refactor!");
				return;
			}
		});
		editorpopup.add(menuRename);
	
		display();
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	
	private void display() {
		
		lastWStart = -1;
		lastWEnd = -1;
		
		editor.setToolTipText(null);
		clearHighlights();
		
		EditorPrinter printer = new EditorPrinter(this);
		function.print(printer);
		text = printer.toString();
		clip = printer.getClip();

		
		editor.setText(highlightSyntax(convert(printer.toString())));
		
		editor.getCaret().setVisible(true);
		editor.getCaret().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.err.println(e.toString());
				processCaretChangeEvent(e);
			}
		});
		
	}
	
	private void processCaretChangeEvent(ChangeEvent e) {
		lastWStart = -1;
		lastWEnd = -1;
		
		editor.setToolTipText(null);
		clearHighlights();
		
		editor.getCaret().setVisible(true);
		
		int dot = htmlPosToStr(editor.getCaret().getDot());
		if (dot < 0 || dot >= text.length())
			return;
		
		int start = dot;
		int end = text.length();
		
		if (SYNTAX_PARSER_CHARS.indexOf(text.charAt(dot)) == -1)
			return;
		
		for (int i = dot; i >= 0; i--) {
			if (SYNTAX_PARSER_CHARS.indexOf(text.charAt(i)) == -1)
				break;
			start = i;
		}
		
		for (int i = dot; i < text.length(); i++) {
			if (SYNTAX_PARSER_CHARS.indexOf(text.charAt(i)) == -1) {
				end = i;
				break;
			}
		}
		
		if (start == end)
			return;
		
		if (CARET_DEBUG) {
			System.err.println("dot:" + dot + ", wStart:" + start + ", wEnd:" + end);
			highlight(strPosToHtml(start), strPosToHtml(end), CARET_DEBUG_COLOR);
		}
		
		processCaretOnWord(lastWStart = start, lastWEnd = end);
	}
	
	private void processCaretOnWord(int wordStart, int wordEnd) {
		if (clip[wordStart] instanceof CallExpressionNode) {
			CallExpressionNode call = (CallExpressionNode)clip[wordStart];
			//highlight(strPosToHtml(wordStart), strPosToHtml(wordEnd), CARET_HL_COLOR);
			highlightAll(call.getInfo());
			editor.setToolTipText(call.getInfo().toString());
		}
		else if (clip[wordStart] instanceof VariableLoadNode) {
			highlightAll(((VariableLoadNode)clip[wordStart]).getVariable());
		}
		else if (clip[wordStart] instanceof VariableAssignationNode) {
			highlightAll(((VariableAssignationNode)clip[wordStart]).getVariable());
		}
		else if (clip[wordStart] instanceof ConfigurationLoadNode) {
			highlightAll(((ConfigurationLoadNode)clip[wordStart]).getInfo());
		}
		else if (clip[wordStart] instanceof ConfigurationStoreNode) {
			highlightAll(((ConfigurationStoreNode)clip[wordStart]).getInfo());
		}
		else if (clip[wordStart] instanceof BitConfigurationLoadNode) {
			highlightAll(((BitConfigurationLoadNode)clip[wordStart]).getInfo());
		}
		else if (clip[wordStart] instanceof BitConfigurationStoreNode) {
			highlightAll(((BitConfigurationStoreNode)clip[wordStart]).getInfo());
		}
	}
	
	
	private void highlightAll(LocalVariable var) {
		for (int i = 0; i < clip.length;) {
			if (clip[i] instanceof VariableLoadNode) {
				VariableLoadNode v = (VariableLoadNode)clip[i];
				if (v.getVariable() == var) {
					highlight(strPosToHtml(i), strPosToHtml(i + v.getVariable().getName().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == v)
						i++;
					continue;
				}
			}
			else if (clip[i] instanceof VariableAssignationNode) {
				VariableAssignationNode v = (VariableAssignationNode)clip[i];
				if (v.getVariable() == var) {
					highlight(strPosToHtml(i), strPosToHtml(i + v.getVariable().getName().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == v)
						i++;
					continue;
						
				}
			}
			i++;
		}
	}
	
	private void highlightAll(FunctionInfo info) {
		List<Object> handled = new ArrayList<Object>();
		for (int i = 0; i < clip.length;) {
			if (!handled.contains(clip[i]) && clip[i] instanceof CallExpressionNode) {
				CallExpressionNode call = (CallExpressionNode)clip[i];
				if (call.getInfo().getName().equals(info.getName()) && call.getInfo().getReturnType().equals(info.getReturnType())) {
					highlight(strPosToHtml(i), strPosToHtml(i + info.getName().length()), CARET_HL_COLOR);
					handled.add(call);
				}
			}
			i++;
		}
	}
	
	private void highlightAll(ConfigInfo info) {
		for (int i = 0; i < clip.length;) {
			if (clip[i] instanceof ConfigurationLoadNode) {
				ConfigurationLoadNode cLoad = (ConfigurationLoadNode)clip[i];
				if (cLoad.getInfo() == info) {
					highlight(strPosToHtml(i), strPosToHtml(i + cLoad.toString().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == cLoad)
						i++;
					continue;
				}
			}
			else if (clip[i] instanceof ConfigurationStoreNode) {
				ConfigurationStoreNode cStore = (ConfigurationStoreNode)clip[i];
				if (cStore.getInfo() == info) {
					highlight(strPosToHtml(i), strPosToHtml(i + cStore.toString().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == cStore)
						i++;
					continue;	
				}
			}
			i++;
		}
	}
	
	private void highlightAll(BitConfigInfo info) {
		for (int i = 0; i < clip.length;) {
			if (clip[i] instanceof BitConfigurationLoadNode) {
				BitConfigurationLoadNode cLoad = (BitConfigurationLoadNode)clip[i];
				if (cLoad.getInfo() == info) {
					highlight(strPosToHtml(i), strPosToHtml(i + cLoad.toString().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == cLoad)
						i++;
					continue;
				}
			}
			else if (clip[i] instanceof BitConfigurationStoreNode) {
				BitConfigurationStoreNode cStore = (BitConfigurationStoreNode)clip[i];
				if (cStore.getInfo() == info) {
					highlight(strPosToHtml(i), strPosToHtml(i + cStore.toString().length()), CARET_HL_COLOR);
					while (i < clip.length && clip[i] == cStore)
						i++;
					continue;	
				}
			}
			i++;
		}
	}
	
	private String convert(String text) {
		StringBuilder bld = new StringBuilder(text.length() * 6);
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&')
				bld.append("&amp;");
			else if (text.charAt(i) == '<')
				bld.append("&lt;");
			else if (text.charAt(i) == '>')
				bld.append("&gt;");
			else if (text.charAt(i) == '\n')
				bld.append("<br>");
			else if (text.charAt(i) == '\t')
				bld.append("&emsp;&emsp;&emsp;&emsp;");
			else if (text.charAt(i) == ' ')
				bld.append("&nbsp;");
			else
				bld.append(text.charAt(i));
		}
		return bld.toString();
	}
	

	private String highlightSyntax(String text) {
		StringBuilder bld = new StringBuilder();
		StringBuilder buff = new StringBuilder();
		char quote = '\0';
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (quote == '\0' && (c == '"' || c == '\'')) {
				if (buff.length() > 0) {
					bld.append(buff.toString());
					buff = new StringBuilder();
				}
				quote = c;
				bld.append("<b><font color=rgb(" + QUOTE_COLOR.getRed() + "," + QUOTE_COLOR.getGreen() + "," + QUOTE_COLOR.getBlue() + ")>");
				bld.append(c);
			}
			else if (quote != '\0') {
				bld.append(c);
				if (c == quote) {
					bld.append("</b></font>");
					quote = '\0';
				}	
			}
			else if (SYNTAX_PARSER_CHARS.indexOf(c) != -1) {
				buff.append(c);
			}
			else {
				bld.append(highlightWord(buff.toString()));
				bld.append(c);
				buff = new StringBuilder();
			}
		}
		if (buff.length() > 0) {
			bld.append(highlightWord(buff.toString()));
		}
		
		return bld.toString();
	}
	
	private String highlightWord(String word) {
		for (int i = 0; i < KEYWORDS.length; i++)
			if (KEYWORDS[i].equals(word)) {
				return "<b><font color=rgb(" + KEYWORD_COLOR.getRed() + "," + KEYWORD_COLOR.getGreen() + "," + KEYWORD_COLOR.getBlue() + ")>" + word + "</font></b>";
			}
		return word;
	}
	
	
	private int htmlPosToStr(int htmlPos) {
		int calc = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\t')
				calc += 4;
			else
				calc += 1;
			if (htmlPos == calc)
				return i;
		}
		return -1;
	}
	
	private int strPosToHtml(int strPos) {
		int add = 1;
		for (int i = 0; i < strPos; i++)
			if (text.charAt(i) == '\t')
				add += 3;
		return strPos + add;
	}
	
	private boolean highlight(int start, int end, Color color) {
		try {
			Object obj = editor.getHighlighter().addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(color));
			tags.add(obj);
			return true;
		} catch (BadLocationException e) {
			return false;
		}	
	}
	
	private void clearHighlights() {
		for (Object tag : tags)
			editor.getHighlighter().removeHighlight(tag);
		tags.clear();
	}
	
	
	public Window getWindow() {
		return window;
	}

	public void setSaveFile(File saveFile) {
		this.saveFile = saveFile;
	}

	public File getSaveFile() {
		return saveFile;
	}
	
	public JEditorPane getEditor() {
		return editor;
	}

	public FunctionNode getFunction() {
		return function;
	}

	public int getScriptID() {
		return scriptID;
	}
	
	public static boolean CARET_DEBUG = false;
	public static String SYNTAX_PARSER_CHARS = "0123456789QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm_-";
	public static String[] KEYWORDS = new String[] 
	{ 
		"boolean", "byte", "case", "char", "continue", "default",
		"do", "double", "else", "float", "for", "if", "int", 
		"long", "new", "newstruct", "return", "short", "switch",
		"void", "while", "string", "object", "break",
	};
	public static Color KEYWORD_COLOR = new Color( 127, 0, 85);
	public static Color QUOTE_COLOR = Color.BLUE;
	public static Color CARET_HL_COLOR = new Color(192, 192, 192);
	public static Color CARET_DEBUG_COLOR = Color.GREEN;

}
