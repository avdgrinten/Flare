package org.managarm.korona.syntax;

public class StLetExpr extends StNode {
	private String p_name;
	private StNode p_defn;
	private StNode p_expr;
	
	public StLetExpr(String name, StNode defn, StNode expr) {
		p_name = name;
		p_defn = defn;
		p_expr = expr;
	}
	public String getName() {
		return p_name;
	}
	public StNode getDefn() {
		return p_defn;
	}
	public StNode getExpr() {
		return p_expr;
	}
}
