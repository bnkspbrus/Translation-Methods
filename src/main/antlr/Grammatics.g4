grammar Grammatics;

grammatics: header treeMembers? ruleLexer* ruleParser (ruleParser | ruleLexer)*;
treeMembers: '@' 'tree' '::' 'members' CODE;
header: 'grammar' TERMINAL ';';
ruleParser: NONTERMINAL ':' alternative ('|' alternative)* ';';
alternative: sequence CODE? |;
sequence: (TERMINAL | NONTERMINAL)+;
ruleLexer: TERMINAL ':' REGEXPR ';';
REGEXPR: ["].*?["];
NONTERMINAL: [a-z][a-zA-Z]*;
TERMINAL: [A-Z][a-zA-Z]*;
WHITESPACES : [ \t\r\n]+ -> skip;
CODE: '{' .*? '}';