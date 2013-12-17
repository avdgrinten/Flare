package org.managarm.aurora.util;

import org.managarm.aurora.lang.AuTerm;

public class NamedVar extends NamedTerm {
	private Object name;
	private AuTerm type;
	
	public NamedVar(AuTerm annotation,
			Object name, AuTerm type) {
		super(annotation);
		this.name = name;
		this.type = type;
	}
	public Object getName() {
		return name;
	}
	public AuTerm getType() {
		return type;
	}
	@Override public String toString() {
		return "$" + name + ":" + type;
	}
	
	@Override public AuTerm map(TermMap fun) {
		return new NamedVar(this.getAnnotation(),
				name, fun.map(type));
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return new NamedVar(this.getAnnotation(),
				name, type.replace(subterm, replacement));
	}
	@Override public boolean wellformed() {
		return type.wellformed();
	}
	@Override public AuTerm type() {
		return type;
	}
	@Override public AuTerm reduce() {
		return new NamedVar(this.getAnnotation(), name,
				type.reduce());
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return new NamedVar(this.getAnnotation(), name,
				type.embed(embed_depth, limit));
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return new NamedVar(this.getAnnotation(), name,
				type.apply(depth, term));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return type.verifyVariable(depth, type);
	}
}
