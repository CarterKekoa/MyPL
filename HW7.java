
/**
 * Author: S. Bowers
 * Assign: 7
 * File: HW7.java
 *
 * Basic test driver for the interpreter.
 */


import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class HW7 {

  public static void main(String[] args) {
    try {
      // determine if file or standard in
      InputStream istream = System.in;
      if (args.length == 1) {
        istream = new FileInputStream(new File(args[0]));
      }
      // create the lexer
      Lexer lexer = new Lexer(istream);
      // create and run the parser
      Parser parser = new Parser(lexer);
      StmtList stmtList = parser.parse();
      // create and run the type checker
      TypeChecker typeChecker = new TypeChecker();
      stmtList.accept(typeChecker);
      // create and run the interpreter
      Interpreter interpreter = new Interpreter();
      int result = interpreter.run(stmtList);
      System.exit(result);
    } catch (MyPLException e) {
      System.out.println(e);
      System.exit(1);
    } catch (FileNotFoundException e) {
      System.out.println("Unable to open file '" + args[0] + "'");
      System.exit(1);
    }
     
  }
  
}
