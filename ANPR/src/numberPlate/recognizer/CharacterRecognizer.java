package numberPlate.recognizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import numberPlate.imageanalysis.Char;

public abstract class CharacterRecognizer {
    
    // handwriting recognition:
    // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35
    // 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z
    public static char[] alphabet = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C',
        'D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z'
    };
    
    public static float[][] features = {
        {0,1,0,1}, //0
        {1,0,1,0}, //1
        {0,0,1,1}, //2
        {1,1,0,0}, //3
        {0,0,0,1}, //4        
        {1,0,0,0}, //5
        {1,1,1,0}, //6        
        {0,1,1,1}, //7
        {0,0,1,0}, //8        
        {0,1,0,0}, //9
        {1,0,1,1}, //10       
        {1,1,0,1}  //11    
    };
    
    public class RecognizedChar {
        public class RecognizedPattern {
            private char chr;
            private float cost;
            RecognizedPattern(char chr, float value) {
                this.chr = chr;
                this.cost = value;
            }
            public char getChar() {
                return this.chr;
            }
            public float getCost() {
                return this.cost;
            }
        }
        public class PatternComparer implements Comparator {
            int direction;
            public PatternComparer(int direction) {
                this.direction = direction;
            }
            public int compare(Object o1, Object o2) {
                float cost1 = ((RecognizedPattern)o1).getCost();
                float cost2 = ((RecognizedPattern)o2).getCost();
                
                int ret = 0;
                
                if (cost1 < cost2) ret = -1;
                if (cost1 > cost2) ret =  1;
                if (direction==1) ret *= -1;
                return ret;
            }
        }
        
        private Vector<RecognizedPattern> patterns;
        private boolean isSorted;
        
        RecognizedChar() {
            this.patterns = new Vector<RecognizedPattern>();
            this.isSorted = false;
        }
        public void addPattern(RecognizedPattern pattern) {
            this.patterns.add(pattern);
        }
        public boolean isSorted() {
            return this.isSorted;
        }
        public void sort(int direction) {
            if (this.isSorted) return;
            this.isSorted = true;
            Collections.sort(patterns, (Comparator<? super RecognizedPattern>)
                                       new PatternComparer(direction));
        }
        
        public Vector<RecognizedPattern> getPatterns() {
            if (this.isSorted) return this.patterns;
            return null; // if not sorted
        }
        public RecognizedPattern getPattern(int i) {
            if (this.isSorted) return this.patterns.elementAt(i);
            return null;
        }
        
        public BufferedImage render() {
            int width=500;
            int height=200;
            BufferedImage histogram = new BufferedImage(width+20, height+20, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = histogram.createGraphics();
            
            graphic.setColor(Color.LIGHT_GRAY);
            Rectangle backRect = new Rectangle(0,0,width+20,height+20);
            graphic.fill(backRect);
            graphic.draw(backRect);
            
            graphic.setColor(Color.BLACK);
            
            int colWidth = width / this.patterns.size();
            int left, top;
            
            
            for (int ay = 0; ay <= 100; ay += 10) {
                int y = 15 + (int)((100 - ay) / 100.0f * (height - 20));
                graphic.drawString(new Integer(ay).toString(), 3 , y+11);
                graphic.drawLine(25,y+5,35,y+5);
            }
            graphic.drawLine(35,19,35,height );
            
            graphic.setColor(Color.BLUE);
            
            for (int i=0; i<patterns.size(); i++) {
                left = i * colWidth + 42;
                top = height - (int)( patterns.elementAt(i).cost * (height - 20));
                
                graphic.drawRect(
                        left,
                        top ,
                        colWidth - 2,
                        height - top );
                graphic.drawString( patterns.elementAt(i).chr+" " , left + 2,
                        top - 8);
            }
            return histogram;
        }
    }
    
    
    /** Creates a new instance of CharacterRecognizer */
    public CharacterRecognizer() {
    }
    
    public abstract RecognizedChar recognize(Char chr)  throws Exception;
}
