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
 * This constraint specifies that ALL the patterns used to generate a map must
 * satisfy at least one of the "tags" and none of the "negativeTags".
 * 
 */
public class ApplyToAllConstraint extends Constraint {
    int direction;
    List<Label> tags = new ArrayList<>();
    List<Label> negativeTags = new ArrayList<>();
    
    public ApplyToAllConstraint(int a_direction) {
        direction = a_direction;        
    }

    public ApplyToAllConstraint(int a_direction, Label tag) {
        direction = a_direction;
        tags.add(tag);
    }
    
    public ApplyToAllConstraint(int a_direction, Label a_border, boolean positive) {
        direction = a_direction;
        if (positive) tags.add(a_border);
                 else negativeTags.add(a_border);
    }
    
    
    public void addTag(Label tag) {
        tags.add(tag);
    }
    
    public void addNegativeTag(Label tag) {
        negativeTags.add(tag);
    }
    
    
    public boolean checkConstraint(TilePattern tp) {
        if (!tags.isEmpty()) {
            if (!tp.satisfiesAtLeastOneConstraint(direction, tags)) return false;
        }
        if (!negativeTags.isEmpty()) {
            if (tp.satisfiesAtLeastOneConstraint(direction, negativeTags)) return false;
        }
        return true;
    }
}
