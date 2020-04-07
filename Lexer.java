
/**
 * Author: Carter Mooring
 * Assign: 2
 * Date: Jan. 29th, 2020
 *
 * NOTE:
 *    My test Files are named e9.mypl and p5.mypl and they are both accompanied with a .out file.
 *
 * The lexer implementation tokenizes a given input stream. The lexer
 * implements a pull-based model via the nextToken function such that
 * each call to nextToken advances the lexer to the next token (which
 * is returned by nextToken). The file has been completed read when
 * nextToken returns the EOS token. Lexical errors in the source file
 * result in the nextToken function throwing a MyPL Exception.
 */

import java.util.*;
import java.io.*;

public class Lexer {

  private BufferedReader buffer; // handle to input stream
  private int line;
  private int column;

  /** 
   */
  public Lexer(InputStream instream) {
    buffer = new BufferedReader(new InputStreamReader(instream));
    this.line = 1;
    this.column = 0;
  }

  /**
   * Returns next character in the stream. Returns -1 if end of file.
   */
  private int read() throws MyPLException {
    try {
      int ch = buffer.read();
      return ch;
    } catch (IOException e) {
      error("read error", line, column + 1);
    }
    return -1;
  }

  /**
   * Returns next character without removing it from the stream.
   */
  private int peek() throws MyPLException {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = read();
      buffer.reset();
    } catch (IOException e) {
      error("read error", line, column + 1);
    }
    return ch;
  }

  /**
   * Print an error message and exit the program.
   */
  private void error(String msg, int line, int column) throws MyPLException {
    throw new MyPLException("Lexer", msg, line, column);
  }

  /**
   */
  public Token nextToken() throws MyPLException {
    if (peek() == -1) { // if at end of file, return EOS Token
      return new Token(TokenType.EOS, "", line, column);
    }
    char symbol; // the current character symbol
    symbol = (char) read();
    if (column == 0) { // if column is 0 then reset to 1
      column += 1;
    } else { // otherwise increase overall value of column by 1
      column++;
    }
    int secretColumn = column; // to keep track of original 'secret' column values when printing
    String lexeme = ""; // for strings
    // CHECK WHITE SPACE
    if (Character.isWhitespace(symbol)) { // while white
      while (symbol == '\n') {
        line += 1;
        column = 1;
        symbol = (char)read();
      }
      while (Character.isWhitespace(symbol)) {
        symbol = (char)read();
        column += 1;
      }
    } else if (symbol == -1) {
      return new Token(TokenType.EOS, "", line, column);
    }
  
    // BASIC SYMBOL CHECK
    if (symbol == ',') {
      return new Token(TokenType.COMMA, ",", line, column);
    } else if (symbol == '.') {
      return new Token(TokenType.DOT, ".", line, column);
    } else if (symbol == '+') {
      return new Token(TokenType.PLUS, "+", line, column);
    } else if (symbol == '-') {
      return new Token(TokenType.MINUS, "-", line, column);
    } else if (symbol == '*') {
      return new Token(TokenType.MULTIPLY, "*", line, column);
    } else if (symbol == '/') {
      return new Token(TokenType.DIVIDE, "/", line, column);
    } else if (symbol == '%') {
      return new Token(TokenType.MODULO, "%", line, column);
    } else if (symbol == '=') {
      return new Token(TokenType.EQUAL, "=", line, column);
    } else if (symbol == '>') {
      if (peek() == '=') {
        read();
        column += 1;
        return new Token(TokenType.GREATER_THAN_EQUAL, ">=", line, column - 1);
      }
      return new Token(TokenType.GREATER_THAN, ">", line, column);
    } else if (symbol == '<') {
      if (peek() == '=') {
        read();
        column += 1;
        return new Token(TokenType.LESS_THAN_EQUAL, "<=", line, column - 1);
      }
      return new Token(TokenType.LESS_THAN, "<", line, column);
    } else if (symbol == '!') {
      if (peek() == '=') {
        read();
        column += 1;
        return new Token(TokenType.NOT_EQUAL, "!=", line, column - 1);
      }
      return new Token(TokenType.NOT, "!", line, column);
    } else if (symbol == '(') {
      return new Token(TokenType.LPAREN, "(", line, column);
    } else if (symbol == ')') {
      return new Token(TokenType.RPAREN, ")", line, column);
    } else if (symbol == ':') {
      if (peek() == '=') {
        column += 1;
        read();
        return new Token(TokenType.ASSIGN, ":=", line, column - 1);
      }
    } else if (symbol == '#') {
      while (symbol != '\n') {
        symbol = (char) read();
      }
      column = 0;
      line++;
      return nextToken();
    } else if (symbol == '\'') {
      String properChar = "";
      properChar += symbol;
      secretColumn = column;
      symbol = (char) read();
      lexeme += symbol;
      while (symbol != '\'') {
        symbol = (char) read();
        properChar += symbol;
      }
      properChar += symbol;
      if (properChar.length() > 3) { // for my own test case later
        String msg = "not a valid char ";
        throw new MyPLException("Lexer", msg, line, secretColumn);
      } else {
        return new Token(TokenType.CHAR_VAL, lexeme, line, secretColumn);
      }
    } else if (symbol == '\"') { // we know the first symbol is a " for a string, column at first " spot
      secretColumn = column; // holds column value of first "
      if (peek() == '\"') { // if the next symbol is another ", meaning "" then read it and +1 col, print
        read();
        column++;
        return new Token(TokenType.STRING_VAL, lexeme, line, secretColumn);
      }

      symbol = (char) read(); // move to spot right of first " and add it to lexeme

      while (symbol != '\"') { // now we know there is stuff in the string so we will go until we reach the
                               // second "
        lexeme += symbol; // will add current symbol, this prevents the last " from being printed
        column++;
        symbol = (char) read();
        if (symbol == '\n') {
          String msg = "found newline within string ";
          throw new MyPLException("Lexer", msg, line, column + 1);
        }
      }
      column++; // this is for the final iteration where the second " does not get accounted for

      return new Token(TokenType.STRING_VAL, lexeme, line, secretColumn);
    }

    String err = "Lexer";

    // DIGIT CHECKS
    if (Character.isDigit(symbol)) {
      lexeme += symbol; // holds first digit
      boolean isValid = true;
      secretColumn = column;

      while (!Character.isWhitespace((char) peek())) { // while the next spot is not a space
        if (Character.isDigit((char) peek())) { // if next spot is a digit, add it to lexeme, iterate col
          column += 1;
          lexeme += (char) read();
        } else if ((char) peek() == '.') { // else if next spot is a ., read it, change col count, bool false
          column += 1;
          isValid = false;
          lexeme += (char) read();

          if (!Character.isDigit((char) peek())) { // check if the spot after the . is a digit, if not it is invalid
            String msg = "missing digit in float '" + lexeme + "'";
            throw new MyPLException("Lexer", msg, line, secretColumn);
          }
        } else if (Character.isLetter((char) peek())) { // else if next spot is a letter after a number
          String msg = "unexpected symbol '" + (char) peek() + "'";
          throw new MyPLException("Lexer", msg, line, column + 1);
        } else { // for when the number is followed by an opperation
          break;
        }
      }
      // if here, we know the next spot is a white space or something else like +

      if (isValid) {
        if (lexeme.charAt(0) == '0' && lexeme.length() > 1) {
          String msg = "leading zero in '" + lexeme + "'";
          throw new MyPLException("Lexer", msg, line, secretColumn);
        }
        return new Token(TokenType.INT_VAL, lexeme, line, secretColumn);
      }
      return new Token(TokenType.DOUBLE_VAL, lexeme, line, secretColumn);

    }

    // RESERVED WORDS
    if (Character.isLetter(symbol)) {
      lexeme += symbol; // add first letter of word to lexeme
      secretColumn = column; // hold original column

      while (Character.isLetter((char) peek()) || Character.isDigit((char) peek()) || (char) peek() == '_') { // while we dont have the whole string                                                                                                                                                                                                                           
        column += 1;
        lexeme += (char) read(); // read next value and update column
      }

      switch (lexeme) {
      case "int":
        return new Token(TokenType.INT_TYPE, lexeme, line, secretColumn);
      case "double":
        return new Token(TokenType.DOUBLE_TYPE, lexeme, line, secretColumn);
      case "char":
        return new Token(TokenType.CHAR_TYPE, lexeme, line, secretColumn);
      case "string":
        return new Token(TokenType.STRING_TYPE, lexeme, line, secretColumn);
      case "bool":
        return new Token(TokenType.BOOL_TYPE, lexeme, line, secretColumn);
      case "type":
        return new Token(TokenType.TYPE, lexeme, line, secretColumn);
      case "and":
        return new Token(TokenType.AND, lexeme, line, secretColumn);
      case "or":
        return new Token(TokenType.OR, lexeme, line, secretColumn);
      case "not":
        return new Token(TokenType.NOT, lexeme, line, secretColumn);
      case "neg":
        return new Token(TokenType.NEG, lexeme, line, secretColumn);
      case "while":
        return new Token(TokenType.WHILE, lexeme, line, secretColumn);
      case "for":
        return new Token(TokenType.FOR, lexeme, line, secretColumn);
      case "to":
        return new Token(TokenType.TO, lexeme, line, secretColumn);
      case "do":
        return new Token(TokenType.DO, lexeme, line, secretColumn);
      case "if":
        return new Token(TokenType.IF, lexeme, line, secretColumn);
      case "then":
        return new Token(TokenType.THEN, lexeme, line, secretColumn);
      case "else":
        return new Token(TokenType.ELSE, lexeme, line, secretColumn);
      case "elif":
        return new Token(TokenType.ELIF, lexeme, line, secretColumn);
      case "end":
        return new Token(TokenType.END, lexeme, line, secretColumn);
      case "fun":
        return new Token(TokenType.FUN, lexeme, line, secretColumn);
      case "var":
        return new Token(TokenType.VAR, lexeme, line, secretColumn);
      case "set":
        return new Token(TokenType.SET, lexeme, line, secretColumn);
      case "return":
        return new Token(TokenType.RETURN, lexeme, line, secretColumn);
      case "new":
        return new Token(TokenType.NEW, lexeme, line, secretColumn);
      case "nil":
        return new Token(TokenType.NIL, lexeme, line, secretColumn);
      case "true":
        return new Token(TokenType.BOOL_VAL, lexeme, line, secretColumn);
      case "false":
        return new Token(TokenType.BOOL_VAL, lexeme, line, secretColumn);
      default:
        return new Token(TokenType.ID, lexeme, line, secretColumn);
      }
    }

    // CHECK WHITE SPACE
    if (Character.isWhitespace(symbol)) { // while white
      symbol = (char) read();
      column += 1;
    }

    if (symbol == -1 || peek() == -1) { // if at end of file, return EOS Token
      column = 0; // set to 0 because end of file
      return new Token(TokenType.EOS, "", line, column);
    }

    String msg = "unexpected symbol ’" + symbol + "’";
    throw new MyPLException(err, msg, line, column);
  }
}
