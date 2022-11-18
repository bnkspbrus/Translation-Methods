grammar Grammatics;

grammatics: header ruleLexer* ruleParser (ruleParser | ruleLexer)*;
header: 'grammar' TERMINAL ';';
ruleParser: NONTERMINAL ':' sequense ('|' sequense)* ';';
sequense: (TERMINAL | NONTERMINAL)+;
ruleLexer: TERMINAL ':' REGEXPR ';';
REGEXPR: ['](~['])*['];
NONTERMINAL: [a-z][a-zA-Z]*;
TERMINAL: [A-Z][a-zA-Z]*;
WHITESPACES : [ \t\r\n]+ -> skip;