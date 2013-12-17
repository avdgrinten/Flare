package org.managarm.korona.syntax;

public class StIdent extends StNode {
	private String p_string;
	
	public StIdent(String string) {
		p_string = string;
	}
	public String string() {
		return p_string;
	}
	@Override public String toString() {
		return p_string;
	}
}
