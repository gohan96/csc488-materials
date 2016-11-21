package compiler488.runtime;

/**
 * Exception subclass for reporting machine address errors
 *
 * @author Danny House
 */
public class MemoryAddressException extends Exception {
	private static final long serialVersionUID = 1L;

	public MemoryAddressException(String msg) {
		super(msg);
	}
}
