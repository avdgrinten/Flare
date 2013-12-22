package org.managarm.aurora.builtin;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuOperator;

import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;

public class DoubleArithmetic {
	public static AuConstant.Descriptor doubleType = new AuConstant.Descriptor(mkMeta()) {
		@Override public String toString() { return "Double"; }
	};
	
	public static class DoubleLit extends AuConstant.Descriptor {
		public static DoubleLit extract(AuTerm term) {
			return (DoubleLit)((AuConstant)term).getDescriptor();
		}
		
		private double p_value;
		
		public DoubleLit(Double value) {
			super(mkConst(doubleType));
			this.p_value = value;
		}
		public Double getValue() {
			return p_value;
		}
		
		@Override public String toString() {
			return Double.toString(p_value);
		}
		@Override public int hashCode() {
			return (int)p_value;
		}
		@Override public boolean equals(Object object) {
			if(!(object instanceof DoubleLit))
				return false;
			DoubleLit other = (DoubleLit)object;
			return p_value == other.p_value;
		}
	};

	public static AuOperator.Descriptor doubleAdd = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(doubleType))), 2) {
		@Override public String toString() { return "doubleAdd"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new DoubleLit(x + y));
		}
	};
	public static AuOperator.Descriptor doubleSub = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(doubleType))), 2) {
		@Override public String toString() { return "doubleSub"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new DoubleLit(x - y));
		}
	};
	public static AuOperator.Descriptor doubleMul = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(doubleType))), 2) {
		@Override public String toString() { return "doubleMul"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new DoubleLit(x * y));
		}
	};
	public static AuOperator.Descriptor doubleDiv = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(doubleType))), 2) {
		@Override public String toString() { return "doubleDiv"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new DoubleLit(x / y));
		}
	};

	public static AuOperator.Descriptor doubleEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) == 0));
		}
	};
	public static AuOperator.Descriptor doubleInEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleInEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) != 0));
		}
	};
	public static AuOperator.Descriptor doubleLt = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleLt"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) < 0));
		}
	};
	public static AuOperator.Descriptor doubleGt = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleGt"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) > 0));
		}
	};
	public static AuOperator.Descriptor doubleLe = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleLe"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) <= 0));
		}
	};
	public static AuOperator.Descriptor doubleGe = new AuOperator.EvalDescriptor(
			mkPi(mkConst(doubleType),
			 mkPi(mkConst(doubleType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "doubleGe"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			Double x = DoubleLit.extract(args[0]).getValue();
			Double y = DoubleLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.compareTo(y) >= 0));
		}
	};
}
