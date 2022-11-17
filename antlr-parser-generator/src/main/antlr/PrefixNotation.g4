grammar PrefixNotation;
codeBlock: '(' codeBlock ')'
          | branch
          | assign
          | print
          ;
assign: '=' ID expression;
print: 'print' expression;
expression: compare | arithmetical | logical | single | '(' expression ')';
expressionLogical: logical | compare | singleLogical | '(' expressionLogical ')';
branch : 'if' expressionLogical codeBlock (elseBlock | elifBlock);
elifBlock : branch;
elseBlock: assignPrint |;
assignPrint : assign | print | '(' assignPrint ')';
single: BOOL | INT | singleVariable;
singleLogical: BOOL | singleVariable;
singleArithmetical: INT | singleVariable;
singleVariable: ID;
operandLogical: singleLogical | logical | compare | '(' operandLogical ')';
operandArithmetical: singleArithmetical | arithmetical | '(' operandArithmetical ')';
logical: SIGN_LOGICAL operandLogical (operandLogical |);
compare: SIGN_COMPARE operandArithmetical operandArithmetical;
arithmetical: SIGN_ARITHMETICAL operandArithmetical (operandArithmetical |);
SIGN_LOGICAL: '&&' | '||' | '!';
SIGN_ARITHMETICAL: '+' | '-' | '*' | '/';
SIGN_COMPARE: '<' | '>' | '<=' | '>=' | '==';
BOOL: 'True' | 'False';
INT: [1-9]*[0-9];
ID : [a-z]+;
WS : [ \t\r\n]+ -> skip;