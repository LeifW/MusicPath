#!/bin/sh
FORMAT=svg
roqet bands2nodes.sparql -D $1 -r dot | sed -e 's/digraph/graph/;s/->/--/' | circo -T$FORMAT -o`echo $1 | cut -d/ -f 4`.$FORMAT
