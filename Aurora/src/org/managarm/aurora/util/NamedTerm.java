package org.managarm.aurora.util;

import java.util.ArrayList;
import java.util.List;

import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;

public abstract class NamedTerm extends AuTerm {
	public static NamedVar mkNamedVar(Object name, AuTerm type) {
		return new NamedVar(null, name, type);
	}
	public static NamedVar mkNamedVarExt(AuTerm annotation,
			Object name, AuTerm type) {
		return new NamedVar(annotation, name, type);
	}
	public static NamedLambda mkNamedLambda(Object name, AuTerm bound, AuTerm expr) {
		return new NamedLambda(null, name, bound, expr);
	}
	public static NamedLambda mkNamedLambdaExt(AuTerm annotation,
			Object name, AuTerm bound, AuTerm expr) {
		return new NamedLambda(annotation, name, bound, expr);
	}
	public static NamedPi mkNamedPi(Object name, AuTerm bound, AuTerm codomain) {
		return new NamedPi(null, name, bound, codomain);
	}
	public static NamedPi mkNamedPiExt(AuTerm annotation,
			Object name, AuTerm bound, AuTerm codomain) {
		return new NamedPi(annotation, name, bound, codomain);
	}
	
	public static AuTerm resolve(AuTerm term) {
		return resolve(term, new ArrayList<Object>());
	}
	private static AuTerm resolve(AuTerm term, List<Object> stack) {
		if(term instanceof AuApply) {
			AuApply apply = (AuApply)term;
			
			AuTerm function = resolve(apply.getFunction(), stack);
			AuTerm argument = resolve(apply.getArgument(), stack);
			return mkApplyExt(apply.getAnnotation(),
					function, argument);
		}else if(term instanceof AuConstant) {
			return term;
		}else if(term instanceof AuLambda) {
			AuLambda lambda = (AuLambda)term;
			
			AuTerm bound = resolve(lambda.getBound(), stack);
			stack.add(null);
			AuTerm expr = resolve(lambda.getExpr(), stack);
			stack.remove(stack.size() - 1);
			return mkLambdaExt(lambda.getAnnotation(), bound, expr);
		}else if(term instanceof AuMeta) {
			return term;
		}else if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			
			AuTerm[] arguments = new AuTerm[operator.numArguments()];
			for(int i = 0; i < operator.numArguments(); i++)
				arguments[i] = resolve(operator.getArgument(i), stack);
			return mkOperatorExt(operator.getAnnotation(),
					operator.getDescriptor(), arguments);
		}else if(term instanceof AuPi) {
			AuPi pi = (AuPi)term;
			
			AuTerm bound = resolve(pi.getBound(), stack);
			stack.add(null);
			AuTerm codomain = resolve(pi.getCodomain(), stack);
			stack.remove(stack.size() - 1);
			return mkPiExt(pi.getAnnotation(), bound, codomain);
		}else if(term instanceof AuVar) {
			AuVar var = (AuVar)term;
			
			AuTerm type = resolve(var.getType(), stack);
			return mkVarExt(var.getAnnotation(), var.getDepth(), type);
		}else if(term instanceof NamedLambda) {
			NamedLambda lambda = (NamedLambda)term;
			
			AuTerm bound = resolve(lambda.getBound(), stack);
			stack.add(lambda.getName());
			AuTerm expr = resolve(lambda.getExpr(), stack);
			stack.remove(stack.size() - 1);
			return mkLambdaExt(lambda.getAnnotation(), bound, expr);
		}else if(term instanceof NamedPi) {
			NamedPi pi = (NamedPi)term;
			
			AuTerm bound = resolve(pi.getBound(), stack);
			stack.add(pi.getName());
			AuTerm expr = resolve(pi.getCodomain(), stack);
			stack.remove(stack.size() - 1);
			return mkPiExt(pi.getAnnotation(), bound, expr);
		}else if(term instanceof NamedVar) {
			NamedVar var = (NamedVar)term;
			
			AuTerm type = resolve(var.getType(), stack);
			int index = stack.indexOf(var.getName());
			if(index == -1)
				return new NamedVar(var.getAnnotation(),
						var.getName(), type);
			int depth = stack.size() - 1 - index;
			return mkVarExt(var.getAnnotation(), depth, type);
		}else throw new RuntimeException("Illegal term " + term);
	}
	
	public NamedTerm(AuTerm annotation) {
		super(annotation);
	}
	
	@Override public boolean verifyClosed(int max_depth) {
		throw new UnsupportedOperationException();
	}

}
