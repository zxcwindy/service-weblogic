grammar Filter;

// prog
//     : stat+
//     ;

// line : expr EOF ;

parse
    : expr EOF
    ;

expr
    : '(' expr ')'            # parenExpr
    | expr op=(MUL|DIV) expr  # MulDiv
    | expr op=(ADD|SUB) expr  # AddSub
    | expr '==' expr          # eqExpr
    | expr '>=' expr          # gtEqExpr
    | expr '<=' expr          # leEqExpr
    | expr '>' expr           # gtExpr
    | expr '<' expr           # leExpr
    | expr '&&' expr          # andExpr
    | expr '||' expr          # orExpr
    | INT                     # int
    | FLOAT                   # float
    | ID                      # id
    ;

MUL : '*' ;

DIV : '/' ;

ADD : '+' ;

SUB : '-' ;

ID  : DIGIT+ '.' ('30'|'d'|'w'|'m') '.' [a-zA-Z0-9]+ ;

INT : '-'?DIGIT+ ;

FLOAT : '-'?DIGIT+ '.' DIGIT*    // 匹配1. 39. 3.14159等等
     | '-'?'.' DIGIT+           // 匹配.1 .14159
     ;

fragment
DIGIT: [0-9] ;              // 匹配单个数字

WS  : [ \t\r\n]+ -> skip ;    // toss out whitespace
