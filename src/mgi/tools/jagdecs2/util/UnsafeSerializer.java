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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


import sun.misc.Unsafe;

/**
 * Serializer which uses sun.misc.Unsafe to perform 
 * required operations.
 * Despite it's name it is 100% safe and stable to use.
 */
public class UnsafeSerializer {

	public Unsafe unsafe; //= Unsafe.getUnsafe();
	
	public UnsafeSerializer() {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
			field.setAccessible(false);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Failed to obtain instance of unsafe.");
		}
	}

	public Object readObject(ByteBuffer buffer) {
		buffer.setPosition(0);
		return readObjectImpl(new Session(buffer));
	}
	
	private Object readObjectImpl(Session session) {
		ByteBuffer buffer = session.buffer;
		if (buffer.getPosition() >= buffer.getBuffer().length)
			return null;
		
		int objID = buffer.readInt();
		boolean hasObject = buffer.readUByte() == 1;
		if (hasObject)
			return session.hasObject(objID) ? session.container[objID] : null;
		int nextBlock = buffer.readInt();
		try {
			Class<?> objType = findClass(buffer.readString());
			if (objType.isPrimitive())
				throw new RuntimeException("Fatal error!");
			if (objType.isArray()) {
				int arrayLength = buffer.readInt();
				Class<?> element = objType.getComponentType();
				Object array = Array.newInstance(element, arrayLength);
				session.addObject(array, objID);
				long base = unsafe.arrayBaseOffset(objType);
				long size = element == long.class || element == double.class ? 8 : (element == boolean.class || element == byte.class ? 1 : (element == short.class || element == char.class ? 2 : 4));
				for (int i = 0; i < arrayLength; i++) {
					if (element.isPrimitive()) {
						for (long a = 0; a < size; a++)
							unsafe.putByte(array, base + ((long)i * size) + a , (byte)buffer.readUByte());
					}
					else if (buffer.readUByte() == 1) // != null
						Array.set(array, i, readObjectImpl(session));
				}
			}
			else {
				Object obj = unsafe.allocateInstance(objType);
				session.addObject(obj,objID);
				
				while (buffer.readInt() != 0) {
					buffer.setPosition(buffer.getPosition() - 4);
					int nextField = buffer.readInt();
					try {
						Class<?> declaredOn = findClass(buffer.readString());
						String name = buffer.readString();
						Class<?> fType = findClass(buffer.readString());
						if (declaredOn == null || fType == null || !declaredOn.isAssignableFrom(objType))
							throw new RuntimeException("Invalid data");
						Field field = findField(declaredOn, name, fType);
						if (field == null)
							throw new RuntimeException("Can't find field");
						if (fType.isPrimitive()) {
							long offset = unsafe.objectFieldOffset(field);
							int size = fType == long.class || fType == double.class ? 8 : (fType == boolean.class || fType == byte.class ? 1 : (fType == short.class || fType == char.class ? 2 : 4));
							for (long i = 0; i < size; i++)
								unsafe.putByte(obj, offset + i, (byte) buffer.readUByte());
						}
						else if (buffer.readUByte() == 1) { // != null
							field.setAccessible(true);
							field.set(obj, readObjectImpl(session));
							field.setAccessible(false);
						}
						
						if (buffer.getPosition() != nextField)
							throw new RuntimeException("Corrupted block");
					}
					catch (Throwable t) {
						t.printStackTrace();
						buffer.setPosition(nextField);
						return null;
					}
				}
			}
			
			if (buffer.getPosition() != nextBlock)
				throw new RuntimeException("Corrupted block");
			
			return session.container[objID];
		}
		catch (Throwable t) {
			t.printStackTrace();
			buffer.setPosition(nextBlock);
			return null;
		}
	}


	public ByteBuffer writeObject(Object object) {
		ByteBuffer buffer = new ByteBuffer(1024 * 1024 * 5); // 5mb
		writeObjectImpl(new Session(buffer), object);
		return new ByteBuffer(buffer.toArray(0, buffer.getPosition()));
	}

	private void writeObjectImpl(Session session, Object object) {
		ByteBuffer buff = session.buffer;
		if (object == null)
			return;
		int objectID = session.objectID(object);
		boolean hasObject = true;
		if (objectID == -1) {
			hasObject = false;
			objectID = session.addObject(object);
		}
		buff.writeInt(objectID);
		buff.writeByte(hasObject ? 1 : 0); // confirmation , helps to prevent data corruption when class data is different from the one written in file.
		if (!hasObject) {
			Class<?> clazz = object.getClass();
			if (clazz.isPrimitive())
				throw new RuntimeException("Not object.");
			int markerPosition = buff.getPosition();
			buff.writeInt(0); // later will be overwrited by marker
			buff.writeString(clazz.getName());
			if (clazz.isArray()) {
				Class<?> element = clazz.getComponentType();
				int length = Array.getLength(object);
				buff.writeInt(length);
				long base = unsafe.arrayBaseOffset(clazz);
				long size = element == long.class || element == double.class ? 8 : (element == boolean.class || element == byte.class ? 1 : (element == short.class || element == char.class ? 2 : 4));
				for (int i = 0; i < length; i++) {
					if (element.isPrimitive()) {
						for (long a = 0; a < size; a++)
							buff.writeByte(unsafe.getByte(object, base + ((long)i * size) + a));
					}
					else {
						Object value = Array.get(object, i);
						buff.writeByte(value == null ? 0 : 1);
						writeObjectImpl(session,value);
					}
				}
			}
			else {
				for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
					for (Field field : current.getDeclaredFields()) {
						if (Modifier.isStatic(field.getModifiers())) //|| Modifier.isTransient(field.getModifiers()))
							continue;
						int fieldMarkerPosition = buff.getPosition();
						buff.writeInt(0); // later will be overwritten by marker
						Class<?> fType = field.getType();
						buff.writeString(current.getName());
						buff.writeString(field.getName());
						buff.writeString(fType.getName());
						if (fType.isPrimitive()) {
							long offset = unsafe.objectFieldOffset(field);
							int size = fType == long.class || fType == double.class ? 8 : (fType == boolean.class || fType == byte.class ? 1 : (fType == short.class || fType == char.class ? 2 : 4));
							for (long i = 0; i < size; i++)
								buff.writeByte(unsafe.getByte(object, offset + i));
						}
						else {
							field.setAccessible(true);
							Object value = null; 
							try { value = field.get(object); }
							catch (Throwable t) { }				
							field.setAccessible(false);
							buff.writeByte(value == null ? 0 : 1);
							writeObjectImpl(session,value);
						}
						int originalPosition = buff.getPosition();
						buff.setPosition(fieldMarkerPosition);
						buff.writeInt(originalPosition);
						buff.setPosition(originalPosition);
					}
				}
				buff.writeInt(0);
			}
			int originalPosition = buff.getPosition();
			buff.setPosition(markerPosition);
			buff.writeInt(originalPosition);
			buff.setPosition(originalPosition);
		}
	}

	private static class Session {
		private ByteBuffer buffer;
		private Object[] container = new Object[0];

		private Session(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		private int objectID(Object object) {
			for (int i = 0; i < container.length; i++)
				if (container[i] == object)
					return i;
			return -1;
		}
		
		private boolean hasObject(int objectID) {
			return objectID >= 0 && objectID < container.length && container[objectID] != null;
		}
		
		private void addObject(Object object,int objID) {
			if (objID >= container.length) {
				Object[] rebuff = new Object[objID + 1];
				System.arraycopy(container, 0, rebuff, 0, container.length);
				container = rebuff;
			}
			container[objID] = object;
		}

		private int addObject(Object object) {
			Object[] rebuff = new Object[container.length + 1];
			System.arraycopy(container, 0, rebuff, 0, container.length);
			rebuff[container.length] = object;
			container = rebuff;
			return container.length - 1;
		}

	}
	
	private Class<?> findClass(String utfName) {
		if (utfName.equals("void"))
			return void.class;
		else if (utfName.equals("boolean"))
			return boolean.class;
		else if (utfName.equals("byte"))
			return byte.class;
		else if (utfName.equals("short"))
			return short.class;
		else if (utfName.equals("char"))
			return char.class;
		else if (utfName.equals("int"))
			return int.class;
		else if (utfName.equals("long"))
			return long.class;
		else if (utfName.equals("float"))
			return float.class;
		else if (utfName.equals("double"))
			return double.class;
		else {
			try {
				return Class.forName(utfName);
			}
			catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}
	
	private Field findField(Class<?> declaredOn, String name, Class<?> type) {
		for (Field field : declaredOn.getDeclaredFields())
			if (field.getName().equals(name) && field.getType().equals(type))
				return field;
		return null;
	}

}
