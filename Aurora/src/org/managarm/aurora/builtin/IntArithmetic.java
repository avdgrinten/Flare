package org.managarm.aurora.builtin;

import java.math.BigInteger;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuOperator;

import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkApply;

public class IntArithmetic {
	public static AuConstant.Descriptor intType = new AuConstant.Descriptor(mkMeta()) {
		@Override public String toString() { return "Int"; }
	};
	
	public static class IntLit extends AuConstant.Descriptor {
		public static IntLit extract(AuTerm term) {
			return (IntLit)((AuConstant)term).getDescriptor();
		}
		
		private BigInteger value;
		
		public IntLit(BigInteger value) {
			super(mkConst(intType));
			this.value = value;
		}
		public IntLit(long value) {
			this(BigInteger.valueOf(value));
		}
		public BigInteger getValue() {
			return value;
		}
		@Override public String toString() { return value.toString(); }
	};

	public static AuOperator.Descriptor intAdd = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(intType))), 2) {
		@Override public String toString() { return "intAdd"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new IntLit(x.add(y)));
		}
	};
	public static AuOperator.Descriptor intSub = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(intType))), 2) {
		@Override public String toString() { return "intSub"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new IntLit(x.subtract(y)));
		}
	};
	public static AuOperator.Descriptor intMul = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(intType))), 2) {
		@Override public String toString() { return "intMul"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new IntLit(x.multiply(y)));
		}
	};
	public static AuOperator.Descriptor intDiv = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(intType))), 2) {
		@Override public String toString() { return "intDiv"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new IntLit(x.divide(y)));
		}
	};
	public static AuOperator.Descriptor intMod = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(intType))), 2) {
		@Override public String toString() { return "intMod"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new IntLit(x.mod(y)));
		}
	};

	public static AuOperator.Descriptor intEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) == 0));
		}
	};
	public static AuOperator.Descriptor intInEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intInEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) != 0));
		}
	};
	public static AuOperator.Descriptor intLt = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intLt"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) < 0));
		}
	};
	public static AuOperator.Descriptor intGt = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intGt"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) > 0));
		}
	};
	public static AuOperator.Descriptor intLe = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intLe"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) <= 0));
		}
	};
	public static AuOperator.Descriptor intGe = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkConst(intType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "intGe"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[1].primitive())
				return mkOperator(this, args);
			BigInteger x = IntLit.extract(args[0]).getValue();
			BigInteger y = IntLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) >= 0));
		}
	};

	public static AuOperator.Descriptor intFold = new AuOperator.EvalDescriptor(
			mkPi(mkConst(intType),
			 mkPi(mkMeta(),
			  mkPi(mkVar(0, mkMeta()),
			   mkPi(mkPi(mkVar(1, mkMeta()),
					   mkPi(mkConst(IntArithmetic.intType),
							   mkVar(3, mkMeta()))),
			    mkVar(2, mkMeta()))))), 4) {
		@Override public String toString() { return "intFold"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive() || !args[2].primitive()
					|| !args[3].primitive())
				return mkOperator(this, args);
			AuTerm value = args[2];
			BigInteger n = IntLit.extract(args[0]).getValue();
			for(int i = 0; i < n.intValue(); i++)
				value = mkApply(mkApply(args[3], value),
						mkConst(new IntLit(i))).reduce();
			return value;
		}
	};
}
