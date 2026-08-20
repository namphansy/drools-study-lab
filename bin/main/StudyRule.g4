grammar StudyRule;

ruleExpression
    : condition (logicalOperator condition)* EOF
    ;

condition
    : IDENTIFIER operator value
    ;

logicalOperator
    : 'AND'
    | 'OR'
    ;

operator
    : '=='
    | '!='
    | '>'
    | '>='
    | '<'
    | '<='
    ;

value
    : NUMBER
    | STRING
    | IDENTIFIER
    ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

STRING
    : '"' (~["\r\n])* '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
