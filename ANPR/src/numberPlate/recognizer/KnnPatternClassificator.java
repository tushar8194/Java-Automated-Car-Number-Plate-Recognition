package numberPlate.recognizer;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import numberPlate.imageanalysis.Char;
import numberPlate.intelligence.Intelligence;


public class KnnPatternClassificator extends CharacterRecognizer {
    Vector<Vector<Double>> learnVectors;
    
    public KnnPatternClassificator() throws IOException {
        String path = Intelligence.configurator.getPathProperty("char_learnAlphabetPath");
       // System.out.println("Path=================>"+path);
        String alphaString = "0123456789abcdefghijklmnopqrstuvwxyz";
        
        // initialization vector to the desired size (reset items)
        this.learnVectors = new Vector<Vector<Double>>();
        for (int i=0; i<alphaString.length(); i++) this.learnVectors.add(null);
//        int count=0;
        File folder = new File(path);
        for (String fileName : folder.list()) {
        	//System.out.println("KnnPatternClassifier.java========fileName :"+fileName+"=========count:"+count);count++;
            int alphaPosition = alphaString.indexOf(fileName.toLowerCase().charAt(0));
           // System.out.println("KnnPatternClassifier.java===postion :"+alphaPosition);
            if (alphaPosition==-1)  continue; // is not sound filename, skip

            Char imgChar = new Char(path+File.separator+fileName);
            //System.out.println("Image : "+imgChar);
            imgChar.normalize();
            // Writing to the position in the vector
           // System.out.println("KnnPatternClassifier.java=======Feature"+imgChar.extractFeatures()+"=========count:"+count);count++;
            this.learnVectors.set(alphaPosition, imgChar.extractFeatures());

        }
        
        // vector control items
        for (int i=0; i<alphaString.length(); i++) 
            if (this.learnVectors.elementAt(i)==null) throw new IOException("Warning : alphabet in "+path+" is not complete");
      
    }
 
    public RecognizedChar recognize(Char chr) throws Exception {
        Vector<Double> tested = chr.extractFeatures();
        int minx = 0;
        float minfx = Float.POSITIVE_INFINITY;
        
        RecognizedChar recognized = new RecognizedChar();
        
        for (int x = 0; x < this.learnVectors.size(); x++) {
            // for better functioning was a mere difference vectors by replacing the Euclidian distance
            float fx = this.simplifiedEuclideanDistance(tested, this.learnVectors.elementAt(x));

            recognized.addPattern(recognized.new RecognizedPattern(this.alphabet[x], fx));
            
            //if (fx < minfx) {
            //    minfx = fx;
            //    minx = x;
            //}
        }
//        return new RecognizedChar(this.alphabet[minx], minfx);
        recognized.sort(0);
        return recognized;
    }
    
    public float difference(Vector<Double> vectorA, Vector<Double> vectorB) {
        float diff = 0;
        for (int x = 0; x<vectorA.size(); x++) {
            diff += Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
        }
        return diff;
    }
    
    public float simplifiedEuclideanDistance(Vector<Double> vectorA, Vector<Double> vectorB) {
        float diff = 0;
        float partialDiff;
        for (int x = 0; x<vectorA.size(); x++) {
            partialDiff = (float)Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
            diff += partialDiff * partialDiff;
        }
        return diff;        
    }
    
}
