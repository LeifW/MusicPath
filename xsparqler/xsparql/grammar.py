# -*- coding: utf-8 -*-
#
#
# xsparql -- XSPARQL Rewriter
#
# Copyright (C) 2007-2009  Nuno Lopes  <nuno.lopes@deri.org>
#                          Thomas Krennwallner  <tkren@kr.tuwien.ac.at>
#                          Waseem Akthar  <waseem.akthar@deri.org>
#                          Axel Polleres  <axel.polleres@deri.org>
#
# This file is part of xsparql.
#
# xsparql is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# xsparql is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with xsparql. If not, see
# <http://www.gnu.org/licenses/>.
#
#


import ply.lex as lex
import ply.yacc as yacc
import sys
import re
import debug

# our rewriting functions
import lifrewriter
import lowrewriter


# print parser debug info
debugInfo = False

#
# XSPARQL Grammar begin
#

# =======================================================================
# ply lexer
# =======================================================================


def recognize(tok):
    debug.recognize(tok)
    return tok

def begin(token, state):
    debug.display('Entering state: ' + state)
    token.lexer.begin(state)
    pass

def push_state(token, state):
    debug.display('Pushing state: ' + state)
    token.lexer.push_state(state)
    pass

def pop_state(token):
    debug.display('Poping state: ')
    token.lexer.pop_state()
    pass

# reserved keywords
reserved = {
   'a'  : 'A',
   'is' : 'IS',
   'eq' : 'EQ',
   'ne' : 'NE',
   'lt' : 'LT',
   'ge' : 'GE',
   'le' : 'LE',
   'gt' : 'GT',
   'for' : 'FOR',
   'from' : 'FROM',
   'limit' : 'LIMIT',
   'offset' : 'OFFSET',
   'let' : 'LET',
   'order' : 'ORDER',
   'by' : 'BY',
   'at' : 'ATS',
   'in' : 'IN',
   'as' : 'AS',
   'descending' : 'DESCENDING',
   'ascending' : 'ASCENDING',
   'stable' : 'STABLE',
   'if' : 'IF',
   'then' : 'THEN',
   'else' : 'ELSE',
   'return' : 'RETURN',
   'construct' : 'CONSTRUCT',
   'where' : 'WHERE',
   'greatest' : 'GREATEST',
   'least' : 'LEAST',
   'collation' : 'COLLATION',
   'child' : 'CHILD',
   'descendant' : 'DESCENDANT',
   'attribute' : 'ATTRIBUTE',
   'self' : 'SELF',
   'descendant-or-self' : 'DESCENDANTORSELF',
   'following-sibling' : 'FOLLOWINGSIBLING',
   'following' : 'FOLLOWING',
   'parent' : 'PARENT',
   'ancestor' : 'ANCESTOR',
   'preceding-sibling' : 'PRECEDINGSIBLING',
   'preceding' : 'PRECEDING',
   'ancestor-or-self' : 'ANCESTORORSELF',
   'ordered' : 'ORDERED',
   'unordered' : 'UNORDERED',
   'declare' : 'DECLARE',
   'namespace' : 'NAMESPACE',
   'default' : 'DEFAULT',
   'element' : 'ELEMENT',
   'function' : 'FUNCTION',
   'base-uri' : 'BASEURI',
   'prefix' : 'PREFIX',
   'base' : 'BASE',
   'and' : 'AND',
   'or' : 'OR',
   'to' : 'TO',
   'div' : 'DIV',
   'idiv' : 'IDIV',
   'mod' : 'MOD',
   'union' : 'UNION',
   'intersect' : 'INTERSECT',
   'except' : 'EXCEPT',
   'instance' : 'INSTANCE',
   'treat' : 'TREAT',
   'castable' : 'CASTABLE',
   'cast' : 'CAST',
   'variable' : 'VARIABLE',
   'external' : 'EXTERNAL',
   'of' : 'OF',
   'empty-sequence' : 'EMPTYSEQUENCE',
   'item' : 'ITEM',
   'node' : 'NODE',
   'document-node' : 'DOCUMENTNODE',
   'text' : 'TEXT',
   'comment' : 'COMMENT',
   'processing-instruction' : 'PROCESSINGINSTRUCTION',
   'schema-attribute' : 'SCHEMAATTRIBUTE',
   'schema-element' : 'SCHEMAELEMENT',
   'document' : 'DOCUMENT',
   'named' : 'NAMED',
   'optional' : 'OPTIONAL',
   'filter' : 'FILTER',
   'str': 'STR',
   'lang' : 'LANG',
   'langmatches' : 'LANGMATCHES',
   'datatype' : 'DATATYPE',
   'bound' : 'BOUND',
   'isIRI' : 'isIRI',
   'isURI' : 'isURI',
   'isBLANK' : 'isBLANK',
   'isLITERAL' : 'isLITERAL',
   'regex' : 'REGEX',
   'true': 'TRUE',
   'false' : 'FALSE',
   'graph': 'GRAPH'
}


# lexer tokens
tokens = [
    'VAR', 'STARTELM', 'ENDELM', 'INTEGER', 'LCURLY', 'RCURLY', 'NCNAME', 'QSTRING', 'DOT', 'AT',
    'CARROT', 'COLON', 'COMMA', 'SLASH', 'LBRACKET', 'RBRACKET', 'LPAR', 'RPAR', 'SEMICOLON',
    'STAR', 'DOTDOT', 'SLASHSLASH','LESSTHAN', 'GREATERTHAN',  'PLUS', 'MINUS', 'UNIONSYMBOL', 'QUESTIONMARK',
    'LESSTHANLESSTHAN', 'GREATERTHANEQUALS', 'LESSTHANEQUALS', 'HAFENEQUALS', 'EQUALS', 'COLONCOLON',
    'STAR_COLON_NCNAME', 'NCNAME_COLON_STAR', 'BNODE', 'BNODE_CONSTRUCT', 'IRI_CONSTRUCT',
    'PREFIXED_NAME', 'UNPREFIXED_NAME', 'PREFIXED_COLON', 'CDATASTART', 'CDATAELMEND', 
    'ORSYMBOL', 'ANDSYMBOL', 'NOT', 'WHITESPACE', 'IRIREF', 'NCNAMEELM', 'ENDTAG'
    ] + reserved.values()


# lexer states
states = [
   ('xmlElementContents','exclusive'),
   ('xmlStartTag','inclusive'),
   ('xmlEndTag','inclusive'),
   ('cdata', 'exclusive')
]

precedence = (
    ('left', 'SLASH','LPAR','GREATERTHAN'),
    ['right'] + reserved.values()
)



from ply.lex import TOKEN



# # NCName will be replaced with SPARQL's PN_PREFIX
# http://www.w3.org/TR/REC-xml-names/#NT-NCName
# # remove leading _
# NCNameStartChar   =   r'([A-Za-z])'
# NCNameChar        =   r'([A-Za-z]|[0-9]|\.|-|_)'
# NCName            =   r'('+NCNameStartChar+')('+NCNameChar+')*'


# http://www.w3.org/TR/rdf-sparql-query/#rPrefixedName
PN_CHARS_BASE    =       r'([A-Za-z])'    #  = NCNameStartChar
# remove leading _
PN_CHARS_U       =       r'('+PN_CHARS_BASE+'|_)'
PN_CHARS         =       r'('+PN_CHARS_U+'|-|[0-9])'  # = NCNameChar - '.'
PN_PREFIX        =       r''+PN_CHARS_BASE+'(('+PN_CHARS+'|\.)*'+PN_CHARS+')?' # = NCName
PN_LOCAL         =       r'(('+PN_CHARS_U+')|[0-9])(('+PN_CHARS+'|\.)*'+PN_CHARS+')?'


PREFIXED_NAME = r''+PN_PREFIX+':'+PN_LOCAL
PREFIXED_COLON = r'('+PN_LOCAL+')?:\ '    # space to avoid preceding::
UNPREFIXED_NAME = r':'+PN_LOCAL


bnode           =      r'_:(' + PN_PREFIX + ')'
bnode_construct =      r'_:(' + PN_PREFIX + ')?\{'
iri_construct =  r'\}:' + PN_PREFIX


iri_prefix = r'(aaa|aaas|acap|cap|cid|crid|data|dav|dict|dns|fax|file|ftp|go|gopher|h323\
|http|https|icap|im|imap|info|ipp|iris|iris.beep|iris.xpc|iris.xpcs|iris.lws\
|ldap|mailto|mid|modem|msrp|mtqp|mupdate|news|nfs|nntp|opaquelocktoken|pop\
|pres|prospero|rtsp|service|shttp|sip|sips|snmp|soap.beep|tag|tel|telnet\
|tftp|thismessage|tip|tv|urn|vemmi|wais|xmlrpc.beep|xmlrpc.beep|xmpp|z39.50r\
|z39.50s|about|adiumxtra|aim|afp|aw|bolo|callto|chrome|cvs|ed2k|feed|fish|gg\
|gizmoproject|iax2|irc|ircs|itms|jar|javascript|keyparc|lastfm|ldaps|magnet\
|mms|msnim|mvn|notes|psyc|paparazzi:http|rmi|rsync|secondlife|sgn|skype|ssh\
|sftp|smb|sms|soldat|steam|svn|teamspeak|unreal|ut2004|ventrilo|view-source\
|webcal|wtai|wyciwyg|xfire|xri|ymsgr)'

iri_body = r'([^<>\"\'\{\}\|\^`\x00-\x20])*'
iri      = r'\<'+iri_prefix+'(:|\.)'+iri_body+'\>'

@TOKEN(iri)
def t_IRIREF(t):
    return recognize(t)




def t_ANY_CDATASTART(t):
    r'<!\[CDATA\['
    push_state(t,'cdata')
    return recognize(t)



startelm   = r'\<([^\/<>\'\{\}\|\^`\x00-\x20])+'
endelm   = r'\</([^<>\'\{\}\|\^`\x00-\x20])+'


@TOKEN(startelm)
def t_STARTELM(t):
    r = recognize(t)
    begin(t, 'xmlStartTag')
    return r



def t_xmlStartTag_GREATERTHAN(t):
    r'>'
    r = recognize(t)
    begin(t, 'xmlElementContents')
    return r

def t_xmlStartTag_ENDTAG(t):
    r'/>'
    r = recognize(t)
    begin(t, 'INITIAL')
    return r


# def t_xmlElementContents_ENDTAG(t):
#     r'</'
#     r = recognize(t)
#     begin(t, 'xmlEndTag')
#     return r



# finding a startelm '<' will end the elementContent state
@TOKEN(startelm)
def t_INITIAL_xmlElementContents_STARTELM(t):
    r = recognize(t)
    begin(t, 'xmlStartTag')
    return r

@TOKEN(endelm)
def t_INITIAL_xmlElementContents_ENDELM(t):
    r = recognize(t)
    begin(t, 'xmlEndTag')
    return r

def t_xmlElementContents_WHITESPACE(t):
    r'([ |\t])+'
    return recognize(t)

#@TOKEN(PN_PREFIX)
def t_xmlElementContents_NCNAMEELM(t):
    r'[^{<\n]+'
    return recognize(t)

def t_xmlEndTag_GREATERTHAN(t):
    r'>'
    r = recognize(t)
    begin(t, 'INITIAL')
    return r

@TOKEN(bnode_construct)
def t_BNODE_CONSTRUCT(t):
    r = recognize(t)
    push_state(t, 'INITIAL')
    return r

@TOKEN(iri_construct)
def t_IRI_CONSTRUCT(t):
    return recognize(t)

@TOKEN(bnode)
def t_BNODE(t):
    return recognize(t)

@TOKEN(PREFIXED_NAME)
def t_PREFIXED_NAME(t):
    return recognize(t)

@TOKEN(UNPREFIXED_NAME)
def t_UNPREFIXED_NAME(t):
    return recognize(t)

@TOKEN(PREFIXED_COLON)
def t_PREFIXED_COLON(t):
    return recognize(t)


def t_INITIAL_xmlElementContents_xmlStartTag_xmlEndTag_RCURLY(t):
    r'}'
    r = recognize(t)
    pop_state(t)
    return r

def t_INITIAL_xmlElementContents_xmlStartTag_xmlEndTag_LCURLY(t):
    r'{'
    r = recognize(t)
    push_state(t, 'INITIAL')
    return r

def t_cdata_CDATAELMEND(t):
    r'(.|\n|\t|\r)*\]\]>'
    pop_state(t)
    return recognize(t)

# takes care of keywords and IRIs
@TOKEN(PN_PREFIX)
def t_NCNAME(t):
    # an NCNAME cannot start with a digit: http://www.w3.org/TR/REC-xml-names/#NT-NCName
    t.type = reserved.get(t.value,'NCNAME')
    return recognize(t)



star_ncname =          r'\*( |\t)*:( |\t)*(' + PN_PREFIX + ')'
ncname_star =          r'('+PN_PREFIX+')( |\t)*:( |\t)*\*'

@TOKEN(star_ncname)
def t_STAR_COLON_NCNAME(t):
    return recognize(t)

@TOKEN(ncname_star)
def t_NCNAME_COLON_STAR(t):
    return recognize(t)




t_SLASH = r'/'
t_SLASHSLASH = r'//'
t_LBRACKET = r'\['
t_RBRACKET = r'\]'
t_LPAR = r'\('
t_RPAR = r'\)'
t_SEMICOLON = r';'
t_INITIAL_QSTRING = r'\"[^\"]*\"'



# t_LCURLY  = r'{'
# t_RCURLY = r'}'

# remove leading _
var = '[\$][a-zA-Z][a-zA-Z0-9\_\-]*'


@TOKEN(var)
def t_VAR(t):
    return recognize(t)


def t_GREATERTHAN(t):
    r'>'
#    t.lexer.push_state('elementContent')
    return recognize(t)

t_INTEGER   = r'[0-9]+'
t_DOT       = r'\.' # PLY 2.2 does not like . to be a literal
t_AT        = r'@'
t_CARROT    = r'\^'
t_COLON     = r'\:'
t_COLONCOLON = r'\:\:'
t_COMMA     = r'\,'
t_EQUALS    = r'='
t_STAR    = r'\*'
t_DOTDOT    = r'\.\.'
t_LESSTHAN = r'<'
#t_GREATERTHAN = r'>'
t_PLUS = r'\+'
t_MINUS = r'\-'
t_UNIONSYMBOL = r'\|'
t_ANDSYMBOL = r'&&'
t_ORSYMBOL = r'\|\|'
t_QUESTIONMARK = r'\?'
t_LESSTHANLESSTHAN = r'\<\<'
t_GREATERTHANEQUALS = r'\>\='
t_LESSTHANEQUALS = r'\<\='
t_HAFENEQUALS = r'\!\='
t_NOT = r'\!'



curly_brackets = 0




## ------------------------------ Comments

def t_INITIAL_comments_SCOM(t):
    r'\#.*'
    pass

t_xmlElementContents_cdata_ignore = ""

t_xmlStartTag_xmlEndTag_ignore = " \r"

## -------------

# Ignored characters
t_INITIAL_ignore = " \t"

# newlines increase line numbers (and will be ignored)
def t_ANY_newline(t):
    r'(\r?\n)+'
    t.lexer.lineno += t.value.count("\n")

# illegal characters will end up here
def t_ANY_error(t):
    col = find_column(t)
    sys.stderr.write("Illegal character: '" + t.value[0] + "' at line "+ `t.lineno` + ', column '+ `col` + '\n')
    raise SyntaxError



# Build the lexer
# all keywords lowercase
lex.lex(debug=0)




# =======================================================================
# ply parser
# =======================================================================


# namespace list
namespaces = []
prefix_namespaces = []
decl_var_ns = ''
count = 0
nsFlag = False

letVars = []

## --------------------------------------------------------- main

## first come, first serve
def p_mainModule(p):
    '''mainModule : prolog queryBody'''

    global prefix_namespaces

    prefix =  'import module namespace _xsparql = "http://xsparql.deri.org/XSPARQLer/xsparql.xquery"\n'
    prefix += 'at "../xsparql.xquery";\n'
    prefix += 'declare namespace _sparql_result = "http://www.w3.org/2005/sparql-results#";\n'
    prefix += 'declare namespace mp = "http://musicpath.org/ns/xsparql-fn#";\n\n'
    prefix += lowrewriter.print_namespaces(prefix_namespaces)

    p[0] = prefix + ''.join(p[1:])


## --------------------------------------------------------- prolog

def p_prolog(p):
    '''prolog : xqueryNS prolog
  	      | sparqlNS prolog
              | xqueryFunction prolog
	      | xqueryVarDeclaration prolog
	      | empty'''
    p[0] = '\n'.join(p[1:])


def p_xqueryFunction(p):
    '''xqueryFunction : DECLARE FUNCTION qname LPAR paramList RPAR enclosedExpr SEMICOLON'''
    p[0] = ' '.join(p[1:])

def p_xqueryVarDeclaration(p):
    '''xqueryVarDeclaration : DECLARE VARIABLE VAR typeDeclaration EXTERNAL SEMICOLON'''
    global letVars
    letVars += [p[3]]
    p[0] = ' '.join(p[1:])

def p_paramList(p):
    '''paramList : param COMMA paramList
                 | param
                 | empty'''
    p[0] = ' '.join(p[1:])

def p_param(p):
    '''param : VAR'''
    p[0] = p[1]



def p_xqueryNS(p):
    '''xqueryNS : defaultNamespaceDecl SEMICOLON
		| namespaceDecl SEMICOLON
		| baseURIDecl SEMICOLON'''
    p[0] = ' '.join(p[1:])


def p_sparqlNS(p):
    '''sparqlNS : directive  '''
    p[0] = ''.join(p[1:])


def p_directive(p):
    '''directive : prefixID
		 | sbase'''
    p[0] = ''.join(p[1:])


def p_prefixID(p):
    '''prefixID : PREFIX prefixIDs'''
    p[0] = ''


# def p_IRIREF(p):
#     '''IRIREF : STARTELM GREATERTHAN'''
#     p[0] = ''.join(p[1:])


def p_prefixIDs0(p):
    '''prefixIDs :  PREFIXED_COLON IRIREF
		 |  qname PREFIXED_COLON IRIREF'''
    global count
    global decl_var_ns

    global prefix_namespaces

    count += 1
    if len(p) == 4 :
        prefix = p[1].rstrip(': ')
        url = ''.join(p[3])
    elif len(p) == 3:
        prefix = p[1].rstrip(': ')
        url = ''.join(p[2])

    # save 'prefix' namespaces
    prefix_namespaces.append(('prefix', prefix, ':', url))

    col = ':'
    nsTag = 'prefix'
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)
    p[0] = ''


def p_prefixIDs1(p):
    '''prefixIDs :  qname COLON IRIREF
		 |  COLON IRIREF'''
    global count
    global decl_var_ns

    global prefix_namespaces

    count += 1
    if len(p) == 4 :
	prefix = ''.join(p[1])
	url = ''.join(p[3])
    elif len(p) == 3:
	prefix = ''
	url = ''.join(p[2])

    # save 'prefix' namespaces
    prefix_namespaces.append(('prefix', prefix, ':', url))

    col = ':'
    nsTag = 'prefix'
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)
    p[0] = ''




def p_sbase(p):
    '''sbase : BASE IRIREF'''
    global count
    global decl_var_ns

    count += 1
    prefix = ''
    url = ''.join(p[2])
    col = ''
    nsTag = 'base'
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)
    p[0] = ''



def p_defaultNamespaceDecl(p):
    '''defaultNamespaceDecl :  DECLARE DEFAULT defaultNamespaceDecls'''
    p[0] = ' '.join(p[1:])


def p_defaultNamespaceDecls(p):
    '''defaultNamespaceDecls :  ELEMENT NAMESPACE QSTRING
			     |  FUNCTION NAMESPACE QSTRING'''
    global namespaces
    global count
    global decl_var_ns
    namespaces.append(('prefix', '',':', p[3]))
    count += 1
    prefix = ''
    nsTag = 'prefix'
    col = ':'
    url = ''.join(p[3])
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)
    p[0] = ' '.join(p[1:])


def p_namespaceDecl(p):
    '''namespaceDecl : DECLARE NAMESPACE qname EQUALS QSTRING'''
    global namespaces
    global count
    global decl_var_ns

    count += 1
    namespaces.append(('prefix', p[3], ':', p[5]))
    prefix = ''.join(p[3])
    url = ''.join(p[5])
    col = ':'
    nsTag = 'prefix'
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)

    p[0] = ' '.join(p[1:])


def p_baseURIDecl(p):
    '''baseURIDecl  : DECLARE BASEURI QSTRING'''
    global namespaces
    global count
    global decl_var_ns
    namespaces.append(('base', '', '', p[2]))
    count += 1
    prefix = ''
    col = ''
    nsTag = 'base'
    url = ''.join(p[3])
    decl_var_ns += lowrewriter.declare_namespaces(nsTag, col, prefix, url, count)
    p[0] = ' '.join(p[1:])


## --------------------------------------------------------- queryBody
# xqilla and saxon behave differently in NodeType variables, it seems
# like name($x_Node/*) does not work as intended
def p_queryBody(p):
    '''queryBody : expr'''
    global nsFlag

    nsVars = lowrewriter.cnv_lst_str(lowrewriter.dec_var, True)
    if nsVars != '' and nsFlag:
	p[0] = decl_var_ns + '\n _xsparql:_serialize(( ' + nsVars + ', "\n" )),\n' + p[1]
    else:
	p[0] = decl_var_ns + p[1]


## ------------------------------ Expressions

def p_expr(p):
    '''expr : expr COMMA exprSingle
	    | exprSingle'''
    if len(p) == 2:
	p[0] = ''.join(p[1][0])
    else:
	p[0] = ''.join(p[1]+' '+p[2]+' '+p[3][0])


def p_enclosedExpr(p):
    '''enclosedExpr : LCURLY expr RCURLY'''
    p[0] = ' '.join(p[1:])


def p_enclosedExprEmpty(p):
    '''enclosedExprEmpty : enclosedExpr
                         | LCURLY RCURLY'''
    p[0] = ' '.join(p[1:])


def p_exprSingle(p):
    '''exprSingle : flworExpr
		  | constructQuery
		  | orExpr
		  | ifExpr'''
    p[0] = p[1]


## ------------------------------ constructQuery / SPARQL

def p_constructQuery(p):
    '''constructQuery : CONSTRUCT constructTemplate datasetClauses WHERE groupGraphPattern solutionmodifier'''
    global nsFlag
    nsFlag = True
    p[0] = (''.join([ r  for r in lowrewriter.buildConstruct(p[2], p[3], p[5][1], p[6], p[5][2])]), [], [])


def p_datasetClauses(p): # list of (from, iri) tuples
    '''datasetClauses : datasetClauses datasetClause
		      | empty'''
    if len(p) == 3:
	p[0] = p[1] + [ p[2] ]
    else: # empty
	p[0] = []


def p_datasetClause(p):
    '''datasetClause : FROM IRIREF
		     | FROM VAR
		     | FROM NAMED IRIREF'''
    if len(p) == 4: # from named
	p[0] = (p[1] + ' ' + p[2], p[3])
    elif len(p) == 3: # from
	p[0] = (p[1], p[2])



## ---------------------------------------------------- ifExpr

def p_ifExpr(p):
    '''ifExpr : IF LPAR expr RPAR THEN exprSingle ELSE exprSingle'''
    p[0] = (p[1]+' '+p[2]+' '+p[3]+' '+p[4]+' '+p[5]+' '+p[6][0]+' '+p[7]+' '+p[8][0], [], [])


## ---------------------------------------------------- flworExpr / Xquery
# http://www.w3.org/TR/xquery/#prod-xquery-FLWORExpr

def p_flworExpr0(p):
    '''flworExpr : flworExprs CONSTRUCT constructTemplate'''
    global nsFlag
    nsFlag = True
    p[0] = (''.join([ r  for r in lifrewriter.build_rewrite_query(p[1][0], p[1][3], p[2], p[3], p[1][2], p[1][1])]), p[1][2], p[1][2])

def p_flworExpr1(p):
    '''flworExpr : flworExprs RETURN exprSingle'''
    p[0] = ( p[1][0] + p[1][3] + ' '+p[2]+' '+p[3][0], p[1][1], p[1][2] )




def p_flworExprs(p):
    '''flworExprs : forletClauses
		  | forletClauses whereClause
		  | forletClauses orderByClause
		  | forletClauses whereClause orderByClause'''
    if len(p) == 2:
	p[0] = ( p[1][0], p[1][1], p[1][2], "" )
    else:
	p[0] = ( p[1][0] ,  p[1][1], p[1][2], '\n'.join(p[2:]) )


# todo: collect variables in let, and build up triples of (expr, variables in scope, position variables in scope)

def p_forletClauses(p):
    '''forletClauses : forletClauses forletClause
		     | forletClause'''
    if (len(p) == 3):
	p[0] = ( p[1][0] + p[2][0], p[1][1] + p[2][1], p[1][2] + p[2][2] )
    else:
	p[0] = p[1]



def p_forletClause0(p):
    '''forletClause : forClause'''
    p[0] = ( p[1][0] , [], p[1][2]  )  # example nasty nasty

def p_forletClause1(p):
    '''forletClause : letClause'''
    p[0] = ( p[1][0] , [], p[1][2]  ) # FIXME: add bound and position variables # @todo: check if OK!

def p_forletClause2(p):
    '''forletClause : sparqlForClause'''
    p[0] = ( p[1][0] , p[1][1], p[1][2]  ) # FIXME: add bound and position variables



# ----------------------------------------------

def p_sparqlForClause(p):
    '''sparqlForClause : FOR sparqlvars datasetClauses WHERE groupGraphPattern solutionmodifier'''
    p[0] = (''.join([ r  for r in lowrewriter.build(p[2][1], p[3], p[5][1], p[6], p[5][2] )]), p[2][1], p[2][2] )



def p_sparqlvars(p):
    '''sparqlvars : VAR sparqlvars
		  | VAR
		  | STAR'''
    if len(p) == 3:
	p[0] = (p[1] + ' ' + p[2][0], [ p[1] ] + p[2][1], [] )
    else:
	p[0] = ( p[1] , [ p[1] ] , [])


# ----------------------------------------------


def p_forClause(p):
    '''forClause : FOR forVars'''
    p[0] = (p[1] + ' ' + p[2][0], p[2][1], p[2][2] )



def p_forVars0(p):
    '''forVars : forVars COMMA forVar'''
    p[0] = ( p[1][0] + p[2] + ' \n ' + p[3][0] , p[1][1] + [ p[3][1] ], p[1][2] + [ p[3][2] ] )

def p_forVars1(p):
    '''forVars : forVar'''
    p[0] = ( p[1][0], [ p[1][1] ] , [ p[1][2] ] )



def p_forVar(p):
    '''forVar : VAR typeDeclaration positionVar IN exprSingle'''
    if len(p[3][1]) == 0:
        prefix_var = lowrewriter.prefix_var(p[1])
        p[0] = ( p[1] + ' at ' + prefix_var + '_Pos ' + p[2] + ' ' + p[4] + ' ' + p[5][0], p[1], prefix_var + '_Pos' )
    else:
	p[0] = (p[1] + ' ' + p[2] + ' ' + p[3][0] + ' ' + p[4] + ' ' + p[5][0], p[1], p[3][1] )



def p_letClause(p):
    '''letClause : LET letVars'''
    global letVars
    letVars += p[2][1]
    p[0] = ('\n' + p[1] + ' ' + p[2][0], p[2][1], p[2][2] )



def p_letVars(p):
    '''letVars : letVars COMMA letVar
	       | letVar'''
    if len(p) == 2:
	p[0] = ( p[1][0] + '\n', [ p[1][1] ] , [ p[1][2] ] ) 
    else:
	p[0] = ( p[1][0] + p[2] + ' \n ' + p[3][0] , p[1][1] + [ p[3][1] ], p[1][2] + [ p[3][2] ] )



def p_letVar(p):
    '''letVar : VAR typeDeclaration COLON EQUALS exprSingle'''
    p[0] = (p[1] + ' ' + p[2] + ' ' + p[3] + p[4] + ' ' + p[5][0],  p[1], [])



####################



def p_constructor(p):
    '''constructor : directConstructor
		   | computedConstructor'''
    p[0] = ' '.join(p[1:])


def p_computedConstructor(p):
    '''computedConstructor : compDocConstructor
			   | compElemConstructor
			   | compAttrConstructor
			   | compTextConstructor
			   | compCommentConstructor
			   | compPIConstructor'''
    p[0] = ' '.join(p[1:])


def p_compDocConstructor(p):
    '''compDocConstructor : DOCUMENT enclosedExpr'''
    p[0] = ' '.join(p[1:])


def p_compElemConstructor(p):
    '''compElemConstructor : ELEMENT qname enclosedExprEmpty 
			   | ELEMENT enclosedExpr enclosedExprEmpty'''
    p[0] = ' '.join(p[1:])



def p_compAttrConstructor(p):
    '''compAttrConstructor : ATTRIBUTE qname enclosedExprEmpty
			   | ATTRIBUTE enclosedExpr enclosedExprEmpty'''
    p[0] = ' '.join(p[1:])


def p_compTextConstructor(p):
    '''compTextConstructor : TEXT enclosedExpr'''
    p[0] = ' '.join(p[1:])


def p_compCommentConstructor(p):
    '''compCommentConstructor : COMMENT enclosedExpr'''
    p[0] = ' '.join(p[1:])


def p_compPIConstructor(p):
    '''compPIConstructor : PROCESSINGINSTRUCTION qname enclosedExprEmpty
			 | PROCESSINGINSTRUCTION enclosedExpr enclosedExprEmpty'''
    p[0] = ' '.join(p[1:])


def p_directConstructor(p):
    '''directConstructor : directElemConstructor'''
    p[0] = ' '.join(p[1:])


# [96]    DirElemConstructor    ::=    "<" QName DirAttributeList ("/>" | (">" DirElemContent* "</" QName S? ">"))
def p_directElemConstructor(p):
    '''directElemConstructor : directElemConstructorElm directElemConstructor  
                             | directElemConstructorElm'''
    p[0] = ''.join(p[1:])

def p_directElemConstructorElm(p):
    '''directElemConstructorElm : STARTELM directAttributeList SLASH GREATERTHAN 
                                | STARTELM directAttributeList ENDTAG
                                | STARTELM directAttributeList GREATERTHAN
                                | STARTELM GREATERTHAN
                                | STARTELM SLASH GREATERTHAN
                                | ENDELM GREATERTHAN
                                | WHITESPACE
                                | NCNAMEELM
                                | CDATASTART CDATAELMEND
			        | enclosedExpr'''
    p[0] = ''.join(p[1:])


def p_directAttributeList(p):
    '''directAttributeList : directAttribute directAttributeList
			   | empty'''
    if len(p) == 3:
	p[0] = ' ' + ''.join(p[1:])
    else:
	p[0] = ''


def p_directAttribute(p):
    '''directAttribute :  qname EQUALS directAttributeValue'''
    p[0] = ' '.join(p[1:])


def p_directAttributeValue(p):
    '''directAttributeValue :  attributeValueContent '''
    p[0] = ''.join(p[1:])


def p_attributeValueContent(p):
    '''attributeValueContent : enclosedExpr
			     | QSTRING'''
    p[0] = ''.join(p[1:])



## ------------------------------ solutionmodifier


def p_solutionmodifier(p):
    '''solutionmodifier : orderclause limitoffsetclause'''
    p[0] = ' '.join(p[1:])


def p_limitoffsetclause(p):
    '''limitoffsetclause : limitclause offsetclause_opt
			 | offsetclause limitclause_opt
                         | empty'''
    p[0] = ' '.join(p[1:])


def p_orderclause(p):
    '''orderclause : ORDER BY VAR
                   | empty'''
    p[0] = ' '.join(p[1:])


def p_limitclause_opt(p):
    '''limitclause_opt : limitclause
                       | empty'''
    p[0] = p[1]


def p_limitclause(p):
    '''limitclause : LIMIT INTEGER'''
    p[0] = ' '.join(p[1:])


def p_offsetclause_opt(p):
    '''offsetclause_opt : offsetclause
                        | empty'''
    p[0] = p[1]


def p_offsetclause(p):
    '''offsetclause : OFFSET INTEGER'''
    p[0] = ' '.join(p[1:])


def p_typeDeclaration(p):
    '''typeDeclaration : empty'''
    p[0] = p[1]


def p_positionVar(p):
    '''positionVar : ATS VAR
		   | empty'''
    if len(p) == 3:
	p[0] = (' '.join(p[1:]), p[2])
    else:
	p[0] = ('', '')

def p_orderByClause(p):
    '''orderByClause : ORDER BY orderSpecList
		     | STABLE ORDER BY orderSpecList'''
    p[0] = ' '.join(p[1:])


def p_orderSpecList(p):
    '''orderSpecList : orderSpecList COMMA orderSpec
		     | orderSpec'''
    p[0] = ' '.join(p[1:])


def p_orderSpec(p):
    '''orderSpec : exprSingle orderDirection emptyHandling
		 | exprSingle orderDirection emptyHandling COLLATION uriliteral'''
    p[0] = p[1][0] + ' '.join(p[2:])


def p_orderDirection(p):
    '''orderDirection : ASCENDING
		      | DESCENDING
		      | empty'''
    p[0] = p[1]


# @todo EMPTY clashes with fn:empty...
def p_emptyHandling(p):
    '''emptyHandling : GREATEST
		     | LEAST
		     | empty'''
    p[0] = ' '.join(p[1:])


def p_whereClause(p):
    '''whereClause : WHERE exprSingle '''
    p[0] = ''.join('\n'+p[1]+' '+p[2][0])


## ------------------------------ orExpr

def p_orExpr(p):
    '''orExpr : andExpr orAndExpr'''
    p[0] = (p[1] +' ' +p[2], [], [])


def p_orAndExpr(p):
    '''orAndExpr : OR orAndExpress
		 | empty'''
    p[0] = ' '.join(p[1:])


def p_orAndExpress(p):
    '''orAndExpress : andExpr orAndExpr'''
    p[0] = ''.join(p[1:])


def p_andExpr(p):
    '''andExpr : comparisonExpr andCompExpr'''
    p[0] = ''.join(p[1:])


def p_andCompExpr(p):
    '''andCompExpr : AND andCompExpress
		   | empty'''
    p[0] = ' '.join(p[1:])


def p_andCompExpress(p):
    '''andCompExpress : comparisonExpr andCompExpr'''
    p[0] = ''.join(p[1:])


def p_comparisonExpr(p):
    '''comparisonExpr : rangeExpr rangeExpress'''
    p[0] = ''.join(p[1:])


def p_rangeExpress(p):
    '''rangeExpress : valueComp rangeExpr
		    | generalComp rangeExpr
		    | nodeComp rangeExpr
		    | empty'''
    p[0] = ' '.join(p[1:])


def p_valueComp(p):
    '''valueComp : EQ
		 | NE
		 | LT
		 | LE
		 | GT
		 | GE'''
    p[0] = ''.join(p[1:])


def p_generalComp(p):
    '''generalComp : EQUALS
		   | LESSTHAN
		   | GREATERTHAN
		   | LESSTHANEQUALS
		   | GREATERTHANEQUALS
		   | HAFENEQUALS'''
    p[0] = ''.join(p[1:])


def p_nodeComp(p):
    '''nodeComp : LESSTHANLESSTHAN
		| GREATERTHAN GREATERTHAN
		| IS'''
    p[0] = ''.join(p[1:])


def p_rangeExpr(p):
    '''rangeExpr : additiveExpr rangeAddiExpr'''
    p[0] = ' '.join(p[1:])


def p_rangeAddiExpr(p):
    '''rangeAddiExpr : TO additiveExpr
		     | empty'''
    p[0] = ' '.join(p[1:])


def p_additiveExpr(p):
    '''additiveExpr : multiplicativeExpr addiMultiExpr'''
    p[0] = ''.join(p[1:])


def p_addiMultiExpr(p):
    '''addiMultiExpr : PLUS addiMultiExpress
		     | MINUS addiMultiExpress
		     | empty'''
    p[0] = ''.join(p[1:])


def p_addiMultiExpress(p):
    '''addiMultiExpress : multiplicativeExpr addiMultiExpr'''
    p[0] = ''.join(p[1:])


def p_multiplicativeExpr(p):
    '''multiplicativeExpr : unionExpr multiUnionExpr'''
    p[0] = ''.join(p[1:])


def p_multiUnionExpr(p):
    '''multiUnionExpr : STAR multiUnionExpress
		      | DIV multiUnionExpress
		      | IDIV multiUnionExpress
		      | MOD multiUnionExpress
		      | empty'''
    p[0] = ''.join(p[1:])


def p_multiUnionExpress(p):
    '''multiUnionExpress : unionExpr multiUnionExpr'''
    p[0] = ''.join(p[1:])


def p_unionExpr(p):
    '''unionExpr : intersectExceptionExpr uniIntersectExcExpr'''
    p[0] = ''.join(p[1:])


def p_uniIntersectExcExpr(p):
    '''uniIntersectExcExpr : UNION uniIntersectExcExpress
			   | UNIONSYMBOL uniIntersectExcExpress
			   | empty'''
    p[0] = ''.join(p[1:])


def p_uniIntersectExcExpress(p):
    '''uniIntersectExcExpress : intersectExceptionExpr uniIntersectExcExpr'''
    p[0] = ''.join(p[1:])


def p_intersectExceptionExpr(p):
    '''intersectExceptionExpr : intanceOfExpr interInstanceOfExpr'''
    p[0] = ''.join(p[1:])


def p_interInstanceOfExpr(p):
    '''interInstanceOfExpr : INTERSECT interInstanceOfExpress
			   | EXCEPT interInstanceOfExpress
			   | empty'''
    p[0] = ''.join(p[1:])


def p_interInstanceOfExpress(p):
    '''interInstanceOfExpress : intanceOfExpr interInstanceOfExpr'''
    p[0] = ''.join(p[1:])


def p_intanceOfExpr(p):
    '''intanceOfExpr : treatExpr instanceTreatExpr'''
    p[0] = ''.join(p[1:])


def p_instanceTreatExpr(p):
    '''instanceTreatExpr : INSTANCE OF sequenceType
			 | empty'''
    p[0] = ''.join(p[1:])


def p_treatExpr(p):
    '''treatExpr : castableExpr treatCastableExpr'''
    p[0] = ''.join(p[1:])


def p_treatCastableExpr(p):
    '''treatCastableExpr : TREAT AS sequenceType
			 | empty'''
    p[0] = ''.join(p[1:])


def p_castableExpr(p):
    '''castableExpr : castExpr castableCastExpr'''
    p[0] = ''.join(p[1:])


def p_castableCastExpr(p):
    '''castableCastExpr : CASTABLE AS singleType
			| empty'''
    p[0] = ''.join(p[1:])


def p_castExpr(p):
    '''castExpr : unaryExpr castUnaryExpr'''
    p[0] = ''.join(p[1:])


def p_castUnaryExpr(p):
    '''castUnaryExpr : CAST AS singleType
		     | empty'''
    p[0] = ''.join(p[1:])


def p_unaryExpr(p):
    '''unaryExpr : MINUS valueExpr
		 | PLUS valueExpr
		 | valueExpr'''
    p[0] = ''.join(p[1:])


def p_valueExpr(p):
    '''valueExpr : pathExpr'''
    p[0] = ''.join(p[1:])


def p_sequenceType(p):
    '''sequenceType : EMPTYSEQUENCE LPAR RPAR
		    | itemType occurrenceIndicator'''
    p[0] = ''.join(p[1:])


def p_occurrenceIndicator(p):
    '''occurrenceIndicator : STAR
			   | PLUS
			   | QUESTIONMARK'''
    p[0] = ''.join(p[1:])


def p_itemType(p):
    '''itemType : ITEM LPAR RPAR
 		| atomicType
		| kindTest'''
    p[0] = ''.join(p[1:])


def p_kindTest(p):
    '''kindTest : documentTest
		| elementTest
		| attributeTest
		| schemaElementTest
		| schemaAttributeTest
		| piTest
		| commentTest
		| textTest
		| anyKindTest'''
    p[0] = ''.join(p[1:])


def p_anyKindTest(p):
    '''anyKindTest : NODE LPAR RPAR'''
    p[0] = ''.join(p[1:])


def p_documentTest(p):
    '''documentTest : DOCUMENTNODE LPAR documentTests RPAR'''
    p[0] = ''.join(p[1:])


def p_documentTests(p):
    '''documentTests : elementTest
		     | schemaElementTest
		     | empty'''
    p[0] = ''.join(p[1:])


def p_textTest(p):
    '''textTest : TEXT LPAR RPAR'''
    p[0] = ''.join(p[1:])


def p_commentTest(p):
    '''commentTest : COMMENT LPAR RPAR'''
    p[0] = ''.join(p[1:])


def p_piTest(p):
    '''piTest : PROCESSINGINSTRUCTION LPAR piTests RPAR'''
    p[0] = ''.join(p[1:])


def p_piTests(p):
    '''piTests : qname
	       | QSTRING
	       | empty'''
    p[0] = ''.join(p[1:])


def p_attributeTest(p):
    '''attributeTest : ATTRIBUTE LPAR attributeTests RPAR'''
    p[0] = ''.join(p[1:])


def p_attributeTests(p):
    '''attributeTests : attributeNameOrWildcard COMMA typeName
		      | attributeNameOrWildcard
		      | empty'''
    p[0] = ''.join(p[1:])


def p_attributeNameOrWildcard(p):
    '''attributeNameOrWildcard : attributeName
			       | STAR'''
    p[0] = ''.join(p[1:])


def p_schemaAttributeTest(p):
    '''schemaAttributeTest : SCHEMAATTRIBUTE LPAR attributeDeclaration RPAR'''
    p[0] = ''.join(p[1:])


def p_attributeDeclaration(p):
    '''attributeDeclaration : attributeName'''
    p[0] = ''.join(p[1:])


def p_elementTest(p):
    '''elementTest : ELEMENT LPAR elementTests RPAR '''
    p[0] = ''.join(p[1:])


def p_elementTests(p):
    '''elementTests : elementNameOrWildcard COMMA typeName QUESTIONMARK
		    | elementNameOrWildcard COMMA typeName
		    | elementNameOrWildcard
		    | empty'''
    p[0] = ''.join(p[1:])


def p_elementNameOrWildcard(p):
    '''elementNameOrWildcard : elementName
			     | STAR'''
    p[0] = ''.join(p[1:])


def p_schemaElementTest(p):
    '''schemaElementTest : SCHEMAELEMENT LPAR elementDeclaration RPAR'''
    p[0] = ''.join(p[1:])


def p_elementDeclaration(p):
    '''elementDeclaration : elementName'''
    p[0] = ''.join(p[1:])


def p_attributeName(p):
    '''attributeName : qname'''
    p[0] = ''.join(p[1:])


def p_elementName(p):
    '''elementName : qname'''
    p[0] = ''.join(p[1:])


def p_typeName(p):
    '''typeName : qname'''
    p[0] = ''.join(p[1:])


def p_singleType(p):
    '''singleType : atomicType QUESTIONMARK
		  | atomicType'''
    p[0] = ''.join(p[1:])


def p_atomicType(p):
    '''atomicType : qname'''
    p[0] = ''.join(p[1:])


def p_pathExpr(p):
    '''pathExpr : SLASH
		| SLASH relativePathExpr
		| SLASHSLASH relativePathExpr
		| relativePathExpr'''
    p[0] = ''.join(p[1:])


def p_relativePathExpr(p):
    '''relativePathExpr : relativePathExpr SLASH stepExpr
			| relativePathExpr SLASHSLASH stepExpr
			| stepExpr'''
    p[0] = ''.join(p[1:])


def p_stepExpr(p):
    '''stepExpr : filterExpr
		| axisStep'''
    p[0] = ' '.join(p[1:])


def p_axisStep(p):
    '''axisStep : reverseStep predicateList
		| forwardStep predicateList'''
    p[0] = ''.join(p[1:])


def p_forwardStep(p):
    '''forwardStep : forwardAxis nodeTest
		   | abbrevForwardStep'''
    p[0] = ''.join(p[1:])


def p_forwardAxis(p):
    '''forwardAxis : CHILD COLONCOLON
		   | DESCENDANT COLONCOLON
		   | ATTRIBUTE COLONCOLON
		   | SELF COLONCOLON
		   | DESCENDANTORSELF COLONCOLON
		   | FOLLOWINGSIBLING COLONCOLON
		   | FOLLOWING COLONCOLON'''
    p[0] = ''.join(p[1:])


def p_reverseStep(p):
    '''reverseStep : reverseAxis nodeTest
		   | abbrevReverseStep'''
    p[0] = ''.join(p[1:])


def p_reverseAxis(p):
    '''reverseAxis : PARENT COLONCOLON
		   | ANCESTOR COLONCOLON
		   | PRECEDINGSIBLING COLONCOLON
		   | PRECEDING COLONCOLON
		   | ANCESTORORSELF COLONCOLON'''
    p[0] = ''.join(p[1:])


def p_abbrevForwardStep(p):
    '''abbrevForwardStep : AT nodeTest
			 | nodeTest'''
    p[0] = ''.join(p[1:])


def p_abbrevReverseStep(p):
    '''abbrevReverseStep : DOTDOT'''
    p[0] = ''.join(p[1:])


def p_nodeTest(p):
    '''nodeTest : kindTest
		| nameTest'''
    p[0] = p[1]


def p_nameTest(p):
    '''nameTest : qname
		| wildCard'''
    p[0] = p[1]


def p_wildCard(p):
    '''wildCard : STAR
		| NCNAME_COLON_STAR
		| STAR_COLON_NCNAME'''
    p[0] = ''.join(p[1:])


def p_filterExpr(p):
    '''filterExpr : primaryExpr predicateList'''
    p[0] = ''.join(p[1:])


def p_predicateList(p):
    '''predicateList : predicate
		     | empty'''
    p[0] = p[1]


def p_predicate(p):
    '''predicate : LBRACKET expr RBRACKET'''
    p[0] = ''.join(p[1:])


def p_primaryExpr(p):
    '''primaryExpr : VAR
		   | literal
                   | parenthesizedExpr
		   | contextItemExpr
		   | functionCall
		   | orderedExpr
		   | unorderedExpr
		   | constructor'''
    p[0] = p[1]


def p_uriliteral(p):
    '''uriliteral : stringliteral'''
    p[0] = p[1]


def p_literal(p):
    '''literal : numericliteral
	       | stringliteral'''
    p[0] = p[1]


def p_numericliteral(p):
    '''numericliteral : INTEGER'''
    p[0] = p[1]


def p_stringliteral(p):
    '''stringliteral : QSTRING'''
    p[0] = p[1]


def p_parenthesizedExpr(p):
    '''parenthesizedExpr : LPAR expr RPAR
			 | LPAR RPAR'''
    p[0] = ''.join(p[1:])


def p_contextItemExpr(p):
    '''contextItemExpr : DOT'''
    p[0] = p[1]


def p_orderedExpr(p):
    '''orderedExpr : ORDERED enclosedExpr'''
    p[0] = ' '.join(p[1:])


def p_unorderedExpr(p):
    '''unorderedExpr : UNORDERED enclosedExpr'''
    p[0] = ' '.join(p[1:])


def p_functionCall(p):
    '''functionCall : qname LPAR exprSingles RPAR'''
    p[0] = ''.join(p[1:])


def p_exprSingles(p):
    '''exprSingles : exprSingle exprSingleses'''
    p[0] = ''.join(p[1][0]+' '+p[2])


def p_exprSingleses(p):
    '''exprSingleses : COMMA exprSingles
		     | empty'''
    p[0] = ' '.join(p[1:])



## ---------------------------------------------------------- constructTemplate / SPARQL
## http://www.w3.org/TR/rdf-sparql-query/#rConstructTemplate

def p_constructTemplate(p):
    '''constructTemplate : LCURLY constructTriples RCURLY'''
    p[0] = p[2]



def p_constructTriples(p):
    '''constructTriples : lifttriples DOT constructTriples
			| lifttriples
			| empty'''
    if len(p) == 4:
	p[0] = [ p[1] ] +  p[3]
    elif len(p) == 2 and len(p[1]):
	p[0] = [ p[1] ]
    else:
	p[0] = []


def p_lifttriples(p):
    '''lifttriples : subject predicateObjectList'''
    p[0] = (p[1], p[2])


def p_subject0(p):
    '''subject : resource
	       | iriConstruct'''
    p[0] = [ p[1] ]


def p_subject1(p):
    '''subject : blank
	       | blankConstruct
	       | enclosedExpr'''
    p[0] = p[1]


def p_predicateObjectList(p):
    '''predicateObjectList : verbObjectLists
			   | empty'''
    p[0] = p[1]


def p_verbObjectLists(p):
    '''verbObjectLists : verb objectList SEMICOLON verbObjectLists
		       | verb objectList SEMICOLON
		       | verb objectList'''
    if len(p) == 5:
	p[0] = [ ( p[1], p[2] ) ] + p[4]
    else:
	p[0] = [ ( p[1], p[2] ) ]


def p_objectList(p):
    '''objectList : object COMMA objectList
		  | object'''
    if len(p) == 4:
	p[0] = [ p[1] ] + p[3]
    else:
	p[0] = [ p[1] ]

def p_object(p):
    '''object : resource
	      | blank
	      | rdfliteral
	      | blankConstruct
	      | iriConstruct
	      | literalConstruct'''
    p[0] = p[1]


def p_verb(p):
    '''verb : rdfPredicate
	    | A
	    | iriConstruct'''
    p[0] = p[1]


def p_rdfPredicate(p):
    '''rdfPredicate : resource'''
    p[0] = p[1]



def p_blankConstruct(p):
    '''blankConstruct : BNODE_CONSTRUCT expr RCURLY'''
    p[0] = [ ''.join(p[1:]) ]


def p_iriConstruct(p):
    '''iriConstruct : LESSTHAN enclosedExpr GREATERTHAN
		    | enclosedExpr COLON enclosedExpr
		    | qname COLON enclosedExpr
		    | LCURLY expr IRI_CONSTRUCT 
		    | enclosedExpr COLON qname'''
    p[0] = ''.join(p[1:])


def p_literalConstruct0(p):
    '''literalConstruct :  enclosedExpr'''
    p[0] = ''.join(p[1:])

def p_literalConstruct1(p):
    '''literalConstruct :  INTEGER AT enclosedExpr
                        |  QSTRING AT enclosedExpr
                        |  enclosedExpr AT enclosedExpr
                        |  INTEGER CARROT CARROT iriConstruct
                        |  QSTRING CARROT CARROT iriConstruct
                        |  enclosedExpr CARROT CARROT iri'''
    p[0] = p[1:]



def p_iri(p):
    '''iri :  PREFIXED_NAME
           |  IRIREF
           |  iriConstruct'''
    p[0] = p[1]

## ----------------------------------------------------------


def p_resource(p):
    '''resource : sparqlPrefixedName
		| VAR
		| IRIREF'''
    p[0] = p[1]

def p_blank(p):
    '''blank : bnode
	     | LBRACKET predicateObjectList RBRACKET'''
    if len(p) == 2: # bnode
	p[0] = [ p[1] ]
    else: # non-empty bracketedExpr  @todo: is this correct??
	if (p[2] == ''):
	    p[0] = p[1] + p[3]
	else:
	    p[0] = [ p[1] ] + p[2]

def p_bnode(p):
    '''bnode : BNODE'''
    p[0] = ''.join(p[1:])


def p_rdfliteral(p):
    '''rdfliteral : INTEGER
		  | QSTRING
		  | QSTRING AT qname
		  | QSTRING CARROT CARROT IRIREF
		  | QSTRING CARROT CARROT PREFIXED_NAME'''
    p[0] = ''.join(p[1:]).replace("\"", "\"\"")



## ----------------------------------------------------------


# '{' TriplesBlock? ( ( GraphPatternNotTriples | Filter ) '.'? TriplesBlock? )* '}'
def p_groupGraphPattern(p):
    '''groupGraphPattern : LCURLY triplesBlock groupGraphPattern_star RCURLY'''
    p[0] = p[1:]

def p_groupGraphPattern_star(p):
    '''groupGraphPattern_star : graphPatternNotTriplesOrFilter DOT triplesBlock groupGraphPattern_star
                              | graphPatternNotTriplesOrFilter triplesBlock groupGraphPattern_star
                              | empty'''
    p[0] = p[1:]


def p_graphPatternNotTriplesOrFilter(p):
    '''graphPatternNotTriplesOrFilter : graphPatternNotTriples
                                      | Filter '''
    p[0] = p[1]




def p_graphPatternNotTriples(p):
    '''graphPatternNotTriples : OPTIONAL groupGraphPattern 
                              | groupOrUnionGraphPattern
                              | GraphGraphPattern'''
    p[0] = p[1:]



def p_groupOrUnionGraphPattern(p):
    '''groupOrUnionGraphPattern : groupGraphPattern UNION groupOrUnionGraphPattern
                                | groupGraphPattern'''
    p[0] = p[1:]


def p_GraphGraphPattern(p):
    '''GraphGraphPattern : GRAPH VarOrIRIref groupGraphPattern'''
    p[0] = p[1:]


def p_VarOrIRIref(p):
    ''' VarOrIRIref : VAR
                    | IRIref'''
    p[0] = p[1]



# ----------------------------------- FILTERs 

def p_Filter(p):
    '''Filter : FILTER brackettedExpression
              | FILTER builtInCall
              | FILTER functionCall'''
    p[0] = p[1:]

def p_brackettedExpression(p):
    '''brackettedExpression : LPAR expression RPAR'''
    p[0] = p[1:]


def p_builtInCall(p):
    '''builtInCall : STR LPAR expression RPAR
                   | LANG LPAR expression RPAR
                   | LANGMATCHES LPAR expression COMMA expression RPAR 
                   | DATATYPE LPAR expression RPAR 
                   | BOUND LPAR VAR RPAR 
                   | isIRI LPAR expression RPAR 
                   | isURI LPAR expression RPAR 
                   | isBLANK LPAR expression RPAR 
                   | isLITERAL LPAR expression RPAR 
                   | RegexExpression'''
    p[0] = p[1:]


def p_expression(p):
    '''expression : ConditionalOrExpression'''
    p[0] = p[1:]

def p_ConditionalOrExpression(p):
    '''ConditionalOrExpression : ConditionalAndExpression ConditionalOrExpression_star'''
    p[0] = p[1:]

def p_ConditionalOrExpression_star(p):
    '''ConditionalOrExpression_star : ORSYMBOL ConditionalAndExpression ConditionalOrExpression_star
                                    | empty'''
    p[0] = p[1:]

def p_ConditionalAndExpression(p):
    '''ConditionalAndExpression : ValueLogical ConditionalAndExpression_star'''
    p[0] = p[1:]


def p_ConditionalAndExpression_star(p):
    '''ConditionalAndExpression_star : ANDSYMBOL ValueLogical ConditionalAndExpression_star
                                     | empty'''
    p[0] = p[1:]

def p_ValueLogical(p):
    '''ValueLogical : RelationalExpression'''
    p[0] = p[1:]

def p_RelationalExpression(p):
    '''RelationalExpression : NumericExpression ComparisionExpression'''
    p[0] = p[1:]


def p_ComparisionExpression(p):
    '''ComparisionExpression : EQUALS NumericExpression 
                             | HAFENEQUALS NumericExpression 
                             | LESSTHAN NumericExpression 
                             | GREATERTHAN NumericExpression 
                             | LESSTHANEQUALS NumericExpression 
                             | GREATERTHANEQUALS NumericExpression 
                             | empty'''
    p[0] = p[1:]

def p_NumericExpression(p):
    '''NumericExpression : AdditiveExpression'''
    p[0] = p[1:]

def p_AdditiveExpression(p):
    '''AdditiveExpression : MultiplicativeExpression AdditiveExpression_star'''
    p[0] = p[1:]

def p_AdditiveExpression_star(p):
    '''AdditiveExpression_star : PLUS MultiplicativeExpression AdditiveExpression_star
                               | MINUS MultiplicativeExpression AdditiveExpression_star
                               | empty'''
    p[0] = p[1:]

def p_MultiplicativeExpression(p):
    '''MultiplicativeExpression : UnaryExpression MultiplicativeExpression_star'''
    p[0] = p[1:]

def p_MultiplicativeExpression_star(p):
    '''MultiplicativeExpression_star : STAR UnaryExpression MultiplicativeExpression_star
                                     | SLASH UnaryExpression MultiplicativeExpression_star
                                     | empty'''
    p[0] = p[1:]

def p_UnaryExpression(p):
    '''UnaryExpression : NOT PrimaryExpression 
                       | PLUS PrimaryExpression 
                       | MINUS PrimaryExpression 
                       | PrimaryExpression'''
    p[0] = p[1:]


def p_PrimaryExpression(p):
    '''PrimaryExpression : brackettedExpression 
                         | builtInCall 
                         | IRIrefOrFunction 
                         | rdfliteral 
                         | BooleanLiteral 
                         | BNODE
                         | LBRACKET RBRACKET 
                         | VAR'''
    p[0] = p[1:]


def p_RegexExpression(p):
    '''RegexExpression	: REGEX LPAR expression COMMA expression expression_opt RPAR'''
    p[0] = p[1:]

def p_expression_opt(p):
    '''expression_opt : COMMA  expression
                      | empty'''
    p[0] = p[1:]
    

def p_BooleanLiteral(p):
    '''BooleanLiteral : TRUE
                      | FALSE'''
    p[0] = p[1]

def p_IRIrefOrFunction(p):
    '''IRIrefOrFunction : IRIref Arglist'''
    p[0] = p[1:]

def p_IRIref(p):
    '''IRIref : IRIREF
              | qname'''
    p[0] = p[1]

#  NIL |
def p_Arglist(p):
    '''Arglist : LPAR expression expression_star RPAR '''
    p[0] = p[1:]

def p_expression_star(p):
    '''expression_star : COMMA expression expression_star
                       | empty'''
    p[0] = p[1:]



# ----------------------------------- end FILTERs 


def p_triplesBlock(p):
    '''triplesBlock : lifttriples_where DOT triplesBlock
                    | lifttriples_where
                    | empty'''
    if len(p) == 4:
	p[0] = [ p[1] ] +  p[3]
    elif len(p) == 2 and len(p[1]):
	p[0] = [ p[1] ]
    else:
	p[0] = []


def p_lifttriples_where(p):
    '''lifttriples_where : subject_where predicateObjectList_where '''
    p[0] = (p[1], p[2])


def p_subject_where0(p):
    '''subject_where : resource'''
    p[0] = [ p[1] ]


def p_subject_where1(p):
    '''subject_where : blank'''
    p[0] = p[1]


def p_predicateObjectList_where(p):
    '''predicateObjectList_where : verbObjectLists_where
				 | empty'''
    p[0] = p[1]


def p_verbObjectLists_where(p):
    '''verbObjectLists_where : verb_where objectList_where SEMICOLON verbObjectLists_where
			     | verb_where objectList_where SEMICOLON
			     | verb_where objectList_where'''
    if len(p) == 5:
	p[0] = [ ( p[1], p[2] ) ] + p[4]
    else:
	p[0] = [ ( p[1], p[2] ) ]


def p_objectList_where(p):
    '''objectList_where : object_where COMMA objectList_where
			| object_where'''
    if len(p) == 4:
	p[0] = [ p[1] ] + p[3]
    else:
	p[0] = [ p[1] ]


def p_object_where(p):
    '''object_where : resource
		    | blank
		    | rdfliteral'''
    p[0] = p[1]


def p_verb_where(p):
    '''verb_where : rdfPredicate
		  | A'''
    p[0] = p[1]



## ----------------------------------------------------------

def p_sparqlPrefixedName(p):
    '''sparqlPrefixedName : PREFIXED_NAME
                          | PREFIXED_COLON
                          | UNPREFIXED_NAME'''
    p[0] = p[1]



## ----------------------------------------------------------

# each reserved word is also a qname to allow it's use for instance in
# path expressions

# these add conflicts:
#              | PREFIX
#              | BASE
#              | FOR
#              | DECLARE
#              | ORDER
#              | BY

def p_qname(p):
    '''qname : prefixedName
	     | unprefixedName
             | A
             | IS
             | EQ
             | NE
             | LT
             | GE
             | LE
             | GT
             | FROM
             | LIMIT
             | OFFSET
             | LET
             | ATS
             | IN
             | AS
             | DESCENDING
             | ASCENDING
             | STABLE
             | IF
             | THEN
             | ELSE
             | RETURN
             | CONSTRUCT
             | WHERE
             | GREATEST
             | LEAST
             | COLLATION
             | CHILD
             | DESCENDANT
             | ATTRIBUTE
             | SELF
             | DESCENDANTORSELF
             | FOLLOWINGSIBLING
             | FOLLOWING
             | PARENT
             | ANCESTOR
             | PRECEDINGSIBLING
             | PRECEDING
             | ANCESTORORSELF
             | ORDERED
             | UNORDERED
             | NAMESPACE
             | DEFAULT
             | ELEMENT
             | FUNCTION
             | BASEURI
             | AND
             | OR
             | TO
             | DIV
             | IDIV
             | MOD
             | UNION
             | INTERSECT
             | EXCEPT
             | INSTANCE
             | TREAT
             | CASTABLE
             | CAST
             | OF
             | EMPTYSEQUENCE
             | ITEM
             | NODE
             | DOCUMENTNODE
             | TEXT
             | COMMENT
             | PROCESSINGINSTRUCTION
             | SCHEMAATTRIBUTE
             | SCHEMAELEMENT
             | DOCUMENT
             | NAMED
             | OPTIONAL
             | FILTER
             | STR
             | LANG
             | LANGMATCHES
             | DATATYPE
             | BOUND
             | isIRI
             | isURI
             | isBLANK
             | isLITERAL
             | REGEX
             | TRUE
             | FALSE
             | GRAPH'''
    p[0] = ''.join(p[1:])


def p_unprefixedName(p):
    '''unprefixedName : localPart'''
    p[0] = ''.join(p[1:])

def p_prefixedName(p):
    '''prefixedName : PREFIXED_NAME'''
    p[0] = ''.join(p[1:])

def p_localPart(p):
    '''localPart : NCNAME'''
    p[0] = ''.join(p[1:])




## ------------------------------ empty rule

def p_empty(p):
    '''empty : '''
    p[0] = ''



#
# XSPARQL Grammar end
#

# Compute column.
def find_column(token):
    global instring

    i = token.lexpos
    while i > 0:
	if instring[i] == '\n': break
	i -= 1
    column = (token.lexpos - i)
    return column-1

def p_error(p):
    '''Error rule for syntax errors -> ignore them gracefully by
    throwing a SyntaxError.'''

    debug.debug("ERROR", p)
    if(p == None):
	sys.stderr.write('Syntax error at end of file\n')
    else:
	col = find_column(p)
	sys.stderr.write('Syntax error: \'' + p.value + '\' at line '+ `p.lineno` + ', column '+ `col` + '\n')

    raise SyntaxError



def generate_parser():
    '''called at build time'''
    yacc.yacc(tabmodule = 'parsetab', outputdir = './xsparql')


def get_parser():
    '''called in rewrite to read the installed parser'''
    return yacc.yacc(debug = 0, tabmodule = 'xsparql.parsetab', write_tables = 0)



# ---------------------------- initial function

instring = ''


def rewrite(s):
    '''Rewrite s using our XSPARQL grammar. If we find a syntax error,
       we bail out and return the original input.'''

    # store the input string to calculate the column in case of error
    global instring
    instring = s

    try:
	parser = get_parser()
        
        # no query is given
        if s == '':
            sys.stderr.write('Error: empty query file\n')
            sys.exit(1)

	# parse s, and get rewritten string back
        if debugInfo: 
            result = parser.parse(s, debug=1)
        else:
            result = parser.parse(s)

	if result:
	   return result + '\n'
	else:
	   return ''

    except SyntaxError:
	sys.exit(1)



def reLexer(s):
    lexer = lex.lex()
    lexer.input(s)
    while 1:
	 tok = lexer.token()
	 if not tok: break
	 print tok

    sys.exit(0)



if __name__ == "__main__":
    instring = ''.join(sys.stdin.readlines())
    output = rewrite(instring)
    print output
