package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkConst;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Products {
	public static AuTerm utilProduct(AuTerm a, AuTerm b) {
		return mkOperator(product, a.type(), b.type(), a, b);
	}
	public static AuTerm utilProjectL(AuTerm product) {
		AuOperator ptype = (AuOperator)product.type();
		if(ptype.getDescriptor() != productType)
			throw new IllegalArgumentException();
		AuTerm ltype = ptype.getArgument(0);
		AuTerm rtype = ptype.getArgument(1);
		return mkOperator(projectL, ltype, rtype, product);
	}
	public static AuTerm utilProjectR(AuTerm product) {
		AuOperator ptype = (AuOperator)product.type();
		if(ptype.getDescriptor() != productType)
			throw new IllegalArgumentException();
		AuTerm ltype = ptype.getArgument(0);
		AuTerm rtype = ptype.getArgument(1);
		return mkOperator(projectR, ltype, rtype, product);
	}
	
	public static AuOperator.Descriptor productType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkMeta())), 2) {
		@Override public String toString() {
			return "Product";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static AuOperator.Descriptor product = new AuOperator.Descriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkVar(1, mkMeta()),
			   mkPi(mkVar(1, mkMeta()),
			    mkOperator(productType, mkVar(3, mkMeta()), mkVar(2, mkMeta())))))), 4) {
		@Override public String toString() {
			return "product";
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
	
	public static AuOperator.Descriptor projectL = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkOperator(productType, mkVar(1, mkMeta()), mkVar(0, mkMeta())),
			   mkVar(2, mkMeta())))), 3) {
		@Override public String toString() {
			return "projectL";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[2].primitive())
				return mkOperator(this, args);
			AuOperator operator = (AuOperator)args[2];
			return operator.getArgument(2);
		}
	};
	
	public static AuOperator.Descriptor projectR = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkOperator(productType, mkVar(1, mkMeta()), mkVar(0, mkMeta())),
			   mkVar(1, mkMeta())))), 3) {
		@Override public String toString() {
			return "projectR";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[2].primitive())
				return mkOperator(this, args);
			AuOperator operator = (AuOperator)args[2];
			return operator.getArgument(3);
		}
	};

	public static AuOperator.GroundDescriptor isProductType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkConst(Bool.boolType)), 1) {
		@Override public String toString() { return "isProductType"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[0].primitive())
				return mkOperator(this, args);
			if(!(args[0] instanceof AuOperator))
				return mkConst(new Bool.BoolLit(false));
			AuOperator operator = (AuOperator)args[0];
			return mkConst(new Bool.BoolLit(operator.getDescriptor() == productType));
		}
	};
}
