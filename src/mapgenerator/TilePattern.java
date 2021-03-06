/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapgenerator;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import util.Label;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class TilePattern {
    public static int DEBUG = 0;
    public static char directions[]={'w','n','e','s'};

    public static char EMPTY_TILE = ' ';
    
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int TAG = 4;
    public static final int TYPE = 5;
    
    int dx;
    int dy;
    boolean canRotate = false;
    int priority;
    double weight;
    char [][][]pattern;
    List<ContentLocationRecord> objects;
    
    List<Label> typeTags = new ArrayList<>();
    
    // connections;
    List<Label> north = new ArrayList<>();
    List<Label> east = new ArrayList<>();
    List<Label> south = new ArrayList<>();
    List<Label> west = new ArrayList<>();
    List<Label> contentTags = new ArrayList<>();
        
    public TilePattern(int a_dx, int a_dy, int a_layers) {
        dx = a_dx;
        dy = a_dy;
        pattern = new char[a_layers][a_dx][a_dy];
        objects = new ArrayList<>();
    }

    // only the "objects" list is cloned, since that will be modified by the pattern-based generator
    public TilePattern(TilePattern p) {
        dx = p.dx;
        dy = p.dy;
        canRotate = p.canRotate;
        priority = p.priority;
        weight = p.weight;
        pattern = p.pattern;
        objects = new ArrayList<>();
        objects.addAll(p.objects);

        north = new ArrayList<>();
        north.addAll(p.north);

        east = new ArrayList<>();
        east.addAll(p.east);

        south = new ArrayList<>();
        south.addAll(p.south);

        west = new ArrayList<>();
        west.addAll(p.west);

        contentTags = new ArrayList<>();
        contentTags.addAll(p.contentTags);
    }
    

    public static TilePattern fromXML(Element e) throws Exception {
        int nlayers = 0;
        List l = e.getChildren("layer");
        for(Object o:l) {
            Element le = (Element)o;
            int layer = Integer.parseInt(le.getAttributeValue("layer"));
            if (nlayers<layer+1) nlayers = layer+1;
        }
        
        TilePattern t = new TilePattern(Integer.parseInt(e.getAttributeValue("width")),
                                        Integer.parseInt(e.getAttributeValue("height")),
                                        nlayers);
        if (e.getAttributeValue("canrotate")!=null &&
            e.getAttributeValue("canrotate").equals("true")) t.canRotate = true;
        t.priority = Integer.parseInt(e.getAttributeValue("priority"));
        t.weight = Double.parseDouble(e.getAttributeValue("weight"));
        StringTokenizer st = new StringTokenizer(e.getAttributeValue("type"),", ");
        while(st.hasMoreTokens()) t.typeTags.add(new Label(st.nextToken()));
        st = new StringTokenizer(e.getAttributeValue("north"),", ");
        while(st.hasMoreTokens()) t.north.add(new Label(st.nextToken()));
        st = new StringTokenizer(e.getAttributeValue("east"),", ");
        while(st.hasMoreTokens()) t.east.add(new Label(st.nextToken()));
        st = new StringTokenizer(e.getAttributeValue("south"),", ");
        while(st.hasMoreTokens()) t.south.add(new Label(st.nextToken()));
        st = new StringTokenizer(e.getAttributeValue("west"),", ");
        while(st.hasMoreTokens()) t.west.add(new Label(st.nextToken()));
        String tag = e.getAttributeValue("tag");
        if (tag!=null) {
            st = new StringTokenizer(tag,", ");
            while(st.hasMoreTokens()) t.contentTags.add(new Label(st.nextToken()));
        }
        
        for(Object o:l) {
            Element le = (Element)o;
            int layer = Integer.parseInt(le.getAttributeValue("layer"));
            List lr = le.getChildren("row");
            int row_idx = 0;
            for(Object o2:lr) {
                Element rw = (Element)o2;
                String row = rw.getText();
                for(int i = 0;i<t.dx;i++) t.pattern[layer][i][row_idx] = row.charAt(i);
                row_idx ++;
            }
        }
        
        for(Object o:e.getChildren("object")) {
            Element e2 = (Element)o;
            t.objects.add(ContentLocationRecord.fromXML(e2));
        }
        
        return t;
    }    
    
    
    public void writeToXML(XMLWriter w) {
        writeOpenTagToXML(w);
        writeBodyToXML(w);
        writeClosingTagToXML(w);
    }
    
    
    // I separate this into three functions, so that it is easy to extend this class, and extend the writeToXML function:
    public void writeOpenTagToXML(XMLWriter w) {
        String attributeString = "width=\""+dx+"\" height=\""+dy+"\" " + 
                                 labelListToXMLAttribute("type", typeTags) + " " +
                                 labelListToXMLAttribute("tag", contentTags) + " " +
                                 labelListToXMLAttribute("north", north) + " " +
                                 labelListToXMLAttribute("east", east) + " " +
                                 labelListToXMLAttribute("south", south) + " " +
                                 labelListToXMLAttribute("west", west) + " " + 
                                 (canRotate ? "canrotate=\"true\" ":"") + 
                                 "priority=\""+priority+"\" weight=\""+weight+"\"";        
        w.tagWithAttributes("pattern", attributeString);
    }
    
    
    public void writeBodyToXML(XMLWriter w) {
        for(int k = 0;k<pattern.length;k++) {
            w.tagWithAttributes("layer","layer=\""+k+"\"");
            for(int i=0;i<dy;i++) {
                String row = "";
                for(int j = 0;j<dx;j++) row += pattern[k][j][i];
                w.rawXMLRespectingTabs("<row>"+row+"</row>");
            }
            w.tag("/layer");
        }
        for(ContentLocationRecord clr:objects) {
            w.rawXMLRespectingTabs("<object x=\""+clr.x+"\" y=\""+clr.y+"\" "+
                                            (clr.width>1 ? "width=\""+clr.width+"\" ":"") + 
                                            (clr.height>1 ? "height=\""+clr.height+"\" ":"") + 
                                            "type=\""+clr.type+"\"/>");   
        }
    }

    
    public void writeClosingTagToXML(XMLWriter w) {
        w.tag("/pattern");
    }
    
    String labelListToXMLAttribute(String attributeName, List<Label> ll) {
        String out = attributeName + "=\"";
        boolean first = true;
        for(Label l:ll) {
            if (first) {
                out+=l;
            } else {
                out+="," + l;
            }
            first = false;
        }
        return out + "\"";
    }
    
    
    public boolean checkForUndefinedSymbols(HashMap<Label,Character> type2Symbol,
                                            HashMap<Character,Label> symbol2Type) {
        for(int l = 0;l<pattern.length;l++) {
            for(int i = 0;i<dy;i++) {
                for(int j = 0;j<dx;j++) {
                    Label type = symbol2Type.get(pattern[l][j][i]);
                    if (type==null) {
                        System.out.println("Undefined symbol '" + pattern[l][j][i] + "'");
                        return false;
                    }
                }
            }
        }
        for(ContentLocationRecord clr:objects) {
            if (type2Symbol.get(clr.type)==null) {
                System.out.println("Undefined symbol '" + clr.type + "'");
                return false;                
            }
        }
        return true;
    }
    
    
    public TilePattern rotateClockWise() throws Exception {
        TilePattern p = new TilePattern(dy, dx, pattern.length);
        p.typeTags = new ArrayList<Label>();
        p.typeTags.addAll(typeTags);
        p.priority = priority;
        p.weight = weight;
        for(int l = 0;l<pattern.length;l++) {
//            System.out.println("l = " + l);
            for(int i = 0;i<dx;i++) {
                for(int j = 0;j<dy;j++) {
                    p.pattern[l][dy - (1 + j)][i] = pattern[l][i][j];
//                    System.out.print(p.pattern[l][dy - (1 + j)][i]);
                }
//                System.out.println("");
            }
        }
        
        p.north = west;
        p.east = north;
        p.south = east;
        p.west = south;
        p.contentTags = new ArrayList<>();
        // rotate the path tags:
        for(Label tag:contentTags) {
            boolean found = false;
            int dl = directions.length;
            for(int d1 = 0;d1<dl;d1++) {
                for(int d2 = d1+1;d2<dl;d2++) {
                    Label oldTag = new Label("path-"+directions[d1] + "-" + directions[d2]);
                    if (tag.equals(oldTag)) {
                        int newd1 = (d1+1)%dl;
                        int newd2 = (d2+1)%dl;
                        if (newd1>newd2) {
                            newd1 = (d2+1)%dl;
                            newd2 = (d1+1)%dl;
                        }
                        Label newTag = new Label("path-"+directions[newd1] + "-" + directions[newd2]);
                        found = true;
                        p.contentTags.add(newTag);
                    }
                }
            }
            if (!found) p.contentTags.add(tag);
        }
 
        for(ContentLocationRecord po:objects) {
            ContentLocationRecord po2 = new ContentLocationRecord(po);
            po2.x = (dx-1) - po.y;
            po2.y = po.x;
            p.objects.add(po2);
        }
        
        
        return p;
    }

    public int getDx() {
        return dx;
    }
    
    public int getDy() {
        return dy;
    }
    
    public int getNLayers() {
        return pattern.length;
    }
    
    public boolean getCanRotate() {
        return canRotate;
    }
    
    public void setCanRotate(boolean cr) {
        canRotate = cr;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public double getWeight(HashMap<Label,Double> multipliers) {
        double w = weight;
        for(Label tag:contentTags) {
            Double m = multipliers.get(tag);
            if (m!=null) w*=m;
        }
        return w;
    }
    
    public List<Label> getNorth() {
        return north;
    }
    
    public List<Label> getEast() {
        return east;
    }
    
    public List<Label> getSouth() {
        return south;
    }
    
    public List<Label> getWest() {
        return west;
    }
    
    public List<Label> getType() {
        return typeTags;
    }
    
    public char[][][] getPattern() {
        return pattern;
    }
    
    public List<ContentLocationRecord> getObjects() {
        return objects;
    }

    boolean matchesWestWith(TilePattern p) {
        for(Label tag:west) if (p.getEast().contains(tag)) return true;
        return false;
    }

    boolean matchesEastWith(TilePattern p) {
        for(Label tag:east) if (p.getWest().contains(tag)) return true;
        return false;
    }

    boolean matchesNorthWith(TilePattern p) {
        for(Label tag:north) if (p.getSouth().contains(tag)) return true;
        return false;
    }

    boolean matchesSouthWith(TilePattern p) {
        for(Label tag:south) if (p.getNorth().contains(tag)) return true;
        return false;
    }

    
    public boolean satisfiesAtLeastOneConstraint(int direction, List<Label> constraints) {
        for(Label c:constraints) {
            if (satisfiesConstraint(direction, c)) return true;
        }
        return false;
    }

    
    public boolean satisfiesConstraint(int direction, Label constraint) {
        switch(direction) {
            case NORTH: if (north.contains(constraint)) return true;
                        break;
            case EAST: if (east.contains(constraint)) return true;
                        break;
            case SOUTH: if (south.contains(constraint)) return true;
                        break;
            case WEST: if (west.contains(constraint)) return true;
                        break;
            case TAG: if (contentTags.contains(constraint)) return true;
                        break;
            case TYPE: if (typeTags.contains(constraint)) return true;
                        break;
        }
        return false;
    }

    public boolean uniquelySatisfiesConstraint(int direction, Label constraint) {
        switch(direction) {
            case NORTH: if (north.contains(constraint) && north.size()==1) return true;
                        break;
            case EAST: if (east.contains(constraint) && east.size()==1) return true;
                        break;
            case SOUTH: if (south.contains(constraint) && south.size()==1) return true;
                        break;
            case WEST: if (west.contains(constraint) && west.size()==1) return true;
                        break;
            case TAG: if (contentTags.contains(constraint) && contentTags.size()==1) return true;
                        break;
            case TYPE: if (typeTags.contains(constraint) && typeTags.size()==1) return true;
                        break;
        }
        return false;
    }

    void removeTag(int direction, Label constraint) {
        switch(direction) {
            case NORTH: north.remove(constraint);
                break;
            case EAST: east.remove(constraint);
                break;
            case SOUTH: south.remove(constraint);
                break;
            case WEST: west.remove(constraint);
                break;
            case TAG: contentTags.remove(constraint);
                break;
            case TYPE: typeTags.remove(constraint);
                break;
        }
    }
}
