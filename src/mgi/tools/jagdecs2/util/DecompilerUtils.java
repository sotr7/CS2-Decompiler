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


package mgi.tools.jagdecs2.util;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.ast.CaseAnnotation;
import mgi.tools.jagdecs2.ast.FlowBlock;
import mgi.tools.jagdecs2.ast.SwitchFlowBlockJump;

public class DecompilerUtils {

	public static class SwitchCase {
		private CaseAnnotation[] annotations;
		private FlowBlock block;
		
		public SwitchCase(CaseAnnotation[] annotations,FlowBlock block) {
			this.annotations = annotations;
			this.block = block;
		}
	
		public CaseAnnotation[] getAnnotations() {
			return annotations;
		}
	
		public FlowBlock getBlock() {
			return block;
		}
		
		public void setBlock(FlowBlock block) {
			this.block = block;
		}
		
		public String toString() {
			StringBuilder bld = new StringBuilder();
			for (int i = 0; i < annotations.length; i++) {
				bld.append(annotations[i]);
				if ((i + 1) < annotations.length)
					bld.append(" AND ");
			}
			bld.append("\t GOTO flow_" + block.getBlockID());
			return bld.toString();
		}
		
	}

	public static SwitchCase[] makeSwitchCases(SwitchFlowBlockJump sbj) {
		SwitchCase[] buff = new SwitchCase[sbj.getCases().length];
		FlowBlock lastBlock = null;
		List<CaseAnnotation> annotations = new ArrayList<CaseAnnotation>();
		int count = 0;
		for (int i = 0; i < sbj.getCases().length; i++) {
			if (sbj.getTargets()[i] == lastBlock)
				annotations.add(sbj.getDefaultIndex() == i ? new CaseAnnotation() : new CaseAnnotation(sbj.getCases()[i]));
			else {
				if (lastBlock != null) {
					CaseAnnotation[] ann = new CaseAnnotation[annotations.size()];
					int aWrite = 0;
					for (CaseAnnotation a : annotations)
						ann[aWrite++] = a;
					buff[count++] = new SwitchCase(ann,lastBlock);
				}
				lastBlock = sbj.getTargets()[i];
				annotations.clear();
				annotations.add(sbj.getDefaultIndex() == i ? new CaseAnnotation() : new CaseAnnotation(sbj.getCases()[i]));
			}
		}
		if (lastBlock != null) {
			CaseAnnotation[] ann = new CaseAnnotation[annotations.size()];
			int aWrite = 0;
			for (CaseAnnotation a : annotations)
				ann[aWrite++] = a;
			buff[count++] = new SwitchCase(ann,lastBlock);
		}
		
		if (count == buff.length)
			return buff;
		
		SwitchCase[] full = new SwitchCase[count];
		System.arraycopy(buff, 0, full, 0, count);
		return full;
	}
	
}
