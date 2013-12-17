package org.managarm.jet.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class Cell {
	public static class Id {
		
	}
	
	public static class Register extends Cell {
		private Type p_type;
		private int p_index;
		
		public Register(Type type, int index) {
			p_type = type;
			p_index = index;
		}
		public Type getType() {
			return p_type;
		}
		public int getIndex() {
			return p_index;
		}
		
		@Override public void load(MethodVisitor method) {
			if(p_type == Type.INT_TYPE
					|| p_type == Type.BOOLEAN_TYPE) {
				method.visitVarInsn(Opcodes.ILOAD, p_index);
			}else throw new RuntimeException("Illegal type " + p_type);
		}
		@Override public void store(MethodVisitor method) {
			if(p_type == Type.INT_TYPE
					|| p_type == Type.BOOLEAN_TYPE) {
				method.visitVarInsn(Opcodes.ISTORE, p_index);
			}else throw new RuntimeException("Illegal type " + p_type);
		}
	}
	
	public abstract void load(MethodVisitor method);
	public abstract void store(MethodVisitor method);
}
