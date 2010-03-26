target =  ../webapp/stylesheets/

all : $(target)root.xsl

$(target)%.xsl : %.pxsl
	pxslcc -x $< > $@
