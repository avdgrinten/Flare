package org.managarm.aurora.util;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;

public class Unificator {
	public static class ImUnknown extends AuOperator.Descriptor {
		public static boolean instance(AuTerm term) {
			if(!(term instanceof AuOperator))
				return false;
			return ((AuOperator)term).getDescriptor() instanceof ImUnknown;
		}
		public static ImUnknown extract(AuTerm term) {
			return (ImUnknown)((AuOperator)term).getDescriptor();
		}
		
		public ImUnknown() {
			super(mkPi(mkMeta(),
			 mkVar(0, mkMeta())), 1);
		}
		@Override public String toString() {
			return "imUnknown{" + System.identityHashCode(this) + "}";
		}
		
		@Override protected boolean reducible(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor any = new AuOperator.Descriptor(mkPi(mkMeta(),
			 mkVar(0, mkMeta())), 1) {
		@Override public String toString() {
			return "any";
		}
		@Override protected boolean reducible(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor dummy = new AuOperator.Descriptor(mkPi(mkMeta(),
			 mkVar(0, mkMeta())), 1) {
		@Override public String toString() {
			return "dummy";
		}
		@Override protected boolean reducible(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static interface Observer {
		public void onReplace(AuOperator unknown, AuTerm value);
	}
	public static class ReplaceObserver implements Observer {
		private AuTerm p_term;

		public ReplaceObserver(AuTerm term) {
			p_term = term;
		}
		public AuTerm getTerm() {
			return p_term;
		}
		
		@Override public void onReplace(AuOperator unknown, AuTerm value) {
			p_term = TermHelpers.replace(p_term, unknown, value);
		}
	}
	
	public enum Result {
		kResEqual,
		kResUnifiable,
		kResDistinct
	}
	
	public static class Disagreement {
		public AuOperator prototype;
		public AuTerm instance;
		public Disagreement(AuOperator prototype, AuTerm instance) {
			this.prototype = prototype;
			this.instance = instance;
		}
	}
	
	public static Result equalModAny(AuTerm proto_term, AuTerm inst_term) {
		if(inst_term.isOperator(any))
			return Result.kResEqual;
		if(ImUnknown.instance(proto_term)) {
			Result type_res = equalModAny(proto_term.type(), inst_term.type());
			if(type_res == Result.kResDistinct)
				return Result.kResDistinct;
			if(type_res == Result.kResUnifiable)
				return Result.kResUnifiable;
			if(ImUnknown.instance(inst_term)
				&& ((AuOperator)proto_term).getDescriptor()
					== ((AuOperator)inst_term).getDescriptor())
				return Result.kResEqual;
			return Result.kResUnifiable;
		}
		
		if(proto_term instanceof AuMeta) {
			if(!(inst_term instanceof AuMeta))
				return Result.kResDistinct;
			return Result.kResEqual;
		}else if(proto_term instanceof AuVar) {
			if(!(inst_term instanceof AuVar))
				return Result.kResDistinct;
			AuVar prototype = (AuVar)proto_term;
			AuVar instance = (AuVar)inst_term;
			if(prototype.getDepth() != instance.getDepth())
				return Result.kResDistinct;
			return equalModAny(prototype.getType(),
					instance.getType());
		}else if(proto_term instanceof AuLambda) {
			if(!(inst_term instanceof AuLambda))
				return Result.kResDistinct;
			AuLambda prototype = (AuLambda)proto_term;
			AuLambda instance = (AuLambda)inst_term;
			Result bound_res = equalModAny(prototype.getBound(),
					instance.getBound());
			if(bound_res != Result.kResEqual)
				return bound_res;
			Result expr_res = equalModAny(prototype.getExpr(),
					instance.getExpr());
			if(expr_res != Result.kResEqual)
				return expr_res;
			return Result.kResEqual;
		}else if(proto_term instanceof AuPi) {
			if(!(inst_term instanceof AuPi))
				return Result.kResDistinct;
			AuPi prototype = (AuPi)proto_term;
			AuPi instance = (AuPi)inst_term;
			Result bound_res = equalModAny(prototype.getBound(),
					instance.getBound());
			if(bound_res != Result.kResEqual)
				return bound_res;
			Result codomain_res = equalModAny(prototype.getCodomain(),
					instance.getCodomain());
			if(codomain_res != Result.kResEqual)
				return codomain_res;
			return Result.kResEqual;
		}else if(proto_term instanceof AuApply) {
			if(!(inst_term instanceof AuApply))
				return Result.kResDistinct;
			AuApply prototype = (AuApply)proto_term;
			AuApply instance = (AuApply)inst_term;
			Result func_res = equalModAny(prototype.getFunction(),
					instance.getFunction());
			if(func_res != Result.kResEqual)
				return func_res;
			Result arg_res = equalModAny(prototype.getArgument(),
					instance.getArgument());
			if(arg_res != Result.kResEqual)
				return arg_res;
			return Result.kResEqual;
		}else if(proto_term instanceof AuOperator) {
			if(!(inst_term instanceof AuOperator))
				return Result.kResDistinct;
			AuOperator prototype = (AuOperator)proto_term;
			AuOperator instance = (AuOperator)inst_term;
			if(!prototype.getDescriptor().equals(instance.getDescriptor()))
				return Result.kResDistinct;
			if(prototype.numArguments() != instance.numArguments())
				return Result.kResDistinct;
			for(int i = 0; i < prototype.numArguments(); i++) {
				Result res = equalModAny(prototype.getArgument(i),
						instance.getArgument(i));
				if(res == Result.kResEqual)
					continue;
				return res;
			}
			return Result.kResEqual;
		}else if(proto_term instanceof AuConstant) {
			if(!(inst_term instanceof AuConstant))
				return Result.kResDistinct;
			AuConstant prototype = (AuConstant)proto_term;
			AuConstant instance = (AuConstant)inst_term;
			if(!prototype.getDescriptor().equals(instance.getDescriptor()))
				return Result.kResDistinct;
			return Result.kResEqual;
		}else throw new RuntimeException("Illegal term " + proto_term);
	}
	
	public static Disagreement findDisagreement(AuTerm proto_term, AuTerm inst_term) {
		Result term_res = equalModAny(proto_term, inst_term);
		if(term_res == Result.kResEqual)
			throw new AssertionError("Terms match");
		if(term_res == Result.kResDistinct)
			throw new AssertionError("Terms are distinct");
		
		if(ImUnknown.instance(proto_term)) {
			Result type_res = equalModAny(proto_term.type(), inst_term.type());
			if(type_res == Result.kResDistinct)
				throw new AssertionError("Types are distinct");
			if(type_res == Result.kResUnifiable)
				return findDisagreement(proto_term.type(), inst_term.type());
			
			return new Disagreement((AuOperator)proto_term, inst_term);
		}
		
		if(proto_term instanceof AuVar) {
			if(!(inst_term instanceof AuVar))
				throw new AssertionError("Terms are distinct");
			AuVar prototype = (AuVar)proto_term;
			AuVar instance = (AuVar)inst_term;
			
			if(prototype.getDepth() != instance.getDepth())
				throw new AssertionError("Terms are distinct");
			return findDisagreement(prototype.getType(), instance.getType());
		}else if(proto_term instanceof AuPi) {
			if(!(inst_term instanceof AuPi))
				throw new AssertionError("Terms are distinct");
			AuPi prototype = (AuPi)proto_term;
			AuPi instance = (AuPi)inst_term;
				
			Result bound_res = equalModAny(prototype.getBound(),
					instance.getBound());
			if(bound_res == Result.kResUnifiable)
				return findDisagreement(prototype.getBound(),
						instance.getBound());

			Result codomain_res = equalModAny(prototype.getCodomain(),
					instance.getCodomain());
			if(codomain_res == Result.kResUnifiable)			
				return findDisagreement(prototype.getCodomain(),
						instance.getCodomain());
			throw new AssertionError("Terms match");
		}else if(proto_term instanceof AuApply) {
			if(!(inst_term instanceof AuApply))
				throw new AssertionError("Terms are distinct");
			AuApply prototype = (AuApply)proto_term;
			AuApply instance = (AuApply)inst_term;
			
			Result func_res = equalModAny(prototype.getFunction(),
					instance.getFunction());
			if(func_res == Result.kResUnifiable)
				return findDisagreement(prototype.getFunction(),
						instance.getFunction());

			Result arg_res = equalModAny(prototype.getArgument(),
					instance.getArgument());
			if(arg_res == Result.kResUnifiable)			
				return findDisagreement(prototype.getArgument(),
						instance.getArgument());
			throw new AssertionError("Terms match");
		}else if(proto_term instanceof AuOperator) {
			if(!(inst_term instanceof AuOperator))
				throw new AssertionError("Terms are distinct");
			AuOperator prototype = (AuOperator)proto_term;
			AuOperator instance = (AuOperator)inst_term;
			
			AuOperator.Descriptor proto_desc = prototype.getDescriptor();
			AuOperator.Descriptor inst_desc = instance.getDescriptor();
			if(!proto_desc.equals(inst_desc))
				throw new AssertionError("Terms are distinct");
			if(prototype.numArguments() != instance.numArguments())
				throw new AssertionError("Terms are distinct");
			
			for(int i = 0; i < prototype.numArguments(); i++) {
				AuTerm proto_arg = prototype.getArgument(i);
				AuTerm inst_arg = instance.getArgument(i);
				
				Result res = equalModAny(proto_arg, inst_arg);
				if(res == Result.kResUnifiable)
					return findDisagreement(proto_arg, inst_arg);
			}
			throw new AssertionError("Terms match");
		}else throw new RuntimeException("Illegal term " + proto_term);
	}
	
	public static AuTerm unknownToIndef(AuTerm in) {
		TermHelpers.Map function = new TermHelpers.Map() {
			@Override public AuTerm map(AuTerm in) {
				if(ImUnknown.instance(in)) {
					AuOperator operator = (AuOperator)in;
					return mkOperator(Unificator.any, operator.getArgument(0));
				}else return TermHelpers.defaultMap(in, this);
			}
		};
		return function.map(in);
	}
	
	private AuTerm p_prototype;
	
	public Unificator(AuTerm prototype) {
		p_prototype = prototype;
	}
	
	public AuTerm getPrototype() { return p_prototype; }
	
	public boolean unify(AuTerm instance, Observer observer) {
		while(true) {
//			System.out.println("Unify...");
			Result res = equalModAny(p_prototype, instance);
			if(res == Result.kResEqual)
				return true;
			if(res == Result.kResDistinct)
				return false;
			
			Disagreement disagree = findDisagreement(p_prototype, instance);
//			System.out.println("   Disagree: " + disagree.prototype + " vs " + disagree.instance);
			
			if(disagree.prototype instanceof AuOperator
					&& ((AuOperator)disagree.prototype).getDescriptor() instanceof ImUnknown) {
//				System.out.println("Replace " + disagree.prototype + " <- " + disagree.instance);
				p_prototype = TermHelpers.replace(p_prototype,
						disagree.prototype, disagree.instance);
				if(observer != null)
					observer.onReplace(disagree.prototype, disagree.instance);
//				System.out.println("  prototype: " + p_prototype);
//				System.out.println("  instance: " + instance);
			}else return false;
		}
	}
}
