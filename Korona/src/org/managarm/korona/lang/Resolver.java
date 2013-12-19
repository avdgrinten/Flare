package org.managarm.korona.lang;

import static org.managarm.aurora.lang.AuTerm.mkApply;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.util.NamedTerm.mkNamedLambdaExt;
import static org.managarm.aurora.util.NamedTerm.mkNamedPi;
import static org.managarm.aurora.util.NamedTerm.mkNamedVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.builtin.Symbols;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.util.Descriptor;
import org.managarm.aurora.util.Descriptor.RecordPath;
import org.managarm.aurora.util.NamedTerm;
import org.managarm.aurora.util.TermHelpers;
import org.managarm.aurora.util.Unificator;
import org.managarm.korona.syntax.StAccess;
import org.managarm.korona.syntax.StApply;
import org.managarm.korona.syntax.StFile;
import org.managarm.korona.syntax.StIdent;
import org.managarm.korona.syntax.StLambda;
import org.managarm.korona.syntax.StLiteral;
import org.managarm.korona.syntax.StMeta;
import org.managarm.korona.syntax.StNode;
import org.managarm.korona.syntax.StPi;
import org.managarm.korona.syntax.StRoot;

public class Resolver {
	// stores all symbols that are generated as part of this source file
	private List<AuTerm> p_symbols = new ArrayList<AuTerm>();
	
	private Module p_rootModule = new Module(null, null); 
	private Module p_thisModule;	
	private List<Module> p_imports = new ArrayList<Module>();
	
	private List<Scope> scopeStack = new ArrayList<Scope>();
	
	public Resolver() {
		
	}
	
	private void pushScope(Scope scope) {
		scopeStack.add(scope);
	}
	private void popScope() {
		scopeStack.remove(scopeStack.size() - 1);
	}
	
	private void resolveBind(String identifier, List<AuTerm> res) {
		for(int i = scopeStack.size() - 1; i >= 0; i--) {
			Scope item = scopeStack.get(i);
			if(!(item instanceof Scope.BindScope))
				continue;
			Scope.BindScope scope = (Scope.BindScope)item;
			if(!scope.getName().equals(identifier))
				continue;
			res.add(mkNamedVar(scope.getUid(), scope.getType()));
		}
	}
	private void resolveExtern(String identifier,
			Module module, List<AuTerm> res) {
		Iterator<Module> sub_it = module.subModuleIterator();
		while(sub_it.hasNext()) {
			Module submodule = sub_it.next();
			
			if(!submodule.getName().equals(identifier))
				continue;
			res.add(mkConst(submodule));
		}
		
		Iterator<AuTerm> symbol_it = module.symbolIterator();
		while(symbol_it.hasNext()) {
			Descriptor desc = new Descriptor(symbol_it.next());
			String glob_name = desc.getString(new RecordPath("name"));
			String glob_module = desc.getString(new RecordPath("module"));
			AuTerm glob_type = desc.get(new RecordPath("type"));
			AuTerm glob_defn = desc.get(new RecordPath("defn"));
			
			if(!glob_name.equals(identifier))
				continue;

			if(desc.getFlag(new RecordPath("fEmbedSymbol"))) {
				res.add(glob_defn);
			}else{
				res.add(mkOperator(Symbols.symbolInline,
						mkConst(new Strings.StringLit(glob_module)),
						mkConst(new Strings.StringLit(glob_name)),
						glob_type));
			}
		}
	}
	
	private List<AuTerm> resolveIdentifier(String identifier) {
		List<AuTerm> res = new ArrayList<AuTerm>();
		resolveBind(identifier, res);
		resolveExtern(identifier, p_thisModule, res);
		for(Module module : p_imports)
			resolveExtern(identifier, module, res);
		resolveExtern(identifier, p_rootModule, res);
		return res;
	}
	
	public void addBuiltin(AuConstant.Descriptor desc) {
		addBuiltin(desc.toString(), mkConst(desc));
	}
	public void addBuiltin(AuOperator.Descriptor desc) {
		addBuiltin(desc.toString(), AuOperator.makeBuiltin(desc));
	}
	public void addBuiltin(String name, AuTerm builtin) {
		Module module = p_rootModule.addSubModule("Builtin");
		
		Descriptor desc = new Descriptor(Descriptor.emptyRecord);
		desc.setString(module.getPath(), new RecordPath("module"));
		desc.setString(name, new RecordPath("name"));
		desc.set(builtin.type(), new RecordPath("type"));
		desc.set(builtin, new RecordPath("defn"));
		desc.setFlag(true, new RecordPath("fEmbedSymbol"));
		module.addSymbol(desc.asTerm());
	}
	
	public Iterator<AuTerm> symbolIterator() {
		return p_symbols.iterator();
	}
	
	public void buildFile(StFile in) {
		for(int i = 0; i < in.numElements(); i++)
			buildRoot(in.getElement(i));
	}
	public void buildRoot(StNode in) {
		if(in instanceof StRoot.Import) {
			StRoot.Import root = (StRoot.Import)in;
			p_imports.add(p_rootModule.addSubModule(root.getModule()));
		}else if(in instanceof StRoot.Module) {
			StRoot.Module root = (StRoot.Module)in;
			
			if(p_thisModule != null)
				throw new RuntimeException("Nested module directive");
			p_thisModule = p_rootModule.addSubModule(root.getModule());
			for(int i = 0; i < root.numMembers(); i++)
				buildRoot(root.getMember(i));
			p_thisModule = null;
		}else if(in instanceof StRoot.Symbol) {
			StRoot.Symbol root = (StRoot.Symbol)in;
			
			if(p_thisModule == null)
				throw new RuntimeException("Expected module directive"
						+ " before symbol declaration");
			
			AuTerm type = null;
			AuTerm defn = null;

			if(root.getDefn() != null
					&& root.getType() != null) {
				defn = buildExpr(root.getDefn());
				type = buildExpr(root.getType());
				if(!defn.type().equals(type))
					throw new RuntimeException("Declared type of"
						+ root.getName() + " does not match implicit type");
			}else if(root.getDefn() != null) {
				defn = buildExpr(root.getDefn());
				type = defn.type();
			}else if(root.getType() != null) {
				type = buildExpr(root.getType());
			}else throw new AssertionError("No type or definition for "
					+ root.getName());
			
			if(defn != null)
				System.out.println(root.getName() + " := " + defn);
			
			Descriptor desc = new Descriptor(Descriptor.emptyRecord);
			desc.setString(p_thisModule.getPath(), new RecordPath("module"));
			desc.setString(root.getName(), new RecordPath("name"));
			desc.set(type, new RecordPath("type"));
			if((root.getFlags() & StRoot.Symbol.kFlagExport) != 0)
				desc.set(defn, new RecordPath("defn"));
			if((root.getFlags() & StRoot.Symbol.kFlagEmbed) != 0)
				desc.setFlag(true, new RecordPath("fEmbedSymbol"));
			p_thisModule.addSymbol(desc.asTerm());

			if((root.getFlags() & StRoot.Symbol.kFlagExport) != 0)
				p_symbols.add(desc.asTerm());
		}else throw new RuntimeException("Illegal root " + in);
	}
		
	public AuTerm buildExpr(StNode in) {
		if(in instanceof StMeta) {
			return mkMeta();
		}else if(in instanceof StLambda) {
			StLambda expr = (StLambda)in;
			
			KorUid arg_uid = new KorUid();
			AuTerm type = buildExpr(expr.getArgument().type());
			Descriptor annotation = new Descriptor(Descriptor.emptyRecord);
			annotation.setFlag(expr.getImplicit(), new RecordPath("fImplicit"));
			
			AuTerm annotate_term = annotation.asTerm();
			if(annotate_term.equals(Descriptor.emptyRecord))
				annotate_term = null;
			
			pushScope(new Scope.BindScope(expr.getArgument().ident(),
					arg_uid, type));
			AuTerm res = mkNamedLambdaExt(annotate_term, arg_uid, type,
					buildExpr(expr.getExpr()));
			popScope();
			return NamedTerm.resolve(res);
		}else if(in instanceof StPi) {
			StPi expr = (StPi)in;
			
			KorUid arg_uid = new KorUid();
			AuTerm type = buildExpr(expr.getArgument().type());
			
			pushScope(new Scope.BindScope(expr.getArgument().ident(),
					arg_uid, type));
			AuTerm res = mkNamedPi(new KorUid(), type,
					buildExpr(expr.getCodomain()));
			popScope();
			return NamedTerm.resolve(res);
		}else if(in instanceof StApply) {
			return buildApply(in, Collections.<AuTerm>emptyList());
		}else if(in instanceof StLiteral.LitInt) {
			StLiteral.LitInt expr = (StLiteral.LitInt)in;
			return mkConst(new IntArithmetic.IntLit(expr.value()));
		}else if(in instanceof StLiteral.LitString) {
			StLiteral.LitString expr = (StLiteral.LitString)in;
			return mkConst(new Strings.StringLit(expr.value()));
		}else if(in instanceof StIdent) {
			StIdent expr = (StIdent)in;
			
			List<AuTerm> res = resolveIdentifier(expr.string());
			if(res.size() == 0)
				throw new RuntimeException("Could not resolve " + in);
			if(res.size() > 1)
				throw new RuntimeException(in + " is ambiguous");
			return res.get(0);
		}else if(in instanceof StAccess) {
			StAccess expr = (StAccess)in;
			
			AuTerm left = buildExpr(expr.getLeft());
			if(left.type().isConstant(Module.moduleType)) {
				Module module = (Module)((AuConstant)left).getDescriptor();
				
				List<AuTerm> res = new ArrayList<AuTerm>();
				resolveExtern(expr.getIdentifier(), module, res);
				if(res.size() == 0)
					throw new RuntimeException("There is no "
							+ expr.getIdentifier() + " in " + module.getName()); 
				if(res.size() > 1)
					throw new RuntimeException(expr.getIdentifier()
							+ " is ambiguous");
				return res.get(0);
			}else throw new AssertionError("Illegal lhs for access");
		}else throw new RuntimeException("Illegal syntax node " + in);
	}
	
	public AuTerm unifyApply(final AuTerm function, AuTerm argument) {
		AuPi ftype = (AuPi)function.type();
		AuTerm argtype = argument.type();
		
		// unify the function argument type and the supplied argument type.
		Unificator.ReplaceObserver unified_func
				= new Unificator.ReplaceObserver(function);		
		Unificator type_unificator = new Unificator(ftype.getBound());
		if(!type_unificator.unify(Unificator.unknownToIndef(argtype),
				unified_func))
			return null;

		return mkApply(unified_func.getTerm(), argument);
	}
	
	public void overload(AuTerm function, List<AuTerm> arguments,
			List<AuTerm> res) {
		if(arguments.size() == 0) {
			if(!TermHelpers.anyTerm(function, new TermHelpers.Predicate() {
					@Override public boolean test(AuTerm in) {
						return Unificator.ImUnknown.instance(in);
					}}))
				res.add(function);
			return;
		}
		
		if(!(function.type() instanceof AuPi))
			return;
		AuPi ftype = (AuPi)function.type();
		
		Descriptor annotation = new Descriptor(ftype.getAnnotation());
		if(annotation.asTerm() != null
				&& annotation.getFlag(new RecordPath("fImplicit"))) {
			Unificator.ImUnknown unknown = new Unificator.ImUnknown();
			AuTerm signature = mkApply(function,
					mkOperator(unknown, ftype.getBound()));
			overload(signature, arguments, res);
			return;
		}
		
		AuTerm natural_res = unifyApply(function, arguments.get(0));
		if(natural_res != null)
			overload(natural_res, arguments.subList(1,  arguments.size()), res);
	}
	
	public AuTerm buildApply(StNode function, List<AuTerm> arguments) {
		if(function instanceof StApply) {
			StApply apply = (StApply)function;
			
			List<AuTerm> stacked_args = new ArrayList<AuTerm>();
			stacked_args.add(buildExpr(apply.getArgument()));
			stacked_args.addAll(arguments);
			return buildApply(apply.getFunction(), stacked_args);
		}else if(function instanceof StIdent) {
			StIdent ident = (StIdent)function;
			
			List<AuTerm> resolved = resolveIdentifier(ident.string());
			if(resolved.size() == 0)
				throw new RuntimeException("Could not resolve " + ident);
			
			List<AuTerm> overloads = new ArrayList<AuTerm>();
			for(AuTerm symbol : resolved)
				overload(symbol, arguments, overloads);
			
			if(overloads.size() == 0)
				throw new RuntimeException("No valid overload for " + ident);
			if(overloads.size() > 1)
				throw new RuntimeException("Overload for " + ident + " is ambiguous");
			return overloads.get(0);
		}else{
			AuTerm res = buildExpr(function);
			for(int i = 0; i < arguments.size(); i++)
				res = mkApply(res, arguments.get(i));
			return res;
		}
	}
}
