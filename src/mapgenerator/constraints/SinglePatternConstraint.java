/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapgenerator.constraints;

import java.util.ArrayList;
import java.util.List;
import mapgenerator.TilePattern;
import util.Label;

/**
 *
 * @author santi
 */
public class SinglePatternConstraint extends Constraint {
    int direction;
    List<Label> tags = new ArrayList<>();
    List<Label> negativeTags = new ArrayList<>();
    int x=-1, y=-1;
    String ID = null;
    
    public SinglePatternConstraint(int a_direction) {
        direction = a_direction;        
    }

    public SinglePatternConstraint(int a_direction, Label tag) {
        direction = a_direction;
        tags.add(tag);
    }
    
    public SinglePatternConstraint(int a_direction, Label a_border, int a_x, int a_y, String a_ID) {
        direction = a_direction;
        tags.add(a_border);
        x = a_x;
        y = a_y;
        ID = a_ID;
    }
    
    
    public void addTag(Label tag) {
        tags.add(tag);
    }
    
    public void addNegativeTag(Label tag) {
        negativeTags.add(tag);
    }
    
    public boolean checkConstraint(TilePattern tp, int px, int py) {
        if (!tags.isEmpty()) {
            if (!tp.satisfiesAtLeastOneConstraint(direction, tags)) return false;
        }
        if (!negativeTags.isEmpty()) {
            if (tp.satisfiesAtLeastOneConstraint(direction, negativeTags)) return false;
        }
        if (x!=-1 && px!=x) return false; 
        if (y!=-1 && py!=y) return false; 
        return true;
    }    
}
