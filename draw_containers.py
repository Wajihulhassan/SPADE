#!/usr/bin/env python3

import sys
import random
from collections import defaultdict
flag = False
if len(sys.argv) < 3:
    print("provide more arguments")

if len(sys.argv) == 4:
    flag = True

lines = []
lines_with_edges = []
lines_without_edges = []
lines_without_contid = []
nodes_map = defaultdict(list)

start = """digraph spade2dot {
graph [rankdir = "RL"];
node [fontname="Helvetica" fontsize="8" style="filled" margin="0.0,0.0"];
edge [fontname="Helvetica" fontsize="8"]; """
end_bracket = "}"

with open(sys.argv[1],'r') as fil:
    for l in fil:
        if '->' in l:
            lines_with_edges.append(l)
        else:
            lines_without_edges.append(l)

if flag:
    with open(sys.argv[3],'r') as fil:
        for l in fil:
            if '->' in l:
                lines_with_edges.append(l)
            else:
                lines_without_edges.append(l)


for line in lines_without_edges:
    if 'Cont_ID' in line:
        tokens = line.split('\\n')
        token = [x for x in tokens if x.startswith('Cont_ID')]
        spl = token[0].split(":")
        nodes_map[spl[1]].append(line)
    elif 'subtype:network' in line:
        lines_without_contid.append(line)

with open(sys.argv[2], 'w') as fil:
    fil.write(start)
    for l in lines_without_contid:
        fil.write(l)
    for key, val in nodes_map.items():
        color = [round(random.random(), 5) for x in range(0,3)]
        r = lambda: random.randint(0,255)
        hex_str = '#%02X%02X%02X' % (r(),r(),r())
        subgraph_start = "subgraph cluster_%s { label=\"UUID = %s\" color=\"%s86\" style=\"filled,dashed\" ;\n" % (str(key), str(key),hex_str)
        
        fil.write('\n' + subgraph_start + '\n')
        for l in val:
            fil.write(l)
        fil.write(end_bracket+'\n')
    for l in lines_with_edges:
        fil.write(l)
    fil.write(end_bracket+'\n')
