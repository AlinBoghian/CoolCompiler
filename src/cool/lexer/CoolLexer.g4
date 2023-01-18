lexer grammar CoolLexer;

tokens { ERROR }

@header{
    package cool.lexer;
}

@members{
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

WS
    :   [ \n\f\r\t]+ -> skip
    ;


IF : 'if';
THEN : 'then';
ELSE : 'else';
FI: 'fi';

CASE: 'case';
OF: 'of';
ESAC: 'esac';

WHILE: 'while';
LOOP: 'loop';
POOL: 'pool';

LET : 'let';
IN : 'in';

CLASS: 'class';

INHERITS: 'inherits';

BOOL : 'true' | 'false';

NOT : 'not';

NEW : 'new';

ISVOID : 'isvoid';


STRING : '"' ('\\"' | ~('"' | '\n') | '\r' ~'\n' | ESCAPED_NEWLINE)*
        ('"' {
             String str = getText();
             if (str.length() >= 1024) {
                raiseError("String constant too long");
             }
             if (str.contains("\u0000")) {
                raiseError("String contains null character");
             }
        }
        | NEW_LINE {raiseError("Unterminated string constant");}
        | EOF {raiseError("EOF in string constant");});


ESCAPED_NEWLINE : '\\' NEW_LINE { setText(System.lineSeparator()); };

fragment LETTER : [a-zA-Z];
ID : (LETTER | '_')(LETTER | '_' | DIGIT)*;

fragment DIGIT : [0-9];
INT : DIGIT+;

fragment DIGITS : DIGIT+;

AT : '@';

PERIOD : '.';

SEMI : ';';

COLON : ':';

COMMA : ',';

ASSIGN : '<-';

BRANCH : '=>';

LPAREN : '(';

RPAREN : ')';

LBRACE : '{';

RBRACE : '}';


PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

EQUAL : '=';

LT : '<';

LE : '<=';

COMPL: '~';

fragment NEW_LINE : '\r'? '\n';

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
    : '(*'
      (BLOCK_COMMENT | . | NEW_LINE)*?
      ('*)' | (EOF { raiseError("EOF in comment"); }))
    ;

UMATCHED_COMMENT : '*)' {raiseError("Unmatched *)");};

INVALID : . {raiseError("Invalid character: " + getText());} ;
