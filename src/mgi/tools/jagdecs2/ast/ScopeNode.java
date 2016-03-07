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

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.CodePrinter;
import mgi.tools.jagdecs2.DecompilerException;

public class ScopeNode extends AbstractCodeNode {

    
    /**
     * Contains scope in which this scope is 
     * declared or null if this scope is first.
     */
    private ScopeNode parentScope;
    /**
     * Contains parent node or null if this scope doesn't have
     * parent node.
     */
    private AbstractCodeNode parent;
    /**
     * Contains list of declared local variables.
     */
    private List<LocalVariable> declaredLocalVariables;
    
    
    public ScopeNode() {
    	this(null);
    }
    
    public ScopeNode(ScopeNode parent) {
    	this.parentScope = parent;
    	this.declaredLocalVariables = new ArrayList<LocalVariable>();
    }
    
    
    /**
     * Removes local variable from declared variables list.
     * @param variable
     * @throws DecompilerException
     * If variable does not belong to this scope.
     */
    public void undeclare(LocalVariable variable) throws DecompilerException {
    	if (!declaredLocalVariables.contains(variable))
    		throw new DecompilerException("Variable (" + variable.toString() + ") is not declared!");
    	declaredLocalVariables.remove(variable);
    }
    
    /**
     * Declare's given local variable to this scope.
     * @param variable
     * @throws CompilerException
     * If variable there's variable with the same name declared.
     */
    public void declare(LocalVariable variable) throws DecompilerException {
    	if (this.isDeclared(variable.getName())) {
    		throw new DecompilerException("Variable (" + variable.toString() + ") is already declared!");
    	}
    	this.declaredLocalVariables.add(variable);
    }
    
    /**
     * Get's declared local variable from this scope or one of the parent scopes.
     * @param localName
     * Local name of the variable that should be returned.
     * @return
     * Returns local variable with given localName.
     * @throws CompilerException
     * If the given local variable is not declared.
     */
    public LocalVariable getLocalVariable(String localName) throws DecompilerException {
		for (LocalVariable var : this.declaredLocalVariables) {
		    if (var.getName().equals(localName)) {
		    	return var;
		    }
		}
		if (this.parentScope != null) {
		    return this.parentScope.getLocalVariable(localName);
		}
		throw new DecompilerException("Variable " + localName + " is not declared!");
    }
    
    /**
     * Get's declared local variable from this scope or one of the parent scopes.
     * @param identifier
     * Identifier of the local variable
     * @return
     * Returns local variable with given localName.
     * @throws CompilerException
     * If the given local variable is not declared.
     */
    public LocalVariable getLocalVariable(int identifier) throws DecompilerException {
		for (LocalVariable var : this.declaredLocalVariables) {
		    if (var.getIdentifier() != -1 && var.getIdentifier() == identifier) {
		    	return var;
		    }
		}
		if (this.parentScope != null) {
		    return this.parentScope.getLocalVariable(identifier);
		}
		throw new DecompilerException("Variable " + identifier + " is not declared!");
    }
    
    /**
     * Get's if given local variable is declared in this
     * scope or in parent scopes.
     * @param localName
     * Name of the local variable
     * @return 
     * Wheter given local variable is declared.
     */
    public boolean isDeclared(String localName) {
		for (LocalVariable var : this.declaredLocalVariables) {
		    if (var.getName().equals(localName)) {
		    	return true;
		    }
		}
		if (this.parentScope != null) {
		    return this.parentScope.isDeclared(localName);
		}
		return false;
    }
    
    
    /**
     * Get's if given local variable is declared in this
     * scope or in parent scopes.
     * @param identifier
     * Identifier of the local variable.
     * @return 
     * Wheter given local variable is declared.
     */
    public boolean isDeclared(int identifier) {
		for (LocalVariable var : this.declaredLocalVariables) {
		    if (var.getIdentifier() != -1 && var.getIdentifier() == identifier) {
		    	return true;
		    }
		}
		if (this.parentScope != null) {
		    return this.parentScope.isDeclared(identifier);
		}
		return false;
    }
    
    /**
     * Copies declared variables in this scope only.
     */
    public List<LocalVariable> copyDeclaredVariables() {
    	return new ArrayList<LocalVariable>(this.declaredLocalVariables);
    }
    
    /**
     * Get's if this scopeNode is empty.
     * @return
     */
    public boolean isEmpty() {
    	return this.size() <= 0;
    }
    
    /**
     * Return's all scopes in order.
     */
    public ScopeNode[] makeScopeTree() {
    	ScopeNode[] tree = new ScopeNode[getScopeDepth() + 1];
    	fillScopeTree(tree, tree.length - 1);
    	return tree;
    }
    
    private void fillScopeTree(ScopeNode[] tree, int index) {
    	tree[index] = this;
    	if (parentScope != null)
    		parentScope.fillScopeTree(tree, index - 1);
    }
    
    /**
     * Return's depth of this scope.
     */
    public int getScopeDepth() {
    	if (parentScope == null)
    		return 0;
    	return parentScope.getScopeDepth() + 1;
    }
    
    /**
     * Get's root (first) scope.
     */
    public ScopeNode getRootScope() {
    	if (this.parentScope != null)
    		return this.parentScope.getRootScope();
    	return this;
    }

	public ScopeNode getParentScope() {
		return parentScope;
	}

	/**
     * Find's controllable flow node to which target belongs.
     * Return's null if nothing was found.
     */
    public IControllableFlowNode findControllableNode(FlowBlock target) {
    	if (this instanceof IControllableFlowNode) {
    		if (this instanceof IBreakableNode && ((IBreakableNode)this).canBreak() && ((IBreakableNode)this).getEnd() == target)
    			return (IControllableFlowNode)this;
    		else if (this instanceof IContinueableNode && ((IContinueableNode)this).canContinue() && ((IContinueableNode)this).getStart() == target)
    			return (IControllableFlowNode)this;
    	}
    	if (this.parent != null && this.parent instanceof IControllableFlowNode) {
    		IControllableFlowNode parent = (IControllableFlowNode)this.parent;
    		if (parent instanceof IBreakableNode && ((IBreakableNode)parent).canBreak() && ((IBreakableNode)parent).getEnd() == target)
    			return parent;
    		else if (parent instanceof IContinueableNode && ((IContinueableNode)parent).canContinue() && ((IContinueableNode)parent).getStart() == target)
    			return parent;
    	}
    	if (this.parentScope != null)
    		return this.parentScope.findControllableNode(target);
    	return null;
    }


	public void setParent(AbstractCodeNode parentInstruction) {
		this.parent = parentInstruction;
	}

	public AbstractCodeNode getParent() {
		return parent;
	}
	
	
	private boolean needsBraces() {
		if (!(getParent() instanceof LoopNode) && !(getParent() instanceof IfElseNode))
			return true;
		
		int cElements = size();
		for (LocalVariable var : this.declaredLocalVariables)
			if (var.needsScopeDeclaration())
				cElements++;
		
		return cElements != 1;
	}
	

	@Override
	public void print(CodePrinter printer) {
		boolean braces = needsBraces();		
		
		printer.beginPrinting(this);
		printer.tab();
		if (braces)
			printer.print('{');
		for (LocalVariable var : this.declaredLocalVariables) {
			if (var.needsScopeDeclaration())
				printer.print("\n" + var.toString() + ";");
		}
		boolean caseAnnotationTabbed = false;
		List<AbstractCodeNode> childs = this.listChilds();
		for (AbstractCodeNode node : childs) {
		    if (node instanceof CaseAnnotation && !caseAnnotationTabbed) {
		    	printer.print('\n');
		    	node.print(printer);
		    	printer.tab();
		    	caseAnnotationTabbed = true;
		    }
		    else if (node instanceof CaseAnnotation && caseAnnotationTabbed) {
		    	printer.untab();
		    	printer.print('\n');
		    	node.print(printer);
		    	printer.tab();
		    }
		    else {
		    	printer.print('\n');
			    node.print(printer);
		    }   
	
		}
		if (caseAnnotationTabbed)
			printer.untab();
		printer.untab();
		if (braces)
			printer.print("\n}");
		printer.endPrinting(this);
	}
    
}
