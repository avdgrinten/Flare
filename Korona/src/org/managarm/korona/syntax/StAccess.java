package org.managarm.korona.syntax;

public class StAccess extends StNode {
	private StNode p_left;
	private String p_identifier;
	
	public StAccess(StNode left, String identifier) {
		p_left = left;
		p_identifier = identifier;
	}
	public StNode getLeft() {
		return p_left;
	}
	public String getIdentifier() {
		return p_identifier;
	}
}
