# -*- coding: utf-8 -*-
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


#
# auxiliary variable names
#

import re

#@todo why?
import lowrewriter
import debug
import grammar

#
# rewriting functions
#

var_p = ''
var = ''
count = 1  # counter for temporary variables (used in the valid* functions)
inBnode = 0

def build_rewrite_query(forletExpr, modifiers, construct, graphpattern, variable_p, variable):

    global var
    global var_p

    if '*' in variable:
	var = lowrewriter.variables
    else:
	var = variable

    var_p = variable_p

    let, ret = build_triples(graphpattern, [], [])

    return '\n  ' + forletExpr + ' \n\n' + let + '\n' + modifiers  + '\n\n  return ( ' + ret + ' )'



def build_triples(gp, variable_p, variable ):

#    debug.debug('-------- build_triples', gp)

    ret = ''
    space = ''
    if variable_p != []:
	global var_p
	var_p = variable_p
    if variable != []:
	global var
	var = variable

    let = ''
    cond = ''

    firstelement = True
    for s, polist in gp:

	if not firstelement:
	    ret += ','
	    firstelement = False

	if isinstance(s, str) and not s[0] == "[":

	    if polist != '':
		let_subject, cond_subject, subject, suff_subject = build_bnode('_validSubject', s)
		let_po, cond_po, po, suff_po = build_predicate(subject, polist)
		let += let_subject + let_po
		ret +=  '\n\t ' + cond_subject + cond_po + ' \n\t\t '+ po.rstrip(', ') + '\n\t\t \n ' + suff_po + suff_subject + ' ,'
	    else:
		s = s.lstrip('{')
		s = s.rstrip('}')
		ret += '\n' + s + ','

	else:
	    let_subject, cond_subject, subject, suff_subject = build_subject(s)
	    let_po, cond_po, po, suff_po = build_predicate(subject, polist)  # send rewritten subject

	    let = let + let_subject + let_po

	    if (cond_subject + cond_po == ''):
		if isinstance(s, str) and s == "[]":
		    ret += '_xsparql:_removeEmpty( \n\t\t _xsparql:_serialize(( \n\t\t "[]", ' + po.rstrip(', ') +  ', " .&#xA;" \n\t\t)) \n\t\t ) '
		elif(s[0] == "["):
		    ret += '_xsparql:_removeEmpty( \n\t\t _xsparql:_serialize(( \n\t\t "[", ' + subject.rstrip(', ') + po.rstrip(', .') + ', " ] .&#xA;" \n\t\t)) \n\t\t )'
		else:
		    ret += po.rstrip(', ')
	    else:
		ret +=  '\n\t ' + cond_subject + cond_po + ' \n\t\t '+ po.rstrip(', ') + '\n\t\t \n ' + suff_po + suff_subject + ' ,'

    return let, ret.rstrip(',')



def build_subject(s):

#    debug.debug('------- build_subject', s, len(s))

    if len(s) == 1 and isinstance(s[0], list) and isinstance(s[0][0], str):
	return build_bnode('_validSubject', s[0][0])
    elif len(s) <= 2 and isinstance(s, str): # blank node or subject
	return build_bnode('_validSubject', s)
    elif len(s) == 1 and isinstance(s[0], str): # blank node or subject
	return build_bnode('_validSubject', s[0])
    elif len(s) == 1 and isinstance(s[0], list): # blank node
	return build_predicate("", s[0])
    elif len(s) == 0: # single blank node
	return '', '', '[]'
    else: # polist
	if s[0] == '[' or s[0] == "[]": # first member is an opening bnode bracket
	    global inBnode
	    inBnode = inBnode + 1

	    let1, cond1, ret1, suff1 = build_predicate("", [ s[1] ])
	    let2, cond2, ret2, suff2 = build_predicate("", s[2:])

	    ret = ret1.rstrip(', ')

	    if not ret2 == "":
		ret += ',\n\t\t' + ret2

	    return let1 + let2, cond1 + cond2,  ret + ' \n ', suff1 +suff2
	else:
	    let1, cond1, ret1, suff1 = build_predicate("", [ s[0] ])
	    let2, cond2, ret2, suff2 = build_predicate("", s[1:])
	    return let1 + let2, cond1 + cond2, ' ' + ret1 + ' "&#59;",\n\t\t' + ret2 + ' \n ', suff1+suff2



def build_predicate(subject, p):

#    debug.debug('------- build_predicate', p, len(p))

    if len(p) == 1:
	b = p[0][0]
	if len(b) >= 2 and b[0] == '{' and b[-1] == '}' and b[1:-1].find('{') == -1 and b[1:-1].find('}') == -1:
	    strip = str(b).lstrip('{')
	    b = strip.rstrip('}')

#            let_p, cond_p, ret_p, suff_p = build_bnode('_validPredicate', p[0][1])
	    let, cond, ret, suff = build_object(subject, b, p[0][1])
	    return let, cond,  ' '+ b + ',  ' + ret + ' ', suff
	elif len(b) >= 2 and ( b[0] == '$'or b[0] == '?'):
	     if b[0] == '?':
		 b = b.lstrip('?')
		 b = '$'+ b

	     if listSearch(b):
		 var = lowrewriter.prefix_var(b) + '_RDFTerm'
	     else:
		 var = b

             let_p, cond_p, ret_p, suff_p = build_bnode('_validPredicate', var)
	     let, cond, ret, suff = build_object(subject, ret_p.rstrip(', '),  p[0][1])
	     return let_p + let, cond_p + cond, ret, suff_p + suff
	else:
	     if len(b) >= 2:
		 if(b[0] != '_' and b[1] != ':'):
                     let_p, cond_p, ret_p, suff_p = build_bnode('_validPredicate', b)
                     let, cond, ret, suff = build_object(subject, ret_p.rstrip(', '), p[0][1])
                     return let_p + let, "", cond_p + cond + '  \n\t  ' + ret.rstrip(', ') + '  \n ' + suff_p + suff, ""
	     else:
		 let, cond, ret,suff = build_object(subject, b, p[0][1])
		 return let, "", cond + '  \n\t ' + ret.rstrip(',')  + ', ' + suff, ""
    elif len(p) == 0:
	return '','','', ''
    else:
	d =  p
	if d[0] == '[' :
	    d.remove('[')
	    let1, cond1, ret1, suff1 = build_predicate(subject, [ d[0] ])
	    let2, cond2, ret2, suff2 = build_predicate(subject, [ d[1] ])
	    return let1 + let2, cond1 + cond2, '"[", ' + ret1 + '"&#59;", \n\t\t' + ret2 + ' "]",\n', suff1+suff2
	else:
	    let1, cond1, ret1, suff1 = build_predicate(subject, [ d[0] ])
	    let2, cond2, ret2, suff2 =  build_predicate(subject, d[1:])
	    return let1 + let2, "", '\n\t ' + cond1 +  ret1.rstrip(', ')  + suff1 +',  \n\t ' +  ret2 , ""



def build_object(subject, predicate, o):

#    debug.debug('------- build_object', o)

    if predicate.rstrip(' ')[0] == '$':
        Qpredicate = '" ", ' + predicate + '," "'
    else:
        Qpredicate = '" ' + predicate + ' "'

    if len(o) == 1 and isinstance(o[0], list) and isinstance(o[0][0], str):
	d =  o[0]
        if  ( (len(d) == 3  and d[1] == '@') or (len(d) == 4 and d[1] == '^' and d[2] == '^') ):
	    let,cond,ret,suff = build_bnode('_validObject', d)
            return let, "", cond + ' _xsparql:_serialize(( \n\t\t '+ subject + ' ' + Qpredicate + ', ' + ret.rstrip(',')  + '" .&#xA;"\n\t\t))\n'+suff, ""
	elif d[0] == '[' :
	    d.remove('[')
	    global inBnode

	    let, cond, ret,suff = build_predicate("", d)
	    # distinguish from nested [] ?
	    if inBnode > 0:
		ret = ' _xsparql:_serialize(( \n\t\t ' + Qpredicate + ', ' + '"[", ' + ret.rstrip(', ') + ', " ]" \n\t\t )) \n'
	    else:
		ret = ' _xsparql:_serialize(( \n\t\t ' + subject + ' ' + Qpredicate + ', ' + '"[", ' + ret.rstrip(', ') + ', " ]", " .&#xA;" \n\t\t )) \n'

	    inBnode = inBnode - 1

	    return let, "", cond + ret + suff, ""

	else:
	    let,cond,ret,suff = build_bnode('_validObject', o[0][0])
	    if (subject == "" or subject.strip('\', ') == "[]"):
		return let, cond, ' _xsparql:_serialize((' + Qpredicate + ',  ' + ret.rstrip(', ')  + ', " &#59; "))', suff
	    else:
		return let, "", cond + ' _xsparql:_serialize(( \n\t\t '+ subject + ' ' + Qpredicate + ', ' + ret.rstrip(',')  + '" .&#xA;"\n\t\t))\n'+suff, ""

    elif len(o) == 1 and isinstance(o[0], str):
	let,cond,ret,suff = build_bnode('_validObject', o[0])

	if (subject == "" or subject.strip('\', ') == "[]"):
	    return let, cond, ' _xsparql:_serialize((' + Qpredicate + ', ' + ret.rstrip(', ')  + ', "&#59;"))', suff
	else:
	    return let, "", cond + ' _xsparql:_serialize(( \n\t\t '+ subject + ' ' + Qpredicate + ', ' + ret.rstrip(',')  + '" .&#xA;"\n\t\t))\n'+suff, ""

    elif len(o) == 1 and isinstance(o[0], list):
	return build_predicate(subject, o[0])

    elif len(o) == 0:
	return "", "", '[]', ""
    else:
	let1, cond1, ret1, suff1 = build_object(subject, predicate,  [ o[0] ])
	let2, cond2, ret2, suff2 =  build_object(subject, predicate,  o[1:] )
	return let1 + let2, "", '\n\t ' + cond1 +  ret1  + suff1 +' , \n\t ' +  ret2  , ""



def build_bnode(type, b):

#    debug.debug('----- build_bnode', b, len(b))

    if isinstance(b, list):  # typed or lang literal
        let,cond,ret,suff = build_bnode(type, b[0])
        let = let.rstrip(')\n ')

        if b[1] == '@':
            let += ', "@", ' + b[2].strip('{}') + '))\n'
            return let,cond,ret,suff

        elif b[1] == '^' and b[2] == '^':
            if let == '':
                global count
                var = '$' + type + `count`
                count = count + 1

                let =  'let '+ var +' := _xsparql:_serialize((' +  ret.rstrip(', ')

                cond = 'if ( _xsparql:'+type + '( "",  '+var+'  ) ) then (\n\t\t'
                ret = var + ', '
                suff = ' ) else ""'

            let += ', "^^"'
            iri = b[3]
            if iri[0] == '<' and iri[1] == '{' and iri[-2] == '}' and iri[-1] == '>':
                let += ', ' + iri.strip('<{}>') + '))\n'
            else:
                if iri >= 4 and iri[0] == '<' and iri[1] == '{' and iri[-2] == '}' and iri[-1] == '>':  # iri literal
                    iri =  iri[1:-1]

                bIri =  iri.split('{')

                for elm in bIri:
                    if elm == '':
                        continue

                    if elm.find('}') == -1:
                        let += ', "' + elm + '"'
                    else:
                        sp = elm.split('}')
                        for e in sp:
                            if e == '':
                                continue

                            if e.strip(' ')[0] == '$':
                                let += ', '+ e
                            else:
                                let += ', "'+ e + '"'

                let += "))\n"

            return let,cond,ret,suff

    elif b >= 4 and b[0] == '<' and b[1] == '{' and b[-2] == '}' and b[-1] == '>':  # iri literal
	bIri =  b[1:-1].split('{')
	iri = bIri[0]
	iri = bIri[1].rstrip('}')
	let,cond,ret,suff = genLetCondReturn(type,  [ '"<" ,', iri , ', ">"'] )
	return let,cond,'    '+ ret + '  ,  ', suff

    elif b >= 2 and b[0] == '<' and b[-1] == '>':  # iri literal
	let,cond,ret,suff = genLetCondReturn(type,  [ '"'+b+'"' ] )
	return let,cond,'    '+ ret + '  ,  ', suff

    elif b >= 2 and b[0] == '_' and b[1] == ':':  # bnode
	global var_p
	v = ''
	for i in var_p:
            if i != []:    #  @@fix this
                v += ' data('+str(i[0:])+ '),'
	if b.find('{') == -1 and b.find('}') == -1: #without enclosed {}
	    let,cond,ret,suff = genLetCondReturn(type, [ '"', b , '"', ', "_",', v.rstrip(',')] )
	    return let, cond , ret + ', ', suff
	else:
	    bExpr =  b.split('{')
	    bNode = bExpr[0]
	    expr = bExpr[1].rstrip('}')
            
	    let,cond,ret,suff = genLetCondReturn(type, ['"' , bNode  ,  '",  data(', expr, ')'] )
	    return let,cond, ret +', ', suff
    else:
	if (b >= 2 and b[0] == '{' and b[-1] == '}' and b[1:-1].find('{') == -1 and b[1:-1].find('}') == -1) or (b in grammar.letVars) :  # literal? concatenate " and "
	    strip = str(b).lstrip('{')
	    b = strip.rstrip('}')

	    let,cond,ret, suff = genLetCondReturn(type,  [' \'"\',  ', b,  ',  \'"\'' ])
	    return let,cond, ret + ', ', suff

	elif b >= 2 and (b[0] == '$' or b[0] == '?'):  # var: return $+..
	    if b[0] == '?':
		b = b.lstrip('?')
		b = '$'+ b + ''

	    let,cond,ret,suff = genLetCondReturn(type,  [ b ])
	    return let,cond, ret + ', ', suff

	else:
            pattern = tokenize(b)
            # if the first element (modulo " and ') is the same as the original do not execute the replacement
            if (b >= 2 and (pattern[0].strip('"\'') != b.strip('"\''))):   # literal? concatenate " and "
                let,cond,ret, suff = genLetCondReturn(type,  pattern)
                return let,cond, ret + ', ', suff
            elif type == '_validPredicate':
                return "", "", b , ""
            else:
                return "", "", "  '"+ b + "',  ", ""



def listSearch(list_val):
    global var
    return list_val.strip() in var


def genLetCondReturn(type, value):
    global count

    if len(value) == 1:
	# do something
	var = value[0].strip()
	if listSearch(var): var = lowrewriter.prefix_var(var) + '_RDFTerm'
	let = ''
    else:
	var = '$' + type + `count`
	count = count + 1
	value_all = ''
	for s in value:
	    if listSearch(s): s = lowrewriter.prefix_var(s.strip()) + '_RDFTerm'
	    value_all = value_all + s

	let =  'let '+ var +' := _xsparql:_serialize((' +  value_all +')) \n'



    if listSearch(var):
	rdftype = var + '_NodeType'
	var = lowrewriter.prefix_var(var) + '_RDFTerm'
    else:
	rdftype = '""'


    cond = 'if ( _xsparql:'+type + '( ' + rdftype + ',  '+var+'  ) ) then (\n\t\t'
    suffix = ' ) else ""'


    return let, cond, var, suffix



def tokenize(string):
    
    typedRegexp = re.compile('(.*}")(\^\^|@)(.*)', re.DOTALL) # remove the lang and type part from construct literals
    typed = typedRegexp.split(string)
     
    if len(typed) > 1:
        string = typed[1].strip("\"")

#    tokens = re.split('([^{}]*)(?:({)([^{}]*)(})){1,2}([^{}]*)', string)

    regexp = re.compile('([^{}]*[^{}]*)', re.DOTALL)
    tokens = regexp.split(string)
    
    pattern = []
    enclosed = [False]
    sep = ''

    end = ''
    if tokens[0].strip(" \"") == '{' and tokens[-1].strip(" \"") == '}' and ':' not in tokens:
        pattern.append('\'"\' ')
        end = ", " + '\'"\''
        tokens = tokens[1:-1]
        sep=', '



    for tok in tokens:
        if tok.strip(" ") == '':
            continue
        elif tok == '{':
            if len(enclosed) > 1 and  enclosed[-1]:
                pattern.append(tok)
            enclosed.append(True)
            continue
        elif tok == '}':
            enclosed.pop()
            if len(enclosed) > 1 and enclosed[-1]:
                pattern.append(tok)
            continue
        else:
            if len(pattern) > 1 and pattern[-1] in ('{', '}'):
                sep = ''

            if len(enclosed) > 1 and  enclosed[-1]:
                pattern.append(sep + tok.strip("' "))
            else:
                pattern.append( sep + "'"+tok+"'")

        sep = ', '


    if end != '':
        pattern.append(end)

    if len(typed) > 1:
        pattern.append(sep + "\"" + ''.join(typed[2:]) + "\"")

    return pattern
