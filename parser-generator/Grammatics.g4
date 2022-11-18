grammar Grammatics;

grammatics: header (parserRule | lexerRule)* parserRule (parserRule | lexerRule)*;
header: 'grammar' TERMINAL ';';
parserRule: NON_TERMINAL ':' (TERMINAL | NON_TERMINAL)+ ';';
lexerRule: TERMINAL ':' REG_EXPR ';';
REG_EXPR: ['](~['])*['];
NON_TERMINAL: [a-z][a-zA-Z]*;
TERMINAL: [A-Z][a-zA-Z]*;
WHITESPACES : [ \t\r\n]+ -> skip;