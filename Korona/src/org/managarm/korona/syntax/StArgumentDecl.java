package org.managarm.korona.syntax;

public class StArgumentDecl extends StNode {
	private String p_ident;
	private StNode p_type;
	
	public StArgumentDecl(String ident, StNode type) {
		p_ident = ident;
		p_type = type;
	}
	
	public String ident() {
		return p_ident;
	}
	public StNode type() {
		return p_type;
	}
	
	@Override public String toString() {
		return p_ident + ":" + p_type;
	}
}
