/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2015 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */
package spade.storage;

import java.io.FileWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;
import spade.core.AbstractEdge;
import spade.core.AbstractStorage;
import spade.core.AbstractVertex;
import spade.utility.Execute;
import java.util.List;
/**
 * A storage implementation that writes data to a DOT file.
 *
 * @author Dawood Tariq
 */
public class Graphviz extends AbstractStorage {

    private FileWriter outputFile;
    private final int TRANSACTION_LIMIT = 1000;
    private int transaction_count;
    private String filePath;

    @Override
    public boolean initialize(String arguments) {
        try {
            if (arguments == null) {
                return false;
            }
            filePath = arguments;
            outputFile = new FileWriter(filePath, false);
            transaction_count = 0;
            outputFile.write("digraph spade2dot {\n"
                    + "graph [rankdir = \"RL\"];\n"
                    + "node [fontname=\"Helvetica\" fontsize=\"8\" style=\"filled\" margin=\"0.0,0.0\"];\n"
                    + "edge [fontname=\"Helvetica\" fontsize=\"8\"];\n");
            return true;
        } catch (Exception exception) {
            Logger.getLogger(Graphviz.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
    }

    private void checkTransactions() {
        transaction_count++;
        if (transaction_count == TRANSACTION_LIMIT) {
            try {
                outputFile.flush();
                outputFile.close();
                outputFile = new FileWriter(filePath, true);
                transaction_count = 0;
            } catch (Exception exception) {
                Logger.getLogger(Graphviz.class.getName()).log(Level.SEVERE, null, exception);
            }
        }
    }

    @Override
    public boolean putVertex(AbstractVertex incomingVertex) {
        try {
            StringBuilder annotationString = new StringBuilder();
            for (Map.Entry<String, String> currentEntry : incomingVertex.getAnnotations().entrySet()) {
                String key = currentEntry.getKey();
                String value = currentEntry.getValue();
                if (key == null || value == null) {
                    continue;
                }
		if (key == "epoch" || key == "source" || key == "egid" || key == "euid" || key == "start time" || key == "commandline" || key == "gid" || key == "cwd" || key == "key" || key == "subtype" || key == "version"){
		    continue;
		}
		
                annotationString.append(key);
                annotationString.append(":");
                annotationString.append(value);
                annotationString.append("\\n");
            }
            String vertexString = annotationString.substring(0, annotationString.length());
            String shape = "box";
            String color = "white";
            String type = incomingVertex.getAnnotation("type");
            if (type.equalsIgnoreCase("Agent") || type.equalsIgnoreCase("Principal")) {
                shape = "octagon";
                color = "rosybrown1";
            } else if (type.equalsIgnoreCase("Process") || type.equalsIgnoreCase("Activity") || type.equalsIgnoreCase("Subject")) {
                shape = "box";
                color = "lightsteelblue1";
		//Logger.getLogger(Graphviz.class.getName()).log(Level.INFO, "PUTTING process vertex");
            } else if (type.equalsIgnoreCase("Artifact") || type.equalsIgnoreCase("Entity") || type.equalsIgnoreCase("Object")) {
                shape = "ellipse";
                color = "khaki1";
                try {
                    String subtype = incomingVertex.getAnnotation("subtype");
                    if (subtype.equalsIgnoreCase("network")) {
                        shape = "diamond";
                        color = "palegreen1";
                    }
                } catch (Exception exception) {
                    // Ignore
                }
            } else if(type.equalsIgnoreCase("Event")){
            	shape = "doublecircle";
            	color = "red";
            }

            String key = Hex.encodeHexString(incomingVertex.bigHashCode());
            outputFile.write("\"" + key + "\" [label=\"\\n" + vertexString.replace("\"", "'") + "\" shape=\"" + shape + "\" fillcolor=\"" + color + "\"];\n");
            checkTransactions();
            return true;
        } catch (Exception exception) {
            Logger.getLogger(Graphviz.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
    }

    @Override
    public boolean putEdge(AbstractEdge incomingEdge) {
        try {
            StringBuilder annotationString = new StringBuilder();
            for (Map.Entry<String, String> currentEntry : incomingEdge.getAnnotations().entrySet()) {
                String key = currentEntry.getKey();
                String value = currentEntry.getValue();
                if (key == null || value == null) {
                    continue;
                }
		if (key == "event id" || key == "time" || key == "source"){
		    continue;
		}
                annotationString.append(key);
                annotationString.append(":");
                annotationString.append(value);
                annotationString.append("\\n");
            }
            String color = "black";
            String type = incomingEdge.getAnnotation("type");
            if (type.equalsIgnoreCase("Used")) {
                color = "green";
            } else if (type.equalsIgnoreCase("WasGeneratedBy")) {
                color = "red";
            } else if (type.equalsIgnoreCase("WasTriggeredBy") || type.equalsIgnoreCase("WasInformedBy")) {
                color = "blue";
            } else if (type.equalsIgnoreCase("WasControlledBy") || type.equalsIgnoreCase("WasAssociatedWith")) {
                color = "purple";
            } else if (type.equalsIgnoreCase("WasDerivedFrom")) {
                color = "orange";
            } else if(type.equalsIgnoreCase("SimpleEdge")){
            	color = "black";
            }

            String style = "solid";
            if (incomingEdge.getAnnotation("success") != null && incomingEdge.getAnnotation("success").equals("false")) {
                style = "dashed";
            }

            String edgeString = annotationString.toString();
            if (edgeString.length() > 0) {
                edgeString = "(" + edgeString.substring(0, edgeString.length() - 2) + ")";
            }

            String srckey = Hex.encodeHexString(incomingEdge.getSourceVertex().bigHashCode());
            String dstkey = Hex.encodeHexString(incomingEdge.getDestinationVertex().bigHashCode());

            outputFile.write("\"" + srckey + "\" -> \"" + dstkey + "\" [label=\"" + edgeString.replace("\"", "'") + "\" color=\"" + color + "\" style=\"" + style + "\"];\n");
            checkTransactions();
            return true;
        } catch (Exception exception) {
            Logger.getLogger(Graphviz.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
    }

    @Override
    public boolean shutdown() {
        try {
            outputFile.write("}\n");
            outputFile.close();
	    String cmd = "/usr/local/bin/python3 draw_containers.py " + filePath + " /tmp/output.dot";
	    Logger.getLogger(Graphviz.class.getName()).log(Level.INFO, cmd);
	    List<String> cmdOutput = Execute.getOutput(cmd);
            return true;
        } catch (Exception exception) {
            Logger.getLogger(Graphviz.class.getName()).log(Level.SEVERE, null, exception);
            return false;
        }
    }
}
