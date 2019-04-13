import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Class responsible for parsing and updating fields for translation during the
 * VM -> hack assembly process.
 *
 * - Handles the parsing of a single .vm file.
 * - Reads a VM command, parses it into it's lexical components, and provides convenient
 *      access to these components.
 * - Ignores all whitespace and comments.
 *
 * @author Jay Montoya
 * @version 1.0
 */
public class Parser {

    /** the first argument of the VM command (ex. 'pop local') **/
    private String arg1;

    /** the memory segment if applicable */
    private String arg2;

    /** the second argument of the VM command (usually having to do with registers) **/
    private int arg3;



    /** a scanner object to be used to read the input file **/
    private Scanner inputFile;

    /** the type of command the parser has encounters (see CommandType.java) **/
    private CommandType commandType;

    /** raw line of the file **/
    private String rawLine;
    private String cleanLine;
    private int lineNumber;

    /**
     * Constructor for a parser object given the file name
     * @param fileName the path of the file you wish to read
     */
    public Parser(String fileName) {
        // initialize the scanner field
        try {
            inputFile = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        lineNumber = 0;
    }

    /**
     * Simple method for determining if the parser can parse more lines
     * @return a boolean value indicating if the parse can continue.
     */
    public boolean hasMoreCommands() {
        return inputFile.hasNextLine();
    }

    /**
     * Void method responsible for advancing and parsing the given file.
     * Reads the next command from the input and makes it the current command.
     * Should be called only if hasMoreCommands() is true. Initially, there is
     * no current command
     */
    public void advance() {
        // get the net raw line.
        rawLine = inputFile.nextLine(); // hi

        cleanLine();

        if (cleanLine.equals("")) {
            //if (VMTranslator.DEBUG) System.out.println("\t\t\tEMPTY LINE");
            //System.out.println("Unrecognized Command!");
            commandType = CommandType.C_NONE;
            setArg1("");
            arg2 = "";
            return;
        } else {
            lineNumber++;
        }

        // parse everything
        try {
            parse();
        } catch (Exception e) {
            System.out.println("FATAL ERROR");
            System.out.println(e.getMessage());
            System.exit(0);
        }

        // show results
        if (VMTranslator.DEBUG && commandType != CommandType.C_NONE) System.out.println("\tadvance() LINE " + lineNumber + " [ "
                + cleanLine + " ] // " + getCommandType() + " // " + arg1 + " " + arg2 + " " + arg3);


    }


    /**
     * Helper method for determining the command type of the line
     */
    private void parse() throws Exception {

        StringTokenizer st = new StringTokenizer(cleanLine);
        if (VMTranslator.DEBUG) System.out.println("\tcleanLine -> " + cleanLine);

        String firstWord = st.nextToken(" ");
        if (VMTranslator.DEBUG) System.out.println("\tfirstWord -> " + firstWord);

        if (validateArithmetic(firstWord)) {
            // then it is an arithmetic command
            arg1 = firstWord;
            commandType = CommandType.C_ARITHMETIC;

            // checking for extraneous syntax
            if (st.hasMoreTokens()) {
                String badToken = st.nextToken();
                throw new VMTranslatorException("[ILLEGAL SYNTAX]: arithmetic commands must be stand alone -> found '"
                        + badToken + "'");
            }

        } else if (firstWord.equals("push")) {
            String secondWord = st.nextToken(" ");
            if (!validateMemSegment(secondWord)) {
                throw new VMTranslatorException("[INVALID MEMORY SEGMENT]: '" + secondWord + "' is not a validated memory segment");
            }

            commandType = CommandType.C_PUSH;
            setArg1(firstWord);
            arg2 = secondWord;

            // get the specific number
            setArg3(Integer.parseInt(st.nextToken()));

        } else if (firstWord.equals("pop")) {
            String secondWord = st.nextToken(" ");
            if (!validateMemSegment(secondWord)) {
                throw new VMTranslatorException("[INVALID MEMORY SEGMENT]: '" + secondWord + "' is not a validated memory segment");
            }
            commandType = CommandType.C_POP;
            arg2 = secondWord;

            // get the specific number
            setArg3(Integer.parseInt(st.nextToken()));

            // check for popping a constant
            if (arg2.equals("constant")) {
                throw new VMTranslatorException("[ILLEGAL COMMAND]: attempt to pop constant " + arg3);
            }

        } else if (validateBranchingCommand(firstWord)) {
            String secondWord = st.nextToken(" ");

            // set the command
            switch (firstWord) {
                case "label":
                    commandType = CommandType.C_LABEL;
                    break;
                case "goto":
                    commandType = CommandType.C_GOTO;
                    break;
                case "if-goto":
                    commandType = CommandType.C_IF;
                    break;
            }

            // let the second argument be the secondWord
            // we are defining a label so we will recognize this as the label we wish to define.
            // TODO: Check the label mapper to see if this word has already been mapped
            // TODO: Make a label mapper lol
            arg2 = secondWord;

            // checking for extraneous syntax
            if (st.hasMoreTokens()) {
                String badToken = st.nextToken();
                throw new VMTranslatorException("[ILLEGAL SYNTAX]: label commands must be two-part -> found extraneous '"
                        + badToken + "'");
            }
        } else if (validateFunctionCommand(firstWord)) {
            String secondWord = "";
            try {
                secondWord = st.nextToken(" ");
            } catch (Exception e) {
                System.out.println("nice try buddy");
            }

            // set the command
            switch (firstWord) {
                case "function":
                    commandType = CommandType.C_FUNCTION;
                    break;
                case "call":
                    commandType = CommandType.C_CALL;
                    break;
                case "return":
                    commandType = CommandType.C_RETURN;
                    break;
            }

            // let this second word be the function name
            // TODO: Check the function name mapper to see if this word has already been mapped
            // TODO: Make a function mapper lol <- combine with the label mapper??
            if (!secondWord.equals(""))
                arg2 = secondWord;

            // let the next integer be the nArgs
            // get the specific number (this will be the nArgs)
            if (!secondWord.equals(""))
                setArg3(Integer.parseInt(st.nextToken()));

        } else {
            throw new VMTranslatorException("[INVALID COMMAND]: unrecognized command! '" + firstWord + "'");
        }
    }

    /**
     * Accessor method for the command type of the current line.
     * @return the command type as an enum CommandType
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Accessor method for the arg2 field.
     * @return arg2 string
     */
    public String getArg2() {
        return arg2;
    }

    /**
     * Accessor method for the lines read
     * @return lines read as a string
     */
    public String getLinesRead() { return lineNumber + ""; }


    /**
     * Validates an arithmetic command given the first word of the line.
     * @param word The command as a string.
     * @return Boolean result.
     */
    public boolean validateArithmetic(String word) {
        return (word.equals("add") ||
                word.equals("sub") ||
                word.equals("neg") ||
                word.equals("eq") ||
                word.equals("gt") ||
                word.equals("lt") ||
                word.equals("and") ||
                word.equals("or") ||
                word.equals("not") );
    }

    /**
     * Validates a proper memory segment given as a string.
     * @param word The memory segment as a string.
     * @return A boolean result.
     */
    public boolean validateMemSegment(String word) {
        return (word.equals("local") ||
                word.equals("argument") ||
                word.equals("this") ||
                word.equals("that") ||
                word.equals("constant") ||
                word.equals("static") ||
                word.equals("pointer") ||
                word.equals("temp") );
    }

    /**
     * Validates a branching command given the first word.
     * @param word The command as a string.
     * @return A boolean result.
     */
    public boolean validateBranchingCommand(String word) {
        return (word.equals("label") ||
                word.equals("goto") ||
                word.equals("if-goto"));
    }

    /**
     * Validates a function command given the first word.
     * @param word The command as a string.
     * @return A boolean result.
     */
    public boolean validateFunctionCommand(String word) {
        return (word.equals("function") ||
                word.equals("call") ||
                word.equals("return"));
    }

    /**
     * Helper method for cleaning the line contents/
     * @return a String representing a clean line in the file.
     */
    public void cleanLine() {
        cleanLine = rawLine.trim();
        int index = cleanLine.indexOf("//");


        cleanLine = (index != -1)
                ? cleanLine.substring(0, index).trim()
                : cleanLine.trim();
    }


    /**
     * Accessor method for arg1. Returns the first argumnt of the
     * command. In the case of C_ARITHMETIC, the command itself,
     * (add, sub, etc.) is returned. Should not be called if the
     * current command is C_RETURN.
     * @return arg1
     */
    public String arg1() {
        return arg1;
    }

    /** Accessor method for arg3. Returns the second argument of the
     * current command. Should be called only if the current command
     * is C_PUSH, C_POP, C_FUNCTION, or C_CALL.
     * @return arg3
     */
    public int arg3() {
        return arg3;
    }

    /**
     * Mutator method for arg1
     * @param arg1 arg1
     */
    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    /**
     * Mutator method for arg3
     * @param arg3 arg3
     */
    public void setArg3(int arg3) {
        this.arg3 = arg3;
    }
}
