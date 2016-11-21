package compiler488.experimental;

import compiler488.runtime.ExecutionException;
import compiler488.runtime.Machine;
import compiler488.runtime.MachineExecutor;
import compiler488.runtime.MemoryAddressException;

/**
 * Machine Playground: experiment with the CSC488 pseudo machine by manually
 * writing assembly programs
 *
 * Put this file under `src/compiler488/experimental` in your codebase.
 * Compile and run this program from within your IDE, or from shell with:
 *
 *      $ ant compile
 *      $ java -cp bin/ compiler488.experimental.MachinePlayground
 *
 * @author Peter McCormick
 */
public class MachinePlayground {
    public static void main(String[] args) { new MachinePlayground().runMain(args); }

    protected MachineExecutor machine;

    /** The virtual machine memory address where the `emit*` helper methods will write the next word to */
    protected short currentAddr;

    /** Whether virtual machine tracing will be enabled or not */
    protected boolean enableTracing = false;

    public void runMain(String[] args) {
        // Setup a new virtual machine
        machine = new MachineExecutor(System.err, System.out, System.in);

        // Reset the machine into a known state
        reset();

        if (enableTracing) {
            machine.setTracingEnabled(true);
            emitWord(Machine.TRON);
        }

        programPrint42();
        //programHelloWorld1();
        //programHelloWorld2();
        //programArithmetic();
        //programCounter();

        /*
         * EXERCISES (write these out in assembly by hand):
         *
         * 1. Modify `programCounter` to evaluate the loop condition check at
         *      the top (as in a genuine `while` loop)
         *
         * 2. Write assembly that evaluates the 488 Source Language expression:
         *
         *          ( 1 >= 2 + 3  ?  4 > 5  :  6 not= 7 )
         *
         * 3. Translate the following (don't forget that the Boolean operators
         *      are short-circuiting, so the following will require branching):
         *
         *          true or (1 < 2)
         *
         * 4. Translate the following and use the stack to reserve memory for
         *      the local variables (you will need to use the ADDR, LOAD, STORE and
         *      PUSHMT instructions, and think carefully about your stack layout):
         *
         *      {
         *          var x, y : integer
         *          var b : boolean
         *
         *          x := 10
         *          y := x * 2
         *          b := x < y
         *          x := y
         *          y := 0
         *          b := true
         *      }
         *
         * 5. Translate the following:
         *
         *      {
         *          function add(x : integer, y : integer) : integer {
         *              return with x + y
         *          }
         *
         *          write add(1, 2), newline
         *          write add(3, add(4, 5)), newline
         *          write add(add(6, 7), 8), newlinse
         *          write add(add(9, 10), add(11, 12)), newline
         *      }
         */

        // Emit a safety HALT to terminate the machine, and then run what we
        // have emitted so far
        emitWord(Machine.HALT);
        run();
    }

    public void programPrint42() {
        /*
         * To get comfortable with writing assembly programs for a stack-based
         * machine, it is a useful exercise to carefully think through how your
         * instructions will affect the contents of the stack.
         */

        emitWord(Machine.PUSH);
        emitWord(42);
        // Stack: 42

        emitWord(Machine.PRINTI);
        // Stack: (empty)

        emitWord(Machine.PUSH);
        emitWord('\n');
        // Stack: '\n' (10)

        emitWord(Machine.PRINTC);
        // Stack: (empty)
    }

    public void programHelloWorld1() {
        emitWord(Machine.PUSH);
        emitWord('H');
        // Stack: 'H'

        emitWord(Machine.PRINTC);
        // Stack: (empty)

        emitWord(Machine.PUSH);
        emitWord('i');
        // Stack: 'i'

        emitWord(Machine.PRINTC);
        // Stack: (empty)

        emitWord(Machine.PUSH);
        emitWord('!');
        // Stack: '!'

        emitWord(Machine.PRINTC);
        // Stack: (empty)

        emitWord(Machine.PUSH);
        emitWord('\n');
        // Stack: '\n' (10)

        emitWord(Machine.PRINTC);
        // Stack: (empty)
    }

    public void programHelloWorld2() {
        String str = "Hello World!\n";

        // A Java loop generates a long sequence of instructions
        for (int i = 0; i < str.length(); i++) {
            emitWord(Machine.PUSH);
            int c = str.charAt(i);
            emitWord(c);
            emitWord(Machine.PRINTC);
        }
    }

    public void programArithmetic() {
        emitPush(42);
        // Stack: 42

        emitPush(4);
        // Stack: 42 4

        emitWord(Machine.DUPN);
        // Stack: 42 42 42 42

        emitWord(Machine.ADD);
        // Stack: 42 42 84

        // y=84, x=42, n = x - y == -42
        emitWord(Machine.SUB);
        // Stack: 42 -42

        // y=-42, x=42, n = x < y == 42 < -42 == 0
        emitWord(Machine.LT);
        // Stack: 0
    }

    public void programCounter() {
        emitPush(5);
        // Stack: 5

        /*
         * `startAddr` will remember the address of the DUP instruction that
         * is the start of the loop
         */
        int startAddr = currentAddr;

        /** START OF LOOP */

        emitWord(Machine.DUP);
        // Stack: 5 5

        emitWord(Machine.PRINTI);
        // Stack: 5

        emitPush('\n');
        // Stack: 5 '\n' (10)

        emitWord(Machine.PRINTC);
        // Stack: 5

        emitPush(1);
        // Stack: 5 1

        emitWord(Machine.SUB);
        // Stack: 4

        emitWord(Machine.DUP);
        // Stack: 4 4

        emitPush(0);
        // Stack: 4 4 0

        emitWord(Machine.EQ);
        // Stack: 4 0

        emitPush(startAddr);
        // Stack: 4 0 <startAddr>

        emitWord(Machine.BF);
        // Stack: 4
        // PC == startAddr

        /** END OF LOOP */

        int endAddr = currentAddr;

        emitWord(Machine.POP);
    }

    public void reset() {
        currentAddr = 0;
        machine.reset();

        /*
         * When the virtual machine starts, it will begin by running the
         * instruction at memory address 0.
         */
        machine.setPC((short) 0);

        /*
         * 1000 is not necessarily greater than the address of the last
         * instruction. These values are arbitrary and will require more
         * principled thinking to determine.
         */
        machine.setMSP((short) 1000);
        machine.setMLP((short) 2000);
    }

    public void run() {
        try {
            machine.run();
        } catch (ExecutionException e) {
            System.err.println("Exception during machine run");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void emitWord(int value) {
        // Helper method to save on extraneous casts
        emitWord((short) value);
    }

    public void emitWord(short value) {
        try {
            machine.writeMemory(currentAddr, value);
        } catch (MemoryAddressException e) {
            System.err.println("Illegal address " + currentAddr);
            e.printStackTrace();
            System.exit(1);
        }

        currentAddr++;
    }

    /**
     * Emit a PUSH opcode followed by the required constant parameter.
     *
     * NB: Simple helper methods like this go a long way to ensuring you never
     * forget to emit the required number of words for instructions that are 2
     * or 3 words in length. Plus they make your code generation methods more
     * concise and easier to follow.
     *
     * @param c constant to push
     * @return the memory address of the constant
     */
    public int emitPush(int c) {
        emitWord(Machine.PUSH);

        int constantAddr = currentAddr;
        emitWord(c);

        /*
         * QUESTION: During code generation, can you imagine a scenario whereby
         * you need to emit some code at a point before you actually know the
         * precise value of the constant you will eventually need? Consider how
         * you would implement code generation for a forward jump, and when that
         * might be useful...
         */
        return constantAddr;
    }

    public int emitPushUndefined() {
        return emitPush(Machine.UNDEFINED);
    }
}
