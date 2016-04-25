/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapgenerator;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import util.Pair;
import util.Sampler;

import java.util.*;
import mapgenerator.constraints.BorderConstraint;
import mapgenerator.constraints.Constraint;
import mapgenerator.constraints.NotBorderConstraint;
import mapgenerator.constraints.ApplyToAllConstraint;
import util.Label;

/**
 *
 * @author santi
 */
public class PatternBasedLocationGenerator {
    public static int DEBUG = 0;

    public static boolean patternOverlap = false;
    
    HashMap<Label,Character> typeToSymbol_table = new HashMap<>();
    HashMap<Character,Label> symboltoType_table = new HashMap<>();
    List<TilePattern> patterns = new ArrayList<>();
    Random r = new Random();
    

    public PatternBasedLocationGenerator(List<TilePattern> a_patterns, 
                                         HashMap<Label, Character> a_typeToSymbol,
                                         HashMap<Character, Label> a_symbolTotype) {
        patterns = a_patterns;
        typeToSymbol_table = a_typeToSymbol;
        symboltoType_table = a_symbolTotype;
    }
    
    
    public char typeToSymbol(Label type) throws Exception {
        Character symbol = typeToSymbol_table.get(type);
        if (symbol==null) throw new Exception("No symbol for type '"+type+"'");
        return symbol;
    }
    
    public Label symbolToType(char symbol) {
        return symboltoType_table.get(symbol);
    }

    
    public TilePattern generate(int widthInPatterns, int heightInPatterns,
                                int patternWidth, int patternHeight,
                                List<Constraint> constraints,
                                HashMap<Label,Double> multipliers) throws Exception {
        int width = patternOverlap ? widthInPatterns*(patternWidth-1)+1 : widthInPatterns*patternWidth;
        int height = patternOverlap ? heightInPatterns*(patternHeight-1)+1 : heightInPatterns*patternHeight;
        int nlayers = 0;
        for(TilePattern tp:patterns) {
            if (tp.getNLayers()>nlayers) nlayers = tp.getNLayers();
        }
        char [][][]tiles = new char[nlayers][width][height];
        for(int l = 0;l < nlayers;l++) {
            for(int i = 0;i < height;i++) {
                for(int j = 0;j < width;j++) {
                    tiles[l][j][i] = TilePattern.EMPTY_TILE;
                }
            }
        }
        
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + ".generate: map is " + width + " * " + height + " tiles in size.");
 
        // initialize generator and apply type and border constraints:
        List<TilePattern> [][]possibilities = new List[widthInPatterns][heightInPatterns];
        TilePattern[][]selected = new TilePattern[widthInPatterns][heightInPatterns];
        for(int i = 0;i<heightInPatterns;i++) {
            for(int j = 0;j<widthInPatterns;j++) {
                possibilities[j][i] = new ArrayList<>();
                for(TilePattern p:patterns) {
                    boolean filtered = false;
                    if (p.getDx()!=patternWidth ||
                        p.getDy()!=patternHeight) filtered = true;
                    if (!filtered) {
                        for(Constraint c:constraints) {
                            if (c instanceof BorderConstraint) {
                                if (!((BorderConstraint)c).checkConstraint(p, j, i, widthInPatterns, heightInPatterns)) 
                                    filtered = true;
                            } else if (c instanceof NotBorderConstraint) {
                                if (!((NotBorderConstraint)c).checkConstraint(p, j, i, widthInPatterns, heightInPatterns)) 
                                    filtered = true;
                            } else if (c instanceof ApplyToAllConstraint) {
                                if (!((ApplyToAllConstraint)c).checkConstraint(p)) 
                                    filtered = true;
                            }
                            if (filtered) break;
                        }
                    }
                    if (!filtered) possibilities[j][i].add(p);
                }
                selected[j][i] = null;
            }
        }
                
        if (DEBUG>=1) {
            System.out.println("PatternBasedLocationGenerator.generate: Possibilities after initial constraints:");
            for(int i = 0;i<heightInPatterns;i++) {
                for(int j = 0;j<widthInPatterns;j++) {
                    if (possibilities[j][i]==null) {
                        System.out.print("- ");
                    } else {
                        System.out.print(possibilities[j][i].size() + " ");
                    }
                }
                System.out.println();
            }
        }
        
        
        // single pattern constraints:
        // ...

        
        if (DEBUG>=1) {
            System.out.println("PatternBasedLocationGenerator.generate: Possibilities after single pattern constraints:");
            for(int i = 0;i<heightInPatterns;i++) {
                for(int j = 0;j<widthInPatterns;j++) {
                    if (possibilities[j][i]==null) {
                        System.out.print("- ");
                    } else {
                        System.out.print(possibilities[j][i].size() + " ");
                    }
                }
                System.out.println();
            }
        }

        // path constraints:
        // ...

        
        if (DEBUG>=1) {
            System.out.println("PatternBasedLocationGenerator.generate: Possibilities after path constraints:");
            for(int i = 0;i<heightInPatterns;i++) {
                for(int j = 0;j<widthInPatterns;j++) {
                    if (possibilities[j][i]==null) {
                        System.out.print("- ");
                    } else {
                        System.out.print(possibilities[j][i].size() + " ");
                    }
                }
                System.out.println();
            }
        }
                        
        // generate:
        TilePattern[][]result = generateDFS(possibilities, selected, true, multipliers);

        if (result==null) {
            throw new Exception("PatternBasedLocationGenerator.generate: room could not be generated!");
        } else {
            if (DEBUG>=1) System.out.println("PatternBasedLocationGenerator.generate: room generated!");

            // clone the patterns, and remove duplicated objects in the edge of the patterns:
            for(int i = 0;i<heightInPatterns;i++) {
                for (int j = 0; j < widthInPatterns; j++) {
                    if (selected[j][i] != null) {
                        selected[j][i] = new TilePattern(selected[j][i]);
                        if (patternOverlap) {
                            if (j < widthInPatterns - 1 && selected[j + 1][i] != null) {
                                // remove all the objects along the right edge:
                                List<ContentLocationRecord> toDelete = new ArrayList<>();
                                for (ContentLocationRecord clr : selected[j][i].objects) {
                                    if (clr.x == widthInPatterns - 1) {
                                        toDelete.add(clr);
                                    }
                                }
                                selected[j][i].objects.removeAll(toDelete);
                            }
                            if (i < heightInPatterns - 1 && selected[j][i + 1] != null) {
                                // remove all the objects along the bottom edge:
                                List<ContentLocationRecord> toDelete = new ArrayList<>();
                                for (ContentLocationRecord clr : selected[j][i].objects) {
                                    if (clr.y == heightInPatterns - 1) {
                                        toDelete.add(clr);
                                    }
                                }
                                selected[j][i].objects.removeAll(toDelete);
                            }
                        }
                    }
                }
            }

            // generate the tiles:
            for(int i = 0;i<heightInPatterns;i++) {
                for(int j = 0;j<widthInPatterns;j++) {
                    if (selected[j][i]!=null) {
                        for(int l = 0;l< tiles.length;l++) {
                            for(int ii = 0;ii<patternHeight;ii++) {
                                for(int jj = 0;jj<patternWidth;jj++) {
                                    if (patternOverlap) {
                                        tiles[l][j * patternWidth + jj][i * patternHeight + ii] = selected[j][i].getPattern()[l][jj][ii];
                                    } else {
                                        tiles[l][j * patternWidth + jj][i * patternHeight + ii] = selected[j][i].getPattern()[l][jj][ii];

                                    }
                                }
                            }
                        }
                    } 
                }
            }
        }
                
        if (DEBUG>=1) {
            for(int i = 0;i<height;i++) {
                for(int j = 0;j<width;j++) {
                    for(int l = tiles.length-1;l>=0;l--) {
                        if (tiles[l][j][i]!= TilePattern.EMPTY_TILE || l==0) {
                            System.out.print(tiles[l][j][i]);
                            break;
                        }
                    }
                }
                System.out.println("");
            }
        }

        // find the specific locations for all the content:
        List<ContentLocationRecord> contentLocations = new LinkedList<>();
        /*
        for(ContentLocationRecord ccl:coarseContentLocations) {
            ContentLocationRecord cl = new ContentLocationRecord(ccl);
            List<String> candidates = new ArrayList<>();
            if (cl.n!=null &&
                (cl.n instanceof LGraphNode) &&
                ((LGraphNode)(cl.n)).subsumedBy( Ontology.lockLabel)) {
                candidates.add("door");
                candidates.add("castle-locked");
                candidates.add("darktower-locked");
            } else {
                candidates.add(cl.type);
            }

            TilePattern pattern = selected[ccl.x][ccl.y];
            ContentLocationRecord found = null;
            for(ContentLocationRecord cl2:pattern.objects) {
                if (candidates.contains(cl2.type)) {
                    // found!
                    if (patternOverlap) {
                        cl.x = ccl.x * (patternDx - 1) + cl2.x;
                        cl.y = ccl.y * (patternDy - 1) + cl2.y;
                    } else {
                        cl.x = ccl.x * patternDx + cl2.x;
                        cl.y = ccl.y * patternDy + cl2.y;
                    }
//                    System.out.println(cl2.type + ": " + cl2.width + "(" + cl2.x + "," + cl2.y + ")");
                    cl.width = cl2.width;
                    cl.height = cl2.height;
                    cl.type = cl2.type;
                    found = cl2;
                    break;
                }
            }
            if (found==null)
                throw new Exception("Cannot find a content location record for a coarse location record of type "+ccl.type+"!!");
            pattern.objects.remove(found);
            contentLocations.add(cl);
        }
        */

        // add the leftover content in the patterns:
        for(int i = 0;i<heightInPatterns;i++) {
            for(int j = 0;j<widthInPatterns;j++) {
                if (selected[j][i]!=null) {
                    for (ContentLocationRecord cl2 : selected[j][i].objects) {
                        ContentLocationRecord cl = new ContentLocationRecord(cl2);
                        if (patternOverlap) {
                            cl.x = j * (patternWidth - 1) + cl2.x;
                            cl.y = i * (patternHeight - 1) + cl2.y;
                        } else {
                            cl.x = j * patternWidth + cl2.x;
                            cl.y = i * patternHeight + cl2.y;
                        }
                        contentLocations.add(cl);
                    }
                }
            }
        }

        if (DEBUG>=1) {
            System.out.println("PatternBasedLocationGenerator.generate: content:");
            for(ContentLocationRecord cl:contentLocations) {
                //if (cl.n!=null) 
                System.out.println("  " + cl.type + ": " + cl.x + "," + cl.y);
            }
        }
        
        TilePattern resultPattern = new TilePattern(width, height, tiles.length);
        
        for(int l = 0;l<tiles.length;l++) {
            for(int i = 0;i<height;i++) {
                for(int j = 0;j<width;j++) {
                    resultPattern.getPattern()[l][j][i] = tiles[l][j][i];
                }
            }
        }
        resultPattern.getObjects().addAll(contentLocations);
        
        return resultPattern;
    }

/*
    public void addPathConstraints(List<TilePattern> [][]possibilities,
                                   List<ContentLocationRecord> coarseContentLocations,
                                   List<ContentLocationRecord> passageLocations) throws Exception
    {
        int dx = possibilities.length;
        int dy = possibilities[0].length;
        char buffer[][] = new char[dx][dy];

        // populate the buffer with room ('.') and no-room ('x') cells:
        if (DEBUG>=2) System.out.println("PatternBasedLocationGenerator.addPathConstraints:");
        for(int i = 0;i<dy;i++) {
            for(int j = 0;j<dx;j++) {
                if (possibilities[j][i]!=null && !possibilities[j][i].isEmpty()) {
                    buffer[j][i]='.';
                } else {
                    buffer[j][i]='x';
                }
            }
        }
        for(ContentLocationRecord clr:passageLocations) buffer[clr.x][clr.y]='+';
        for(ContentLocationRecord clr:coarseContentLocations) {
            buffer[clr.x][clr.y]='*';
            if (DEBUG>=2) System.out.println("important location: " + clr.x + "," + clr.y + " -> " + clr.type);
        }
        if (DEBUG>=2) {
            for (int i = 0; i < dy; i++) {
                for (int j = 0; j < dx; j++) {
                    System.out.print(buffer[j][i]);
                }
                System.out.println("");
            }
        }

        addPathConstraintsToLocation(possibilities, buffer, 0, 0, dx, dy);
    }
*/
/*
    public void addPathConstraintsToLocation(List<TilePattern> [][]possibilities,
                                             char buffer[][],
                                             int x0, int y0,
                                             int x1, int y1) throws Exception {
        int dx = buffer.length;
        int dy = buffer[0].length;
        List<Integer> keypoints = new ArrayList<>();

        // 1) find all the key points:
        for(int i = y0;i<y1;i++) {
            for(int j = x0;j<x1;j++) {
                if (buffer[j][i]=='*' || buffer[j][i]=='+') keypoints.add(j+i*dx);
            }
        }

        if (keypoints.isEmpty()) return;

        // 2) initialize the path graph:
        int paths[][] = new int[dx][dy];
        for(int i = 0;i<dy;i++) {
            for(int j = 0;j<dx;j++) paths[j][i] = -1;
        }

        // 3) add the first keypoint:
        int keypoint = keypoints.remove(0);
        int x = keypoint % dx;
        int y = keypoint / dx;
        paths[x][y] = 0;

        // 4) add the rest of the key-points one-by-one to the path graph:
        while(!keypoints.isEmpty()) {
            keypoint = keypoints.remove(0);
            x = keypoint % dx;
            y = keypoint / dx;

            // find a path to any point in the path graph:
            addPath(paths, x, y, x0, y0, x1, y1);
        }

        // 5) Figure out how paths shuld be connected:
        for(int i = y0;i<y1;i++) {
            for (int j = x0; j < x1; j++) {
                if (paths[j][i]!=-1) {
                    if (j>x0 && paths[j-1][i]!=-1) {
                        if (j<x1-1 && paths[j+1][i]!=-1) paths[j][i] |= 1;
                        if (i>0 && paths[j][i-1]!=-1) paths[j][i] |= 2;
                        if (i<y1-1 && paths[j][i+1]!=-1) paths[j][i] |= 4;
                    } else if (i>0 && paths[j][i-1]!=-1) {
                        if (j<x1-1 && paths[j+1][i]!=-1) paths[j][i] |= 8;
                        if (i<y1-1 && paths[j][i+1]!=-1) paths[j][i] |= 16;
                    } else if (j<x1-1 && paths[j+1][i]!=-1) {
                        if (i<y1-1 && paths[j][i+1]!=-1) paths[j][i] |= 32;
                    }
                }
            }
        }

        if (DEBUG>=2) {
            System.out.println("Paths for (" + x0 + "," + y0 + ")-(" + x1 + "," + y1 + "):");
            for (int i = y0; i < y1; i++) {
                for (int j = x0; j < x1; j++) {
                    if (paths[j][i] == -1) System.out.print(" ");
                    else if (paths[j][i] == 0) System.out.print(".");
                    else if (paths[j][i] == 1) System.out.print("-");
                    else if (paths[j][i] == 2) System.out.print("/");
                    else if (paths[j][i] == 4) System.out.print("\\");
                    else if (paths[j][i] == 8) System.out.print("\\");
                    else if (paths[j][i] == 16) System.out.print("|");
                    else if (paths[j][i] == 32) System.out.print("/");
                    else System.out.print("+");
                }
                System.out.println("");
            }
        }

        // 6) add the constraints:
        for(int i = y0;i<y1;i++) {
            for (int j = x0; j < x1; j++) {
                if (paths[j][i]==-1) continue;
                if ((paths[j][i]&1) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-w-e"), possibilities);
                if ((paths[j][i]&2) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-w-n"), possibilities);
                if ((paths[j][i]&4) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-w-s"), possibilities);
                if ((paths[j][i]&8) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-n-e"), possibilities);
                if ((paths[j][i]&16) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-n-s"), possibilities);
                if ((paths[j][i]&32) != 0) addConstraint(j, i, TilePattern.TAG, new Label("path-e-s"), possibilities);
            }
        }
    }


    public void addPath(int paths[][], int start_x, int start_y,
                        int x0, int y0, int x1, int y1) throws Exception {
        int dx = paths.length;
        int dy = paths[0].length;
        List<Integer> open = new ArrayList<>();
        List<Integer> open_parents = new ArrayList<>();
        int closed[][] = new int[dx][dy];
        for(int i = y0;i<y1;i++)
            for(int j = x0;j<x1;j++) closed[j][i] = -1;

        open.add(start_x + start_y*dx);
        open_parents.add(-1);
        int solution_x = -1;
        int solution_y = -1;
        while(!open.isEmpty()) {
            int current = open.remove(0);
            int current_parent = open_parents.remove(0);
            int x = current % dx;
            int y = current / dx;
            if (paths[x][y]!=-1) {
                solution_x = x;
                solution_y = y;
                break; // solution found!
            }
            if (current_parent==-1) {
                closed[x][y] = 0;
            } else {
                int px = current_parent % dx;
                int py = current_parent / dx;
                if (closed[x][y]!=-1 && closed[x][y]<=closed[px][py]+1) continue;   // we had already arrived here by a shorter path
                closed[x][y] = closed[px][py]+1;
            }
            if (x>x0 && closed[x-1][y]==-1) {
                open.add(x-1 + y*dx);
                open_parents.add(current);
            }
            if (x<x1-1 && closed[x+1][y]==-1) {
                open.add(x+1 + y*dx);
                open_parents.add(current);
            }
            if (y>y0 && closed[x][y-1]==-1) {
                open.add(x + (y-1)*dx);
                open_parents.add(current);
            }
            if (y<y1-1 && closed[x][y+1]==-1) {
                open.add(x + (y+1)*dx);
                open_parents.add(current);
            }
        }

        if (DEBUG>=3) {
            for (int i = y0; i < y1; i++) {
                for (int j = x0; j < x1; j++) {
                    System.out.print(closed[j][i] + " ");
                }
                System.out.println("");
            }
        }


        // reconstruct the path:
        if (solution_x==-1) throw new Exception("no path could be found to add key point (" + start_x + "," + start_y +")!!!");

        List<Integer> path = new ArrayList<>();
        path.add(solution_x + solution_y * dx);
        int current_x = solution_x;
        int current_y = solution_y;
        if (DEBUG>=3) System.out.println("Path:");
        paths[current_x][current_y] = 0;
        while(current_x!=start_x || current_y!=start_y) {
            if (DEBUG>=3) System.out.println("  " + current_x + "," + current_y);
            // find the next point:
            int best_cost = -1;
            int best_x = -1;
            int best_y = -1;
            if (current_x>x0) {
                if (best_cost==-1 || (closed[current_x-1][current_y]!=-1 && closed[current_x-1][current_y]<best_cost)) {
                    best_cost = closed[current_x-1][current_y];
                    best_x = current_x-1;
                    best_y = current_y;
                }
            }
            if (current_x<x1-1) {
                if (best_cost==-1 || (closed[current_x+1][current_y]!=-1 && closed[current_x+1][current_y]<best_cost)) {
                    best_cost = closed[current_x+1][current_y];
                    best_x = current_x+1;
                    best_y = current_y;
                }
            }
            if (current_y>y0) {
                if (best_cost==-1 || (closed[current_x][current_y-1]!=-1 && closed[current_x][current_y-1]<best_cost)) {
                    best_cost = closed[current_x][current_y-1];
                    best_x = current_x;
                    best_y = current_y-1;
                }
            }
            if (current_y<y1-1) {
                if (best_cost==-1 || (closed[current_x][current_y+1]!=-1 && closed[current_x][current_y+1]<best_cost)) {
//                    best_cost = closed[current_x][current_y+1];
                    best_x = current_x;
                    best_y = current_y+1;
                }
            }
            path.add(best_x + best_y * dx);
            current_x = best_x;
            current_y = best_y;
            paths[current_x][current_y] = 0;
        }
    }
*/

    public TilePattern[][] generateDFS(List<TilePattern> [][]possibilities, TilePattern[][]selected, boolean randomize, HashMap<Label,Double> multipliers) throws Exception {
//        List<Pair<Integer,List<TilePattern>>> restore;

        if (!sanityCheck(selected)) {
            throw new Exception("Sanity check failed!!!");
        }

        // find the position to generate:
        int selectedNOptions = 0;
        int selectedBestPriority = 0;
        int selectedX = -1;
        int selectedY = -1;
        int pdx = possibilities.length;
        int pdy = possibilities[0].length;
        int cellsLeftToFill = 0;
        for(int i = 0;i<pdy;i++) {
            for(int j = 0;j<pdx;j++) {
                if (selected[j][i]==null && possibilities[j][i]!=null) {
                    cellsLeftToFill++;
                    int n = possibilities[j][i].size();
                    if (n>0) {
                        int bestPriority = possibilities[j][i].get(0).priority;
                        if (selectedNOptions==0) {
                            selectedNOptions = n;
                            selectedBestPriority = bestPriority;
                            selectedX = j;
                            selectedY = i;
                        } else {
                            if (n<selectedNOptions || (n==selectedNOptions && bestPriority>selectedBestPriority)) {
                                selectedNOptions = n;
                                selectedBestPriority = bestPriority;
                                selectedX = j;
                                selectedY = i;
                            }
                        }
                    } else {
                        // backtrack
                        if (DEBUG>=1) {
                            System.out.println("backtracking because of " + j + "," + i);
                            printSuroundingConstraints(j,i,possibilities);
                        }
                        return null;
                    }
                }
            }
        }
        if (DEBUG>=1) System.out.println(cellsLeftToFill + ": " + selectedX + "," + selectedY + " with " + selectedNOptions);
        if (selectedX==-1) {
            // we are done!
            return selected;
        }
        if (selectedNOptions==0) {
            // backtrack:
            return null;
        }
        
        // - sort according to priority and weight:
        Collections.sort(possibilities[selectedX][selectedY], new Comparator<TilePattern>() {
            public int compare(TilePattern o1, TilePattern o2) {
                if (o1.priority == o2.priority) {
                    return Double.compare(o1.getWeight(multipliers), o2.getWeight(multipliers));
                } else {
                    return -Integer.compare(o1.priority, o2.priority);
                }
            }
        });
        
        if (randomize) {
            // randomize each priority section of the list, according to the weights:
            List<TilePattern> result = new ArrayList<>();
            while(!possibilities[selectedX][selectedY].isEmpty()) {
                List<TilePattern> tmp = new LinkedList<>();
                List<Double> distribution = new LinkedList<>();
                int priority = possibilities[selectedX][selectedY].get(0).priority;
                while(!possibilities[selectedX][selectedY].isEmpty() && 
                      possibilities[selectedX][selectedY].get(0).priority == priority) {
                    TilePattern p = possibilities[selectedX][selectedY].remove(0);
                    tmp.add(p);
                    distribution.add(p.getWeight(multipliers));
                }
                if (DEBUG>=2) System.out.println("randomizing with distribution: " + distribution);
                while(!tmp.isEmpty()) {
                    int n = Sampler.weighted(distribution);
                    result.add(tmp.remove(n));
                    distribution.remove(n);
                }
            }
            
            possibilities[selectedX][selectedY] = result;
        }

        RestoreStructure r = new RestoreStructure(possibilities, selected);
        for(TilePattern p:possibilities[selectedX][selectedY]) {
            if (setPattern(selectedX, selectedY, p, possibilities, selected)) {
                TilePattern[][]result = generateDFS(possibilities, selected, randomize, multipliers);
                if (result!=null) return result;
            }
            r.restore(possibilities, selected);
//            restore = setPattern(selectedX, selectedY, p, possibilities, selected);
//            if (restore==null) continue;
//            TilePattern[][]result = generateDFS(possibilities, selected, randomize, multipliers);
//            if (result!=null) return result;
//            restore(restore, possibilities, selected);
        }
        
        if (DEBUG>=1) System.out.println("backtracking!");
        return null;
    }

    /*
    public void restore(List<Pair<Integer,List<TilePattern>>> restore, List<TilePattern> [][]possibilities, TilePattern[][]selected) {
        int pdx = possibilities.length;
        for(Pair<Integer,List<TilePattern>> tmp:restore) {
            int xtmp = tmp.m_a % pdx;
            int ytmp = tmp.m_a / pdx;
            if (tmp.m_b!=null) {
                possibilities[xtmp][ytmp].addAll(tmp.m_b);
                if (possibilities[xtmp][ytmp].size() > 1) selected[xtmp][ytmp] = null;
            } else {
                selected[xtmp][ytmp] = null;
            }
        }
    }
    */
    
    // returns the list of patterns that have been removed, for restoring them after backtracking:
    public List<TilePattern> addConstraint(int x, int y, int direction, Label constraint, List<TilePattern> [][]possibilities) {
        List<TilePattern> toDelete = new ArrayList<>();
        if (possibilities[x][y]==null) return toDelete;
        for(TilePattern p:possibilities[x][y]) {
            if (!p.satisfiesConstraint(direction,constraint)) toDelete.add(p);
        }
        possibilities[x][y].removeAll(toDelete);
        return toDelete;
    }

    // returns the list of patterns that have been removed, for restoring them after backtracking:
    public List<TilePattern> addNegativeConstraint(int x, int y, int direction, Label constraint, List<TilePattern> [][]possibilities) {
        List<TilePattern> toDelete = new ArrayList<>();
        List<TilePattern> toUpdate = new ArrayList<>();
        if (possibilities[x][y]==null) return toDelete;
        for(TilePattern p:possibilities[x][y]) {
            if (p.uniquelySatisfiesConstraint(direction,constraint)) {
                toDelete.add(p);
            } else if (p.satisfiesConstraint(direction,constraint)) {
                toUpdate.add(p);
            }
        }
        possibilities[x][y].removeAll(toDelete);
        for(TilePattern p:toUpdate) {
            possibilities[x][y].remove(p);
            TilePattern p2 = new TilePattern(p);
            p2.removeTag(direction, constraint);
//            System.out.println(direction + ": " + constraint);
            possibilities[x][y].add(p2);
        }
        return toDelete;
    }

    // returns the list of patterns that have been removed, for restoring them after backtracking:
    public List<TilePattern> addConstraint(int x, int y, int direction, List<Label> alternativeConstraints, List<TilePattern> [][]possibilities) {
        List<TilePattern> toDelete = new ArrayList<>();
        if (possibilities[x][y]==null) return toDelete;
        for(TilePattern p:possibilities[x][y]) {
            boolean any = false;
            for(Label constraint:alternativeConstraints) {
                if (p.satisfiesConstraint(direction,constraint)) {
                    any = true;
                    break;
                }
            }
            if (!any) toDelete.add(p);
        }
        possibilities[x][y].removeAll(toDelete);
        return toDelete;
    }
    

    public boolean satisfiableConstraint(int x, int y, int direction, Label constraint, List<TilePattern> [][]possibilities) {
        if (possibilities[x][y]==null) return false;
        for(TilePattern p:possibilities[x][y]) {
            if (p.satisfiesConstraint(direction,constraint)) return true;
        }
        return false;
    }

    
    public boolean inference(List<TilePattern> [][]possibilities, TilePattern[][]selected) {
        int pdx = possibilities.length;
        int pdy = possibilities[0].length;
        for(int i = 0;i<pdy;i++) {
            for(int j = 0;j<pdx;j++) {
                if (selected[j][i]==null && possibilities[j][i]!=null) {
                    if (possibilities[j][i].isEmpty()) {
                        if (DEBUG>=1) {
                            System.out.println("backtracking because of " + j + "," + i);
                            printSuroundingConstraints(j, i, possibilities);
                        }
                        return false;
                    }
                    if (possibilities[j][i].size()==1) {
                        TilePattern p = possibilities[j][i].get(0);
//                        removed.add(new Pair<>(j+i*pdx,null));
                        selected[j][i] = p;
                        if (DEBUG>=1) System.out.println("inference: " + j + "," + i);
                        // add constraints:
//                        if (j>0 && selected[j-1][i]==null) removed.add(new Pair<>(j-1+i*pdx,addConstraint(j-1,i, TilePattern.EAST,p.getWest(),possibilities)));
//                        if (i>0 && selected[j][i-1]==null) removed.add(new Pair<>(j+(i-1)*pdx,addConstraint(j,i-1, TilePattern.SOUTH,p.getNorth(),possibilities)));
//                        if (j<pdx-1 && selected[j+1][i]==null) removed.add(new Pair<>(j+1+i*pdx,addConstraint(j+1,i, TilePattern.WEST,p.getEast(),possibilities)));
//                        if (i<pdy-1 && selected[j][i+1]==null) removed.add(new Pair<>(j + (i + 1) * pdx, addConstraint(j, i + 1, TilePattern.NORTH, p.getSouth(), possibilities)));
                        if (j>0 && selected[j-1][i]==null && possibilities[j-1][i]!=null) {
                            addConstraint(j - 1, i, TilePattern.EAST, p.getWest(), possibilities);
                            if (possibilities[j-1][i].isEmpty()) {
                                if (DEBUG>=1) {
                                    System.out.println("backtracking because of " + (j - 1) + "," + i);
                                    printSuroundingConstraints(j - 1, i, possibilities);
                                }
                                return false;
                            }
                        }
                        if (i>0 && selected[j][i-1]==null && possibilities[j][i-1]!=null) {
                            addConstraint(j,i-1, TilePattern.SOUTH,p.getNorth(),possibilities);
                            if (possibilities[j][i-1].isEmpty()) {
                                if (DEBUG>=1) {
                                    System.out.println("backtracking because of " + (j) + "," + (i - 1));
                                    printSuroundingConstraints(j, i - 1, possibilities);
                                }
                                return false;
                            }
                        }
                        if (j<pdx-1 && selected[j+1][i]==null && possibilities[j+1][i]!=null) {
                            addConstraint(j+1,i, TilePattern.WEST,p.getEast(),possibilities);
                            if (possibilities[j+1][i].isEmpty()) {
                                if (DEBUG>=1) {
                                    System.out.println("backtracking because of " + (j + 1) + "," + i);
                                    printSuroundingConstraints(j + 1, i, possibilities);
                                }
                                return false;
                            }
                        }
                        if (i<pdy-1 && selected[j][i+1]==null && possibilities[j][i+1]!=null) {
                            addConstraint(j,i+1, TilePattern.NORTH,p.getSouth(),possibilities);
                            if (possibilities[j][i+1].isEmpty()) {
                                if (DEBUG>=1) {
                                    System.out.println("backtracking because of " + (j) + "," + (i + 1));
                                    printSuroundingConstraints(j, i + 1, possibilities);
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public boolean setPattern(int j, int i, TilePattern p, List<TilePattern> [][]possibilities, TilePattern[][]selected) {
//        public List<Pair<Integer,List<TilePattern>>> setPattern(int j, int i, TilePattern p, List<TilePattern> [][]possibilities, TilePattern[][]selected) {
//        List<Pair<Integer,List<TilePattern>>> removed = new ArrayList<>();
        int pdx = possibilities.length;
        int pdy = possibilities[0].length;
        
//        System.out.println(j + "," + i + " = " + p);

        selected[j][i] = p;
        
        // add constraints:
//        if (j>0 && selected[j-1][i]==null) removed.add(new Pair<>(j-1+i*pdx,addConstraint(j-1,i, TilePattern.EAST,p.getWest(),possibilities)));
//        if (i>0 && selected[j][i-1]==null) removed.add(new Pair<>(j+(i-1)*pdx,addConstraint(j,i-1, TilePattern.SOUTH,p.getNorth(),possibilities)));
//        if (j<pdx-1 && selected[j+1][i]==null) removed.add(new Pair<>(j+1+i*pdx,addConstraint(j+1,i, TilePattern.WEST,p.getEast(),possibilities)));
//        if (i<pdy-1 && selected[j][i+1]==null) removed.add(new Pair<>(j+(i+1)*pdx,addConstraint(j,i+1, TilePattern.NORTH,p.getSouth(),possibilities)));
        if (j>0 && selected[j-1][i]==null && possibilities[j-1][i]!=null) addConstraint(j-1,i, TilePattern.EAST,p.getWest(),possibilities);
        if (i>0 && selected[j][i-1]==null && possibilities[j][i-1]!=null) addConstraint(j,i-1, TilePattern.SOUTH,p.getNorth(),possibilities);
        if (j<pdx-1 && selected[j+1][i]==null && possibilities[j+1][i]!=null) addConstraint(j+1,i, TilePattern.WEST,p.getEast(),possibilities);
        if (i<pdy-1 && selected[j][i+1]==null && possibilities[j][i+1]!=null) addConstraint(j, i + 1, TilePattern.NORTH, p.getSouth(), possibilities);
//        List<Pair<Integer,List<TilePattern>>> tmp = inference(possibilities, selected);
        if (!inference(possibilities, selected)) return false;
//        if (tmp==null) {
//            selected[j][i] = null;
//            restore(removed,possibilities, selected);
//            return null;
//        }
//        removed.addAll(tmp);
//        return removed;
        return true;
    }


    public boolean sanityCheck(TilePattern[][]selected) {
        int dx = selected.length;
        int dy = selected[0].length;
        for(int i = 0;i<dy;i++) {
            for(int j = 0;j<dx;j++) {
                if (selected[j][i]==null) continue;
                if (j>0 && selected[j-1][i]!=null) if (!selected[j][i].matchesWestWith(selected[j - 1][i])) {
                    System.out.println("sanityCheck failed: west");
                    return false;
                }
                if (j<dx-1 && selected[j+1][i]!=null) if (!selected[j][i].matchesEastWith(selected[j + 1][i])) {
                    System.out.println("sanityCheck failed: east");
                    return false;
                }
                if (i>0 && selected[j][i-1]!=null) if (!selected[j][i].matchesNorthWith(selected[j][i - 1])) {
                    System.out.println("sanityCheck failed: north");
                    return false;
                }
                if (i<dy-1 && selected[j][i+1]!=null) if (!selected[j][i].matchesSouthWith(selected[j][i + 1])) {
                    System.out.println("sanityCheck failed: south");
                    return false;
                }
            }
        }
        return true;
    }

    public void printSuroundingConstraints(int x, int y, List<TilePattern> possibilities[][]) {
        int dx = possibilities.length;
        int dy = possibilities[0].length;
        System.out.println("Constraints around " + x+ "," + y + ":");
        if (x>0 && possibilities[x-1][y]!=null) {
            HashSet<Label> tags = new HashSet<>();
            for(TilePattern p:possibilities[x-1][y]) tags.addAll(p.getEast());
            System.out.println("  west:" + tags);
        }
        if (y>0 && possibilities[x][y-1]!=null) {
            HashSet<Label> tags = new HashSet<>();
            for(TilePattern p:possibilities[x][y-1]) tags.addAll(p.getSouth());
            System.out.println("  north:" + tags);
        }
        if (x<dx-1 && possibilities[x+1][y]!=null) {
            HashSet<Label> tags = new HashSet<>();
            for(TilePattern p:possibilities[x+1][y]) tags.addAll(p.getWest());
            System.out.println("  east:" + tags);
        }
        if (y<dy-1 && possibilities[x][y+1]!=null) {
            HashSet<Label> tags = new HashSet<>();
            for(TilePattern p:possibilities[x][y+1]) tags.addAll(p.getNorth());
            System.out.println("  south:" + tags);
        }
    }
}
