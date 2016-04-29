/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapgenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import mapgenerator.constraints.ApplyToAllConstraint;
import mapgenerator.constraints.BorderConstraint;
import mapgenerator.constraints.Constraint;
import mapgenerator.constraints.NotBorderConstraint;
import mapgenerator.constraints.PathConstraint;
import mapgenerator.constraints.SinglePatternConstraint;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import pngrenderer.PNGRenderer;
import util.Label;
import util.Pair;
import util.XMLWriter;


/**
 *
 * @author santi
 */
public class PatternBasedMapGenerator {
    public static int DEBUG = 0;
    
    List<String> patternFileNames = new ArrayList<>();
    List<String> symbolFileNames = new ArrayList<>();
    List<Constraint> constraints = new ArrayList<>();
    int patternWidth = 5;
    int patternHeight = 5;
    HashMap<Label,Double> multipliers = new HashMap<>();
    List<TilePattern> patterns = new ArrayList<>();
    HashMap<Label,Character> type2Symbol = new HashMap<>();
    HashMap<Character,Label> symbol2Type = new HashMap<>();

    ConstraintSolver generator = null;
    PNGRenderer renderer = null;

    public PatternBasedMapGenerator(String configFileName, boolean initializePNGrenderer) throws Exception {
        
        loadInputConfiguration(configFileName);
        
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
                                                
        generator = new ConstraintSolver(patterns, type2Symbol, symbol2Type);        
        if (initializePNGrenderer) {
            renderer = new PNGRenderer();
        }
    }
    
    
    public TilePattern generate(int widthInPatterns, int heightInPatterns) throws Exception {
        return generator.generate(widthInPatterns, heightInPatterns, 
                                 patternWidth, patternHeight, 
                                 constraints, multipliers);
    }
    
    
    public TilePattern generate(int widthInPatterns, int heightInPatterns,
                                List<Constraint> additionalConstraints,
                                HashMap<Label,Double> additionalMultipliers) throws Exception {
        List<Constraint> finalConstraints = new ArrayList<>();
        finalConstraints.addAll(constraints);
        finalConstraints.addAll(additionalConstraints);
        
        HashMap<Label,Double> finalMultipliers = new HashMap<>();
        finalMultipliers.putAll(multipliers);
        for(Label ml:additionalMultipliers.keySet()) {
            Double mv = finalMultipliers.get(ml);
            if (mv==null) {
                finalMultipliers.put(ml, additionalMultipliers.get(ml));
            } else {
                finalMultipliers.put(ml, mv * additionalMultipliers.get(ml));
            }
        }
        
        return generator.generate(widthInPatterns, heightInPatterns, 
                                 patternWidth, patternHeight, 
                                 finalConstraints, finalMultipliers);
    }    
    
    
    public BufferedImage renderPNG(TilePattern result) throws Exception {
        return renderer.render(result, symbol2Type);
    }
    
           
    public void loadInputConfiguration(String fileName) throws Exception {
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
        
        // load constraints:
        Element c_e_l = root.getChild("constraints");
        if (c_e_l!=null) {
            for(Object o:c_e_l.getChildren()) {
                Element c_e = (Element)o;
                String type_att = c_e.getAttributeValue("type");
                String tag_att = c_e.getAttributeValue("tag");
                int type = -1;
                if (type_att!=null) {
                    if (type_att.equals("type")) type = TilePattern.TYPE;
                    if (type_att.equals("tag")) type = TilePattern.TAG;
                    if (type_att.equals("north")) type = TilePattern.NORTH;
                    if (type_att.equals("east")) type = TilePattern.EAST;
                    if (type_att.equals("south")) type = TilePattern.SOUTH;
                    if (type_att.equals("west")) type = TilePattern.WEST;
                }
                List<Label> tags = new ArrayList<>();
                List<Label> negativeTags = new ArrayList<>();
                if (tag_att!=null) {
                    StringTokenizer st = new StringTokenizer(tag_att,", ");
                    while(st.hasMoreTokens()) {
                        String tag = st.nextToken();
                        if (tag.startsWith("~")) {
                            negativeTags.add(new Label(tag.substring(1)));
                        } else {
                            tags.add(new Label(tag));
                        }
                    }
                }

                if (c_e.getName().equals("applyToAllConstraint")) {
                    ApplyToAllConstraint c = new ApplyToAllConstraint(type);
                    for(Label t:tags) c.addTag(t);
                    for(Label t:negativeTags) c.addNegativeTag(t);
                    constraints.add(c);
                } else if (c_e.getName().equals("borderConstraint")) {
                    BorderConstraint c = new BorderConstraint();
                    for(Label t:tags) c.addTag(t);
                    for(Label t:negativeTags) c.addNegativeTag(t);
                    constraints.add(c);
                } else if (c_e.getName().equals("notBorderConstraint")) {
                    NotBorderConstraint c = new NotBorderConstraint();
                    for(Label t:tags) c.addTag(t);
                    for(Label t:negativeTags) c.addNegativeTag(t);
                    constraints.add(c);
                } else if (c_e.getName().equals("singlePatternConstraint")) {
                    SinglePatternConstraint c = new SinglePatternConstraint(type);
                    for(Label t:tags) c.addTag(t);
                    for(Label t:negativeTags) c.addNegativeTag(t);
                    if (c_e.getAttributeValue("x")!=null) c.setX(Integer.parseInt(c_e.getAttributeValue("x")));
                    if (c_e.getAttributeValue("y")!=null) c.setY(Integer.parseInt(c_e.getAttributeValue("y")));
                    if (c_e.getAttributeValue("id")!=null) c.setID(c_e.getAttributeValue("id"));
                    constraints.add(c);
                } else if (c_e.getName().equals("pathConstraint")) {
                    PathConstraint c = new PathConstraint();
                    String patterns = c_e.getAttributeValue("patterns");
                    StringTokenizer st = new StringTokenizer(patterns,", ");
                    while(st.hasMoreTokens()) c.addID(st.nextToken());
                    constraints.add(c);
                }
            }
        }

        if (DEBUG>=1) System.out.println(constraints.size() + " constraints loaded.");

        // load multipliers:
        for(Object o:root.getChildren("multiplier")) {
            Element m_e = (Element)o;
            multipliers.put(new Label(m_e.getAttributeValue("tag")),
                            Double.parseDouble(m_e.getAttributeValue("factor")));
        }
        
        if (renderer!=null) {
            // load tile graphic mappings:
            Element gom_e = root.getChild("graphicOutputMapping");
            if (gom_e!=null) renderer.parseXMLConfig(gom_e);
        }
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
    

    public char typeToSymbol(String type) {
       return type2Symbol.get(type);
    }

    
    public Label symbolToType(char symbol) {
        return symbol2Type.get(symbol);
    }
}
