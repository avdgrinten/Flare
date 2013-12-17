package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuConstant extends AuTerm {
	public static class Descriptor {
		private AuTerm signature;
		
		public Descriptor(AuTerm type) {
			this.signature = type;
		}
		public AuTerm getSignature() {
			return signature;
		}
	}
	
	private Descriptor descriptor;
	
	public AuConstant(AuTerm annotation, Descriptor descriptor) {
		super(annotation);
		this.descriptor = descriptor;
	}
	public Descriptor getDescriptor() {
		return descriptor;
	}
	@Override public String toString() {
		return descriptor.toString();
	}
	@Override public int hashCode() {
		return descriptor.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuConstant))
			return false;
		AuConstant other = (AuConstant)object;
		return descriptor.equals(other.descriptor);
	}

	@Override public AuTerm type() {
		return descriptor.signature;
	}
	@Override public boolean primitive() {
		return true;
	}

	@Override public AuTerm reduce() {
		return this;
	}
	@Override public boolean wellformed() {
		return descriptor.signature.wellformed();
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return this;
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return this;
	}
	@Override public AuTerm map(TermMap fun) {
		return new AuConstant(this.getAnnotation(),
				descriptor);
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
