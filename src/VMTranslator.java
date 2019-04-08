/**
 * Drives the entire process of the VM Translation.
 *
 * ALGORITHM:
 * 1) Constructs a Parser to handle the file input.
 * 2) Constructs a CodeWriter to handle the output file.
 * 3) Marches through the input file, parsing each line and generating code from it.
 *
 * INPUT: fileName.vm
 * OUTPUT: fileName.asm
 */
public class VMTranslator {

    public static final boolean DEBUG = true;
    public static final String IN_FILE_NAME = "StackTest";

    /** CodeWriter object for writing the code for each command **/
    private CodeWriter codeWriter;

    /** Parser object for advancing through the file and parsing. **/

    /** the evil main method that drives the entire VMTranslator-inator **/
    public static void main(String[] args) {

        // set up parser and codeWriter streams
        Parser parser = new Parser("src/in_files/" + IN_FILE_NAME + ".vm");
        CodeWriter codeWriter = new CodeWriter("src/out_files/" + IN_FILE_NAME + ".asm");

        // while parser can continue
        while (parser.hasMoreCommands()) {
            // advance the parser
            parser.advance();

            // write the code
            if (parser.getCommandType() == CommandType.C_ARITHMETIC) {
                codeWriter.writeArithmetic(parser.arg1());

                codeWriter.getContinueLabel();
                codeWriter.getTrueLabel();

            } else if (parser.getCommandType() == CommandType.C_PUSH ||
                    parser.getCommandType() == CommandType.C_POP) {
                if (DEBUG) System.out.println("command type is push or pop");
                codeWriter.writePushPop(parser.getCommandType(), parser.getArg2(),
                        parser.arg3());
            } else {
                // nothing
            }

        }

        codeWriter.writeEnding();

        if (DEBUG) System.out.println("\tprocess finished | " + parser.getLinesRead() + " lines read");

        codeWriter.close();
    }
}
