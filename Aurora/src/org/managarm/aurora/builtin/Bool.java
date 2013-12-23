package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Bool {
	public static AuConstant.Descriptor boolType = new AuConstant.Descriptor(AuTerm.mkMeta()) {
		@Override public String toString() { return "Bool"; }
	};

	public static class BoolLit extends AuConstant.Descriptor {
		public static BoolLit extract(AuTerm term) {
			return (BoolLit)((AuConstant)term).getDescriptor();
		}
		
		private boolean value;
		
		public BoolLit(boolean value) {
			super(mkConst(boolType));
			this.value = value;
		}
		public boolean getValue() {
			return value;
		}
		
		@Override public String toString() {
			return Boolean.toString(value);
		}
		@Override public int hashCode() {
			return value ? 0 : 1;
		}
		@Override public boolean equals(Object object) {
			if(!(object instanceof BoolLit))
				return false;
			BoolLit other = (BoolLit)object;
			return value == other.value;
		}
	};

	public static AuOperator.Descriptor boolNot = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(boolType),
			  mkConst(Bool.boolType)), 1) {
		@Override public String toString() { return "boolNot"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			boolean x = BoolLit.extract(args[0]).getValue();
			return mkConst(new Bool.BoolLit(!x));
		}
	};
	
	public static AuOperator.Descriptor boolOr = new AuOperator.EvalDescriptor(
			mkPi(mkConst(boolType),
			 mkPi(mkConst(boolType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "boolOr"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			boolean x = BoolLit.extract(args[0]).getValue();
			boolean y = BoolLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x || y));
		}
	};
	public static AuOperator.Descriptor boolAnd = new AuOperator.EvalDescriptor(
			mkPi(mkConst(boolType),
			 mkPi(mkConst(boolType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "boolAnd"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			boolean x = BoolLit.extract(args[0]).getValue();
			boolean y = BoolLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x && y));
		}
	};
	
	public static AuOperator.Descriptor boolEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(boolType),
			 mkPi(mkConst(boolType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "boolEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			boolean x = BoolLit.extract(args[0]).getValue();
			boolean y = BoolLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x == y));
		}
	};
	public static AuOperator.Descriptor boolInEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(boolType),
			 mkPi(mkConst(boolType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "boolInEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			boolean x = BoolLit.extract(args[0]).getValue();
			boolean y = BoolLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x != y));
		}
	};
	
	public static AuOperator.Descriptor ite = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkConst(boolType),
			  mkPi(mkVar(1, mkMeta()),
			   mkPi(mkVar(2, mkMeta()),
			    mkVar(3, mkMeta()))))), 4) {
		@Override public String toString() {
			return "ite";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			BoolLit lit = BoolLit.extract(args[1]);
			return lit.value ? args[2] : args[3];
		}
	};
}
