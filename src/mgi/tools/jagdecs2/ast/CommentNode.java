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


package mgi.tools.jagdecs2.ast;

import mgi.tools.jagdecs2.CodePrinter;

public class CommentNode extends AbstractCodeNode {
	
	public static final int STANDART_STYLE = 0;
	public static final int LOGO_STYLE = 1;

	private String comment;
	private int style;
    
    public CommentNode(String comment, int style) {
    	this.comment = comment;
    	this.style = style;
    }
    
    public int numLines() {
    	int total = 0;
    	for (int i = 0; i < comment.length(); i++)
    		if (comment.charAt(i) == '\n')
    			total++;
    	return total;
    }

	public String getComment() {
		return comment;
	}
	
	public int getStyle() {
		return style;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		if (numLines() > 0) {
			if (style == LOGO_STYLE) {
				printer.print("/* \n * ");
				printer.print(comment.replace("\n", "\n * "));
				printer.print("\n */");
			}
			else {
				printer.tab();
				printer.print("/* \n");
				printer.print(comment);
				printer.untab();
				printer.print("\n */");
			}
		}
		else {
			printer.print("// " + comment);
		}
		printer.endPrinting(this);
	}




}
