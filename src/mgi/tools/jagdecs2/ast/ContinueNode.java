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

public class ContinueNode extends AbstractCodeNode implements IFlowControlNode {

	private IContinueableNode node;
	private ScopeNode selfScope;
	
	
    public ContinueNode(ScopeNode selfScope, IContinueableNode node) {
    	this.node = node;
    	this.selfScope = selfScope;
    	// don't need to write self scope because it's just for label checks. (Not expressed).
		if (this.getSelfScope().getParent() != node && node.getLabelName() == null)
			node.enableLabelName();
    }
    
    @Override
	public IContinueableNode getNode() {
		return node;
	}

	public ScopeNode getSelfScope() {
		return selfScope;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		if (this.getSelfScope().getParent() == node)
			printer.print("continue;");
		else
			printer.print("continue " + node.getLabelName() + ";");
		printer.endPrinting(this);
	}

}
