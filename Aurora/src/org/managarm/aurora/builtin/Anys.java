package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Anys {
	public static AuTerm utilBox(AuTerm value) {
		return mkOperator(any, value.type(), value);
	}
	public static AuTerm utilExtract(AuTerm any) {
		return mkOperator(anyExtract, any);
	}
	
	public static AuConstant.Descriptor anyType = new AuConstant.Descriptor(
			mkMeta()) {
		@Override public String toString() {
			return "Any";
		}
	};
	
	public static AuOperator.Descriptor any = new AuOperator.Descriptor(
			mkPi(mkMeta(),
			 mkPi(mkVar(0, mkMeta()),
			  mkConst(anyType))), 2) {
		@Override public String toString() {
			return "any";
		}

		@Override protected boolean reductive(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return true;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static AuOperator.Descriptor anyMeta = new AuOperator.EvalDescriptor(
			mkPi(mkConst(anyType),
			 mkMeta()), 1) {
		@Override public String toString() {
			return "anyMeta";
		}
		
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive())
				return mkOperator(this, args);
			AuOperator operator = (AuOperator)args[0]; 
			return operator.getArgument(0);
		}
	};
	public static AuOperator.Descriptor anyExtract = new AuOperator.EvalDescriptor(
			mkPi(mkConst(anyType),
			 mkOperator(anyMeta, mkVar(0, mkConst(anyType)))), 1) {
		@Override public String toString() {
			return "anyExtract";
		}
		
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive())
				return mkOperator(this, args);
			AuOperator operator = (AuOperator)args[0];
			return operator.getArgument(1);
		}
	};
}
