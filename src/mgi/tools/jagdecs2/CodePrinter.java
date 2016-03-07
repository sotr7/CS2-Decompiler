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


package mgi.tools.jagdecs2;

import java.io.StringWriter;

import mgi.tools.jagdecs2.ast.AbstractCodeNode;

public class CodePrinter {

	protected StringWriter writer;
	private int tabs;
	
	
	public CodePrinter() {
		writer = new StringWriter();
		tabs = 0;
	}
	
	/**
	 * Method , unused by default that notifies that specific node is
	 * about to be printed.
	 */
	public void beginPrinting(AbstractCodeNode node) { }
	
	/**
	 * Method, unused by default that notifies that specific node was
	 * printed.
	 */
	public void endPrinting(AbstractCodeNode node) { }
	
	
	public void print(CharSequence str) {
		for (int i = 0; i < str.length(); i++)
			print(str.charAt(i));
	}
	
	public void print(char c) {
		writer.append(c);
		if (c == '\n')
			writer.append(getTabs());
	}
	
	protected String getTabs() {
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < this.tabs; i++)
			tabs.append('\t');
		return tabs.toString();
	}
	
	public void tab() {
		tabs++;
	}
	
	public void untab() {
		if (tabs <= 0)
			throw new RuntimeException("Not tabbed!");
		tabs--;
	}
	
	@Override
	public String toString() {
		writer.flush();
		return writer.toString();
	}
	
	public static String print(AbstractCodeNode node) {
		CodePrinter printer = new CodePrinter();
		node.print(printer);
		return printer.toString();
	}
	
}
