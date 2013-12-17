package org.managarm.jet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.link.Loader;
import org.managarm.jet.compiler.ClassFile;
import org.managarm.jet.compiler.ClassInfo;
import org.managarm.jet.compiler.Namespace;
import org.managarm.jet.compiler.Receiver;

public class JetLoader extends ClassLoader {
	private Namespace p_namespace;
	private Loader p_loader;
	private Map<String, ClassInfo> p_classes
			= new HashMap<String, ClassInfo>();
	
	private Receiver p_receiver = new Receiver() {
		@Override public void onDependency(ClassInfo dependency) {
			p_classes.put(dependency.getPath(), dependency);
		}
	};
	
	public JetLoader(Namespace namespace, Loader loader) {
		p_namespace = namespace;
		p_loader = loader;
	}
	
	public void addModule(String module) {
		Iterator<AuTerm> it = p_loader.symbolIterator(module);
		if(it == null)
			throw new RuntimeException("Could not find module " + module);
		
		List<AuTerm> symbols = new ArrayList<AuTerm>();
		while(it.hasNext())
			symbols.add(it.next());
		
		ClassInfo info = new ClassInfo.ModuleClass(p_namespace, module,
				symbols.toArray(new AuTerm[symbols.size()]));
		p_classes.put(info.getPath(), info);
	}
	
	@Override public Class<?> findClass(String name)
			throws ClassNotFoundException {
		String internal = name.replace('.', '/');
		if(!p_classes.containsKey(internal))
			throw new ClassNotFoundException("Could not find " + name);

		ClassFile file = p_classes.get(internal).buildCode(p_receiver);
		byte[] bytecode = file.getBytecode();
		return super.defineClass(name, bytecode, 0, bytecode.length);
	}
}
