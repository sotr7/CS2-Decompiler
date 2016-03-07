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


package mgi.tools.jagdecs2.instructions;

import mgi.tools.jagdecs2.util.InstructionInfo;

public class SwitchInstruction extends AbstractInstruction {

	private int[] cases;
	private Label[] targets;
	private int defaultIndex;

	public SwitchInstruction(InstructionInfo info, int[] cases, Label[] targets) {
		super(info);
		this.cases = cases;
		this.targets = targets;
		this.defaultIndex = -1;
	}

	public int[] getCases() {
		return cases;
	}

	public Label[] getTargets() {
		return targets;
	}
	
	public int getDefaultIndex() {
		return defaultIndex;
	}
	
	
	public void attachDefault(Label default_) {
		Label[] nTargets = new Label[targets.length + 1];
		int[] nCases = new int[cases.length + 1];
		
		System.arraycopy(targets, 0, nTargets, 0, targets.length);
		System.arraycopy(cases, 0, nCases, 0, cases.length);
		
		targets = nTargets;
		cases = nCases;
		
		targets[targets.length - 1] = default_;
		cases[cases.length - 1] = Integer.MIN_VALUE;
		
		defaultIndex = targets.length - 1;
	}
	
	
	public void sort() {
		int[] sCases = new int[cases.length];
		Label[] sTargets = new Label[targets.length];
		boolean[] usage = new boolean[cases.length];
		boolean defaultAssigned = false;
		for (int sWrite = 0; sWrite < sCases.length; sWrite++) {
			int lowestAddr = Integer.MAX_VALUE;
			int lowestIndex = -1;
			for (int i = 0; i < cases.length; i++)
				if (!usage[i] && targets[i].getAddress() < lowestAddr)
					lowestAddr = targets[lowestIndex = i].getAddress();
			if (!defaultAssigned && defaultIndex == lowestIndex) {
				defaultAssigned = true;
				defaultIndex = sWrite;
			}
			usage[lowestIndex] = true;
			sCases[sWrite] = cases[lowestIndex];
			sTargets[sWrite] = targets[lowestIndex];
		}
		cases = sCases;
		targets = sTargets;
	}
	
	
	
	

	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append("switch { \n");
		for (int i = 0; i < cases.length; i++) {
			bld.append(i == defaultIndex ? "\tdefault: \n" : "\tcase " + cases[i] + ": \n");
			bld.append("\t\t" + Opcodes.getOpcodeName(Opcodes.GOTO) + "\t" + targets[i].toString() + " \n");
		}
		bld.append("}");
		return bld.toString();
	}

}
