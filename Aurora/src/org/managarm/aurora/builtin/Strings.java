package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkPi;

import java.math.BigInteger;

import org.managarm.aurora.builtin.Bool.BoolLit;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Strings {
	public static AuConstant.Descriptor stringType = new AuConstant.Descriptor(AuTerm.mkMeta()) {
		@Override public String toString() { return "String"; }
	};
	
	public static class StringLit extends AuConstant.Descriptor {
		public static StringLit extract(AuTerm term) {
			return (StringLit)((AuConstant)term).getDescriptor();
		}
		
		private String value;
		
		public StringLit(String value) {
			super(mkConst(stringType));
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
		@Override public String toString() {
			return '"' + value + '"';
		}
		@Override public boolean equals(Object object) {
			if(!(object instanceof StringLit))
				return false;
			StringLit other = (StringLit)object;
			return value.equals(other.value);
		}
	};
	
	public static AuOperator.Descriptor strEq = new AuOperator.EvalDescriptor(
			mkPi(mkConst(stringType),
			 mkPi(mkConst(stringType),
			  mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "strEq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			String y = StringLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.equals(y)));
		}
	};
	
	public static AuOperator.Descriptor strCmp = new AuOperator.EvalDescriptor(
			mkPi(mkConst(stringType),
			 mkPi(mkConst(stringType),
			  mkConst(IntArithmetic.intType))), 2) {
		@Override public String toString() { return "strCmp"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			String y = StringLit.extract(args[1]).getValue();
			return mkConst(new IntArithmetic.IntLit(x.compareTo(y)));
		}
	};
	
	public static AuOperator.Descriptor strConcat = new AuOperator.EvalDescriptor(
			mkPi(mkConst(stringType),
			 mkPi(mkConst(stringType),
			  mkConst(stringType))), 2) {
		@Override public String toString() { return "strConcat"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			String y = StringLit.extract(args[1]).getValue();
			return mkConst(new StringLit(x.concat(y)));
		}
	};
	
	public static AuOperator.Descriptor strLen = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			  mkConst(IntArithmetic.intType)), 1) {
		@Override public String toString() { return "strLen"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			return mkConst(new IntArithmetic.IntLit(x.length()));
		}
	};
	
	public static AuOperator.Descriptor strEmpty = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			  mkConst(Bool.boolType)), 1) {
		@Override public String toString() { return "strEmpty"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			return mkConst(new Bool.BoolLit(x.isEmpty()));
		}
	};
	
	public static AuOperator.Descriptor strTrim = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			  mkConst(stringType)), 1) {
		@Override public String toString() { return "strTrim"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			return mkConst(new StringLit(x.trim()));
		}
	};
	
	public static AuOperator.Descriptor strContains = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			  mkPi(mkConst(stringType),
			   mkConst(Bool.boolType))), 2) {
		@Override public String toString() { return "strContains"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			String y = StringLit.extract(args[1]).getValue();
			return mkConst(new Bool.BoolLit(x.contains(y)));
		}
	};
	
	public static AuOperator.Descriptor strTl = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			    mkConst(stringType)), 1) {
		@Override public String toString() { return "strTl"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			return mkConst(new StringLit(x.toLowerCase()));
		}
	};
	
	public static AuOperator.Descriptor strTu = new AuOperator.EvalDescriptor(
			mkPi(mkConst(stringType),
					mkConst(stringType)), 1) {
		@Override public String toString() { return "strTu"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			return mkConst(new StringLit(x.toUpperCase()));
		}
	};
	
	public static AuOperator.Descriptor strSub = new AuOperator.EvalDescriptor(
			 mkPi(mkConst(stringType),
			  mkPi(mkConst(IntArithmetic.intType),
			   mkPi(mkConst(IntArithmetic.intType),
			    mkConst(stringType)))), 3) {
		@Override public String toString() { return "strSub"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			String x = StringLit.extract(args[0]).getValue();
			BigInteger y = IntArithmetic.IntLit.extract(args[1]).getValue();
			BigInteger z = IntArithmetic.IntLit.extract(args[2]).getValue();
			return mkConst(new StringLit(x.substring(y.intValue(), z.intValue())));
		}
	};
}
