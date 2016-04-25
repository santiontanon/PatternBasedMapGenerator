/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapgenerator;

import java.util.HashMap;
import util.Pair;
import util.XMLWriter;


/**
 *
 * @author santi
 */
public class TilePatternWithVariables extends TilePattern {
    
    HashMap<String, Pair<Integer,Integer>> locationVariableBindings = new HashMap<>();
    
    public TilePatternWithVariables(int a_dx, int a_dy, int a_layers) {
        super(a_dx, a_dy, a_layers);
    }
    
    public void addVariableBinding(String v, Pair<Integer,Integer> b) {
        locationVariableBindings.put(v, b);
    }
    
    
    public HashMap<String, Pair<Integer,Integer>> getVariableBindings() {
        return locationVariableBindings;
    } 
    
    
    public void writeToXML(XMLWriter w) {
        writeOpenTagToXML(w);
        writeBodyToXML(w);
        for(String v:locationVariableBindings.keySet()) {
            Pair<Integer,Integer> b = locationVariableBindings.get(v);
            w.rawXMLRespectingTabs("<locationvariable name=\""+v+"\" x=\""+b.m_a+"\" y=\""+b.m_b+"\"/>");
        }
        writeClosingTagToXML(w);
    }
    
}
