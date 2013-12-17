package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuMeta extends AuTerm {
	public AuMeta(AuTerm annotation) {
		super(annotation);
	}
	
	@Override public String toString() {
		return "Meta";
	}
	@Override public int hashCode() {
		return 1;
	}
	@Override public boolean equals(Object object) {
		return object instanceof AuMeta;
	}

	@Override public AuTerm type() {
		return this;
	}
	@Override public boolean primitive() {
		return true;
	}

	@Override public AuTerm reduce() {
		return this;
	}
	@Override public boolean wellformed() {
		return true;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return this;
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return this;
	}
	@Override public AuTerm map(TermMap fun) {
		return this;
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return this;
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return true;
	}
	@Override public boolean verifyClosed(int max_depth) {
		return true;
	}
}
