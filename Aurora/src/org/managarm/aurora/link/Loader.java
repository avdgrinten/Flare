package org.managarm.aurora.link;

import java.util.Iterator;

import org.managarm.aurora.lang.AuTerm;

public interface Loader {
	public Iterator<AuTerm> symbolIterator(String module);
	public AuTerm getSymbol(String module, String name, AuTerm type);
}
