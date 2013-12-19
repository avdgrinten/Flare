package org.managarm.aurora.builtin;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

public class Mutation {
	public static AuOperator.Descriptor mutatorType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkMeta()), 1) {
		@Override public String toString() { return "Mutator"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};

	public static class Embed extends AuOperator.GroundDescriptor {
		public Embed() {
			super(mkPi(mkMeta(),
				 mkPi(mkVar(0, mkMeta()),
				  mkOperator(mutatorType, mkVar(1, mkMeta())))), 2);
		}
		@Override public String toString() { return "embed"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor embed = new Embed();


	public static class Seq extends AuOperator.GroundDescriptor {
		public Seq() {
			super(mkPi(mkMeta(),
				 mkPi(mkMeta(),
				  mkPi(mkOperator(mutatorType, mkVar(1, mkMeta())),
				   mkPi(mkPi(mkVar(2, mkMeta()), mkOperator(mutatorType, mkVar(2, mkMeta()))),
				    mkOperator(mutatorType, mkVar(2, mkMeta())))))), 4);
		}
		@Override public String toString() { return "seq"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor seq = new Seq();

	public static class Join extends AuOperator.GroundDescriptor {
		public Join() {
			super(mkPi(mkMeta(),
				 mkPi(mkMeta(),
				  mkPi(mkMeta(),
				   mkPi(mkOperator(mutatorType, mkVar(2, mkMeta())),
				    mkPi(mkOperator(mutatorType, mkVar(3, mkMeta())),
				     mkPi(mkPi(mkVar(4, mkMeta()), mkPi(mkVar(4, mkMeta()),
				    		 mkOperator(mutatorType, mkVar(4, mkMeta())))),
				      mkOperator(mutatorType, mkVar(3, mkMeta())))))))), 6);
		}
		@Override public String toString() { return "join"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor join = new Join();

	public static class Loop extends AuOperator.GroundDescriptor {
		public Loop() {
			super(mkPi(mkMeta(),
				 mkPi(mkVar(0, mkMeta()),
				  mkPi(mkPi(mkVar(1, mkMeta()),
						  mkOperator(mutatorType, mkConst(Bool.boolType))),
				   mkPi(mkPi(mkVar(2, mkMeta()), mkOperator(mutatorType, mkVar(3, mkMeta()))),
				    mkOperator(mutatorType, mkVar(3, mkMeta())))))), 4);
		}
		@Override public String toString() { return "loop"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor loop = new Loop();
}
