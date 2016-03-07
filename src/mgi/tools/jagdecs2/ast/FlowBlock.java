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

import mgi.tools.jagdecs2.CS2Stack;
import mgi.tools.jagdecs2.CodePrinter;

public class FlowBlock extends AbstractCodeNode {

	/**
	 * Contains ID of this flow block.
	 * Main flow block ID is always 0.
	 */
	private int blockID;
	/**
	 * Contains all blocks to which this blocks 
	 * jumps with instructions.
	 */
	private List<FlowBlock> sucessors;
	/**
	 * Contains all blocks which jumps to this block
	 * with instructions.
	 */
	private List<FlowBlock> predecesors;
	/**
	 * Contains next flow block by code order.
	 * Can be null if this block is the last block.
	 */
	private FlowBlock next;
	/**
	 * Contains previous flow block by code order.
	 * Can be null if this block is the first block.
	 */
	private FlowBlock prev;
	/**
	 * Contains stack content before jumping/walking into this
	 * block dumped to local variables. 
	 */
	private CS2Stack variableStack;
	/**
	 * Contains start address of this flow block in instructions.
	 * Not guaranteed to be correct.
	 */
	private int startAddress;

	public FlowBlock() {
		this(0, 0, new CS2Stack());
	}
	
	public FlowBlock(int blockID, int startAddress,CS2Stack stack) {
		this.blockID = blockID;
		this.startAddress = startAddress;
		this.variableStack = stack;
		this.sucessors = new ArrayList<FlowBlock>();
		this.predecesors = new ArrayList<FlowBlock>();
	}
	
	
	
	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print("flow_" + this.blockID + ":");
		printer.tab();
		List<AbstractCodeNode> childs = this.listChilds();
		for (AbstractCodeNode node : childs) {
			printer.print('\n');
			node.print(printer);
		}
		printer.untab();
		printer.endPrinting(this);
	}
	
	public List<FlowBlock> getPredecesors() {
		return predecesors;
	}
	
	public List<FlowBlock> getSucessors() {
		return sucessors;
	} 

	public void setNext(FlowBlock next) {
		this.next = next;
	}

	public FlowBlock getNext() {
		return next;
	}

	public void setPrev(FlowBlock prev) {
		this.prev = prev;
	}

	public FlowBlock getPrev() {
		return prev;
	}
	
	public CS2Stack getStack() {
		return variableStack;
	}

	public int getStartAddress() {
		return startAddress;
	}
	
	public int getBlockID() {
		return blockID;
	}
	
	public boolean isSynthethic() {
		return startAddress < 0;
	}



}
