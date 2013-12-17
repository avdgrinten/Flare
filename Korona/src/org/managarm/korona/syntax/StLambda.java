package org.managarm.korona.syntax;

public class StLambda extends StNode {
	private StArgumentDecl p_argument;
	private StNode p_expr;
	private boolean p_implicit;
	
	public StLambda(StArgumentDecl argument, StNode expr,
			boolean implicit) {
		p_argument = argument;
		p_expr = expr;
		p_implicit = implicit;
	}
	public StArgumentDecl getArgument() { return p_argument; }
	public StNode getExpr() { return p_expr; }
	public boolean getImplicit() { return p_implicit; }
	
	public String toString() {
		return p_argument + "=>(" + p_expr + ")";
	}
}
