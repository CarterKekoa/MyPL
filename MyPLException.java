/**
 * Author: S. Bowers
 * File: MyPLException.java
 * 
 */

public class MyPLException extends Exception {

  private String type;
  private String message;
  private int line;
  private int column;
  private Object returnValue;
  
  
  public MyPLException(String type, String message, int line, int column) {
    this.type = type;
    this.message = message;
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return type + " error: " + message + " at line " + line + " column " + column;
  }

  
  // for return value passing


  public MyPLException(Object value) {
    this.type = "return";
    this.returnValue = value;
  }

  public boolean isReturnException() {
    return type.equals("return");
  }

  public Object getReturnValue() {
    return returnValue;
  }

  

}
