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
import mgi.tools.jagdecs2.ast.ConditionalFlowBlockJump;
import mgi.tools.jagdecs2.ast.FlowBlock;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.ast.LocalVariable;
import mgi.tools.jagdecs2.ast.ReturnNode;
import mgi.tools.jagdecs2.ast.SwitchFlowBlockJump;
import mgi.tools.jagdecs2.ast.UnconditionalFlowBlockJump;
import mgi.tools.jagdecs2.ast.VariableAssignationNode;
import mgi.tools.jagdecs2.ast.VariableLoadNode;

public class LocalVariablesAnalyzerT1 {
	
	@SuppressWarnings("unused")
	private CS2Decompiler decompiler;
	private FunctionNode function;
	private FlowBlock[] blocks;
	

	private boolean[] processed;
	private FlowBlockState[] states;
	
	private Map<Integer, LocalVariable> variables;
	private Map<AbstractCodeNode, List<Integer>> accesses;

	public LocalVariablesAnalyzerT1(CS2Decompiler decompiler, FunctionNode function, FlowBlock[] blocks) {
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
		variables = new HashMap<Integer, LocalVariable>();
		accesses = new HashMap<AbstractCodeNode, List<Integer>>();
		
		FlowBlockState s = new FlowBlockState();
		for (LocalVariable arg : function.getArgumentLocals()) {
			AbstractCodeNode dummy = new AbstractCodeNode() {
				@Override
				public void print(CodePrinter printer) {
					printer.beginPrinting(this);
					printer.print("dummy");
					printer.endPrinting(this);
				}
				
				@Override
				public int getCodeAddress() {
					return Integer.MIN_VALUE;
				}
			};
			
			accesses.put(dummy, s.set(dummy, arg, true));
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
		for (AbstractCodeNode n : accesses.keySet())
			merge(new ArrayList<Integer>(accesses.get(n)));		
		
		
		List<LocalVariable> old = function.getScope().copyDeclaredVariables();
		for (LocalVariable v : old)
			function.getScope().undeclare(v);
		
		List<LocalVariable> nvars = new ArrayList<LocalVariable>();
		List<LocalVariable> nargs = new ArrayList<LocalVariable>();
		
		int count = 0;
		int argcount = 0;
		for (List<Integer> vars : accesses.values()) {
			if (vars.size() != 1)
				throw new DecompilerException("Not merged");
			
			LocalVariable v = variables.get(vars.get(0));
			if (nvars.contains(v))
				continue;
			
			nvars.add(v);
			if (v.isArgument()) {
				v.setNeedsScopeDeclaration(false);
				nargs.add(v);
			}
			v.setName(v.isArgument() ? ("a" + argcount++) : ("v" + count++));
		}
		
		System.err.println(nargs.size() + "," + function.getArgumentLocals().length);
		if (nargs.size() != function.getArgumentLocals().length)
			throw new DecompilerException("something failed");
		
		int write = 0;
		for (LocalVariable v : nargs)
			function.getArgumentLocals()[write++] = v;
		
		for (LocalVariable v : nvars)
			function.getScope().declare(v);
		
		for (AbstractCodeNode n : accesses.keySet()) {
			LocalVariable nvar = variables.get(accesses.get(n).get(0));
			if (n instanceof VariableLoadNode)
				((VariableLoadNode)n).setVariable(nvar);
			else if (n instanceof VariableAssignationNode)
				((VariableAssignationNode)n).setVariable(nvar);
			else if (n.getCodeAddress() == Integer.MIN_VALUE)
				; // XXX this is our dummy node
			else
				throw new DecompilerException("logic error");
		}
	}
	
	private void merge(List<Integer> vars) {
		if (vars.size() < 2)
			return;
		
		int main = vars.get(0);
		LocalVariable vmain = variables.get(main);
		String name = vmain.getName();
		CS2Type type = vmain.getType();
		boolean arg = vmain.isArgument();
		for (int var : vars) {
			if (var == main)
				continue;
			
			LocalVariable vother = variables.get(var);
			if (!vother.getName().equals(name) || !vother.getType().equals(type))
				throw new DecompilerException("Can't merge " + vmain + " with " + vother);
			arg = arg || vother.isArgument();
		}
		
		LocalVariable merged = new LocalVariable(name, type, arg);
		variables.put(main, merged);
		
		for (AbstractCodeNode n : accesses.keySet()) {
			List<Integer> varlist = accesses.get(n);
			
			boolean has = false;
			Iterator<Integer> it$ = varlist.iterator();
			while (it$.hasNext()) {
				Integer var = it$.next();
				if (vars.contains(var)) {
					it$.remove();
					has = true;
				}
			}
			
			if (has)
				varlist.add(main);
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
		else if (n instanceof VariableLoadNode) {
			VariableLoadNode ldr = (VariableLoadNode)n;
			accesses.put(ldr, state.get(n, ldr.getVariable()));
		}
		else if (n instanceof VariableAssignationNode) {
			VariableAssignationNode ldr = (VariableAssignationNode)n;
			accesses.put(ldr, state.set(n, ldr.getVariable(), false));
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
		private Map<LocalVariable, List<Integer>> state;
		
		public FlowBlockState() {
			state = new HashMap<LocalVariable, List<Integer>>();
		}
		
		public List<Integer> set(Object obj, LocalVariable var, boolean arg) {
			if (!variables.containsKey(obj.hashCode()))
				variables.put(obj.hashCode(), new LocalVariable(var.getName(), var.getType(), arg));
			if (!state.containsKey(var))
				state.put(var, new ArrayList<Integer>());
			
			List<Integer> v = state.get(var);
			v.clear();
						
			v.add(obj.hashCode());
			return new ArrayList<Integer>(v);
		}
		
		
		public List<Integer> get(Object obj, LocalVariable var) {
			if (!state.containsKey(var))
				throw new DecompilerException("Accessing unassigned variable!");
			return new ArrayList<Integer>(state.get(var));
		}
		
		public boolean merge(FlowBlockState other) {
			boolean updated = false;
			for (LocalVariable ovar : other.state.keySet()) {
				if (!state.containsKey(ovar))
					continue;
				
				List<Integer> l = state.get(ovar);
				for (Integer i : other.state.get(ovar)) {
					if (!l.contains(i)) {
						l.add(i);
						updated = true;
					}
				}
				
			}
			return updated;
		}
		
		public FlowBlockState copy() {
			FlowBlockState s = new FlowBlockState();
			for (LocalVariable n : state.keySet())
				s.state.put(n, new ArrayList<Integer>(state.get(n)));
			return s;
		}
	}
}
