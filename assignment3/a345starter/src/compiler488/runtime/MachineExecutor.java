package compiler488.runtime;

import java.io.InputStream;
import java.io.PrintStream;
import compiler488.compiler.Main;

/**
 * MachineExecutor: an interpreted implementation of the CSC488 pseudo machine.
 *
 * @author Dave Wortman
 */
public final class MachineExecutor implements Machine {
	/** Initial value of program counter. */
	protected short startPC;

	/** First empty slot in the initially empty run time stack. */
	protected short startMSP;

	/** Last memory location available for run time stack. */
	protected short startMLP;

	/** Hardware display registers. */
	protected static int[] display;

	/** Index of top entry in the display (for checking, dumps.) */
	protected int displayMax;

	/** Hardware memory. */
	protected short[] memory;

	/** Hardware program counter for the Machine. */
	protected short pc;

	/**
	 * Hardware memory stack pointer.
	 *
	 * <strong>NB:</strong> The convention is that <code>msp</code> points at
	 * the <em>first> unused entry in the stack.
	 */
	protected short msp;

	/** Hardware memory limit pointer. */
	protected short mlp;

	/** Hardware bottom stack pointer, lowest valid entry in the stack. */
	protected short bottomOfStack;

	/**
	 * Hardware executing flag, true while executing instructions.
	 *
	 * Set to false by runError and HALT instruction
	 */
	protected boolean executing;

	/**
	 * Hardware error flag. Set to true is an error occurred false at start of
	 * execution, set to true only by runError.
	 */
	protected boolean errorOccurred;

	/** Clean, printable input/output characters. */
	protected final static int CHARMASK = 0x7F;

	protected boolean tracingEnabled = false;

	/**
	 * The stream where machine output (i.e. from PRINTC and PRINTI
	 * instructions) is sent.
	 */
	protected PrintStream machineOutput;

	/** The stream where status output regarding execution is sent. */
	protected PrintStream statusOutput;

	/**
	 * The stream where machine input (i.e. from the READI instruction) is drawn
	 * from.
	 */
	protected InputStream machineInput;

	public MachineExecutor(PrintStream statusOutput, PrintStream machineOutput, InputStream machineInput) {
		this.statusOutput = statusOutput;
		this.machineOutput = machineOutput;
		this.machineInput = machineInput;

		display = new int[Machine.DISPLAY_SIZE];
		memory = new short[Machine.MEMORY_SIZE];
		reset();
	}

	public boolean getTracingEnabled() {
		return tracingEnabled;
	}

	public void setTracingEnabled(boolean tracingEnabled) {
		this.tracingEnabled = tracingEnabled;
	}

	public void reset() {
		// Initialize display
		for (int i = 0; i < Machine.DISPLAY_SIZE; i++) {
			display[i] = Machine.MIN_INTEGER;
		}

		// Forces error if unfilled display entry is used
		displayMax = -1; // top of display

		// Initialize memory
		for (int i = 0; i < Machine.MEMORY_SIZE; i++) {
			memory[i] = Machine.UNDEFINED;
		}

		// Just in case...
		memory[0] = Machine.HALT;

		// Initialize start variables to force error if they
		// are not set by code generation
		startPC = -1;
		startMSP = -1;
		startMLP = -1;
	}

	public void writeMemory(short addr, short value) throws MemoryAddressException {
		if (addr < 0 || addr >= Machine.MEMORY_SIZE) {
			// Memory address out of range
			throw new MemoryAddressException("  writeMemory: invalid address: " + addr);
		}

		// Policy: do not check for UNDEFINED
		memory[addr] = value;
	}

	public short readMemory(short addr) throws MemoryAddressException {
		if (addr < 0 || addr >= Machine.MEMORY_SIZE) {
			// memory address out of range
			throw new MemoryAddressException("  readMemory: invalid address: " + addr);
		}

		// Policy: do not check for UNDEFINED
		return memory[addr];
	}

	public void setPC(short addr) {
		startPC = addr;
	}

	public void setMSP(short addr) {
		startMSP = addr;
	}

	public void setMLP(short addr) {
		startMLP = addr;
	}

	protected static String Blanks = new String("                                           ");

	/**
	 * Pad string by appending blanks to specified size. <BR>
	 * Does nothing if toPad is already longer than toSize.
         * Assumes value of toSize is less than length of Blanks
	 *
	 * @param toPad
	 *            string to be padded with blanks
	 * @param toSize
	 *            desired length of blank padded string
	 */
	protected static void padString(StringBuffer toPad, int toSize) {
		if (toPad.length() < toSize && toSize - toPad.length() < Blanks.length() )
			toPad.append(Blanks.substring(0, toSize - toPad.length()));
		return;
	}

	/**
	 * Dump all machine instructions.
	 *
	 * @param dumpSink
	 *            PrintStream sink for the dump
	 * @throws ExecutionException
	 *             from formatInstruction
	 */
	public void dumpInstructions(PrintStream dumpSink) throws ExecutionException {
		StringBuffer printThis = new StringBuffer(32);
		StringBuffer temp = new StringBuffer(64);
		int secondColumn = 40;

		int addr = 0;
		while (addr < bottomOfStack) {
			int startInst = addr;
			// advance addr based on length of instruction
			addr += formatInstruction(addr, printThis);
			int endInst = addr - 1;
			temp = new StringBuffer("");
			temp.append("memory[ " + startInst + " .. " + endInst + " ]" + " = ");
			temp.append(printThis);
			// print another instruction (if any) on the same line
			if (addr < bottomOfStack) {
				padString(temp, secondColumn);
				startInst = addr;
				// advance addr based on length of instruction
				addr += formatInstruction(addr, printThis);
				endInst = addr - 1;
				dumpSink.println(temp + "memory[ " + startInst + " .. " + endInst + " ]" + " = " + printThis);
			} else {
				dumpSink.println(temp);
			}
		}
	}

	/**
	 * Dump contents on memory locations to string buffer for dump
	 *
	 * @param sink
	 *            String buffer to dump to
	 * @param howMany
	 *            number of memory locations to dump
	 */
	public void dumpStack(StringBuffer sink, int howMany) {
		// dump up to howMany locations from top of stack to sink
		// Dump stack memory
		if (0 <= msp && msp < Machine.MEMORY_SIZE) {
			int mspmin = (msp - howMany >= 0 ? msp - howMany : 0);
			int mspmax = (msp < Machine.MEMORY_SIZE ? msp - 1 : Machine.MEMORY_SIZE - 1);

			mspmin = (mspmin < bottomOfStack ? bottomOfStack : mspmin);

			if (mspmax < mspmin) {
				sink.append(""); // nothing to dump
				return;
			}

			sink.append("memory[" + mspmin + " .. " + mspmax + "] = ");

			for (int i = mspmin; i <= mspmax; i++) {
				sink.append(memory[i] + "  ");
			}
		} else {
			sink.append(""); // bad msp, can't dump stack
		}
	}

	/**
	 * dump state of machine to stdout <BR>
	 * Dumps key machine registers, active part of display <BR>
	 * top 8 (or less) entries in the hardware stack
	 *
	 * @param msg
	 *            prepend msg to start of output
	 * @param pc
	 *            value of hardware program counter
	 * @param msp
	 *            value of hardware stack pointer
	 * @param mlp
	 *            value of hardware stack limit register
	 */
	public void dumpMachineState(String msg, int pc, int msp, int mlp) {
		StringBuffer S = new StringBuffer("\t");
		statusOutput.print("\n" + msg);
		// dump control of registers
		statusOutput.print("  pc = " + pc + ", msp = " + msp + ", mlp = " + mlp + "\n");

		// Dump the active display
		if (displayMax >= 0) {
			statusOutput.print("\tdisplay[0 .. " + displayMax + "] = ");

			for (int i = 0; i <= displayMax; i++) {
				statusOutput.print(display[i] + "  ");
			}

			statusOutput.print("\n");
		}

		// Dump stack memory
		dumpStack(S, 8);
		statusOutput.println(S);
	}

	/**
	 * Internal procedure to print error message and stop execution <BR>
	 * dumps machine state
	 *
	 * @param msg
	 *            error message to print
	 * @throws ExecutionException
	 *             propagate error message outward.
	 */
	protected void runError(String msg) throws ExecutionException {
		String msgBuff = "Execution Error -  ";
		msgBuff = msgBuff + msg + "\n";

		dumpMachineState(msgBuff, pc, msp, mlp);

		throw new ExecutionException("  " + msg);
	}

	/**
	 * Formats machine instruction at memory[addr] <BR>
	 * Output includes the arguments of the instruction.
	 *
	 * @param addr
	 *            address of instruction to format
	 * @param printThis
	 *            string buffer sink for formatted instruction
	 * @return number of machine words occupied by the instruction and its
	 *         arguments
	 * @throws ExecutionException
	 *             invalid/missing instruction length
	 */
	protected int formatInstruction(int addr, StringBuffer printThis) throws ExecutionException {
		short opCode = memory[addr];

		if (0 <= opCode && opCode < Machine.INSTRUCTION_NAMES.length) {
			switch (Machine.INSTRUCTION_LENGTHS[opCode]) {
			case 0: // The lengths for BR, BF and HALT are hacks.
			case 1:
				printThis.replace(0, printThis.length(), Machine.INSTRUCTION_NAMES[opCode]);
				return 1;

			case 2:
				printThis.replace(0, printThis.length(), Machine.INSTRUCTION_NAMES[opCode] + " " + memory[addr + 1]);
				return 2;

			case 3:
				printThis.replace(0, printThis.length(),
						Machine.INSTRUCTION_NAMES[opCode] + " " + memory[addr + 1] + " " + memory[addr + 2]);
				return 3;

			default:
				throw new ExecutionException("  formatInstruction: Machine.INSTRUCTION_LENGTHS [" + opCode + "] = "
						+ Machine.INSTRUCTION_LENGTHS[opCode]);
			}
		} else {
			printThis.replace(0, printThis.length(), "not an instruction: " + opCode);
			return 1;
		}
	}

	/**
	 * Function for validating execution This function checks that lowBound
	 * &lt;= value &lt;= highBound runError is called with msg if this is not
	 * true
	 *
	 * @param value
	 *            The value to be checked
	 * @param lowBound
	 *            Lower bound for value
	 * @param highBound
	 *            Upper Bound for value
	 * @param msg
	 *            Error message if bound is violated
	 * @throws ExecutionException
	 *             from runError
	 */
	protected void rangeCheck(int value, int lowBound, int highBound, String msg) throws ExecutionException {
		if (value < lowBound || value > highBound) {
			runError(msg);
		}
	}

	// Functions for manipulating the machine stack.
	// Note: the convention is that msp refers to the first UNUSED entry in
	// the stack.
	// The TOP item on the stack is in memory[msp - 1].

	/**
	 * Increment stack pointer.
	 */
	protected void spush() {
		msp++;
	}

	/**
	 * Decrement stack pointer.
	 */
	protected void spop() {
		msp--;
	}

	/**
	 * Value of top of stack.
	 *
	 * @return value of top of stack
	 */
	protected short top() {
		return memory[msp - 1];
	}

	/**
	 * Value of top of stack + 1.
	 *
	 * @return value of top of stack + 1
	 */
	protected short topp1() {
		return memory[msp];
	}

	/**
	 * Value of top of stack - 1.
	 *
	 * @return value of top of stack - 1
	 */
	protected short topm1() {
		return memory[msp - 2];
	}

	public void run() throws ExecutionException {
		// Source for all READ instructions
		TextReader inputSource = new TextReader(machineInput);

		int intInput; // input for READI
		// counting, iLimit set by ILIMIT instruction
		boolean counting = false; // count instructions to limit execution
		int iCount = 0; // count of instructions executed
		int iLimit = Integer.MAX_VALUE; // instruction execution limit

		// Initialize registers. Validate initial execution state.
		mlp = startMLP;
		rangeCheck(mlp, 0, Machine.MEMORY_SIZE, "Initial value of mlp out of range");

		msp = startMSP; // The first empty slot.
		rangeCheck(msp, 0, mlp - 1, "Initial value of msp out of range");

		// Remember bottom of stack to check for stack underflow
		// Must be set before dumpInstructions is called
		bottomOfStack = startMSP;

		pc = startPC; // Execution starts here.
		rangeCheck(pc, 0, Machine.MEMORY_SIZE - 1, "Initial value of pc outside memory");

		// Dump instruction memory if requested.

		if (Main.dumpCode) {
			dumpInstructions(Main.dumpStream);
		}

		if (Main.supressExecution) {
			statusOutput.println("Execution suppressed by control flag.\n");
			return;
		}

		dumpMachineState("Start Execution", pc, msp, mlp);

		// During the execution of each instruction:
		// opCode contains the instruction code and pc refers to the instruction

		// Only a runError can make it true.
		errorOccurred = false;

		// This instance variable can be set to false by HALT or by a runError.
		executing = true;
		while (executing) {
			// Validate current state of the machine
			// Execute one instruction from memory

			rangeCheck(pc, 0, Machine.MEMORY_SIZE - 1, "Program counter outside memory.\n");

			if (msp < bottomOfStack) {
				runError("Run stack underflow.");
				return;
			}

			if (msp >= mlp) {
				runError("Run stack overflow.");
				return;
			}

			iCount++; // Count instructions executed
			if (counting) {
				if (iCount > iLimit) // count exceeded
					runError("Instruction execution limit (" + iLimit + ") exceeded");
			}

			if (tracingEnabled) {
				StringBuffer printThis = new StringBuffer();
				formatInstruction(pc, printThis);
				printThis.insert(0, pc + ": ");
				padString(printThis, 20);
				dumpStack(printThis, 8);
				statusOutput.println(printThis);
			}

			short n, v, ll, addr;
			int atemp;

			// Fetch and execute the next instruction
			short opCode = memory[pc];

			switch (opCode) {
			// ADDR LL on: push value of display[LL] + ON to stack
			case ADDR:
				ll = memory[pc + 1];
				rangeCheck(ll, 0, Machine.DISPLAY_SIZE - 1, "ADDR: Display index out of range.\n");
				spush();
				memory[msp - 1] = (short) (display[ll] + memory[pc + 2]);
				break;

			// LOAD: push the value of memory[TOP] to the stack
			case LOAD:
				addr = memory[msp - 1];
				rangeCheck(addr, 0, Machine.MEMORY_SIZE - 1, "LOAD address out of range.\n");
				if (memory[addr] == UNDEFINED) {
					runError("Attempt to LOAD undefined value.\n");
				} else {
					memory[msp - 1] = memory[addr];
				}
				break;

			// STORE: store a value on top of the stack in memory
			case STORE:
				v = memory[msp - 1];
				spop();
				addr = memory[msp - 1];
				spop();
				// rangeCheck(addr, 0, Machine.MEMORY_SIZE-1,
				// Disallow stores into code area.
				rangeCheck(addr, bottomOfStack - 1, Machine.MEMORY_SIZE - 1, "STORE address out of range.\n");
				memory[addr] = v;
				break;

			// PUSH V: push V to the stack
			case PUSH:
				spush();
				memory[msp - 1] = memory[pc + 1];
				break;

			// PUSHMT: effectively, push MT to the top of the stack
			case PUSHMT:
				spush();
				memory[msp - 1] = (short) (msp - 1);
				break;

			// SETD LL: set display[LL] to the top of the stack
			case SETD:
				addr = memory[msp - 1];
				spop();
				ll = memory[pc + 1];
				rangeCheck(ll, 0, Machine.DISPLAY_SIZE - 1, "SETD display index out of range.\n");
				if (addr != MIN_INTEGER) { // special case - uninitialized
					rangeCheck(addr, bottomOfStack, mlp, "SETD display entry out of range.\n");
				}
				displayMax = (displayMax > ll ? displayMax : ll);
				display[ll] = addr;
				break;

			// POPN: do n pops, where n is the value on top of the stack
			// Underflow error will be caught before next instruction
			case POPN:
				msp -= memory[msp - 1];
				spop();
				break;

			// POP: pop the top of the machine stack
			case POP:
				spop();
				break;

			/*
			 * DUPN: leave n copies of the next-to-the -top stack item on the
			 * top of the stack, where n is the initial top of the stack value
			 */
			case DUPN:
				n = memory[msp - 1];
				spop();
				v = memory[msp - 1];
				spop();
				rangeCheck(msp + n, bottomOfStack, mlp, "DUPN stack overflow.\n");
				for (int i = msp; i <= msp - 1 + n; i++) {
					memory[i] = v;
				}
				msp += n;
				break;

			// DUP: push the top of the stack
			case DUP:
				spush();
				memory[msp - 1] = topm1();
				break;

			// BR: branch to the address on the top of the stack
			case BR:
				pc = memory[msp - 1]; // BR sets pc directly
				spop();
				break;

			// BF: branch to address atop the stack if the next-to-the-top
			// value is MACHINE_FALSE
			case BF:
				addr = memory[msp - 1];
				spop();
				v = memory[msp - 1];
				// rangeCheck(v , MACHINE_FALSE, MACHINE_TRUE, "BF argument is
				// not a Boolean value");
				spop();
				if (v == MACHINE_FALSE) {
					pc = (short) addr;
				} else {
					// BF sets pc directly
					pc++;
				}
				break;

			// NEG: arithmetic negation of top of stack
			case NEG:
				memory[msp - 1] = (short) (-memory[msp - 1]);
				if (memory[msp - 1] == UNDEFINED) {
					runError("Arithmetic underflow - NEG operator");
					return;
				}
				break;

			/*
			 * ADD, SUB, MUL, DIV, EQ, LT, OR: arithmetic and logical
			 * operations. If the top of the stack is y, and the next item down
			 * is x, then OP (where OP=ADD, SUB, ... , OR) performs x OP y. Some
			 * meager overflow checking is done.
			 */
			case ADD:
				atemp = topm1() + top();
				rangeCheck(atemp, MIN_INTEGER, MAX_INTEGER, "ADD operator overflow or underflow");
				spop();
				memory[msp - 1] = (short) atemp;
				break;

			case SUB:
				atemp = topm1() - top();
				rangeCheck(atemp, MIN_INTEGER, MAX_INTEGER, "SUB operator overflow or underflow");
				spop();
				memory[msp - 1] = (short) atemp;
				break;

			case MUL:
				atemp = topm1() * top();
				rangeCheck(atemp, MIN_INTEGER, MAX_INTEGER, "MUL operator overflow or underflow");
				spop();
				memory[msp - 1] = (short) atemp;
				break;

			case DIV:
				atemp = 0;
				v = memory[msp - 1];
				spop();
				if (v != 0) {
					atemp = memory[msp - 1] / v;
				} else {
					runError("Attempt to divide by zero.\n");
				}
				rangeCheck(atemp, MIN_INTEGER, MAX_INTEGER, "DIV operator overflow or underflow");
				memory[msp - 1] = (short) atemp;
				break;

			case EQ:
				spop();
				memory[msp - 1] = (short) (memory[msp - 1] == topp1() ? MACHINE_TRUE : MACHINE_FALSE);
				break;

			case LT:
				spop();
				memory[msp - 1] = (short) (memory[msp - 1] < topp1() ? MACHINE_TRUE : MACHINE_FALSE);
				break;

			case OR:
				spop();
				rangeCheck(memory[msp - 1], MACHINE_FALSE, MACHINE_TRUE, "OR operand is not a Boolean value");
				rangeCheck(memory[msp], MACHINE_FALSE, MACHINE_TRUE, "OR operand is not a Boolean value");
				memory[msp - 1] = (short) ((memory[msp - 1] == MACHINE_TRUE || memory[msp] == MACHINE_TRUE)
						? MACHINE_TRUE : MACHINE_FALSE);
				break;

			/*
			 * SWAP: swap the top two stack items. Quite useful in implementing
			 * other arithmetic/boolean operations efficiently
			 */
			case SWAP:
				v = topm1();
				memory[msp - 2] = memory[msp - 1];
				memory[msp - 1] = v;
				break;

			/*
			 * READC: machine input operation. One character of input is read,
			 * and pushed to the top of the stack
			 */
			case READC:
				spush();
				memory[msp - 1] = (short) (inputSource.readChar() & CHARMASK);
				break;

			/*
			 * PRINTC: print the top of the stack as a character, and pop the
			 * stack. Used for implementing output functions.
			 */
			case PRINTC:
				machineOutput.print((char) (memory[msp - 1] & CHARMASK));
				spop();
				break;

			/**
			 * READI: read an integer up to the next non-integer, and push this
			 * integer to the top of the stack. See the machine description
			 * handout for more details.
			 */
			case READI:
				intInput = inputSource.readInt();
				rangeCheck(intInput, MIN_INTEGER, MAX_INTEGER, "READI: Integer input out of range");
				spush();
				memory[msp - 1] = (short) intInput;
				break;

			/**
			 * PRINTI: print the top of the stack as an integer, and pop the
			 * stack
			 */
			case PRINTI:
				machineOutput.print(memory[msp - 1]);
				spop();
				break;

			// HALT: halt execution
			case HALT:
				executing = false;
				break;

			// TRON: start tracing machine execution
			case TRON:
				dumpMachineState("Start trace (TRON).\n", pc, msp, mlp);
				break;

			// TROFF: stop tracing machine execution
			case TROFF:
				tracingEnabled = false;
				dumpMachineState("End trace (TROFF).\n", pc, msp, mlp);
				break;

			// ILIMIT V: Set instruction count limit to V
			case ILIMIT:
				iLimit = memory[pc + 1];
				if (iLimit > 0) {
					counting = true;
					iCount = 0;
				} else {
					counting = false;
					iLimit = Integer.MAX_VALUE; // set to safe value
				}
				break;

			default:
				runError("Illegal instruction code.\n");
				break;
			}
			// end of switch on instruction code

			// update program counter to next instruction
			pc += Machine.INSTRUCTION_LENGTHS[opCode];
		}

		// End interpreter main loop

		// Clean up after execution
		dumpMachineState("End Execution.\n", pc, msp, mlp);
	}
}
