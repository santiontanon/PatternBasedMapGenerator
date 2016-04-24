
import java.io.FileWriter;
import java.io.OutputStreamWriter;
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
import util.XMLWriter;

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
        HashMap<Label,Character> type2Symbol = new HashMap<>();
        HashMap<Character,Label> symbol2Type = new HashMap<>();
        
        List<String> patternFileNames = new ArrayList<>();
        patternFileNames.add("data/patternsForest.xml");
        patternFileNames.add("data/patternsCastle.xml");
        patternFileNames.add("data/patternsHouse.xml");
        patternFileNames.add("data/patternsVillage.xml");
        
        loadSymbols("data/symbols.xml", type2Symbol, symbol2Type);
        for(String fn:patternFileNames) {
            List<TilePattern> l = loadTilePatterns(fn);
            patterns.addAll(l);
        }
        
        symbol2Type.put(TilePattern.EMPTY_TILE,new Label("empty"));
        type2Symbol.put(new Label("empty"), TilePattern.EMPTY_TILE);
        for(TilePattern tp:patterns) tp.checkForUndefinedSymbols(type2Symbol, symbol2Type);
                
        List<Label> typeTags = new ArrayList<>();
        typeTags.add(new Label("forest"));
        HashMap<Label,Double> multipliers = new HashMap<>();
        
        PatternBasedLocationGenerator generator = new PatternBasedLocationGenerator(patterns, type2Symbol, symbol2Type);
        
//        PatternBasedLocationGenerator.DEBUG = 1;
        TilePattern result = generator.generate(5, 4, 5, 5, typeTags, multipliers);
        
        XMLWriter w = new XMLWriter(new OutputStreamWriter(System.out));
        if (result!=null) result.writeToXML(w);
        w.close();
    }
    
    
    public static void loadSymbols(String fileName,
                                   HashMap<Label,Character> type2Symbol,
                                   HashMap<Character,Label> symbol2Type) throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        Element root = builder.build(fileName).getRootElement();
        if (DEBUG>=1) System.out.println("Loading file '" + fileName + "'...");

        for(Object o:root.getChildren("tile")) {
            Element e = (Element)o;
            symbol2Type.put(e.getAttributeValue("symbol").charAt(0), new Label(e.getAttributeValue("type")));
            type2Symbol.put(new Label(e.getAttributeValue("type")), e.getAttributeValue("symbol").charAt(0));
        }        
    }
    
    
    public static List<TilePattern> loadTilePatterns(String fileName) throws Exception
    {
        List<TilePattern> patterns = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        Element root = builder.build(fileName).getRootElement();
        if (DEBUG>=1) System.out.println("Loading file '" + fileName + "'...");

        for(Object o:root.getChildren("pattern")) {
            Element e = (Element)o;
            TilePattern p = TilePattern.fromXML(e);
            if (p.getCanRotate()) {
                // generate rotations:
                p.setCanRotate(false);
                patterns.add(p);
                for(int i = 0;i<3;i++) {
                    p = p.rotateClockWise();
                    patterns.add(p);
                }
            } else {
                patterns.add(p);            
            }
        }
        if (DEBUG>=1) System.out.println("" + patterns.size() + " loaded.");
    
        return patterns;
    }
    
    
    static TilePattern translateOutput(Pair<char[][][], List<ContentLocationRecord>> result) {
        return null;
    }
}
