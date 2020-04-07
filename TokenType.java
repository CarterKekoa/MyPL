/**
 * Author: S. Bowers
 * Assign: 2
 *
 * The set of token types in MyPL.  Note that EOS denotes the "end of
 * stream", which is used to signal the end of the source file. Token
 * types with VAL denote value types (e.g., 42), whereas token types
 * with TYPE denote corresponding types (e.g., the type int). The
 * token type TYPE denotes the 'type' reserved word used when defining
 * structured types.
 */

public enum TokenType {
  // basic symbols
  COMMA,
  DOT,
  PLUS,
  MINUS,
  MULTIPLY,
  DIVIDE,
  MODULO,
  EQUAL,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL,
  NOT_EQUAL,
  LPAREN,
  RPAREN,
  ASSIGN,
  EOS, 
  // data types
  INT_VAL,
  DOUBLE_VAL,
  CHAR_VAL,
  STRING_VAL,
  BOOL_VAL,
  // reserved words
  INT_TYPE,
  BOOL_TYPE,
  DOUBLE_TYPE,
  CHAR_TYPE,
  STRING_TYPE,
  TYPE,
  AND,
  OR,
  NOT,
  NEG,
  WHILE,
  FOR,
  TO,
  DO,
  IF,
  THEN,
  ELSE,
  ELIF,
  END,
  FUN,
  VAR,
  SET,
  RETURN,
  NEW,
  NIL,
  // identifiers
  ID
}
