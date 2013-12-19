package org.managarm.aurora.link;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkApply;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.builtin.Io;
import org.managarm.aurora.builtin.Locals;
import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Interpreter {
	private static class Data {
		private AuTerm value;
	}
	private static class InternalRef extends AuOperator.Descriptor {
		public static InternalRef extract(AuTerm term) {
			return (InternalRef)((AuOperator)term).getDescriptor();
		}
		private Data p_data;
		
		public InternalRef(Data data) {
			super(mkPi(mkMeta(),
				mkVar(0, mkMeta())), 1);
			p_data = data;
		}
		public AuTerm read() {
			return p_data.value;
		}
		public void write(AuTerm value) {
			p_data.value = value;
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
	}
	
	public AuTerm execute(AuTerm term) {
		if(term.isOperator(Mutation.lift)) {
			AuOperator operator = (AuOperator)term;
			return operator.getArgument(1);
		}else if(term.isOperator(Mutation.seq)) {
			AuOperator operator = (AuOperator)term;
			AuTerm pred = execute(operator.getArgument(2));
			AuTerm res = mkApply(operator.getArgument(3), pred);
			return execute(res);
		}else if(term.isOperator(Locals.localAlloc)) {
			AuOperator operator = (AuOperator)term;
			AuTerm type = operator.getArgument(0);
			AuTerm scope = operator.getArgument(2);
			
			Data data = new Data();
			InternalRef ref = new InternalRef(data);
			
			return execute(mkApply(scope, mkOperator(ref,
					mkOperator(Locals.localType, type))));
		}else if(term.isOperator(Locals.localWrite)) {
			AuOperator operator = (AuOperator)term;
			InternalRef ref = InternalRef.extract(operator.getArgument(1));
			ref.write(operator.getArgument(2));
			return mkConst(Nil.nilValue);
		}else if(term.isOperator(Locals.localRead)) {
			AuOperator operator = (AuOperator)term;
			InternalRef ref = InternalRef.extract(operator.getArgument(1));
			return ref.read();
		}else if(term.isOperator(Io.print)) {
			AuOperator operator = (AuOperator)term;
			System.out.println(operator.getArgument(1));
			return mkConst(Nil.nilValue);
		}else throw new RuntimeException("Illegal term " + term);
	}
}
