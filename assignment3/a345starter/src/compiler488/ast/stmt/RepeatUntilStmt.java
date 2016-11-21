package compiler488.ast.stmt;

import compiler488.ast.ASTList;
import compiler488.ast.PrettyPrinter;
import compiler488.ast.expn.Expn;

/**
 * Represents a loop in which the exit condition is evaluated after each pass.
 */
public class RepeatUntilStmt extends LoopingStmt {
	public RepeatUntilStmt(Expn expn, ASTList<Stmt> body) {
		super(expn, body);
	}

	@Override
	public void prettyPrint(PrettyPrinter p) {
		p.print("repeat ");
		body.prettyPrintBlock(p);
		p.println(" until ");
		expn.prettyPrint(p);
	}
}
