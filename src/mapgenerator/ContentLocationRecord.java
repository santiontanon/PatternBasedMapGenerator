/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapgenerator;

import org.jdom.Element;
import util.Label;

/**
 *
 * @author santi
 */
public class ContentLocationRecord {
    public int x=-1, y=-1, width=1,height=1;
    public Label type = null;

    public ContentLocationRecord() {

    }

    public ContentLocationRecord(int a_x, int a_y, Label a_type) {
        x = a_x;
        y = a_y;
        type = a_type;
        if (type==null) System.err.println("null type when initializing ContentLocationRecord!");
    }

    public ContentLocationRecord(ContentLocationRecord r) {
        x = r.x;
        y = r.y;
        width = r.width;
        height = r.height;
        type = r.type;
    }

    public static ContentLocationRecord fromXML(Element e) throws Exception {
        ContentLocationRecord p = new ContentLocationRecord();

        p.x = Integer.parseInt(e.getAttributeValue("x"));
        p.y = Integer.parseInt(e.getAttributeValue("y"));
        if (e.getAttributeValue("width")==null) {
            p.width = 1;
        } else {
            p.width = Integer.parseInt(e.getAttributeValue("width"));
        }
        if (e.getAttributeValue("height")==null) {
            p.height = 1;
        } else {
            p.height = Integer.parseInt(e.getAttributeValue("height"));
        }
        p.type = new Label(e.getAttributeValue("type"));
        if (p.type==null) {
            throw new Exception("null type when initializing ContentLocationRecord from xml!");
        }

        return p;
    }

}
