import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
 *
 * @author Jay Montoya
 */
public class VMTranslator {

    // constants
    public static final boolean DEBUG = true;
    public static final String DIRECTORY_NAME
            = "src/HW09_TestFiles/FunctionCalls/FibonacciElement";

    /** CodeWriter object for writing the code for each command **/
    private CodeWriter codeWriter;

    /** the evil main method that drives the entire VMTranslator-inator **/
    public static void main(String[] args) {

        // add each .vm file to the directory
        ArrayList<File> vmFiles = new ArrayList<>();

        //get the directory name name
        StringTokenizer st = new StringTokenizer(DIRECTORY_NAME);
        String name = st.nextToken("/");
        while (st.hasMoreTokens()) {
            name = st.nextToken("/");
        }

        if (DEBUG) System.out.println("Directory name is: " + name);

        // create a new codewriter
        CodeWriter codeWriter = new CodeWriter(DIRECTORY_NAME + "/" + name + ".asm");

        //code to find all vmFiles in the directory and add them to the ArrayList
        File dir = new File(DIRECTORY_NAME);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(".vm")) {
                    vmFiles.add(child);
                }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
            System.out.println("ERROR: Directory is not a directory! =(");
            System.exit(0);
        }

        // write the bootstrap code
        codeWriter.writeInit();

        // for every vm file we have
        for (File e : vmFiles) {
            // inform the codeWriter we translating a new file
            codeWriter.setFileName(e.getName());
            if (DEBUG) System.out.println("file changed! now translating " + e.getName());

            // set up parser and codeWriter streams
            Parser parser = new Parser(e.getPath());

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
                } else if (parser.getCommandType() == CommandType.C_LABEL) {
                    if (DEBUG) System.out.println("command type is a label declaration");
                    codeWriter.writeLabel(parser.getArg2()); // TODO: Write the method for this
                } else if (parser.getCommandType() == CommandType.C_GOTO) {
                    if (DEBUG) System.out.println("command is a unconditional GOTO");
                    codeWriter.writeGoTo(parser.getArg2()); // TODO: Write the method for this
                } else if (parser.getCommandType() == CommandType.C_IF) {
                    if (DEBUG) System.out.println("command is an if-goto");
                    codeWriter.writeIfGoTo(parser.getArg2()); // TODO: Write the method for this
                } else if (parser.getCommandType() == CommandType.C_FUNCTION) {
                    if (DEBUG) System.out.println("command type is a function declaration");
                    codeWriter.writeFunction(parser.getArg2(), parser.arg3()); // TODO: Write the method for this
                } else if (parser.getCommandType() == CommandType.C_CALL) {
                    if (DEBUG) System.out.println("command type is a function call");
                    codeWriter.writeCall(parser.getArg2(), parser.arg3()); // TODO: Write the method for this
                } else if (parser.getCommandType() == CommandType.C_RETURN) {
                    if (DEBUG) System.out.println("command is a return");
                    codeWriter.writeReturn(); // TODO: Write the method for this
                }

            }

            if (DEBUG) System.out.println("\tprocess finished | " + parser.getLinesRead() + " lines read");
        }
        codeWriter.writeEnding();
        codeWriter.close();
    }
}
