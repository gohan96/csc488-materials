package compiler488.ast.decl;

import compiler488.ast.BaseAST;
import compiler488.ast.type.Type;

/**
 * The common features of declarations.
 */
public abstract class Declaration extends BaseAST {
	/** The name of the thing being declared. */
	protected String name;

	/** The type of thing being declared. */
	protected Type type = null;

	public Declaration(String name, Type type) {
		super();

		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}
}
