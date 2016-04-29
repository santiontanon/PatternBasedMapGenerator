
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import mapgenerator.PatternBasedLocationGenerator;
import mapgenerator.TilePattern;
import util.XMLWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author santi
 */
public class Main {
    
    public static int DEBUG = 0;
    
    public static void main(String args[]) throws Exception {                
        PatternBasedLocationGenerator generator = null;
        
        if (args.length<3) {
            printInstructions();
            System.exit(0);
        }
        
        String inputFileName = null;
        String outputFileName = null;
        String pngOutputFileName = null;
        boolean initializePNGGenerator = false;
        
        inputFileName = args[0];
        int widthInPatterns = Integer.parseInt(args[1]);
        int heightInPatterns = Integer.parseInt(args[2]);
        if (args.length>3 && !args[3].startsWith("-")) outputFileName = args[3];
        for(int i = 3;i<args.length;i++) {
            if (args[i].equals("-d1")) {
                DEBUG = 1;
                PatternBasedLocationGenerator.DEBUG = 1;
            }
            if (args[i].equals("-d2")) {
                DEBUG = 1;
                PatternBasedLocationGenerator.DEBUG = 2;
            }
            if (args[i].startsWith("-png:")) {
                initializePNGGenerator = true;
                pngOutputFileName = args[i].substring(5);
            }
        }

        generator = new PatternBasedLocationGenerator(inputFileName, initializePNGGenerator);                                                        
        TilePattern result = generator.generate(widthInPatterns, heightInPatterns);
        
        if (pngOutputFileName!=null) {
            BufferedImage img = generator.renderPNG(result);
            ImageIO.write(img, "png", new File(pngOutputFileName));
        }        
        
        if (outputFileName==null) {
            XMLWriter w = new XMLWriter(new OutputStreamWriter(System.out));
            if (result!=null) result.writeToXML(w);
            w.close();
        } else {
            XMLWriter w = new XMLWriter(new FileWriter(outputFileName));
            if (result!=null) result.writeToXML(w);
            w.close();
        }        
    }
    
    
    public static void printInstructions() {
        System.out.println("Pattern-Based Map Generator (PBMG) v1.0 by Santiago Ontañón (2016)");
        System.out.println("");
        System.out.println("This tool uses a pattern-based approach to generate two-dimensional maps. ");
        System.out.println("");
        System.out.println("Usage: java -classpath PBMG.jar Main intputfilename width height [outputfilename] [options]");
        System.out.println("Example usage: java -classpath PBMG.jar Main examples/sampleInput.xml 7 5 examples/output.xml");
        System.out.println("");
        System.out.println("The output file name is optional, and if not specified, the generated map will be just printed to standard output.");
        System.out.println("Options:");
        System.out.println(" -d1: turns on some verbose output of the process");
        System.out.println(" -d2: turns on an even more verbose output of the process");
        System.out.println("");
    }    
}
