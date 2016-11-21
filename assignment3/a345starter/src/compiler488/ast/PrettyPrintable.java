package compiler488.ast;

/**
 * Anything that can pretty-print itself using a <code>PrettyPrinter</code>.
 */
public interface PrettyPrintable {
	/**
	 * Pretty-print this object using the provided printer.
	 *
	 * @param p
	 *            the printer to print with
	 */
	public void prettyPrint(PrettyPrinter p);
}
