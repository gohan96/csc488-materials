package compiler488.ast.stmt;

import compiler488.ast.ASTList;
import compiler488.ast.PrettyPrinter;
import compiler488.ast.Printable;

/**
 * The command to write data on the output device.
 */
public class WriteStmt extends Stmt {
	/** The objects to be printed. */
	private ASTList<Printable> outputs;

	public WriteStmt(ASTList<Printable> outputs) {
		super();
		this.outputs = outputs;
	}

	@Override
	public void prettyPrint(PrettyPrinter p) {
		p.print("write ");
		outputs.prettyPrintCommas(p);
	}

	public ASTList<Printable> getOutputs() {
		return outputs;
	}
}
