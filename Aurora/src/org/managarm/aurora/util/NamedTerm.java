package org.managarm.aurora.util;

import static org.managarm.aurora.lang.AuTerm.mkApplyExt;
import static org.managarm.aurora.lang.AuTerm.mkLambdaExt;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperatorExt;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkPiExt;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkVarExt;

import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;

public abstract class NamedTerm {
	public static AuTerm mkNamedLambda(Name name, AuTerm bound, AuTerm expr) {
		return mkNamedLambdaExt(null, name, bound, expr);
	}
	public static AuTerm mkNamedLambdaExt(AuTerm annotation,
			Name name, AuTerm bound, AuTerm expr) {
		AuTerm res_expr = resolve(expr, name, 0);
		return mkLambdaExt(annotation, bound, res_expr);
	}
	public static AuTerm mkNamedPi(Name name, AuTerm bound, AuTerm codomain) {
		return mkNamedPiExt(null, name, bound, codomain);
	}
	public static AuTerm mkNamedPiExt(AuTerm annotation,
			Name name, AuTerm bound, AuTerm codomain) {
		AuTerm res_codomain = resolve(codomain, name, 0);
		return mkPiExt(annotation, bound, res_codomain);
	}
	
	public static class Name extends AuOperator.Descriptor {
		public Name() {
			super(mkPi(mkMeta(),
			 mkVar(0, mkMeta())), 1);
		}
		@Override public String toString() {
			return "name{" + System.identityHashCode(this) + "}";
		}
		
		@Override protected boolean reductive(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			throw new AssertionError("reduce() called");
		}
	};
	
	private static AuTerm resolve(AuTerm term, Name name,
			int depth) {
		if(term instanceof AuOperator
				&& ((AuOperator) term).getDescriptor() == name) {
			AuOperator operator = (AuOperator)term;
			return mkVarExt(operator.getAnnotation(), depth,
					operator.getArgument(0));
		}
		
		if(term instanceof AuApply) {
			AuApply apply = (AuApply)term;
			
			AuTerm function = resolve(apply.getFunction(), name, depth);
			AuTerm argument = resolve(apply.getArgument(), name, depth);
			return mkApplyExt(apply.getAnnotation(),
					function, argument);
		}else if(term instanceof AuConstant) {
			return term;
		}else if(term instanceof AuLambda) {
			AuLambda lambda = (AuLambda)term;
			
			AuTerm bound = resolve(lambda.getBound(), name, depth);
			AuTerm expr = resolve(lambda.getExpr(), name, depth + 1);
			return mkLambdaExt(lambda.getAnnotation(), bound, expr);
		}else if(term instanceof AuMeta) {
			return term;
		}else if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			
			AuTerm[] arguments = new AuTerm[operator.numArguments()];
			for(int i = 0; i < operator.numArguments(); i++)
				arguments[i] = resolve(operator.getArgument(i), name, depth);
			return mkOperatorExt(operator.getAnnotation(),
					operator.getDescriptor(), arguments);
		}else if(term instanceof AuPi) {
			AuPi pi = (AuPi)term;
			
			AuTerm bound = resolve(pi.getBound(), name, depth);
			AuTerm codomain = resolve(pi.getCodomain(), name, depth + 1);
			return mkPiExt(pi.getAnnotation(), bound, codomain);
		}else if(term instanceof AuVar) {
			AuVar var = (AuVar)term;
			
			AuTerm type = resolve(var.getType(), name, depth);
			return mkVarExt(var.getAnnotation(), var.getDepth(), type);
		}else throw new RuntimeException("Illegal term " + term);
	}
}
