grammar PrefixNotation;
code: '(' code ')'
          | if
          | assign
          | print
          ;
assign: '=' ID expression;
print: 'print' expression;
expression: compare | arithmetical | logical | single;
expression_logical: logical | compare | single_logical;
if : 'if' expression_logical code else_block;
else_block: code |;
single: BOOL | INT | ID | '(' single ')';
single_logical: BOOL | ID | '(' single_logical ')';
single_arithmetical: INT | ID | '(' single_arithmetical ')';
operand_logical: single_logical | logical | compare;
operand_arithmetical: single_arithmetical | arithmetical;
logical: '&&' operand_logical operand_logical
       | '||' operand_logical operand_logical
       | '!' operand_logical
       | '(' logical ')'
       ;
compare: '<' operand_arithmetical operand_arithmetical
       | '>' operand_arithmetical operand_arithmetical
       | '<=' operand_arithmetical operand_arithmetical
       | '>=' operand_arithmetical operand_arithmetical
       | '==' operand_arithmetical operand_arithmetical
       | '(' compare ')'
       ;
arithmetical: '+' operand_arithmetical operand_arithmetical
            | '-' operand_arithmetical operand_arithmetical
            | '*' operand_arithmetical operand_arithmetical
            | '/' operand_arithmetical operand_arithmetical
            | '(' arithmetical ')'
            ;
BOOL: 'True' | 'False';
INT: [1-9]*[0-9];
ID : [a-z]+;
WS : [ \t\r\n]+ -> skip;