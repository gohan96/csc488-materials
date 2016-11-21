package compiler488.parser;

import java_cup.runtime.Symbol;

public class Source488Parser extends BaseParser {
	public String lastError;

	public Source488Parser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * Override the report_error method so it will display the line and column
	 * of where the error occurred in the input as well as the reason for the
	 * error which is passed into the method in the String 'message'.
	 *
	 * @param message
	 *            error message to print
	 * @param info
	 *            symbol containing line/column numbers
	 */
	public void report_error(String message, Object info) {
		String st = "Error";

		if (info instanceof Symbol) {
			Symbol s = (Symbol) info;

			/*
			 * s.left is supposed to hold the line number of the error. s.right
			 * is supposed to hold the column number. If either is < 0 the
			 * parser may have run off the end of the program and a Syntax Error
			 * message without line number may be printed.
			 */

			// Check line number.
			if (s.left >= 0) {
				st += " in line " + (s.left + 1);

				// Check column number.
				if (s.right >= 0) {
					st += ", column " + (s.right + 1);
				}
			} else {
				st += " at end of input ";
			}
		}

		st += ": " + message;
		System.err.println(st);
		lastError = st;
	}

	/**
	 * Override the report_fatal_error method to use the report_error method.
	 */
	/**
	 * @throws SyntaxErrorException
	 */
	public void report_fatal_error(String message, Object info) throws SyntaxErrorException {
		report_error(message, info);
		throw new SyntaxErrorException(message);
	}
}
