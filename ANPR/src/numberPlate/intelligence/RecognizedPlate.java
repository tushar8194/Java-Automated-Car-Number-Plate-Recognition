
package numberPlate.intelligence;

import java.util.Vector;

import numberPlate.recognizer.CharacterRecognizer.RecognizedChar;

public class RecognizedPlate {
    Vector<RecognizedChar> chars;
    
    public RecognizedPlate() {
        this.chars = new Vector<RecognizedChar>();
    }
    
    public void addChar(RecognizedChar chr) {
        this.chars.add(chr);
    }
    
    public RecognizedChar getChar(int i) {
        return this.chars.elementAt(i);
    }

    public String getString() {
        String ret = new String("");
        for (int i=0; i<chars.size();i++) {
            
            ret = ret + this.chars.elementAt(i).getPattern(0).getChar();
        }
        return ret;
    }

}
