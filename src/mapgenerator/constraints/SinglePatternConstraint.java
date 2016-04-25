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
 * 
 * This constraint ensures that at least one of the patterns in the map satisfy
 * the specified tags. If "x" or "y" are specified, it will only take into account
 * patterns in those coordinates (e.g., if "x" is specified, only those positions
 * in the map with that "x" coordinate will be candidates).
 * 
 * the "ID" is used to later refer to the pattern that was selected in the "path
 * constraint".
 * 
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
    
    
    public List<Label> getTags() {
        return tags;
    }
    
    
    public List<Label> getNegativeTags() {
        return negativeTags;
    }
    
    
    public void addNegativeTag(Label tag) {
        negativeTags.add(tag);
    }
    
    
    public int getDirection() {
        return direction;
    }
    
    
    public int getX() {
        return x;
    }
    

    public void setX(int a_x) {
        x = a_x;
    }
    
    
    public int getY() {
        return y;
    }


    public void setY(int a_y) {
        y = a_y;
    }
    
    
    public String getID() {
        return ID;
    }
    
    
    public void setID(String a_ID) {
        ID = a_ID;
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
