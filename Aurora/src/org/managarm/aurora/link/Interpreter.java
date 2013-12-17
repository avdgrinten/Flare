package org.managarm.aurora.link;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkApply;

import org.managarm.aurora.builtin.Io;
import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Interpreter {
	public AuTerm execute(AuTerm term) {
		if(term.isOperator(Mutation.lift)) {
			AuOperator operator = (AuOperator)term;
			return operator.getArgument(1);
		}else if(term.isOperator(Mutation.seq)) {
			AuOperator operator = (AuOperator)term;
			AuTerm pred = execute(operator.getArgument(2));
			AuTerm res = mkApply(operator.getArgument(3), pred);
			return execute(res.reduce());
		}else if(term.isOperator(Io.print)) {
			AuOperator operator = (AuOperator)term;
			System.out.println(operator.getArgument(1));
			return mkConst(Nil.nilValue);
		}else throw new RuntimeException("Illegal term " + term);
	}
}
