package org.managarm.aurora.util;

import org.managarm.aurora.lang.AuTerm;

public class NamedPi extends NamedTerm {
	private Object name;
	private AuTerm bound;
	private AuTerm codomain;
	
	public NamedPi(AuTerm annotation, 
			Object name, AuTerm bound, AuTerm codomain) {
		super(annotation);
		this.name = name;
		this.bound = bound;
		this.codomain = codomain;
	}
	public Object getName() {
		return name;
	}
	public AuTerm getBound() {
		return bound;
	}
	public AuTerm getCodomain() {
		return codomain;
	}
	
	@Override public AuTerm type() {
		return mkMeta();
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
