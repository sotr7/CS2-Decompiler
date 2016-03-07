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
import java.util.List;
import java.util.Map;

import mgi.tools.jagdecs2.ast.AbstractCodeNode;
import mgi.tools.jagdecs2.ast.ConditionalFlowBlockJump;
import mgi.tools.jagdecs2.ast.FlowBlock;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.ast.LocalVariable;
import mgi.tools.jagdecs2.ast.ReturnNode;
import mgi.tools.jagdecs2.ast.SwitchFlowBlockJump;
import mgi.tools.jagdecs2.ast.UnconditionalFlowBlockJump;
import mgi.tools.jagdecs2.ast.VariableAssignationNode;

public class LocalVariablesAnalyzerT2 {
	
	@SuppressWarnings("unused")
	private CS2Decompiler decompiler;
	private FunctionNode function;
	private FlowBlock[] blocks;
	

	private boolean[] processed;
	private FlowBlockState[] states;
	
	private Map<LocalVariable, List<Object>> assigns;

	public LocalVariablesAnalyzerT2(CS2Decompiler decompiler, FunctionNode function, FlowBlock[] blocks) {
		this.decompiler = decompiler;
		this.function = function;
		this.blocks = blocks;
	}
	
	
	
	public void analyze() throws DecompilerException {
		init();
		process();
		end();
	}
	
	
	private void init() {
		processed = new boolean[blocks.length];
		states = new FlowBlockState[blocks.length];
		assigns = new HashMap<LocalVariable, List<Object>>();
		
		FlowBlockState s = new FlowBlockState();
		for (LocalVariable arg : function.getArgumentLocals()) {
			Object o = s.set(null, arg);
			if (!assigns.containsKey(arg))
				assigns.put(arg, new ArrayList<Object>());
			List<Object> l = assigns.get(arg);
			if (!l.contains(o))
				l.add(o);
		}
		
		
		queue(s, blocks[0]);
	}
	
	private void process() {
		int count;
		do {
			count = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i] == null || processed[i])
					continue;
				processed[i] = true;
				
				processNode(states[i], blocks[i]);
				count++;
			}
		}
		while (count > 0);
		
	}
	
	private void end() {	
		for (LocalVariable var : assigns.keySet()) {
			List<Object> objs = assigns.get(var);
			if (objs.size() != 1 || !(objs.get(0) instanceof VariableAssignationNode))
				continue;
			
			VariableAssignationNode fassign = (VariableAssignationNode)objs.get(0);
			
			fassign.setIsDeclaration(true);
			var.setNeedsScopeDeclaration(false);
		}
	}
	
	
	
	private boolean processNode(FlowBlockState state, AbstractCodeNode n) {
		boolean quit = false;
		for (int i = 0; i < n.size(); i++) {
			boolean q = processNode(state, n.read(i));
			quit = quit || q;
		}
		
		if (n instanceof ConditionalFlowBlockJump) {
			ConditionalFlowBlockJump jmp = (ConditionalFlowBlockJump)n;
			queue(state, jmp.getTarget());
		}
		else if (n instanceof UnconditionalFlowBlockJump) {
			UnconditionalFlowBlockJump jmp = (UnconditionalFlowBlockJump)n;
			queue(state, jmp.getTarget());
			return true;
		}
		else if (n instanceof SwitchFlowBlockJump) {
			SwitchFlowBlockJump jmp = (SwitchFlowBlockJump)n;
			for (int i = 0; i < jmp.getTargets().length; i++)
				queue(state, jmp.getTargets()[i]);
			return true;
		}
		else if (n instanceof VariableAssignationNode) {
			VariableAssignationNode ldr = (VariableAssignationNode)n;
			Object o = state.set(ldr, ldr.getVariable());
			if (!assigns.containsKey(ldr.getVariable()))
				assigns.put(ldr.getVariable(), new ArrayList<Object>());
			List<Object> l = assigns.get(ldr.getVariable());
			if (!l.contains(o))
				l.add(o);
		}
		else if (n instanceof ReturnNode) {
			return true;
		}
		else if (n instanceof FlowBlock) {
			FlowBlock f = (FlowBlock)n;
			if (!quit && f.getNext() != null)
				queue(state, f.getNext());
			return true;
		}
		return quit;
	}
	
	
	private void queue(FlowBlockState current, FlowBlock target) {
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i] != target)
				continue;
			if (states[i] == null) {
				states[i] = current.copy();
				return;
			}
			
			if (states[i].merge(current))
				processed[i] = false;
			return;
		}
		throw new DecompilerException("logic error");
	}
	
	
	private class FlowBlockState {
		private Map<LocalVariable, Object> hits;
		
		public FlowBlockState() {
			hits = new HashMap<LocalVariable, Object>();
		}
		
		public Object set(Object obj, LocalVariable var) {
			if (!hits.containsKey(var))
				hits.put(var, obj);			
			return hits.get(var);
		}
		
		public boolean merge(FlowBlockState other) {
			boolean updated = false;
			for (LocalVariable ovar : other.hits.keySet()) {
				if (!hits.containsKey(ovar))
					continue;
				
				if (hits.get(ovar) != other.hits.get(ovar)) {
					hits.put(ovar, null);
					updated = true;
				}			
			}
			return updated;
		}
		
		public FlowBlockState copy() {
			FlowBlockState s = new FlowBlockState();
			for (LocalVariable n : hits.keySet())
				s.hits.put(n, hits.get(n));
			return s;
		}
	}
}
