/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapgenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class RestoreStructure {

    List<TilePattern>[][] possibilities = null;
    TilePattern[][] selected = null;

    public RestoreStructure(List<TilePattern>[][] p, TilePattern[][] s) {
        int dx = p.length;
        int dy = p[0].length;
        possibilities = new List[dx][dy];
        if (s!=null) selected = new TilePattern[dx][dy];
        for (int i = 0; i < dy; i++) {
            for (int j = 0; j < dx; j++) {
                if (p[j][i] == null) {
                    possibilities[j][i] = null;
                } else {
                    possibilities[j][i] = new ArrayList<>();
                    possibilities[j][i].addAll(p[j][i]);
                }
                if (s!=null) selected[j][i] = s[j][i];
            }
        }
    }

    public void restore(List<TilePattern>[][] p, TilePattern[][] s) {
        int dx = p.length;
        int dy = p[0].length;
        for (int i = 0; i < dy; i++) {
            for (int j = 0; j < dx; j++) {
                if (possibilities[j][i] == null) {
                    p[j][i] = null;
                } else {
                    p[j][i] = new ArrayList<>();
                    p[j][i].addAll(possibilities[j][i]);
                }
                if (s!=null) s[j][i] = selected[j][i];
            }
        }
    }
}
