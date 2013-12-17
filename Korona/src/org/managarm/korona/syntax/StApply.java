package org.managarm.korona.syntax;

public class StApply extends StNode {
	private StNode p_function;
	private StNode p_argument;
	
	public StApply(StNode function, StNode argument) {
		p_function = function;
		p_argument = argument;
	}
	public StNode getFunction() {
		return p_function;
	}
	public StNode getArgument() {
		return p_argument;
	}
	
	@Override public String toString() {
		return "(" + p_function + " " + p_argument + ")";
	}
}
