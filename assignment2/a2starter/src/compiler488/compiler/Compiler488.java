package compiler488.compiler;

/**
 * This Compiler488 is an extremely simple scanner/parser driver
 * for Assignment 2
 */

/* File Name: Compiler488.java
 * To Create:
 * After the scanner and the parser, have been created.
 * > javac Compiler488.java
 *
 * To Run:
 * > java   ???Compiler488   program
 * where program is an input file for the compiler
 * This simple minded driver does not read from standard input
 */

import java.io.*;
import compiler488.parser.*;

public class Compiler488 {
    static public void main(String argv[]) {
        /* Start the parser */
        try {
            System.out.println("Start parsing");
            testParser(new File(argv[0]));
            System.out.println("End parsing");
        } catch (Exception e) {
            /* Print exception type and message */
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.out.println("Exception during Parsing");
        }
    }

    static public void testParser (File file) throws Exception {
        Source488Parser p = new Source488Parser(new Lexer(new FileReader(file)));
        Object result = p.parse().value;
    }
}
