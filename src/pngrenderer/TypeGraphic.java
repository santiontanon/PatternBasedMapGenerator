/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pngrenderer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import util.Label;
import util.Sampler;

/**
 *
 * @author santi
 */
public class TypeGraphic {
    Label type;
    
    List<BufferedImage> graphics = new ArrayList<>();
    List<Double> weights = new ArrayList<>();
    
    
    public TypeGraphic(Label a_type) {
        type = a_type;
    }
    
        
    public TypeGraphic(Label a_type, BufferedImage a_graphic, double a_weight) {
        type = a_type;
        addGraphic(a_graphic, a_weight);
    }
    
    
    public void addGraphic(BufferedImage a_graphic, double a_weight) {
        graphics.add(a_graphic);
        weights.add(a_weight);        
    }
    
    
    public BufferedImage draw() throws Exception {
        if (graphics.isEmpty()) return null;
        int idx = Sampler.weighted(weights);
        return graphics.get(idx);
    }
}
