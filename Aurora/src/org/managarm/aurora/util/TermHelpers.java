package org.managarm.aurora.util;

import static org.managarm.aurora.lang.AuTerm.mkApplyExt;
import static org.managarm.aurora.lang.AuTerm.mkLambdaExt;
import static org.managarm.aurora.lang.AuTerm.mkOperatorExt;
import static org.managarm.aurora.lang.AuTerm.mkPiExt;
import static org.managarm.aurora.lang.AuTerm.mkVarExt;

import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;

public class TermHelpers {
	public static interface Predicate {
		public boolean test(AuTerm in);
	}
	public static interface Map {
		public AuTerm map(AuTerm in);
	}
	
	public static boolean anyTerm(AuTerm in, Predicate pred) {
		if(pred.test(in))
			return true;

		if(in instanceof AuMeta) {
			return false;
		}else if(in instanceof AuVar) {
			AuVar term = (AuVar)in;
			return anyTerm(term.getType(), pred);
		}else if(in instanceof AuPi) {
			AuPi term = (AuPi)in;
			return anyTerm(term.getBound(), pred)
					|| anyTerm(term.getCodomain(), pred);
		}else if(in instanceof AuLambda) {
			AuLambda term = (AuLambda)in;
			return anyTerm(term.getBound(), pred)
					|| anyTerm(term.getExpr(), pred);
		}else if(in instanceof AuApply) {
			AuApply term = (AuApply)in;
			return anyTerm(term.getFunction(), pred)
					|| anyTerm(term.getArgument(), pred);
		}else if(in instanceof AuOperator) {
			AuOperator term = (AuOperator)in;
			for(int i = 0; i < term.numArguments(); i++)
				if(anyTerm(term.getArgument(i), pred))
					return true;
			return false;
		}else if(in instanceof AuConstant) {
			return false;
		}else throw new AssertionError("Illegal term " + in);
	}
	
	public static AuTerm defaultMap(AuTerm in, Map func) {
		if(in instanceof AuMeta) {
			return in;
		}else if(in instanceof AuVar) {
			AuVar term = (AuVar)in;
			return mkVarExt(term.getAnnotation(), term.getDepth(),
					func.map(term.getType()));
		}else if(in instanceof AuPi) {
			AuPi term = (AuPi)in;
			return mkPiExt(term.getAnnotation(),
					func.map(term.getBound()),
					func.map(term.getCodomain()));
		}else if(in instanceof AuLambda) {
			AuLambda term = (AuLambda)in;
			return mkLambdaExt(term.getAnnotation(),
					func.map(term.getBound()),
					func.map(term.getExpr()));
		}else if(in instanceof AuApply) {
			AuApply term = (AuApply)in;
			return mkApplyExt(term.getAnnotation(),
					func.map(term.getFunction()),
					func.map(term.getArgument()));
		}else if(in instanceof AuOperator) {
			AuOperator term = (AuOperator)in;
			AuTerm[] new_args = new AuTerm[term.numArguments()];
			for(int i = 0; i < term.numArguments(); i++)
				new_args[i] = func.map(term.getArgument(i));
			return mkOperatorExt(term.getAnnotation(),
					term.getDescriptor(), new_args);
		}else if(in instanceof AuConstant) {
			return in;
		}else throw new AssertionError("Illegal term " + in);
	}
	
	public static AuTerm replace(AuTerm term,
			final AuTerm search, final AuTerm replace) {
		Map function = new Map() {
			@Override public AuTerm map(AuTerm in) {
				if(in.equals(search))
					return replace;
				return defaultMap(in, this);
			}
		};
		return function.map(term);
	}
}
