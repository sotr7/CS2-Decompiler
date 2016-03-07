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

public class LoopNode extends AbstractCodeNode implements IBreakableNode,IContinueableNode {

	public static final int LOOPTYPE_WHILE = 0;
	public static final int LOOPTYPE_DOWHILE = 1;
	public static final int LOOPTYPE_FOR = 2;
	
	/**
	 * Contains pre expression assigns.
	 */
	private VariableAssignationNode[] preAssigns;
    /**
     * Contains expression which type is boolean.
     */
    private ExpressionNode expression;
    /**
     * Contains scope which should be executed if 
     * expression results in true.
     */
    private ScopeNode scope;
    /**
     * Contains after assigns.
     */
    private VariableAssignationNode[] afterAssigns;
    /**
     * Contains start of while node.
     */
    private FlowBlock start;
    /**
     * Contains end of while node.
     */
    private FlowBlock end;
    /**
     * Contains label name of this node.
     */
    private String labelName;
    /**
     * Contains type of this loop node.
     */
    private int type;
    
    public LoopNode(int type, ScopeNode scope, ExpressionNode expr,FlowBlock start,FlowBlock end) {
    	this.type = type;
    	this.expression = expr;
    	this.scope = scope;
    	this.start = start;
    	this.end = end;
		this.write(expr);
		this.write(scope);
		expr.setParent(this);
		this.scope.setParent(this);
    }
    
	public ScopeNode getScope() {
		return scope;
	}

	
	public ExpressionNode getExpression() {
		return expression;
	}
	
	public VariableAssignationNode[] getPreAssigns() {
		return preAssigns;
	}
	
	public VariableAssignationNode[] getAfterAssigns() {
		return afterAssigns;
	}
	
	public void forTransform(VariableAssignationNode[] preAssigns, VariableAssignationNode[] afterAssigns) {
		this.type = LOOPTYPE_FOR;
		this.preAssigns = preAssigns;
		this.afterAssigns = afterAssigns;
		
		setCodeAddress(0);
		for (int i = 0; i < preAssigns.length; i++) {
			preAssigns[i].setParent(this);
			write(preAssigns[i]);
		}
		
		setCodeAddress(size() - 1);
		for (int i = 0; i < afterAssigns.length; i++) {
			afterAssigns[i].setParent(this);
			write(afterAssigns[i]);
		}
	}
	

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		if (this.labelName != null)
			printer.print(labelName + " ");
		if (this.type == LOOPTYPE_WHILE) {
			printer.print("while (");
			expression.print(printer);
			printer.print(") ");
			scope.print(printer);
		}
		else if (this.type == LOOPTYPE_DOWHILE) {
			printer.print("do ");
			scope.print(printer);
			printer.print(" while (");
			expression.print(printer);
			printer.print(");");
		}
		else if (this.type == LOOPTYPE_FOR) {
			if (preAssigns != null) {
				printer.print("for (");
				for (int i = 0; i < preAssigns.length; i++) {
					if (i > 0)
						printer.print(", ");
					preAssigns[i].print(printer);
				}
				printer.print("; ");
				expression.print(printer);
				printer.print(";");
				for (int i = 0; i < afterAssigns.length; i++) {
					printer.print(i > 0 ? ", " : " ");
					afterAssigns[i].print(printer);
				}
				printer.print(") ");
				scope.print(printer);
			}
			else {
				printer.print("for (;");
				expression.print(printer);
				printer.print(";) ");
				scope.print(printer);
			}
		}
		else {
			throw new RuntimeException("Unknown loop type:" + this.type);
		}
		printer.endPrinting(this);
	}
	
	@Override
	public boolean canContinue() {
		return start != null;
	}

	@Override
	public boolean canBreak() {
		return end != null;
	}

	@Override
	public FlowBlock getStart() {
		return start;
	}
	
	@Override
	public FlowBlock getEnd() {
		return end;
	}

	@Override
	public void enableLabelName() {
		labelName = "loop_" + this.hashCode() + ":";
	}

	@Override
	public String getLabelName() {
		return labelName;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}


}
