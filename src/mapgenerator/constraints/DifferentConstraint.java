/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapgenerator.constraints;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class DifferentConstraint extends Constraint {
    List<String> IDs = new ArrayList<>();
    
    public DifferentConstraint() {
    }
    
    
    public List<String> getIDs() {
        return IDs;
    }
    
    
    public void addID(String ID) {
        IDs.add(ID);
    }
}
