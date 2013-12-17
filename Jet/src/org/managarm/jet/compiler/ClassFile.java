package org.managarm.jet.compiler;

public class ClassFile {
	private byte[] p_bytecode;
	
	public ClassFile(byte[] bytecode) {
		p_bytecode = bytecode;
	}
	public byte[] getBytecode() {
		return p_bytecode;
	}
}
