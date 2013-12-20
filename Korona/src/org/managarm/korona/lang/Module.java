package org.managarm.korona.lang;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.util.Descriptor;
import org.managarm.aurora.util.Descriptor.RecordPath;

public class Module extends AuConstant.Descriptor {
	public static AuConstant.Descriptor moduleType = new AuConstant.Descriptor(
			mkMeta()) {
		@Override public String toString() {
			return "Symbol";
		}
	};
	
	// null if this is the root module
	private String p_name;
	private String p_path;
	
	private Map<String, Module> p_subModules = new HashMap<String, Module>();
	private List<AuTerm> p_symbols = new ArrayList<AuTerm>();
	
	public Module(String name, String path) {
		super(mkConst(moduleType));
		p_name = name;
		p_path = path;
	}
	public String getName() { return p_name; }
	public String getPath() { return p_path; }
	
	public Module addSubModule(String name) {
		if(p_subModules.containsKey(name))
			return p_subModules.get(name);
		
		String path = p_path == null ? name : p_path + "." + name; 
		Module submodule = new Module(name, path);
		p_subModules.put(name, submodule);
		return submodule;
	}
	public Iterator<Module> subModuleIterator() {
		return p_subModules.values().iterator();
	}
	
	public void addSymbol(AuTerm symbol) {
		p_symbols.add(symbol);
	}
	public Iterator<AuTerm> symbolIterator() {
		return p_symbols.iterator();
	}
	public boolean hasSymbol(String name, AuTerm type) {
		for(AuTerm symbol : p_symbols) {
			Descriptor desc = new Descriptor(symbol);
			String symb_name = desc.getString(new RecordPath("name"));
			AuTerm symb_type = desc.get(new RecordPath("type"));
			
			if(!symb_name.equals(name))
				continue;
			if(!symb_type.equals(type))
				continue;
			return true;
		}
		return false;
	}
}
