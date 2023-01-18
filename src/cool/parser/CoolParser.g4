parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program : (comment=BLOCK_COMMENT | (class SEMI))+
    ;

class : CLASS type=ID (INHERITS parent=ID)? LBRACE (feats+=feature SEMI)* RBRACE;

feature : name=ID LPAREN (args+=formal (COMMA args+=formal)*)? RPAREN COLON type=ID LBRACE body=expr RBRACE # method
        |  var_decl                                                                          # class_var
        ;

formal : name=ID COLON type=ID;

var_decl: name=ID COLON type=ID (ASSIGN init=expr)?;

case_branch: name=ID COLON type=ID BRANCH body=expr SEMI;


expr
    : STRING                                                                                                # string
    | name=ID LPAREN (params+=expr (COMMA params+=expr)*)? RPAREN                                           # impldispatch
    | object=expr (AT static_method=ID)? PERIOD name=ID LPAREN (params+=expr (COMMA params+=expr)*)? RPAREN # expldispatch
    | COMPL e=expr                                                                                          # compl
    | ISVOID e=expr                                                                                         # isvoid
    | left=expr op=(MULT | DIV) right=expr                                                                  # multDiv
    | left=expr op=(PLUS | MINUS) right=expr                                                                # plusMinus
    | left=expr op=(LT | LE | EQUAL) right=expr                                                             # relational
    | NOT e=expr                                                                                            # not
    | NEW type=ID                                                                                           # new
    | CASE main=expr OF (branches+=case_branch)+ ESAC                                                       # case
    | LET vars+=var_decl (COMMA (vars+=var_decl))* IN body=expr                                             # let
    | LBRACE (expressions+=expr SEMI)+ RBRACE                                                               # block
    | WHILE cond=expr LOOP body=expr POOL                                                                   # while
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                                             # if
    | name=ID ASSIGN expr                                                                                   # assignment
    | LPAREN e=expr RPAREN                                                                                  # paren_expr
    | ID                                                                                                    # variable
    | (INT | BOOL)                                                                                          # intBool
    ;

