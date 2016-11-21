package compiler488.ast.stmt;

import compiler488.ast.ASTList;
import compiler488.ast.PrettyPrinter;
import compiler488.ast.Readable;

/**
 * The command to read data into one or more variables.
 */
public class ReadStmt extends Stmt {
	/** A list of locations to put the values read. */
	private ASTList<Readable> inputs;

	public ReadStmt(ASTList<Readable> inputs) {
		super();
		this.inputs = inputs;
	}

	@Override
	public void prettyPrint(PrettyPrinter p) {
		p.print("read ");
		inputs.prettyPrintCommas(p);
	}

	public ASTList<Readable> getInputs() {
		return inputs;
	}
}
