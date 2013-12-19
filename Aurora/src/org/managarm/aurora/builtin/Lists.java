package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Lists {
	public static AuTerm utilEmpty(AuTerm type) {
		return mkOperator(emptyList, type);
	}
	public static int utilLen(AuTerm list) {
		AuOperator ltype = (AuOperator)list.type();
		if(ltype.getDescriptor() != listType)
			throw new IllegalArgumentException();
		AuTerm basetype = ltype.getArgument(0);
		AuTerm res = mkOperator(listLen, basetype, list);
		return IntArithmetic.IntLit.extract(res).getValue().intValue();
	}
	public static AuTerm utilAppend(AuTerm list, AuTerm value) {
		return mkOperator(listAppend, value.type(), list, value);
	}
	public static AuTerm utilElem(AuTerm list, int index) {
		AuOperator ltype = (AuOperator)list.type();
		if(ltype.getDescriptor() != listType)
			throw new IllegalArgumentException();
		AuTerm basetype = ltype.getArgument(0);
		return mkOperator(listElem, basetype, list,
				mkConst(new IntArithmetic.IntLit(index)),
				mkConst(Proof.tautology));
	}
	
	public static AuOperator.Descriptor listType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkMeta()), 1) {
		@Override public String toString() { return "List"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static class List extends AuOperator.Descriptor {
		public static List extract(AuTerm term) {
			return (List)((AuOperator)term).getDescriptor();
		}
		
		// helper function to construct the type of this operator
		private static AuTerm thisType(int length) {
			AuTerm type = mkOperator(listType,
					mkVar(length, mkMeta()));
			for(int i = 0; i < length; i++)
				type = mkPi(mkVar(length - i - 1, mkMeta()), type);
			return mkPi(mkMeta(), type);
		}

		int p_length;
		public List(int length) {
			super(thisType(length), length + 1);
			p_length = length;
		}
		public int getLength() { return p_length; }
		
		@Override public String toString() {
			if(p_length == 0)
				return "emptyList";
			if(p_length == 1)
				return "singletonList";
			return "list{" + p_length + "}";
		}
		@Override public int hashCode() {
			return p_length;
		}
		@Override public boolean equals(Object object) {
			if(!(object instanceof List))
				return false;
			List other = (List)object;
			return p_length == other.p_length;
		}
		
		@Override protected boolean reducible(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return true;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor emptyList = new List(0);
	public static AuOperator.Descriptor singletonList = new List(1);
	
	public static AuOperator.Descriptor listLen = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkOperator(listType, mkVar(0, mkMeta())),
			  mkConst(IntArithmetic.intType))), 2) {
		@Override public String toString() {
			return "listLen";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[1].primitive())
				return mkOperator(this, args);
			List list = List.extract(args[1]);
			return mkConst(new IntArithmetic.IntLit(list.getLength()));
		}
	};
	public static AuOperator.Descriptor listAppend = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkOperator(listType, mkVar(0, mkMeta())),
			  mkPi(mkVar(1, mkMeta()),
			   mkOperator(listType, mkVar(2, mkMeta()))))), 3) {
		@Override public String toString() {
			return "listAppend";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[1].primitive())
				return mkOperator(this, args);
			AuOperator list = (AuOperator)args[1];
			int length = List.extract(list).getLength();
			
			AuTerm[] new_args = new AuTerm[length + 2];
			new_args[0] = args[0];
			for(int i = 0; i < length; i++)
				new_args[i + 1] = list.getArgument(i + 1);
			new_args[length + 1] = args[2];
			
			return mkOperator(new List(length + 1), new_args);
		}
	};
	public static AuOperator.Descriptor listElem = new AuOperator.EvalDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkOperator(listType, mkVar(0, mkMeta())),
			  mkPi(mkConst(IntArithmetic.intType),
			   mkPi(mkOperator(Proof.proofType,
			       mkOperator(IntArithmetic.intLt,
			           mkVar(0, mkConst(IntArithmetic.intType)),
			           mkOperator(listLen, mkVar(2, mkMeta()),
			               mkVar(1, mkOperator(listType, mkVar(2, mkMeta())))))),
			    mkOperator(listType, mkVar(3, mkMeta())))))), 4) {
		@Override public String toString() {
			return "listElem";
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			if(!args[1].primitive() || !args[2].primitive())
				return mkOperator(this, args);
			AuOperator list = (AuOperator)args[1];
			IntArithmetic.IntLit index = IntArithmetic.IntLit.extract(args[2]);
			return list.getArgument(index.getValue().intValue() + 1);
		}
	};
}
