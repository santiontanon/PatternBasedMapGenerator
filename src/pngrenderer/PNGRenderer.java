package pngrenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import mapgenerator.ContentLocationRecord;
import mapgenerator.TilePattern;
import org.jdom.Element;
import util.Label;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author santi
 */
public class PNGRenderer {

    int tileWidth = 16, tileHeight = 16;
    HashMap<Label, TypeGraphic> typeGraphics = new HashMap<>();
    HashMap<String, BufferedImage> images = new HashMap<>();
    
    public void parseXMLConfig(Element e) throws Exception {
        tileWidth = Integer.parseInt(e.getAttributeValue("tileWidth"));
        tileHeight = Integer.parseInt(e.getAttributeValue("tileHeight"));
        
        for(Object o:e.getChildren("tile")) {
            Element tile_e = (Element)o;
            
            Label l = new Label(tile_e.getAttributeValue("type"));
            int tile = Integer.parseInt(tile_e.getAttributeValue("tile"));
            String file = tile_e.getAttributeValue("file");
            double weight = Double.parseDouble(tile_e.getAttributeValue("weight"));
            int width = 1;
            int height = 1;
            if (tile_e.getAttributeValue("width")!=null) 
                width = Integer.parseInt(tile_e.getAttributeValue("width"));
            if (tile_e.getAttributeValue("height")!=null) 
                height = Integer.parseInt(tile_e.getAttributeValue("height"));
            
            TypeGraphic tg = typeGraphics.get(l);
            if (tg==null) {
                tg = new TypeGraphic(l);
                typeGraphics.put(l,tg);
            }
            BufferedImage img = images.get(file);
            if (img==null) img = img = ImageIO.read(new File(file));
            BufferedImage g = new BufferedImage(width*tileWidth, height*tileHeight, BufferedImage.TYPE_INT_ARGB);
            
            if (tile>0) {
                int widthInTiles = img.getWidth()/tileWidth;
                int srcx = ((tile-1)%widthInTiles) * tileWidth;
                int srcy = ((tile-1)/widthInTiles) * tileHeight;
                g.getGraphics().drawImage(img, 0, 0, width*tileWidth, height*tileHeight,
                                               srcx, srcy, srcx+width*tileWidth, srcy+height*tileHeight, null);
            }
            
            tg.addGraphic(g, weight);
        }
    }
    
    
    public BufferedImage render(TilePattern p, HashMap<Character,Label> symbol2Type) throws Exception {
        int w = p.getDx();
        int h = p.getDy();
        BufferedImage img = new BufferedImage(w*tileWidth, h*tileHeight, BufferedImage.TYPE_INT_ARGB);
        
        // render layers:
        for(int l = 0;l<p.getNLayers();l++) {
            for(int i = 0;i<p.getDy();i++) {
                for(int j = 0;j<p.getDx();j++) {
                    char c = p.getPattern()[l][j][i];
                    Label type = symbol2Type.get(c);
                    TypeGraphic tg = typeGraphics.get(type);
                    if (tg==null) {
                        int code = neighborBasedTypeGraphic(p, l, j, i, c);
                        tg = typeGraphics.get(new Label(type.get() + code));
                    }
                    if (tg!=null) {
                        drawTile(img, j*tileWidth, i*tileHeight, tg);
//                        System.out.println("drawing " + type + " at " + j + ", " + i);
                    } else {
                        System.out.println("Don't know how to draw " + type);
                    }
                }
            }
        }
        
        // render objects:
        for(ContentLocationRecord clr:p.getObjects()) {
            TypeGraphic tg = typeGraphics.get(clr.type);
            if (tg!=null) {
                drawTile(img, clr.x*tileWidth, clr.y*tileHeight, tg);
//                        System.out.println("drawing " + type + " at " + j + ", " + i);
            } else {
                System.out.println("Don't know how to draw " + clr.type);
            }
        }
        
        return img;
    }
    
    
    public void drawTile(BufferedImage img, int x, int y, TypeGraphic tg) throws Exception {
        BufferedImage tile = tg.draw();
        if (tile!=null) {
            img.getGraphics().drawImage(tile, x, y, x+tile.getWidth(), y+tile.getWidth(), 
                                              0, 0, tile.getWidth(), tile.getWidth(), null);
        }
    }

    private int neighborBasedTypeGraphic(TilePattern p, int l, int x, int y, char symbol) {
        int code = 0;
        int masks[] = {1,2,4,8};
        int xoffs[] = {0,0,1,-1};
        int yoffs[] = {1,-1,0,0};
        for(int i = 0;i<4;i++) {
            int x2 = x + xoffs[i];
            int y2 = y + yoffs[i];
            if (x2>=0 && y2>=0 && x2<p.getDx() && y2<p.getDy()) {
                if (p.getPattern()[l][x2][y2]==symbol) {
                    code += masks[i];
                }
            }
        }
        
        return code;
    }
}
