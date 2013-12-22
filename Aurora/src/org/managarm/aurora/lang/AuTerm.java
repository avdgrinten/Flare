package org.managarm.aurora.lang;


public abstract class AuTerm {
	public static boolean verifyWellformed = true;
	
	public static AuMeta mkMeta() {
		return mkMetaExt(null);
	}
	public static AuMeta mkMetaExt(AuTerm annotation) {
		return new AuMeta(annotation);
	}
	
	public static AuVar mkVar(int depth, AuTerm type) {
		return mkVarExt(null, depth, type);
	}
	public static AuVar mkVarExt(AuTerm annotation,
			int depth, AuTerm type) {
		if(verifyWellformed) {
			if(!type.type().equals(mkMeta()))
				throw new RuntimeException("Illegal type");
		}
		
		return new AuVar(annotation, depth, type);
	}
	
	public static AuLambda mkLambda(AuTerm bound, AuTerm expr) {
		return mkLambdaExt(null, bound, expr);
	}
	public static AuLambda mkLambdaExt(AuTerm annotation,
			AuTerm bound, AuTerm expr) {
		if(verifyWellformed) {
			if(!bound.type().equals(mkMeta()))
				throw new RuntimeException("Illegal type");
			if(!expr.verifyVariable(0, bound.embed(1, 0)))
				throw new RuntimeException("Variable type mismatch");
		}
		
		return new AuLambda(annotation, bound, expr);
	}
	
	public static AuPi mkPi(AuTerm bound, AuTerm codomain) {
		return mkPiExt(null, bound, codomain);
	}
	public static AuPi mkPiExt(AuTerm annotation,
			AuTerm bound, AuTerm codomain) {
		if(verifyWellformed) {
			if(!bound.type().equals(mkMeta()))
				throw new RuntimeException("Illegal type");
			if(!codomain.verifyVariable(0, bound.embed(1, 0)))
				throw new RuntimeException("Variable type mismatch");
		}
		
		return new AuPi(annotation, bound, codomain);
	}

	public static AuTerm mkApply(AuTerm function, AuTerm argument) {
		return mkApplyExt(null, function, argument);
	}
	public static AuTerm mkApplyExt(AuTerm annotation,
			AuTerm function, AuTerm argument) {
		if(verifyWellformed) {
			AuTerm func_type = function.type();
			if(!(func_type instanceof AuPi))
				throw new RuntimeException("Illegal function type: " + function);
			AuPi pi = (AuPi)func_type;
			if(!pi.getBound().equals(argument.type()))
				throw new RuntimeException("Function argument type mismatch."
						+ " Expected " +  pi.getBound()
						+ ", received " + argument.type());
		}
		
		if(function.primitive()) {
			AuLambda lambda = (AuLambda)function;
			return lambda.getExpr().apply(0, argument);
		}else return new AuApply(annotation, function, argument);
	}

	public static AuTerm mkConst(AuConstant.Descriptor descriptor) {
		return mkConstExt(null, descriptor);
	}
	public static AuTerm mkConstExt(AuTerm annotation,
			AuConstant.Descriptor descriptor) {
		return new AuConstant(annotation, descriptor);
	}
	
	public static AuTerm mkOperator(AuOperator.Descriptor descriptor,
			AuTerm... arguments) {
		return mkOperatorExt(null, descriptor, arguments);
	}
	public static AuTerm mkOperatorExt(AuTerm annotation,
			AuOperator.Descriptor descriptor,
			AuTerm... arguments) {
		if(verifyWellformed) {
			if(arguments.length != descriptor.getArity())
				throw new RuntimeException("Operator arity mismatch");
			
			AuTerm derived = descriptor.getSignature();
			for(int i = 0; i < descriptor.getArity(); i++) {
				AuPi pi = (AuPi)derived;
				if(!pi.getBound().equals(arguments[i].type()))
					throw new RuntimeException("Operator argument type mismatch."
							+ " Expected " + pi.getBound()
							+ ", received " + arguments[i].type());
				
				AuTerm codomain = pi.getCodomain();
				derived = codomain.apply(0, arguments[i]);
			}
		}
		
		if(descriptor.reducible(arguments))
			return descriptor.reduce(arguments);
		return new AuOperator(annotation, descriptor, arguments);
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
	
	public abstract AuTerm type();
	public abstract boolean primitive();
	public abstract AuTerm apply(int depth, AuTerm term);
	public abstract AuTerm embed(int embed_depth, int limit);
	public abstract boolean verifyVariable(int depth, AuTerm type); 
	public abstract boolean verifyClosed(int max_depth);
}
