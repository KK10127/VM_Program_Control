import sun.tools.java.SyntaxError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;


/**
 * Generates assembly code from the parsed VM command.
 *
 * @author Jay Montoya
 * @version 1.0
 */
public class CodeWriter {

    /** connection to the output file where our hack assembly code will be written **/
    private PrintWriter outputFile;
    private ArithmeticHashMap arithmeticMapper;
    private IdentifierMapper identifierMapper;
    private String outputFileName;
    private int labelNum;
    private int valuableLinesWritten;
    private int currentNumParams;
    private String fileName;


    /**
     * Class extension of a HashMap
     * Necessary for implementation of unique labels when generating an asm file.
     */
    private class ArithmeticHashMap extends HashMap<String, String> {
       // constructor which initializes label tracking variable.
        public ArithmeticHashMap() {
            labelNum = 1;

        }

        /**
         * Overriden get method. When the the key is gt, lt, or eq...
         * then labels are being used, the hashmap must update the values for these
         * keys with new labels that are unique.
         * @param key the arithmetic command
         * @return The assembly language translation of the supplied vm command.
         */
        @Override
        public String get(Object key) {

            // save the old value
            String oldValue = super.get(key);

            // if the key is gt, lt, or eq
            // update the assembly code with the new unique labels.
            if( ((String) key).equals("gt") ||
                    ((String) key).equals("lt") ||
                    ((String) key).equals("eq")) {
                labelNum++;


                // update the new values
                this.put("gt","@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "A = A - 1\n" +
                        "D = M - D\n" +
                        "@TRUE_" + labelNum + "\n" +
                        "D;JGT\n" +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = 0\n" +
                        "@CONTINUE_" + labelNum + "\n" +
                        "0;JMP\n" +
                        getTrueLabel() +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = -1\n" +
                        getContinueLabel());
                this.put("lt", "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "A = A - 1\n" +
                        "D = M - D\n" +
                        "@TRUE_" + labelNum + "\n" +
                        "D;JLT\n" +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = 0\n" +
                        "@CONTINUE_" + labelNum + "\n" +
                        "0;JMP\n" +
                        getTrueLabel() +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = -1\n" +
                        getContinueLabel());
                this.put("eq", "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "A = A - 1\n" +
                        "D = M - D\n" +
                        "@TRUE_" + labelNum + "\n" +
                        "D;JEQ\n" +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = 0\n" +
                        "@CONTINUE_" + labelNum + "\n" +
                        "0;JMP\n" +
                        getTrueLabel() +
                        "@SP\n" +
                        "A = M - 1\n" +
                        "M = -1\n" +
                        getContinueLabel());

                valuableLinesWritten += 15;
            } else if (key.equals("add")) {
                valuableLinesWritten += 5;
            } else if (key.equals("sub")) {
                valuableLinesWritten += 5;
            } else if (key.equals("neg")) {
                valuableLinesWritten += 3;
            } else if (key.equals("and")) {
                valuableLinesWritten += 5;
            } else if (key.equals("or")) {
                valuableLinesWritten += 5;
            } else if (key.equals("not")) {
                valuableLinesWritten += 3;
            }



            // return the previous/current value

            return oldValue;
        }
    }


    /**
     * Opens the output file stream and gets ready to write to it.
     * @param fileName the name of the desired output file as a string
     */
    public CodeWriter(String fileName) {


       // next block of code might throw an exception
        try {

            // establish the connection to the output file
            outputFileName = fileName;
            outputFile = new PrintWriter(fileName);

        } catch (FileNotFoundException e) { // catch that shit

            // exception handling technique
            e.printStackTrace();
            System.exit(0); // force quit

        }

        valuableLinesWritten = 0;
        currentNumParams = 0;
        arithmeticMapper = new ArithmeticHashMap();

        // build the arithmetic mapper
        arithmeticMapper.put("add", "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "A = A -1\n" +
                        "M = M + D\n");
        arithmeticMapper.put("sub", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "M = M - D\n");
        arithmeticMapper.put("neg", "@SP\n" +
                "A = M - 1\n" +
                "M = -M\n");
        arithmeticMapper.put("eq", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "D = M - D\n" +
                "@TRUE_1\n" +
                "D;JEQ\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = 0\n" +
                "@CONTINUE_1\n" +
                "0;JMP\n" +
                "(TRUE_1)\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = -1\n" +
                "(CONTINUE_1)\n");
        arithmeticMapper.put("gt", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "D = M - D\n" +
                "@TRUE_1\n" +
                "D;JGT\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = 0\n" +
                "@CONTINUE_1\n" +
                "0;JMP\n" +
                "(TRUE_1)\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = -1\n" +
                "(CONTINUE_1)\n");
        arithmeticMapper.put("lt", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "D = M - D\n" +
                "@TRUE_1\n" +
                "D;JLT\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = 0\n" +
                "@CONTINUE_1\n" +
                "0;JMP\n" +
                "(TRUE_1)\n" +
                "@SP\n" +
                "A = M - 1\n" +
                "M = -1\n" +
                "(CONTINUE_1)\n");
        arithmeticMapper.put("and", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "M = M&D\n");
        arithmeticMapper.put("or", "@SP\n" +
                "AM = M - 1\n" +
                "D = M\n" +
                "A = A - 1\n" +
                "M = M|D\n");
        arithmeticMapper.put("not", "@SP\n" +
                "A = M - 1\n" +
                "M = !M\n");
    }

    /**
     * Writes to the output file the assembly code that implements the given input command.
     * @param command the given input command as a string
     */
    public void writeArithmetic(String command) {
        if (arithmeticMapper.containsKey(command)) {
            String code = "// " + command + "\n";
            code = code + arithmeticMapper.get(command) + "\n";

            outputFile.write(code);
            outputFile.flush();
        } else {
            // do nothing
        }
    }

    public void setFileName(String name) {
        fileName = name;
    }


    /**
     * Helper method for building and returning a 'continue' label which is unique.
     * @return (CONTINUE_#) where '#' is a unique label id
     */
    public String getContinueLabel() {
        return "(CONTINUE_" + labelNum + ")\n";
    }

    /**
     * Helper method for building and returning a 'true' label which is unique.
     * @return (TRUE_#) where '#' is a unique label id
     */
    public String getTrueLabel() {
        return "(TRUE_" + labelNum + ")\n";
    }


    /**
     * Writes to the output file the assembly code that implements the given command where
     * the given command is either C_PUSH or C_POP.
     * @param commandType the command type as the enumerated data type 'CommandType'
     */
    public void writePushPop(CommandType commandType, String segment, int index) {
        String code = "// " + commandType  + " " + segment + " " + index + "\n";

        // if we're dealing with local, argument, this, or that
        if (segment.equals("local") ||
            segment.equals("argument") ||
            segment.equals("this") ||
            segment.equals("that")) {

            // pushing and popping to these 4 segments use the same code for
            // addr = LCL + arg2
            if (commandType == CommandType.C_PUSH) {
                code = code + "@" + index + "\n" +
                        "D = A\n" +
                        "@" + getSymbolFromWord(segment) + "\n" +
                        "A = M + D\n" +
                        "D = M\n" +
                        "\n" +
                        "@SP\n" +
                        "A = M\n" +
                        "M = D\n" +
                        "@SP\n" +
                        "M = M + 1\n";

                        valuableLinesWritten += 10;
            } else {
                code = code + "@" + index + "\n" +
                        "D = A\n" +
                        "@" + getSymbolFromWord(segment) + "\n" +
                        "A = M + D\n" +
                        "D = A\n" +
                        "@addr\n" +
                        "M = D\n" +
                        "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "@addr\n" +
                        "A = M\n" +
                        "M = D\n";

                        valuableLinesWritten += 13;
            }
        // handling the constant segment
        } else if (segment.equals("constant")) {
            // we can only push these constants
            code = code + "@" + index + "\n" +
                    "D = A\n" +
                    "@SP\n" +
                    "AM = M + 1\n" +
                    "A = A - 1\n" +
                    "M = D\n";

            valuableLinesWritten += 6;


            if (VMTranslator.DEBUG) System.out.println("\t\tcodeWriter - > WRITING CONSTANT CODE");
        // handling the static segment
        } else if (segment.equals("static")) {
            switch(commandType){
                case C_POP:
                    code = code + "@SP\n" +
                            "AM = M-1\n" +
                            "D = M\n" +
                            "@" + outputFileName.substring(14,outputFileName.indexOf('.')) + "." + index + "\n" +
                            "M = D\n";
                    valuableLinesWritten += 5;
                    break;
                case C_PUSH:
                    code = code + "@" + outputFileName.substring(14,outputFileName.indexOf('.')) + "." + index + "\n" +
                            "D = M\n" +
                            "@SP\n" +
                            "AM = M + 1\n" +
                            "A = A - 1\n" +
                            "M = D\n";
                    valuableLinesWritten += 6;
                    break;
            }
        // now for the temp segment
        } else if (segment.equals("temp")) {

            if (commandType == CommandType.C_PUSH) {
                code = code + "@" + index + "\n" +
                        "D = A\n" +
                        "@5\n" +
                        "A = A + D\n" +
                        "D = M\n" +
                        "@SP\n" +
                        "A = M\n" +
                        "M = D\n" +
                        "@SP\n" +
                        "M = M + 1\n";
                valuableLinesWritten += 10;
            } else {
                code = code + "@" + index + "\n" +
                        "D = A\n" +
                        "@5\n" +
                        "D = A + D\n" +
                        "@addr\n" +
                        "M = D\n" +
                        "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "@addr\n" +
                        "A = M\n" +
                        "M = D\n";
                valuableLinesWritten += 12;
            }
        // and the pointer segment
        } else if (segment.equals("pointer")) {

            String thisOrThat = (index == 0) ? getSymbolFromWord("this")
                    : getSymbolFromWord("that");

            if (commandType == CommandType.C_PUSH) {
                code = code + "@" + thisOrThat + "\n" +
                        "D = M\n" +
                        "@SP\n" +
                        "AM = M + 1\n" +
                        "A = A - 1\n" +
                        "M = D\n";
                valuableLinesWritten += 6;
            } else if (commandType == CommandType.C_POP) {
                code = code + "@SP\n" +
                        "AM = M - 1\n" +
                        "D = M\n" +
                        "@" + thisOrThat + "\n" +
                        "M = D\n";
                valuableLinesWritten += 5;
            } else {
                // well then why am I in this method?
            }
        }

        // add a space between each vm command for readability and debugging help
        code = code + "\n";

        // write and flush
        outputFile.write(code);
        outputFile.flush();
    }

    /**
     * Writes an infinite loop at the end of the output file.
     */
    public void writeEnding() {

        String code = "(END)\n" +
                "@END\n" +
                "0;JMP\n";

        valuableLinesWritten += 2;
        // write and flush
        outputFile.write(code);
        outputFile.flush();
    }

    /**
     * Gets the assembly symbol from segment type. ex.. local = LCL
     * @param segment the given segment type as a String
     * @return the assembly symbol corresponding to the memory segment
     */
    private String getSymbolFromWord(String segment) {

        String returnThis = "";

        switch(segment) {
            case "local":
                returnThis = "LCL";
                break;
            case "argument":
                returnThis = "ARG";
                break;
            case "this":
                returnThis = "THIS";
                break;
            case "that":
                returnThis = "THAT";
                break;
            default:
                //throw new SyntaxException();
        }
        return returnThis;
    }

    /**
     * Closes the output file.
     */
    public void close() { outputFile.close(); }

    public void writeInit() {
        outputFile.write("@256\n" +
                "D = A\n" +
                "@SP\n" +
                "M = D\n"// SP = 256
        );
        outputFile.flush();
        writeCall("Sys.init", 0);

        valuableLinesWritten += 4; // TODO: FIX THIS
    }


    public void writeLabel(String labelName) {
        outputFile.write("// LABEL GENERATION\n(" + labelName + ")\n\n");
        outputFile.flush();
    }

    public void writeGoTo(String labelName) {
        outputFile.write("// GOTO\n@" + labelName + "\n0;JMP\n\n");
        outputFile.flush();
        valuableLinesWritten += 2;
    }

    public void writeIfGoTo(String labelName) {
        outputFile.write("// IF-GOTO\n@SP\nAM = M - 1\nD = M\n@" + labelName + "\nD;JNE\n\n");
        outputFile.flush();
        valuableLinesWritten += 5;
    }

    public void writeFunction(String functionName, int nVars) {
        // in assembly, functions are really just labels.
        // so I'm going to simple write a label at this write Command.
        outputFile.write("// DEFINE FUNCTION " + functionName + "\n(" + functionName + ")\n") ;
        currentNumParams = nVars;


        outputFile.flush();
        for ( int i = 1; i <= nVars; i++) {
            // PUSH 0
            writePushPop(CommandType.C_PUSH, "constant", 0);
        }
        outputFile.flush();


        outputFile.write("\n");
        valuableLinesWritten += (2);
        outputFile.flush();
    }

    public void writeCall(String functionName, int nVars) {
        // calling needs to accomplish the following:
        // 1. sets the ARG pointer
        // 2. Saves the callers frame onto the stack
        //      - return address, saved LCL, ARG, THIS, THAT
        // 3.

        // first lets push the function's callers frame onto the stack.
        // push the return address onto the stack
        outputFile.write("//CALL + " + functionName + "\n@" + (valuableLinesWritten + 37 + (5 + nVars) + 5) + "\n" +
                "D = A\n" +
                "@SP\n" +
                "AM = M + 1\n" +
                "A = A-1\n" +
                "M = D\n"); //TODO: FIX THIS

        // now push the LCL
        outputFile.write("@LCL\n" +
                "D = M\n" +
                "@SP\n" +
                "AM = M + 1\n" +
                "A = A - 1\n" +
                "M = D\n");

        // now push ARG
        outputFile.write("@ARG\n" +
                "D = M\n" +
                "@SP\n" +
                "AM = M + 1\n" +
                "A = A - 1\n" +
                "M = D\n");

        // and THIS/THAT
        outputFile.write("@THIS\n" +
                "D = M\n" +
                "@SP\n" +
                "AM = M + 1\n" +
                "A = A - 1\n" +
                "M = D\n");
        outputFile.write("@THAT\n" +
                "D = M\n" +
                "@SP\n" +
                "AM = M + 1\n" +
                "A = A - 1\n" +
                "M = D\n");

        // time to set the arg pointer
        // we pushed 5 things onto the stack, the callers frame,
        outputFile.write("@SP\n" +
                "A = M\n");

        // find the first argument
        for (int i = 1; i <= (5 + nVars); i++) {
            outputFile.write("A = A-1\n");
        }

        outputFile.write("D = A\n" +
                "@ARG\n" + // now go to ARG and set this value
                "M = D\n" +
                "@" + functionName + "\n" +
                "0;JMP\n\n"); // jump to the function to execute code


        // reposition LCL
        outputFile.write("@SP\n" +
                "D = M\n" +
                "@LCL\n" +
                "M = D\n" + // point the LCL segment
                "@SP\n" // go to SP
        );
        outputFile.flush();

        // transfer control GOTO F
        writeGoTo(functionName);

        // declare a label for the return address
        valuableLinesWritten += 37 + (5 + nVars) + 5;
        writeLabel(valuableLinesWritten + "");

        // end of things to do for the call
        outputFile.flush();

    }

    public void writeReturn() {
        // take the top of the stack and copy it into argument 0
        String code = "//RETURN CALL\n@SP\n" +
                "A = M-1\n" +// go to the top of the stack
                "D = M\n" +
                "@ARG\n" +
                "A = M\n" +
                "M = D\n"; // copy top of stack into ARG 0

        // restore segment pointer LCL
        for (int i = 1; i <= (currentNumParams + 1); i++) {
            code = code + "A = A + 1\n";
        }
        // I am now at  the saved LCL, copy M into D and store in LCL
        code = code + "D = M\n" +
                "@LCL\n" +
                "M = D\n";

        // restore segment pointer THIS
        code = code + "@ARG\n" +
                "A = M\n";
        for (int i = 1; i <= (currentNumParams + 3); i++) {
            code = code + "A = A + 1\n";
        }
        code = code + "D = M\n" +
                "@THIS\n" +
                "M = D\n";

        // restore segment pointer THAT
        code = code + "@ARG\n" +
                "A = M\n";
        for (int i = 1; i <= (currentNumParams + 4); i++) {
            code = code + "A = A + 1\n";
        }
        code = code + "D = M\n" +
                "@THAT\n" +
                "M = D\n";
        // restore segment pointer ARG last
        code = code + "@ARG\n" +
                "A = M\n";
        for (int i = 1; i <= (currentNumParams + 2); i++) {
            code = code + "A = A + 1\n";
        }
        code = code + "D = M\n" +
                "@ARG\n" +
                "M = D\n";

        //sets SP to after arg 0
        code = code + "@ARG\n" +
                "D = M\n" +
                "@SP\n" +
                "M = D + 1\n";

        // jump to the return address within the caller's code
        code = code + "@SP\n" +
                "A = M\n";
        for (int i = 1; i <= (currentNumParams - 1); i++) {
            code = code + "A = A + 1\n";
        }
        // I am now at the saved return address jump to this value
        code = code + "A = M\n" +
                "0;JMP\n\n";

        outputFile.write(code);
        outputFile.flush();

        valuableLinesWritten += 32 +
                ((currentNumParams -1) < 0 ? (0) : (currentNumParams -1))  +
                (currentNumParams + 2) + (currentNumParams +4) + (currentNumParams + 3)
                + (currentNumParams + 1);
    }




}
