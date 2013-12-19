package org.managarm.korona.lang;

import java.util.List;

import org.managarm.aurora.lang.AuTerm;

public class Overload {
	private AuTerm p_term;
	private List<Lift> p_lifts;
	
	public Overload(AuTerm function, List<Lift> lifts) {
		p_term = function;
		p_lifts = lifts;
	}
	public AuTerm getTerm() {
		return p_term;
	}
	public int numLifts() {
		return p_lifts.size();
	}
	public Lift getLift(int i) {
		return p_lifts.get(i);
	}
}
