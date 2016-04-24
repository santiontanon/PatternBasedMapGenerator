
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mapgenerator.ContentLocationRecord;
import mapgenerator.PatternBasedLocationGenerator;
import mapgenerator.TilePattern;
import mapgenerator.constraints.BorderConstraint;
import mapgenerator.constraints.Constraint;
import mapgenerator.constraints.NotBorderConstraint;
import mapgenerator.constraints.RegularConstraint;
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
    
    public static int DEBUG = 0;
    
    static List<String> patternFileNames = new ArrayList<>();
    static List<String> symbolFileNames = new ArrayList<>();
    static List<Constraint> constraints = new ArrayList<>();
    static int widthInPatterns = 8;
    static int heightInPatterns = 6;
    static int patternWidth = 5;
    static int patternHeight = 5;
    static HashMap<Label,Double> multipliers = new HashMap<>();

    public static void main(String args[]) throws Exception {
        List<TilePattern> patterns = new ArrayList<>();
        HashMap<Label,Character> type2Symbol = new HashMap<>();
        HashMap<Character,Label> symbol2Type = new HashMap<>();
                
        if (args.length==0 || args.length>2) {
            printInstructions();
            System.exit(0);
        }
        
        String inputFileName = null;
        String outputFileName = null;
        
        inputFileName = args[0];
        if (args.length>1) outputFileName = args[1];
        
        loadInputConfiguration(inputFileName);
        
        symbol2Type.put(TilePattern.EMPTY_TILE,new Label("empty"));
        type2Symbol.put(new Label("empty"), TilePattern.EMPTY_TILE);
        for(String fn:symbolFileNames) {
            loadSymbols(fn, type2Symbol, symbol2Type);
        }
        for(String fn:patternFileNames) {
            List<TilePattern> l = loadTilePatterns(fn);
            for(TilePattern tp:l) {
                if (!tp.checkForUndefinedSymbols(type2Symbol, symbol2Type)) {
                    System.out.println("Error loading pattern file: " + fn);
                    System.exit(1);
                }
                patterns.add(tp);
            }
        }
                                                
        PatternBasedLocationGenerator generator = new PatternBasedLocationGenerator(patterns, type2Symbol, symbol2Type);
        
        PatternBasedLocationGenerator.DEBUG = 1;
        TilePattern result = generator.generate(widthInPatterns, heightInPatterns, patternWidth, patternHeight, constraints, multipliers);
        
        if (outputFileName==null) {
            XMLWriter w = new XMLWriter(new OutputStreamWriter(System.out));
            if (result!=null) result.writeToXML(w);
            w.close();
        } else {
            XMLWriter w = new XMLWriter(new FileWriter(outputFileName));
            if (result!=null) result.writeToXML(w);
            w.close();
        }
    }
    
    
    public static void printInstructions() {
        System.out.println("Pattern-Based Map Generator (PBMG) v1.0 by Santiago Ontañón (2016)");
        System.out.println("");
        System.out.println("This tool uses a pattern-based approach to generate two-dimensional maps. ");
        System.out.println("");
        System.out.println("Example usage: java -classpath PBMG.jar Main examples/sampleInput.xml examples/output.xml");
        System.out.println("");
        System.out.println("The output file name is optional, and if not specified, the generated map will be just printed to standard output.");
        System.out.println("");
    }
    
    
    public static void loadInputConfiguration(String fileName) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Element root = builder.build(fileName).getRootElement();
        if (DEBUG>=1) System.out.println("Loading file '" + fileName + "'...");
        
        for(Object o:root.getChildren("symbolFile")) {
            Element e = (Element)o;
            symbolFileNames.add(e.getAttributeValue("name"));
        }
        for(Object o:root.getChildren("patternsfile")) {
            Element e = (Element)o;
            patternFileNames.add(e.getAttributeValue("name"));
        }
        
        Element ps_e = root.getChild("patternSize");
        patternWidth = Integer.parseInt(ps_e.getAttributeValue("width"));
        patternHeight = Integer.parseInt(ps_e.getAttributeValue("height"));

        Element ms_e = root.getChild("mapSize");
        widthInPatterns = Integer.parseInt(ms_e.getAttributeValue("width"));
        heightInPatterns = Integer.parseInt(ms_e.getAttributeValue("height"));
        
        // load constraints:
        /*
        constraints.add(new RegularConstraint(TilePattern.TYPE, new Label("forest")));
        constraints.add(new BorderConstraint(new Label("outer-wall")));
        constraints.add(new NotBorderConstraint(new Label("outer-wall"), false));
        */
        
        // load multipliers:
        /*
        ...
        */
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
