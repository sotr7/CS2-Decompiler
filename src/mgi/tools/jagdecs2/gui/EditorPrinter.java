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

import mgi.tools.jagdecs2.CodePrinter;
import mgi.tools.jagdecs2.ast.AbstractCodeNode;
import mgi.tools.jagdecs2.util.ArrayQueue;

public class EditorPrinter extends CodePrinter {

	private EditorPanel editor;
	private ArrayQueue<AbstractCodeNode> current;
	private AbstractCodeNode[] buffer;
	private int length;
	
	public EditorPrinter(EditorPanel editor) {
		this.editor = editor;
		this.current = new ArrayQueue<AbstractCodeNode>();
		this.buffer = new AbstractCodeNode[0];
	}
	
	@Override
	public void beginPrinting(AbstractCodeNode node) {
		current.insert(node);
	}
	
	@Override
	public void endPrinting(AbstractCodeNode node) {
		if (current.take() != node)
			throw new RuntimeException("beginPrinting() was not called");
	}
	
	@Override
	public void print(char c) {
		out(c);
		if (c == '\n') {
			String tabs = getTabs();
			for (int i = 0; i < tabs.length(); i++)
				out(tabs.charAt(i));
		}
	}
	
	
	private void out(char c) {
		if (buffer.length <= (length + 1))
			expand();
		buffer[length++] = current.last();
		writer.append(c);
	}
	
	private void expand() {
		AbstractCodeNode[] buffer = new AbstractCodeNode[(this.buffer.length + 1) * 2];
		System.arraycopy(this.buffer, 0, buffer, 0, this.buffer.length);
		this.buffer = buffer;
	}
	
	
	
	public EditorPanel getEditor() {
		return editor;
	}
	
	public AbstractCodeNode[] getClip() {
		if (current.size() > 0)
			throw new RuntimeException("Operation not finished!");
		AbstractCodeNode[] clip = new AbstractCodeNode[length];
		System.arraycopy(buffer, 0, clip, 0, length);
		return clip;
	}
}
