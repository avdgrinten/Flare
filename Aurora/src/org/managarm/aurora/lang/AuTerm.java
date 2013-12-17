package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public abstract class AuTerm {
	public static AuMeta mkMeta() {
		return new AuMeta(null);
	}
	public static AuMeta mkMetaExt(AuTerm annotation) {
		return new AuMeta(annotation);
	}
	public static AuVar mkVar(int depth, AuTerm type) {
		return new AuVar(null, depth, type);
	}
	public static AuVar mkVarExt(AuTerm annotation,
			int depth, AuTerm type) {
		return new AuVar(annotation, depth, type);
	}
	public static AuLambda mkLambda(AuTerm bound, AuTerm expr) {
		return new AuLambda(null, bound, expr);
	}
	public static AuLambda mkLambdaExt(AuTerm annotation,
			AuTerm bound, AuTerm expr) {
		return new AuLambda(annotation, bound, expr);
	}
	public static AuPi mkPi(AuTerm bound, AuTerm codomain) {
		return new AuPi(null, bound, codomain);
	}
	public static AuPi mkPiExt(AuTerm annotation,
			AuTerm bound, AuTerm codomain) {
		return new AuPi(annotation, bound, codomain);
	}
	public static AuApply mkApply(AuTerm function, AuTerm argument) {
		return new AuApply(null, function, argument);
	}
	public static AuTerm mkConst(AuConstant.Descriptor descriptor) {
		return new AuConstant(null, descriptor);
	}
	public static AuTerm mkOperator(AuOperator.Descriptor descriptor,
			AuTerm... arguments) {
		return new AuOperator(null, descriptor, arguments);
	}
	
	public static boolean congruent(AuTerm a, AuTerm b) {
		return a.reduce().equals(b.reduce());
	}
	
	private AuTerm p_annotation;
	
	public AuTerm(AuTerm annotation) {
		p_annotation = annotation;
	}
	public AuTerm getAnnotation() {
		return p_annotation;
	}
	
	public boolean closed() {
		return verifyClosed(-1);
	}
	
	public boolean isOperator(AuOperator.Descriptor descriptor) {
		if(!(this instanceof AuOperator))
			return false;
		AuOperator operator = (AuOperator)this;
		return operator.getDescriptor() == descriptor;
	}
	public boolean isConstant(AuConstant.Descriptor descriptor) {
		if(!(this instanceof AuConstant))
			return false;
		AuConstant operator = (AuConstant)this;
		return operator.getDescriptor() == descriptor;
	}
	
	public abstract boolean wellformed();
	public abstract AuTerm type();
	public abstract AuTerm reduce();
	public abstract boolean primitive();
	public abstract AuTerm apply(int depth, AuTerm term);
	public abstract AuTerm embed(int embed_depth, int limit);
	public abstract AuTerm replace(AuTerm subterm, AuTerm replacement);
	public abstract AuTerm map(TermMap fun);
	public abstract boolean verifyVariable(int depth, AuTerm type); 
	public abstract boolean verifyClosed(int max_depth);
}
