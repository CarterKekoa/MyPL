/**
 * Author: S. Bowers
 * Assign: 2
 *
 * Simple (immutable) container for token information. Token objects
 * are created by the Lexer and returned by Lexer's next_token()
 * function.
 */


public class Token {

  public Token(TokenType type, String lexeme, int row, int column) {
    this.type = type;
    this.lexeme = lexeme;
    this.row = row;
    this.column = column;
  }

  public TokenType type() {
    return type;
  }

  public String lexeme() {
    return lexeme;
  }

  public int row() {
    return row;
  }

  public int column() {
    return column;
  }

  @Override
  public String toString() {
    return type + " '" + lexeme + "' " + row + ":" + column;
  }
    
  private TokenType type;       // the type of the token
  private String lexeme;        // the string value of the token
  private int row;              // row where token occurred
  private int column;           // column where token occured
}
