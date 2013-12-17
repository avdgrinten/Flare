package org.managarm.util.peg;

import java.util.HashMap;
import java.util.Map;

public class PegGrammar<R> {
	private Map<R, PegItem> rules = new HashMap<R, PegItem>();
	
	public void setRule(R name, PegItem rule) {
		rules.put(name, rule);
	}
	public PegItem getRule(R name) {
		return rules.get(name);
	}
	
	public PegItem ref(final R name) {
		final PegGrammar<R> self = this; 
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				return p.parse(self.getRule(name));
			}
		};
	}
}
