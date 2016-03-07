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

import java.util.Arrays;


public class ArrayQueue<T> {

	public static final int INITIAL_CAPACITY = 1000;
	
	private Object[] queue;
	private int top;
	
	
	public ArrayQueue() {
		this(INITIAL_CAPACITY);
	}
	
	public ArrayQueue(int initialCapacity) {
		this.queue = new Object[initialCapacity];
	}

	
	public void insert(T obj) {
		if (top >= queue.length) {
			Object[] newQueue = new Object[queue.length * 2];
			System.arraycopy(queue, 0, newQueue, 0, queue.length);
			queue = newQueue;
		}
		queue[top++] = obj;
	}
	
	@SuppressWarnings("unchecked")
	public T take() {
		if (top <= 0)
			throw new RuntimeException("Nothing to take.");
		return (T)queue[--top];
	}
	
	
	@SuppressWarnings("unchecked")
	public T last() {
		if (top <= 0)
			throw new RuntimeException("No elements.");
		return (T)queue[top - 1];
	}
	
	
	@SuppressWarnings("unchecked")
	public T first() {
		if (top <= 0)
			throw new RuntimeException("No elements.");
		return (T)queue[0];
	}
	
	public int size() {
		return top;
	}
	
	public void clear() {
		top = 0;
		Arrays.fill(queue, null);
	}
	
	/**
	 * Iterate's thru all elements and checks if 
	 * there's at least one element that 
	 * obj.equals(element);
	 */
	public boolean lookup(T obj) {
		for (int i = 0; i < top; i++)
			if (queue[i] != null && obj.equals(queue[i]))
				return true;
		return false;
	}
	
	public void dispose() {
		queue = null;
	}

}
