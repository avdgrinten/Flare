package org.managarm.korona.lang;

import static org.managarm.aurora.lang.AuTerm.mkApply;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.util.NamedTerm.mkNamedLambdaExt;
import static org.managarm.aurora.util.NamedTerm.mkNamedLambda;
import static org.managarm.aurora.util.NamedTerm.mkNamedPi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.managarm.aurora.builtin.DoubleArithmetic;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Locals;
import org.managarm.aurora.builtin.Mutation;
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
import org.managarm.korona.syntax.StLetExpr;
import org.managarm.korona.syntax.StLiteral;
import org.managarm.korona.syntax.StMeta;
import org.managarm.korona.syntax.StNode;
import org.managarm.korona.syntax.StPi;
import org.managarm.korona.syntax.StRoot;

public class Resolver {
	private static AuConstant.Descriptor errorType
		= new AuConstant.Descriptor(mkMeta()) {
		@Override public String toString() {
			return "error";
		}
	};
	private static AuOperator.Descriptor exprError
		= new AuOperator.Descriptor(
			mkPi(mkMeta(),
			 mkVar(0, mkMeta())), 1) {
		@Override public String toString() {
			return "exprError";
		}
		
		@Override protected boolean reducible(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			throw new AssertionError("reduce() called");
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
	};
	
	// stores all symbols that are generated as part of this source file
	private List<AuTerm> p_symbols = new ArrayList<AuTerm>();
	
	private Module p_rootModule = new Module(null, null); 
	private Module p_thisModule;	
	private List<Module> p_imports = new ArrayList<Module>();
	
	// stores the active scopes. used to resolve local identifiers
	private List<Scope> p_scopeStack = new ArrayList<Scope>();
	
	// stores error messages and warnings
	private List<ResolveMsg> p_messages = new ArrayList<ResolveMsg>();
	
	public Resolver() {
		
	}
	
	public boolean okay() {
		boolean okay_flag = true;
		for(ResolveMsg msg : p_messages)
			if(msg.getLevel() == ResolveMsg.Level.kError)
				okay_flag = false;
		return okay_flag;
	}
	public int numMessages() {
		return p_messages.size();
	}
	public ResolveMsg getMessage(int i) {
		return p_messages.get(i);
	}
	
	private void pushScope(Scope scope) {
		p_scopeStack.add(scope);
	}
	private void popScope() {
		p_scopeStack.remove(p_scopeStack.size() - 1);
	}
	
	private void resolveScope(String identifier, List<AuTerm> res) {
		for(int i = p_scopeStack.size() - 1; i >= 0; i--) {
			Scope item = p_scopeStack.get(i);
			if(item instanceof Scope.LetScope) {
				Scope.LetScope scope = (Scope.LetScope)item;
				if(!scope.getName().equals(identifier))
					continue;
				res.add(scope.getDefn());
			}else if(item instanceof Scope.BindScope) {
				Scope.BindScope scope = (Scope.BindScope)item;
				if(!scope.getName().equals(identifier))
					continue;
				res.add(mkOperator(scope.getDescriptor(), scope.getType()));
			}else throw new AssertionError("Illegal scope: " + item);
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
		resolveScope(identifier, res);
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
	public void addSymbol(AuTerm symbol) {
		Descriptor desc = new Descriptor(symbol);
		
		String symb_module = desc.getString(new RecordPath("module"));
		
		Module module = p_rootModule.addSubModule(symb_module);
		module.addSymbol(symbol);
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
			
			if(!p_rootModule.hasSubModule(root.getModule()))
				p_messages.add(new ResolveMsg.NoSuchImport(root.getModule()));
			
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
				if(defn.isOperator(exprError))
					return;
				type = defn.type();
			}else if(root.getType() != null) {
				type = buildExpr(root.getType());
				if(type.isOperator(exprError))
					return;
			}else throw new AssertionError("No type or definition for "
					+ root.getName());
			
			if(defn != null)
				System.out.println(root.getName() + " := " + defn);

			if(p_thisModule.hasSymbol(root.getName(), type)) {
				p_messages.add(new ResolveMsg.DuplicateSymbol(
						p_thisModule.getPath(), root.getName(), type));
				return;
			}
			
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
		}else if(in instanceof StLetExpr) {
			StLetExpr expr = (StLetExpr)in;
			
			AuTerm defn = buildExpr(expr.getDefn());
			if(defn.isOperator(exprError))
				return mkOperator(exprError, mkConst(errorType));
			
			pushScope(new Scope.LetScope(expr.getName(), defn));
			AuTerm res = buildExpr(expr.getExpr());
			popScope();
			return res;
		}else if(in instanceof StLambda) {
			StLambda expr = (StLambda)in;
			
			NamedTerm.Name descriptor = new NamedTerm.Name();
			AuTerm type = buildExpr(expr.getArgument().type());
			Descriptor annotation = new Descriptor(Descriptor.emptyRecord);
			annotation.setFlag(expr.getImplicit(), new RecordPath("fImplicit"));
			
			AuTerm annotate_term = annotation.asTerm();
			if(annotate_term.equals(Descriptor.emptyRecord))
				annotate_term = null;
			
			pushScope(new Scope.BindScope(expr.getArgument().ident(),
					type, descriptor));
			AuTerm res = mkNamedLambdaExt(annotate_term, descriptor, type,
					buildExpr(expr.getExpr()));
			popScope();
			return res;
		}else if(in instanceof StPi) {
			StPi expr = (StPi)in;

			NamedTerm.Name descriptor = new NamedTerm.Name();
			AuTerm type = buildExpr(expr.getArgument().type());
			if(type.isOperator(exprError))
				return mkOperator(exprError, mkConst(errorType));
			
			pushScope(new Scope.BindScope(expr.getArgument().ident(),
					type, descriptor));
			AuTerm res = mkNamedPi(descriptor, type,
					buildExpr(expr.getCodomain()));
			popScope();
			return res;
		}else if(in instanceof StApply) {
			return buildApply(in, Collections.<AuTerm>emptyList());
		}else if(in instanceof StLiteral.LitDecimal) {
			StLiteral.LitDecimal expr = (StLiteral.LitDecimal)in;
			return mkConst(new DoubleArithmetic.DoubleLit(expr.value()));
		}else if(in instanceof StLiteral.LitInt) {
			StLiteral.LitInt expr = (StLiteral.LitInt)in;
			return mkConst(new IntArithmetic.IntLit(expr.value()));
		}else if(in instanceof StLiteral.LitString) {
			StLiteral.LitString expr = (StLiteral.LitString)in;
			return mkConst(new Strings.StringLit(expr.value()));
		}else if(in instanceof StIdent) {
			StIdent expr = (StIdent)in;
			
			List<AuTerm> res = resolveIdentifier(expr.string());
			if(res.size() == 0) {
				p_messages.add(new ResolveMsg.UnresolvedIdentifier(expr));
				return mkOperator(exprError, mkConst(errorType));
			}
			if(res.size() > 1) 
				p_messages.add(new ResolveMsg.AmbiguousIdentifier(expr,
						res.get(0)));
			return res.get(0);
		}else if(in instanceof StAccess) {
			StAccess expr = (StAccess)in;
			
			AuTerm left = buildExpr(expr.getLeft());
			
			// break early if there are already errors
			if(left.isOperator(exprError))
				return mkOperator(exprError, mkConst(errorType));
			
			if(left.type().isConstant(Module.moduleType)) {
				Module module = (Module)((AuConstant)left).getDescriptor();
				
				List<AuTerm> res = new ArrayList<AuTerm>();
				resolveExtern(expr.getIdentifier(), module, res);
				if(res.size() == 0) {
					p_messages.add(new ResolveMsg.IllegalAccess(left, expr.getIdentifier()));
					return mkOperator(exprError, mkConst(errorType));
				}
				if(res.size() > 1) //FIXME: remove StIdent construction
					p_messages.add(new ResolveMsg.AmbiguousIdentifier(
							new StIdent(expr.getIdentifier()), res.get(0)));
				return res.get(0);
			}else{
				p_messages.add(new ResolveMsg.IllegalAccess(left, expr.getIdentifier()));
				return mkOperator(exprError, mkConst(errorType));
			}
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
	
	public AuTerm mutateExtractType(AuTerm type) {
		AuOperator operator = (AuOperator)type;
		return operator.getArgument(0);
	}
	public AuTerm mutateRaise(AuTerm term, AuTerm argument,
			NamedTerm.Name variable) {
		AuTerm embedded = term;
		if(!term.type().isOperator(Mutation.mutatorType))
			embedded = mkOperator(Mutation.embed, term.type(), term);
		
		AuTerm arg_type = argument.type();
		AuTerm input_type = mutateExtractType(arg_type);
		AuTerm res_type = mutateExtractType(embedded.type());
		
		return mkOperator(Mutation.seq,
				input_type, res_type, argument,
				mkNamedLambda(variable, input_type, embedded));
	}
	
	public void overloadLift(Overload overload,
			AuTerm function, AuTerm argument,
			List<AuTerm> rem_arguments, List<AuTerm> res) {
		AuTerm arg_type = argument.type();
		AuTerm input_type = mutateExtractType(arg_type);
		NamedTerm.Name variable = new NamedTerm.Name();
		
		AuTerm lift_res = unifyApply(function,
				mkOperator(variable, input_type));
		if(lift_res == null)
			return;
		
		List<Lift> lifts = new ArrayList<Lift>();
		for(int i = 0; i < overload.numLifts(); i++)
			lifts.add(overload.getLift(i));
		lifts.add(new Lift(argument, input_type, variable));

		Overload applied = new Overload(lift_res, lifts);
		overloadRecursive(applied, rem_arguments, res);
	}
	
	public void overloadRecursive(Overload overload, List<AuTerm> arguments,
			List<AuTerm> res) {
		AuTerm term = overload.getTerm();
		
		if(arguments.size() == 0) {
			if(TermHelpers.anyTerm(term, new TermHelpers.Predicate() {
					@Override public boolean test(AuTerm in) {
						return Unificator.ImUnknown.instance(in);
					}}))
				return;
			
			// apply lift operations to the function before we
			// accept it as possible overload
			AuTerm lifted = term;
			for(int i = 0; i < overload.numLifts(); i++) {
				Lift lift = overload.getLift(i);
				lifted = mutateRaise(lifted, lift.getArgument(),
						lift.getVariable());
			}
			res.add(lifted);
			return;
		}
		
		if(!(term.type() instanceof AuPi))
			return;
		AuPi ftype = (AuPi)term.type();
		AuTerm argument = arguments.get(0);
		AuTerm argtype = argument.type();
		
		Descriptor annotation = new Descriptor(ftype.getAnnotation());
		if(annotation.asTerm() != null
				&& annotation.getFlag(new RecordPath("fImplicit"))) {
			Unificator.ImUnknown unknown = new Unificator.ImUnknown();

			List<Lift> lifts = new ArrayList<Lift>();
			for(int i = 0; i < overload.numLifts(); i++)
				lifts.add(overload.getLift(i));
			
			Overload applied = new Overload(mkApply(term,
					mkOperator(unknown, ftype.getBound())), lifts);
			overloadRecursive(applied, arguments, res);
			return;
		}
		
		if(argtype.isOperator(Mutation.mutatorType)
				&& !ftype.getBound().isOperator(Mutation.mutatorType)) {
			overloadLift(overload, term, argument,
					arguments.subList(1, arguments.size()), res);
		}else if(argtype.isOperator(Locals.localType)
				&& !ftype.getBound().isOperator(Locals.localType)) {
			AuOperator local_type = (AuOperator)argtype;
			AuTerm read = mkOperator(Locals.localRead,
					local_type.getArgument(0), argument);
			overloadLift(overload, term, read,
					arguments.subList(1, arguments.size()), res);
		}else{
			AuTerm natural_res = unifyApply(term, argument);
			if(natural_res != null) {
				List<Lift> lifts = new ArrayList<Lift>();
				for(int i = 0; i < overload.numLifts(); i++)
					lifts.add(overload.getLift(i));
				
				Overload applied = new Overload(natural_res, lifts);
				overloadRecursive(applied, arguments.subList(1,  arguments.size()), res);
			}
		}
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
			
			// break early if there are already errors
			for(AuTerm argument : arguments)
				if(argument.isOperator(exprError))
					return mkOperator(exprError, mkConst(errorType));
			
			List<AuTerm> resolved = resolveIdentifier(ident.string());
			if(resolved.size() == 0) {
				p_messages.add(new ResolveMsg.UnresolvedIdentifier(ident));
				return mkOperator(exprError, mkConst(errorType));
			}
			
			List<AuTerm> overloads = new ArrayList<AuTerm>();
			for(AuTerm symbol : resolved)
				overloadRecursive(new Overload(symbol, Collections.<Lift>emptyList()),
						arguments, overloads);
			
			if(overloads.size() == 0) {
				AuTerm[] arg_types = new AuTerm[arguments.size()];
				for(int i = 0; i < arguments.size(); i++)
					arg_types[i] = arguments.get(i).type();
				p_messages.add(new ResolveMsg.NoOverload(ident, arg_types));
				return mkOperator(exprError, mkConst(errorType));
			}
			if(overloads.size() > 1)
				p_messages.add(new ResolveMsg.AmbiguousOverload(ident,
						overloads.get(0)));
			return overloads.get(0);
		}else{
			// break early if there are already errors
			for(AuTerm argument : arguments)
				if(argument.isOperator(exprError))
					return mkOperator(exprError, mkConst(errorType));
			
			AuTerm res = buildExpr(function);
			for(int i = 0; i < arguments.size(); i++) {
				if(res.isOperator(exprError))
					return mkOperator(exprError, mkConst(errorType));
					
				AuPi fun_type = (AuPi)res.type();
				AuTerm bound_type = fun_type.getBound();
				AuTerm arg_type = arguments.get(i).type();
				
				if(!bound_type.equals(arg_type)) {
					p_messages.add(new ResolveMsg.TypeMismatch(arg_type, bound_type));
					return mkOperator(exprError, mkConst(errorType));
				}
				
				res = mkApply(res, arguments.get(i));
			}
			return res;
		}
	}
}
