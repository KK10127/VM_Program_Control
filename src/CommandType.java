/**
 * Enumerated data type called "CommanndType" which specifies the type of VM command
 * the parser has encountered.
 *
 * @author Jay Montoya
 * @version 1.0
 */
enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_FUNCTION,
    C_RETURN,
    C_CALL,
    C_NONE
}
