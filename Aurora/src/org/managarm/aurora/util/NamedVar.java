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
	@Override public int hashCode() {
		return name.hashCode() + type.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof NamedVar))
			return false;
		NamedVar other = (NamedVar)object;
		return name.equals(other.name)
				&& type.equals(other.type);
	}
	
	@Override public AuTerm type() {
		return type;
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
	@Override public boolean verifyVariable(int depth, AuTerm verify_type) {
		return type.verifyVariable(depth, verify_type);
	}
}
