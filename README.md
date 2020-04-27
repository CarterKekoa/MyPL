# MyPL
This is a Lexer, Parser, and Interpreter (written in Java) for a made up language MyPL that is written in Python. MyPL has it’s own grammar rules that the program follows.

## Basic
>• Made up language for exploring language implementation ideas                                      
• Mixes explicit and implicit typing (for type checking)                                                  
• Has many basic constructs you would find in a PL

**Data Types**
>int, double, bool, char, string, nil

**Values**
>0, 1, 7, 10, 20, 1000000000000 (ints)                                      
1.0, 1.01, 10.3, 0.5, etc (doubles)                                     
true, false (bools)                                     
‘a’, ‘b’, ‘1’, etc (chars)                                  
"foo", "bar", "", etc (strings)                                 
nil (nil value)                                             

**Comments**
```
#this is a single line comment
#we only support single line comments
```

**Variable declarations (Must have initializers)**
```
var int x := 5 #optional explicit type
var int y := 5*3 + 2 #optional explicit type
var z := true #implicit type (preferred)
var u := "foo" #implicit type (preferred)
var string x := nil #required explicit type
```

**Variable assignments**
```
set x := 10 #x must be declared
set z := false #z must be declared
#For loops (only work over int values)
for x := 1 to n do #x declared locally
...
end
```

**Relational comparators and Boolean operations**
>x = 1, x < 1, x <= 1, x > 1, x >= 1, x != 1    
true and false or true      
```
#While loops      
while x > 1 and x < 2 do
...
end
```

**Conditionals**
```
if x = 1 or x = 2 then
...
elif y > 20 and y <= 30 then
...
else
...
end
```

**Math ops**
>x + y #int, double addition                                           
x - y #int, double subtraction                                                  
x * y #int, double multiplication                                           
x / y #Int, double division                                       
x % y #int mod                                            
neg x #int, double negation                             

**Functions**
```
#function that returns an int
fun int f(int x, int y)
var z := x + y
if x < y then
set z := neg z
end
return z
end
#no return value
fun nil g(int x)
print(itos(x)) # convert int to string
print("\n")
end
var x := f(1, 2) # call f
g(x) #call g
```

**Structured types**
```
#type Node
var val := 0

var Node next := nil
end

var n1 := new Node #instance of Node
set n1.val := 10 # attribute access
var n2 := new Node
set n2.val := 20
set n1.next := n2
set n2.next := nil
```

**Built in functions**
```
var string s := nil
var int x := nil
var char c := nil
set s := itos(4) #converts int to string
set x := stoi("4") #converts string to int
set s := dtos(3.1) #converts double to string
set x := stod("3.1") #converts string to double
print("foo") #prints to standard output
set s := read() #read from standard input
set x := length("foo") #number of string chars
set c := get(0, "foo") #get first string char
set s := concat("ab", "cd") #string concatenation
set s := append("ab", ‘c’) #add char to end of string
```

**MyPL Grammar Rules**
```
⟨stmts⟩ ::= ⟨stmt ⟩ ⟨stmts ⟩ | ε
⟨bstmts⟩ ::= ⟨bstmt ⟩ ⟨bstmts ⟩ | ε
⟨stmt⟩ ::= ⟨tdecl ⟩ | ⟨fdecl ⟩ | ⟨bstmt ⟩
⟨bstmt⟩ ::= ⟨vdecl⟩ | ⟨assign⟩ | ⟨cond⟩ | ⟨while⟩ | ⟨for⟩ | ⟨expr⟩ | ⟨exit⟩
⟨tdecl⟩ ::= TYPE ID ⟨vdecls ⟩ END
⟨vdecls⟩ ::= ⟨vdecl ⟩ ⟨vdecls ⟩ | ε
⟨fdecl⟩ ::= FUN ( ⟨dtype ⟩ | NIL ) ID LPAREN ⟨params ⟩ RPAREN ⟨bstmts ⟩ END
⟨params⟩ ::= ⟨dtype⟩ ID ( COMMA ⟨dtype⟩ ID )∗ | ε
⟨dtype⟩ ::= INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
⟨exit⟩ ::= RETURN ( ⟨expr⟩ | ε )
⟨vdecl⟩ ::= VAR ( ⟨dtype⟩ | ε ) ID ASSIGN ⟨expr⟩
⟨assign⟩ ::= SET ⟨lvalue ⟩ ASSIGN ⟨expr ⟩
⟨lvalue⟩ ::= ID(DOTID)∗
⟨cond⟩ ::= IF ⟨expr ⟩ THEN ⟨bstmts ⟩ ⟨condt ⟩ END
⟨condt⟩ ::= ELIF ⟨expr ⟩ THEN ⟨bstmts ⟩ ⟨condt ⟩ | ELSE ⟨bstmts ⟩ | ε
⟨while⟩ ::= WHILE ⟨expr ⟩ DO ⟨bstmts ⟩ END
⟨for⟩ ::= FOR ID ASSIGN ⟨expr ⟩ TO ⟨expr ⟩ DO ⟨bstmts ⟩ END
⟨expr⟩ ::= ( ⟨rvalue⟩ | NOT ⟨expr⟩ | LPAREN ⟨expr⟩ RPAREN ) ( ⟨operator⟩ ⟨expr⟩ | ε )
⟨operator⟩ ::= PLUS | MINUS | DIVIDE | MULTIPLY | MODULO | AND | OR | EQUAL | LESS_THAN | GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
⟨rvalue⟩ ::= ⟨pval⟩ | NIL | NEW ID | ⟨idrval⟩ | NEG ⟨expr⟩
⟨pval⟩ ::= INT_VAL | DOUBLE_VAL | BOOL_VAL | CHAR_VAL | STRING_VAL 
⟨idrval⟩ ::= ID ( DOT ID )∗ | ID LPAREN ⟨exprlist⟩ RPAREN
⟨exprlist⟩ ::= ⟨expr⟩ ( COMMA ⟨expr⟩ )∗ | ε
```
