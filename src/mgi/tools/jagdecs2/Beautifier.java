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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mgi.tools.jagdecs2.ast.AbstractCodeNode;
import mgi.tools.jagdecs2.ast.CommentNode;
import mgi.tools.jagdecs2.ast.FlowBlock;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.ast.LocalVariable;
import mgi.tools.jagdecs2.ast.LoopNode;
import mgi.tools.jagdecs2.ast.PopableNode;
import mgi.tools.jagdecs2.ast.ScopeNode;
import mgi.tools.jagdecs2.ast.SwitchNode;
import mgi.tools.jagdecs2.ast.VariableAssignationNode;
import mgi.tools.jagdecs2.ast.VariableLoadNode;

public class Beautifier {
	
	@SuppressWarnings("unused")
	private CS2Decompiler decompiler;
	private FunctionNode function;

	public Beautifier(CS2Decompiler decompiler, FunctionNode function) {
		this.decompiler = decompiler;
		this.function = function;
	}
	
	
	
	public void beautify() throws DecompilerException {
		if (checkForFlowBlocks(function)) {
			function.setCodeAddress(0);
			while (function.read() instanceof CommentNode)
				;
			function.setCodeAddress(function.getCodeAddress() - 1);
			function.write(new CommentNode("Beautifier was aborted, because this function contains unsolved flow blocks.", CommentNode.STANDART_STYLE));
			return; // we can't beautify 
		}
		
		transformLoops(function, function.getScope());
		moveVarScopesAndRename(function);
	}
	
	
	private void transformLoops(AbstractCodeNode parent, AbstractCodeNode n) {
		for (int i = 0; i < n.size(); i++)
			transformLoops(n, n.read(i));
		
		if (n instanceof LoopNode && ((LoopNode)n).getPreAssigns() == null) {
			LoopNode loop = (LoopNode)n;
			List<PopableNode> delete1 = new ArrayList<PopableNode>();
			List<VariableAssignationNode> pre = new ArrayList<VariableAssignationNode>();
			for (int addr = parent.addressOf(n) - 1; addr > 0; addr--) {
				AbstractCodeNode a = parent.read(addr);
				if (!(a instanceof PopableNode))
					break;
				PopableNode p = (PopableNode)a;
				if (!(p.getExpression() instanceof VariableAssignationNode) || !((VariableAssignationNode)p.getExpression()).isDeclaration())
					break;
				delete1.add(p);
				pre.add((VariableAssignationNode)p.getExpression());
			}
			
			if (pre.size() < 1)
				return;
			
			List<PopableNode> delete2 = new ArrayList<PopableNode>();
			List<VariableAssignationNode> after = new ArrayList<VariableAssignationNode>();
			for (int addr = loop.getScope().size() - 1; addr > 0; addr--) {
				AbstractCodeNode a = loop.getScope().read(addr);
				if (!(a instanceof PopableNode))
					break;
				PopableNode p = (PopableNode)a;
				
				if (!(p.getExpression() instanceof VariableAssignationNode))
					break;
				VariableAssignationNode as = (VariableAssignationNode)p.getExpression();
				if (as.isDeclaration())
					break;
				
				boolean found = false;
				for (VariableAssignationNode x : pre) {
					if (as.getVariable() == x.getVariable()) {
						found = true;
						break;
					}
				}
				
				if (!found)
					break;
				
				delete2.add(p);
				after.add(as);
			}
			
			if (pre.size() > 0 && after.size() > 0) {
				VariableAssignationNode[] p = new VariableAssignationNode[pre.size()];
				VariableAssignationNode[] a = new VariableAssignationNode[after.size()];
				
				int writep = p.length - 1;
				int writea = a.length - 1;
				for (VariableAssignationNode x : pre)
					p[writep--] = x;
				for (VariableAssignationNode x : after)
					a[writea--] = x;
				
				loop.forTransform(p, a);
				
				for (PopableNode d1 : delete1)
					parent.delete(parent.addressOf(d1));
				for (PopableNode d2 : delete2)
					loop.getScope().delete(loop.getScope().addressOf(d2));
			}
		}
	}
	
	private void moveVarScopesAndRename(FunctionNode function) {
		Map<LocalVariable, Map<AbstractCodeNode, ScopeNode>> access = new HashMap<LocalVariable, Map<AbstractCodeNode, ScopeNode>>();
		for (LocalVariable var : function.getScope().copyDeclaredVariables())
			access.put(var, new HashMap<AbstractCodeNode, ScopeNode>());
		
		collectAccess(null, function, access);
		
		Map<LocalVariable, ScopeNode> smap = new HashMap<LocalVariable, ScopeNode>();
		
		for (LocalVariable var : access.keySet()) {
			if (var.isArgument())
				continue;
			
			Map<AbstractCodeNode, ScopeNode> acc = access.get(var);
			
			ScopeNode bestScope = null;
			ScopeNode[] bestScopeTree = null;
			for (ScopeNode n : acc.values()) {
				if (bestScope == null) {
					bestScope = n;
					while (bestScope.getParent() instanceof SwitchNode)
						bestScope = bestScope.getParentScope();
					bestScopeTree = bestScope.makeScopeTree();
					continue;
				}
				
				if (n == bestScope)
					continue;
				
				ScopeNode[] ntree = n.makeScopeTree();
				int amt = Math.min(bestScopeTree.length, ntree.length);
				for (int i = 0; i < amt; i++) {
					if (bestScopeTree[i] != ntree[i]) {
						bestScope = bestScopeTree[i - 1];
						while (bestScope.getParent() instanceof SwitchNode)
							bestScope = bestScope.getParentScope();
						bestScopeTree = bestScope.makeScopeTree();
						break;
					}
				}
				
				if (ntree.length < bestScopeTree.length) {
					bestScope = n;
					bestScopeTree = n.makeScopeTree();
				}
			}
			

			
			if (bestScope != function.getScope()) {
				//System.err.println("Moved " + var + " to " + bestScope);
				function.getScope().undeclare(var);
				bestScope.declare(var);
			}
			
			
			var.setName(var.getName() + "_");
			smap.put(var, bestScope);
			

		}
		
		for (int depth = 0; smap.size() > 0; depth++) {
			Iterator<LocalVariable> it$ = smap.keySet().iterator();
			while (it$.hasNext()) {
				LocalVariable var = it$.next();
				
				ScopeNode scope = smap.get(var);
				if (scope.getScopeDepth() != depth)
					continue;
								
				for (int i = 0;;i++) {
					if (scope.isDeclared("v" + i))
						continue;
					var.setName("v" + i);
					it$.remove();
					break;
				}
			}
		}
		
		
	}
	
	private void collectAccess(ScopeNode scope, AbstractCodeNode n, Map<LocalVariable, Map<AbstractCodeNode, ScopeNode>> all) {
		if (n instanceof ScopeNode)
			scope = (ScopeNode)n;
		
		for (int i = 0; i < n.size(); i++)
			collectAccess(scope, n.read(i), all);
		
		if (n instanceof VariableLoadNode) {
			VariableLoadNode l = (VariableLoadNode)n;
			if (!all.containsKey(l.getVariable()))
				throw new DecompilerException("undeclared var? " + l.getVariable());
			if (scope == null)
				throw new DecompilerException("null scope?");
			
			all.get(l.getVariable()).put(l, scope);
		}
		else if (n instanceof VariableAssignationNode) {
			VariableAssignationNode a = (VariableAssignationNode)n;
			if (!all.containsKey(a.getVariable()))
				throw new DecompilerException("undeclared var? " + a.getVariable());
			if (scope == null)
				throw new DecompilerException("null scope?");
			
			all.get(a.getVariable()).put(a, scope);
		}
	}
	
	
	
	private boolean checkForFlowBlocks(AbstractCodeNode n) {
		for (int i = 0; i < n.size(); i++)
			if (checkForFlowBlocks(n.read(i)))
				return true;
		
		return n instanceof FlowBlock;
	}
	

}
