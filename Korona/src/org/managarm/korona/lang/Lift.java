package org.managarm.korona.lang;

import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.util.NamedTerm;

public class Lift {
	private AuTerm p_argument;
	private AuTerm p_type;
	private NamedTerm.Name p_variable;
	
	public Lift(AuTerm argument, AuTerm type, NamedTerm.Name variable) {
		p_argument = argument;
		p_type = type;
		p_variable = variable;
	}
	public AuTerm getArgument() {
		return p_argument;
	}
	public AuTerm getType() {
		return p_type;
	}
	public NamedTerm.Name getVariable() {
		return p_variable;
	}
}
