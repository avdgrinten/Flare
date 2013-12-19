package org.managarm.aurora.util;

import org.managarm.aurora.lang.AuTerm;

public class NamedLambda extends NamedTerm {
	private Object name;
	private AuTerm bound;
	private AuTerm expr;
	
	public NamedLambda(AuTerm annotation, 
			Object name, AuTerm bound, AuTerm expr) {
		super(annotation);
		this.name = name;
		this.bound = bound;
		this.expr = expr;
	}
	public Object getName() {
		return name;
	}
	public AuTerm getBound() {
		return bound;
	}
	public AuTerm getExpr() {
		return expr;
	}
	
	@Override public AuTerm type() {
		return mkNamedPi(name, bound, expr.type());
	}
	@Override public boolean primitive() {
		throw new UnsupportedOperationException();
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		throw new UnsupportedOperationException();
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		throw new UnsupportedOperationException();
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		throw new UnsupportedOperationException();
	}
}
