
/**
 * Author: Carter Mooring
 * Assign: HW4
 * File: PrintVisitor.java
 *
 * Print Visitor skeleton code for MyPL AST.
 */


import java.io.PrintStream;


public class PrintVisitor implements Visitor {
  private PrintStream out;      // the output stream for printing
  private int indent = 0;       // the current indent level (num spaces)

  // indent helper functions

  // to get a string with the current indentation level (in spaces)
  private String getIndent() {
    return " ".repeat(indent);
  }

  // to increment the indent level
  private void incIndent() {
    indent += 2;
  }

  // to decrement the indent level
  private void decIndent() {
    indent -= 2;
  }

  // visitor functions

  public PrintVisitor(PrintStream printStream) {
    this.out = printStream;
  }

  // statement list
  public void visit(StmtList node) {
    // iterate through each statement list node and delegate
    for (Stmt s : node.stmts) {
      out.print(getIndent());
      s.accept(this);
      out.println();
    }
  }

  // statements
  public void visit(VarDeclStmt node) {
    out.print("var ");
    if (node.varType != null){
      out.print(node.varType.lexeme() + " ");
    }  
    if(node.varId != null){
      out.print(node.varId.lexeme() + " ");
    }
    out.print(":= ");
    node.varExpr.accept(this);
  }

  public void visit(AssignStmt node){
    out.print("set ");
    node.lhs.accept(this);
    out.print(" := ");
    node.rhs.accept(this);
  }

  public void visit(ReturnStmt node){
    out.print("return ");
    if(node.returnExpr != null){
      node.returnExpr.accept(this);
    }
  }

  public void visit(IfStmt node){
    out.print("if ");
    node.ifPart.boolExpr.accept(this);
    out.print(" then\n");
    incIndent();
    node.ifPart.stmtList.accept(this);
    decIndent();
    for(BasicIf elseif : node.elsifs){
      out.print("elif ");
      elseif.boolExpr.accept(this);
      out.print( " then\n");
      incIndent();
      elseif.stmtList.accept(this);
      decIndent();
      if(node.hasElse){
        out.print("else\n");
        incIndent();
        node.elseStmtList.accept(this);
        decIndent();
      }
    }
    out.print(getIndent() + "end");
  }

  public void visit(WhileStmt node){
    out.print("while ");
    node.boolExpr.accept(this);
    out.print( " do\n");
    incIndent();
    node.stmtList.accept(this);
    decIndent();
    out.print(getIndent() + "end");
  }

  public void visit(ForStmt node){
    out.print("for " + node.var.lexeme() + " := ");
    node.startExpr.accept(this);
    out.print(" to ");
    node.endExpr.accept(this);
    out.println(" do");
    incIndent();
    node.stmtList.accept(this);
    decIndent();
    out.println("end\n");
  }

  public void visit(TypeDeclStmt node){
    out.print("\n");
    out.print("type " + node.typeId.lexeme());
    out.print("\n");
    incIndent();
    for(VarDeclStmt stmt : node.fields){
      out.print(getIndent());
      stmt.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print("end\n");
  }

  public void visit(FunDeclStmt node){
   out.print("\n");
   out.print("fun ");
   out.print(node.returnType.lexeme());
   out.print(" ");
   out.print(node.funName.lexeme());
   out.print("(");
   int count = 0;
   for(FunParam item: node.params){
     out.print(item.paramType.lexeme() + " ");
     out.print(item.paramName.lexeme());
     if(count < node.params.size() - 1){
       out.print(", ");
     }
     count++;
   }
   out.print(")\n");
   incIndent();
   node.stmtList.accept(this);
   decIndent();
   
   out.print(getIndent() + "end\n");
  }

  public void visit(Expr node){
    if(node.negated){
      out.print("not ");
    }
    if(node.operator != null){
      out.print("(");
    }
    if(node.first != null){
      node.first.accept(this);
    }
    if(node.operator != null){
      out.print(" " + node.operator.lexeme() + " ");
    }
    if(node.rest != null){
      node.rest.accept(this);
    }
    if(node.operator != null){
      out.print(")");
    }
  }

  public void visit(LValue node){
    int count = 0;
    for(Token item : node.path){
      out.print(item.lexeme());
      if(count < node.path.size() - 1){
        out.print(".");
      }
      count++;
    }
  }

  public void visit(SimpleTerm node){
    if(node.rvalue != null){
     node.rvalue.accept(this);
  }
}

  public void visit(ComplexTerm node){
    node.expr.accept(this);
  }

  public void visit(SimpleRValue node){
   if(node.val.type() == TokenType.STRING_VAL){
     out.print("\"" + node.val.lexeme() + "\"");
   }else {
     out.print(node.val.lexeme());
   }
  }
  public void visit(NewRValue node){
    out.print("new ");
    out.print(node.typeId.lexeme());
  }

  public void visit(CallRValue node){
   int count = 0;
    out.print(node.funName.lexeme().toString() + "(");
    for(Expr items : node.argList){
      items.accept(this);
      if(count < node.argList.size() - 1){
        out.print(", ");
      }
      count++;
    }
    out.print(")");
  }

  public void visit(IDRValue node){
    String msg = "";
    int count = node.path.size();
    for(Token val : node.path){
      if(count > 1){
        msg += (val.lexeme().toString() + ".");
      }else{
        msg += (val.lexeme().toString());
      }
      count--;
    }
    out.print(msg);
  }

  public void visit(NegatedRValue node){
    out.print("neg ");
    node.expr.accept(this);
  }
}
