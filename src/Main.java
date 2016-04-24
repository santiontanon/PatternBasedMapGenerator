
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static mapgenerator.PatternBasedLocationGenerator.DEBUG;
import mapgenerator.TilePattern;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author santi
 */
public class Main {
    
    public static int DEBUG = 1;
    
    public static int main(String args[]) {
        
        // ...
        
        return 0;
    }
    
    
    public static List<TilePattern> loadTilePatterns(String fileName,
                                                     HashMap<String,Character> typeSymbols,
                                                     HashMap<Character,String> symbolTypes) throws Exception
    {
        List<TilePattern> patterns = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        Element root = builder.build(fileName).getRootElement();
        if (DEBUG>=1) System.out.println("Loading file '" + fileName + "'...");

        for(Object o:root.getChildren("tile")) {
            Element e = (Element)o;
            symbolTypes.put(e.getAttributeValue("symbol").charAt(0), e.getAttributeValue("type"));
            typeSymbols.put(e.getAttributeValue("type"), e.getAttributeValue("symbol").charAt(0));
        }
        for(Object o:root.getChildren("pattern")) {
            Element e = (Element)o;
            TilePattern p = TilePattern.fromXML(e);
            patterns.add(p);
            if (e.getAttributeValue("canrotate").equals("true")) {
                // generate rotations:
                for(int i = 0;i<3;i++) {
                    p = p.rotateClockWise();
                    patterns.add(p);
                }
            }
        }
        if (DEBUG>=1) System.out.println("" + patterns.size() + " loaded.");
        for(TilePattern p:patterns) {
            p.precomputePathTags();
        }
    
        return patterns;
    }
}
