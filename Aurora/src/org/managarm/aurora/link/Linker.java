package org.managarm.aurora.link;

import static org.managarm.aurora.lang.AuTerm.mkApplyExt;
import static org.managarm.aurora.lang.AuTerm.mkLambdaExt;
import static org.managarm.aurora.lang.AuTerm.mkOperatorExt;
import static org.managarm.aurora.lang.AuTerm.mkPiExt;
import static org.managarm.aurora.lang.AuTerm.mkVarExt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.builtin.Symbols;
import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;
import org.managarm.aurora.util.Descriptor;
import org.managarm.aurora.util.Descriptor.RecordPath;

public class Linker implements Loader {
	private List<AuTerm> p_symbols = new ArrayList<AuTerm>();
	
	public void linkSymbol(AuTerm in) {
		Descriptor in_desc = new Descriptor(in);
		
		String symb_module = in_desc.getString(new RecordPath("module"));
		String symb_name = in_desc.getString(new RecordPath("name"));
		AuTerm symb_type = in_desc.get(new RecordPath("type"));
		AuTerm symb_defn = in_desc.get(new RecordPath("defn"));
		
		AuTerm resolved_defn = resolve(symb_defn);
		AuTerm resolved_type = resolve(symb_type);
			
		Descriptor desc = new Descriptor(Descriptor.emptyRecord);
		desc.setString(symb_module, new RecordPath("module"));
		desc.setString(symb_name, new RecordPath("name"));
		desc.set(resolved_defn, new RecordPath("defn"));
		desc.set(resolved_type, new RecordPath("type"));
		
		p_symbols.add(desc.asTerm());
	}
	
	@Override public Iterator<AuTerm> symbolIterator(String module) {
		List<AuTerm> res = new ArrayList<AuTerm>();
		for(AuTerm symbol : p_symbols) {
			Descriptor desc = new Descriptor(symbol);
			
			String symb_module = desc.getString(new RecordPath("module"));
			if(!symb_module.equals(module))
				continue;
			res.add(symbol);
		}
		return res.iterator();
	}
	@Override public AuTerm getSymbol(String module, String name, AuTerm type) {
		for(AuTerm symbol : p_symbols) {
			Descriptor desc = new Descriptor(symbol);
			
			String symb_module = desc.getString(new RecordPath("module"));
			String symb_name = desc.getString(new RecordPath("name"));
			AuTerm symb_type = desc.get(new RecordPath("type"));
			
			if(!symb_module.equals(module))
				continue;
			if(!symb_name.equals(name))
				continue;
			if(!symb_type.equals(type))
				continue;
			return symbol;
		}
		return null;
	}
	
	private AuTerm resolve(AuTerm term) {
		if(term instanceof AuOperator
				&& ((AuOperator)term).getDescriptor() == Symbols.symbolInline) {
			AuOperator operator = (AuOperator)term;
			AuTerm module = operator.getArgument(0);
			AuTerm name = operator.getArgument(1);
			AuTerm type = operator.getArgument(2);
			if(!name.primitive() || !type.primitive())
				throw new RuntimeException("Could not resolve " + term);
			
			AuTerm symbol = getSymbol(Strings.StringLit.extract(module).getValue(),
					Strings.StringLit.extract(name).getValue(), type);
			if(symbol == null)
				throw new RuntimeException("Could not find global " + name
						+ " of type " + type);
			Descriptor desc = new Descriptor(symbol);
			
			AuTerm defn = desc.get(new RecordPath("defn"));			
			return defn;
		}
		
		if(term instanceof AuApply) {
			AuApply apply = (AuApply)term;
			
			AuTerm function = resolve(apply.getFunction());
			AuTerm argument = resolve(apply.getArgument());
			return mkApplyExt(apply.getAnnotation(),
					function, argument);
		}else if(term instanceof AuConstant) {
			return term;
		}else if(term instanceof AuLambda) {
			AuLambda lambda = (AuLambda)term;
			
			AuTerm bound = resolve(lambda.getBound());
			AuTerm expr = resolve(lambda.getExpr());
			return mkLambdaExt(lambda.getAnnotation(), bound, expr);
		}else if(term instanceof AuMeta) {
			return term;
		}else if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			
			AuTerm[] arguments = new AuTerm[operator.numArguments()];
			for(int i = 0; i < operator.numArguments(); i++)
				arguments[i] = resolve(operator.getArgument(i));
			return mkOperatorExt(operator.getAnnotation(),
					operator.getDescriptor(), arguments);
		}else if(term instanceof AuPi) {
			AuPi pi = (AuPi)term;
			
			AuTerm bound = resolve(pi.getBound());
			AuTerm codomain = resolve(pi.getCodomain());
			return mkPiExt(pi.getAnnotation(), bound, codomain);
		}else if(term instanceof AuVar) {
			AuVar var = (AuVar)term;
			
			AuTerm type = resolve(var.getType());
			return mkVarExt(var.getAnnotation(), var.getDepth(), type);
		}else throw new RuntimeException("Illegal term " + term);
	}
}
