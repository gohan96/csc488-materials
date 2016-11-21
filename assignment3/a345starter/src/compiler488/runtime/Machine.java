package compiler488.runtime;

/**
 * Machine: a pseudo machine definition for the CSC488 course project.
 *
 * Include constant definitions and a control interface.
 */
public interface Machine {
	/** Size of machine memory */
	public static final short MEMORY_SIZE = 16384;

	/** Size of display (maximum depth of static nesting) */
	public static final short DISPLAY_SIZE = 16;

	/** Hardware value for <strong>true</b> */
	public static final short MACHINE_TRUE = 1;

	/** Hardware value for <strong>false</b> */
	public static final short MACHINE_FALSE = 0;

	/** Hardware limit for largest integer value */
	public static final short MAX_INTEGER = Short.MAX_VALUE;

	/** Hardware limit for smallest integer value */
	public static final short MIN_INTEGER = Short.MIN_VALUE + 1;

	/** Hardware value used to represent undefined */
	public static final short UNDEFINED = Short.MIN_VALUE;

	// The instructions recognized by the Machine.
	public static final short HALT = 0;
	public static final short ADDR = 1;
	public static final short LOAD = 2;
	public static final short STORE = 3;
	public static final short PUSH = 4;
	public static final short PUSHMT = 5;
	public static final short SETD = 6;
	public static final short POP = 7;
	public static final short POPN = 8;
	public static final short DUP = 9;
	public static final short DUPN = 10;
	public static final short BR = 11;
	public static final short BF = 12;
	public static final short NEG = 13;
	public static final short ADD = 14;
	public static final short SUB = 15;
	public static final short MUL = 16;
	public static final short DIV = 17;
	public static final short EQ = 18;
	public static final short LT = 19;
	public static final short OR = 20;
	public static final short SWAP = 21;
	public static final short READC = 22;
	public static final short PRINTC = 23;
	public static final short READI = 24;
	public static final short PRINTI = 25;
	public static final short TRON = 26;
	public static final short TROFF = 27;
	public static final short ILIMIT = 28;

	/**
	 * Table of instruction names.
	 *
	 * NB: This array <em>must</em> stay in alignment with the instruction
	 * values.
	 */
	public static final String[] INSTRUCTION_NAMES = {
			"HALT", "ADDR", "LOAD", "STORE", "PUSH", "PUSHMT", "SETD", "POP",
			"POPN", "DUP", "DUPN", "BR", "BF", "NEG", "ADD", "SUB", "MUL",
			"DIV", "EQ", "LT", "OR", "SWAP", "READC", "PRINTC", "READI",
			"PRINTI", "TRON", "TROFF", "ILIMIT"
	};

	/**
	 * Table of lengths for each instruction. <BR>
	 * NOTE: length of branch instructions is set to ZERO since they directly
	 * change the pc NOTE: length of HALT instruction is ZERO since once we
	 * reach halt, updating the pc is meaningless
	 */
	public static final short[] INSTRUCTION_LENGTHS = {
			0, 3, 1, 1, 2, 1, 2, 1, 1, 1, 1, // HALT .. DUPN
			0, 0, // BR .. BF
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 // READC .. ILIMIT
	};

	/**
	 * Provide direct write access to machine memory to simplify code
	 * generation.
	 *
	 * <strong>Policy:</strong> Does not check value == <code>UNDEFINED</code>.
	 *
	 * @param addr
	 *            memory address to write to
	 * @param value
	 *            value to write to memory
	 * @throws MemoryAddressException
	 *             incorrect memory address
	 */
	void writeMemory(short addr, short value) throws MemoryAddressException;

	/**
	 * Provide direct read access to machine memory to simplify code generation.
	 *
	 * <strong>Policy:</strong> Does not check value == <code>UNDEFINED</code>.
	 *
	 * @param addr
	 *            memory address to read from
	 * @return memory value
	 * @throws MemoryAddressException
	 *             incorrect memory address
	 */
	short readMemory(short addr) throws MemoryAddressException;

	/**
	 * Set the initial program counter value for machine execution.
	 *
	 * <strong>Policy:</strong> Do not check <code>addr</code> for validity,
	 * error will be caught at start of execution.
	 *
	 * @param addr
	 *            memory address for program counter value
	 */
	void setPC(short addr);

	/**
	 * Set initial value of runtime stack base for program execution.
	 *
	 * <strong>Policy:</strong> Do not check <code>addr</code> for validity,
	 * error will be caught at start of execution.
	 *
	 * @param addr
	 *            memory address for start of runtime stack
	 */
	void setMSP(short addr);

	/**
	 * Set initial value of runtime stack top for program execution.
	 *
	 * <strong>Policy:</strong> Do not check <code>addr</code> for validity,
	 * error will be caught at start of execution.
	 *
	 * @param addr
	 *            memory address for top of run time stack
	 */
	void setMLP(short addr);

	/**
	 * Reset the machine back into a default, empty state.
	 */
	void reset();

	/**
	 * Execute the object code instructions stored in the machines' memory.
	 *
	 * @throws ExecutionException
	 *             error during instruction execution
	 */
	void run() throws ExecutionException;
}
