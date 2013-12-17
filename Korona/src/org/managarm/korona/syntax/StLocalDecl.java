package org.managarm.korona.syntax;

public class StLocalDecl extends StNode {
	private String p_ident;
	private StNode p_type;
	private StNode p_initializer;
	
	public StLocalDecl(String ident, StNode type,
			StNode initializer) {
		p_ident = ident;
		p_type = type;
		p_initializer = initializer;
	}
	
	public String ident() {
		return p_ident;
	}
	public StNode type() {
		return p_type;
	}
	public StNode initializer() {
		return p_initializer;
	}
	
	@Override public String toString() {
		return "var " + p_ident + (p_type != null ? ":" + p_type : "")
				+ (p_initializer != null ? "=" + p_initializer : "")
				+ ";";
	}
}
