
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mapgenerator.ContentLocationRecord;
import mapgenerator.PatternBasedLocationGenerator;
import mapgenerator.TilePattern;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import util.Label;
import util.Pair;

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
    
    public static void main(String args[]) throws Exception {
        List<TilePattern> patterns = new ArrayList<>();
        HashMap<String,Character> type2Symbol = new HashMap<>();
        HashMap<Character,String> symbol2Type = new HashMap<>();
        
        List<String> patternFileNames = new ArrayList<>();
        patternFileNames.add("data/patternsForest.xml");
        patternFileNames.add("data/patternsCastle.xml");
        patternFileNames.add("data/patternsHouse.xml");
        patternFileNames.add("data/patternsVillage.xml");
        
        for(String fn:patternFileNames) {
            List<TilePattern> l = loadTilePatterns(fn, type2Symbol, symbol2Type);
            patterns.addAll(l);
        }
        
        List<Label> typeTags = new ArrayList<>();
        typeTags.add(new Label("castle"));
        HashMap<Label,Double> multipliers = new HashMap<>();
        
        PatternBasedLocationGenerator generator = new PatternBasedLocationGenerator(patterns, type2Symbol, symbol2Type);
        
        PatternBasedLocationGenerator.DEBUG = 1;
        Pair<char [][][],List<ContentLocationRecord>> result = generator.generate(5, 4, 5, 5, typeTags, multipliers);
        
        TilePattern resultPattern = translateOutput(result);
        
        System.out.println(resultPattern);
    }
    
    
    public static List<TilePattern> loadTilePatterns(String fileName,
                                                     HashMap<String,Character> type2Symbol,
                                                     HashMap<Character,String> symbol2Type) throws Exception
    {
        List<TilePattern> patterns = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        Element root = builder.build(fileName).getRootElement();
        if (DEBUG>=1) System.out.println("Loading file '" + fileName + "'...");

        for(Object o:root.getChildren("tile")) {
            Element e = (Element)o;
            symbol2Type.put(e.getAttributeValue("symbol").charAt(0), e.getAttributeValue("type"));
            type2Symbol.put(e.getAttributeValue("type"), e.getAttributeValue("symbol").charAt(0));
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

    static TilePattern translateOutput(Pair<char[][][], List<ContentLocationRecord>> result) {
        return null;
    }
}
