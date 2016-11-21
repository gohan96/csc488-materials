package compiler488.runtime;

/**
 * Exception subclass for reporting machine run time errors
 *
 * @author Danny House
 */
public class ExecutionException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExecutionException(String msg) {
		super(msg);
	}
}
