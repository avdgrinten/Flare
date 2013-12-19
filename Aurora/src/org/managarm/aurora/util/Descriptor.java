package org.managarm.aurora.util;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.managarm.aurora.builtin.Anys;
import org.managarm.aurora.builtin.Bool;
import org.managarm.aurora.builtin.IntArithmetic.IntLit;
import org.managarm.aurora.builtin.Lists;
import org.managarm.aurora.builtin.Products;
import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.builtin.Strings.StringLit;
import org.managarm.aurora.lang.AuTerm;

public class Descriptor {
	public static final AuTerm emptyRecord
			= Lists.utilEmpty(mkOperator(Products.productType,
					mkConst(Strings.stringType),
					mkConst(Anys.anyType)));
	
	public abstract static class Path {
		public abstract AuTerm get(AuTerm descriptor);
		public abstract AuTerm set(AuTerm descriptor, AuTerm term);
	}
	public static class RecordPath extends Path {
		private String p_ident;
		
		public RecordPath(String ident) {
			p_ident = ident;
		}
		
		@Override public AuTerm get(AuTerm descriptor) {
			for(int i = 0; i < Lists.utilLen(descriptor); i++) {
				AuTerm item = Lists.utilElem(descriptor, i);
				AuTerm key_term = Products.utilProjectL(item);
				String key = Strings.StringLit.extract(key_term).getValue();
				if(!key.equals(p_ident))
					continue;
				AuTerm value_term = Products.utilProjectR(item);
				return Anys.utilExtract(value_term);
			}
			return null;
		}
		@Override public AuTerm set(AuTerm descriptor, AuTerm term) {
			AuTerm key_term = mkConst(new Strings.StringLit(p_ident));
			AuTerm value_term = Anys.utilBox(term);
			AuTerm item = Products.utilProduct(key_term, value_term);
			return Lists.utilAppend(descriptor, item);
		}
	}
	
	private AuTerm p_descriptor;
	
	public Descriptor(AuTerm descriptor) {
		p_descriptor = descriptor;
	}
	public AuTerm asTerm() {
		return p_descriptor;
	}
	
	public AuTerm get(Path... path) {
		AuTerm current = p_descriptor;
		for(int i = 0; i < path.length; i++)
			current = path[i].get(current);
		return current;
	}
	public void set(AuTerm term, Path... path) {
		p_descriptor = setRecursive(p_descriptor, term, Arrays.asList(path));
	}

	public boolean getFlag(Path... path) {
		AuTerm value = get(path);
		if(value == null)
			return false;
		return Bool.BoolLit.extract(value).getValue();
	}
	public BigInteger getInt(Path... path) {
		return IntLit.extract(get(path)).getValue();
	}
	public String getString(Path... path) {
		AuTerm term = get(path);
		return StringLit.extract(term).getValue();
	}
	public void setFlag(boolean flag, Path... path) {
		if(flag) {
			set(mkConst(new Bool.BoolLit(flag)), path);
		}else{
			//FIXME
			if(!getFlag(path))
				return;
			throw new RuntimeException("Implement this");
		}
	}
	public void setString(String string, Path... path) {
		set(mkConst(new StringLit(string)), path);
	}
	
	private static AuTerm setRecursive(AuTerm descriptor, AuTerm term,
			List<Path> path) {
		if(path.size() == 0)
			return term;
		Path item = path.get(0);
		AuTerm res = setRecursive(item.get(descriptor), term,
				path.subList(1, path.size()));
		return item.set(descriptor, res);
	}
}
