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

/**
 *
 * @author santi
 */
public class TilePattern {
    public static int DEBUG = 0;

    public static int NLAYERS = 2;
    
    public static char EMPTY_TILE = ' ';
    
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int TAG = 4;
    public static final int TYPE = 5;
    
    int dx;
    int dy;
    int priority;
    double weight;
    char [][][]pattern;
    List<ContentLocationRecord> objects;
    
    List<Label> typeTags = null;
    
    // connections;
    List<Label> north = new ArrayList<>();
    List<Label> east = new ArrayList<>();
    List<Label> south = new ArrayList<>();
    List<Label> west = new ArrayList<>();
    List<Label> contentTags = new ArrayList<>();
    
    
    public TilePattern(int a_dx, int a_dy) {
        dx = a_dx;
        dy = a_dy;
        pattern = new char[NLAYERS][a_dx][a_dy];
        objects = new ArrayList<>();
    }

    // only the "objects" list is cloned, since that will be modified by the pattern-based generator
    public TilePattern(TilePattern p) {
        dx = p.dx;
        dy = p.dy;
        priority = p.priority;
        weight = p.weight;
        pattern = p.pattern;
        objects = new ArrayList<>();
        objects.addAll(p.objects);

        north = new ArrayList<>();;
        north.addAll(p.north);

        east = new ArrayList<>();;
        east.addAll(p.east);

        south = new ArrayList<>();;
        south.addAll(p.south);

        west = new ArrayList<>();;
        west.addAll(p.west);

        contentTags = new ArrayList<>();;
        contentTags.addAll(p.contentTags);
    }

    public static TilePattern fromXML(Element e) throws Exception {
        TilePattern t = new TilePattern(Integer.parseInt(e.getAttributeValue("dx")),
                                        Integer.parseInt(e.getAttributeValue("dy")));
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
        st = new StringTokenizer(e.getAttributeValue("tag"),", ");
        while(st.hasMoreTokens()) t.contentTags.add(new Label(st.nextToken()));
        
        String data = e.getChildText("data");        
        String []lines = data.split("\n");
//        for(int i = 0;i<lines.length;i++) lines[i] = lines[i].trim();        
        for(int i = 0,ii=0;i<lines.length;i++) {
            st = new StringTokenizer(lines[i],"\t");
            int layer = 0;
            while(st.hasMoreTokens()) {
                String line = st.nextToken();
                for(int j = 0;j<line.length();j++) {
                    t.pattern[layer][j][ii] = line.charAt(j);
//                    System.out.print((int)t.pattern[layer][j][ii] + " ");
                }
//                System.out.println("");
                layer++;
            }
            if (layer!=0) ii++;
        }

        for(Object o:e.getChildren("object")) {
            Element e2 = (Element)o;
            t.objects.add(ContentLocationRecord.fromXML(e2));
        }
        
        return t;
    }    
    
    public TilePattern rotateClockWise() {
        TilePattern p = new TilePattern(dy, dx);
        p.typeTags = new ArrayList<Label>();
        p.typeTags.addAll(typeTags);
        p.priority = priority;
        p.weight = weight;
        for(int l = 0;l<NLAYERS;l++) {
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
        p.contentTags = contentTags;

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
    
    public char[][][] getPattern() {
        return pattern;
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

    
    boolean satisfiesAtLeastOneConstraint(int direction, List<Label> constraints) {
        for(Label c:constraints) {
            if (satisfiesConstraint(direction, c)) return true;
        }
        return false;
    }

    
    boolean satisfiesConstraint(int direction, Label constraint) {
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

    boolean uniquelySatisfiesConstraint(int direction, Label constraint) {
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


    public void precomputePathTags() throws Exception {
        char buffer[][] = new char[dx][dy];
        if (DEBUG>=1) System.out.println("TilePattern.precomputePathTags:");
        for(int i = 0;i<dy;i++) {
            for(int j = 0;j<dx;j++) {
                buffer[j][i] = (walkable(j,i) ? ' ':'x');
                if (DEBUG>=1) System.out.print(buffer[j][i]);
            }
            if (DEBUG>=1) System.out.println("");
        }


        // mark the left/up/right/down entrances:
        for(int i = 1;i<dy-1;i++) {
            if (buffer[0][i]==' ') buffer[0][i]='l';
            if (buffer[dx-1][i]==' ') buffer[dx-1][i]='r';
        }
        for(int i = 1;i<dx-1;i++) {
            if (buffer[i][0]==' ') buffer[i][0]='u';
            if (buffer[i][dx-1]==' ') buffer[i][dx-1]='d';
        }

        // find if there is path between them:
        char directions[]={'l','u','r','d'};
        for(int d1 = 0;d1<directions.length;d1++) {
            for(int d2 = d1+1;d2<directions.length;d2++) {
//                if (pathBetween(buffer, directions[d1], directions[d2])) {
                if (pathBetweenEach(buffer, directions[d1], directions[d2])) {
                    if (DEBUG>=1) System.out.println("  path-"+directions[d1]+'-'+directions[d2]);
                    contentTags.add(new Label("path-"+directions[d1]+'-'+directions[d2]));
                }
            }
        }
    }

    public boolean pathBetween(char buffer_in[][], char d1, char d2)
    {
        int dx = buffer_in.length;
        int dy = buffer_in[0].length;
        char buffer[][] = new char[dx][dy];
        List<Integer> stack = new ArrayList<>();
        for(int i = 0;i<dy;i++) {
            for (int j = 0; j < dx; j++) {
                buffer[j][i] = buffer_in[j][i];
                if (buffer[j][i]==d1) stack.add(j+i*dx);
            }
        }
        while(!stack.isEmpty()) {
            int tmp = stack.remove(0);
            int x = tmp%dx;
            int y = tmp/dx;
            if (buffer[x][y]==d2) return true;
            if (buffer[x][y]!='x') {
                buffer[x][y]=d1;
                if (x > 0 && buffer[x-1][y]!=d1) stack.add((x-1)+y*dx);
                if (y > 0 && buffer[x][y-1]!=d1) stack.add(x+(y-1)*dx);
                if (x < dx-1 && buffer[x+1][y]!=d1) stack.add((x+1)+y*dx);
                if (y < dy-1 && buffer[x][y+1]!=d1) stack.add(x+(y+1)*dx);
            }
        }
        return false;
    }

    // this returns true if there is apath between EACH cell labeled as d1, and EACH cell labeled as d2
    public boolean pathBetweenEach(char buffer_in[][], char d1, char d2)
    {
        int dx = buffer_in.length;
        int dy = buffer_in[0].length;
        char buffer[][] = new char[dx][dy];
        List<Integer> d1_positions = new ArrayList<>();
        List<Integer> d2_positions = new ArrayList<>();
        for(int i = 0;i<dy;i++) {
            for (int j = 0; j < dx; j++) {
                buffer[j][i] = buffer_in[j][i];
                if (buffer[j][i]==d1) {
                    d1_positions.add(j+i*dx);
                    buffer[j][i] = ' ';
                }
                if (buffer[j][i]==d2) {
                    d2_positions.add(j+i*dx);
                    buffer[j][i] = ' ';
                }
            }
        }
        if (d1_positions.isEmpty() || d2_positions.isEmpty()) return false;
        for(int d1_pos:d1_positions) {
            buffer[d1_pos%dx][d1_pos/dx] = d1;
            for(int d2_pos:d2_positions) {
                buffer[d2_pos%dx][d2_pos/dx] = d2;
                if (!pathBetween(buffer,d1,d2)) return false;
                buffer[d2_pos%dx][d2_pos/dx] = ' ';
            }
            buffer[d1_pos%dx][d1_pos/dx] = ' ';
        }
        return true;
    }


    // TODO: this is right now hardcoded, I should come up with a list of things that are
    //       not walkable from the ontology and processing the character to concept mappings
    public boolean walkable(int x, int y) {
        for(int layer = 0;layer<NLAYERS;layer++) {
            if (pattern[layer][x][y]=='~') return false;
            if (pattern[layer][x][y]=='#') return false;
            if (pattern[layer][x][y]=='@') return false;
            if (pattern[layer][x][y]=='0') return false;
            if (pattern[layer][x][y]=='b') return false;
            if (pattern[layer][x][y]=='e') return false;
            if (pattern[layer][x][y]=='p') return false;
            if (pattern[layer][x][y]=='r') return false;
            if (pattern[layer][x][y]=='t') return false;
            if (pattern[layer][x][y]=='w') return false;
            if (pattern[layer][x][y]=='A') return false;
            if (pattern[layer][x][y]=='K') return false;
            if (pattern[layer][x][y]=='W') return false;
        }
        return true;
    }
}
