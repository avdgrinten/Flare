package org.managarm.jet.compiler;

public class Namespace {
	private String p_prefix;
	private int p_uniqueId = 1;

	public Namespace(String prefix) {
		p_prefix = prefix;
	}
	
	public String getUniqueId() {
		return p_prefix + (p_uniqueId++);
	}
}
