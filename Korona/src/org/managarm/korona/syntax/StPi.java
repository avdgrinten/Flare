package org.managarm.korona.syntax;

public class StPi extends StNode {
	private StArgumentDecl p_argument;
	private StNode p_codomain;
	
	public StPi(StArgumentDecl argument, StNode codomain) {
		p_argument = argument;
		p_codomain = codomain;
	}
	public StArgumentDecl getArgument() { return p_argument; }
	public StNode getCodomain() { return p_codomain; }
	
	public String toString() {
		return p_argument + "->(" + p_codomain + ")";
	}
}
