grammar SimpleBoolean;

parse
    : expr EOF
    ;

expr
    : LPAREN expr RPAREN                            #parenExpr
    | NOT expr                                      #notExpr
    | left=expr op=comparator right=expr            #comparatorExpr
    | left=expr op=binary right=expr                #binaryExpr
    | ID                                            #idExpr
    | DECIMAL                                       #decimalExpr
    ;

comparator
    : GT | GE | LT | LE | EQ
    ;

binary
    : AND | OR
    ;

bool
    :TRUE | FALSE
    ;

AND      : 'AND' ;
OR       : 'OR' ;
NOT      : 'NOT' ;
TRUE     : 'true' ;
FALSE    : 'false' ;
GT       : '>' ;
GE       : '>=' ;
LT       : "<" ;
LE       : '<=' ;
EQ       : '=' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
ID       : [0-9]+ '.' [a-zA-Z]+ ;
DECIMAL  : '-'?[0-9]+ ( '.' [0-9]+ )? ;
WS       : [ \t\r\n]+ -> skip ;
