package numberPlate.intelligence;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import numberPlate.Main;
import numberPlate.configurator.Configurator;
import numberPlate.imageanalysis.Band;
import numberPlate.imageanalysis.CarSnapshot;
import numberPlate.imageanalysis.Char;
import numberPlate.imageanalysis.HoughTransformation;
import numberPlate.imageanalysis.Photo;
import numberPlate.imageanalysis.Plate;
import numberPlate.recognizer.CharacterRecognizer;
import numberPlate.recognizer.KnnPatternClassificator;
import numberPlate.recognizer.CharacterRecognizer.RecognizedChar;


public class Intelligence {
   // private long lastProcessDuration = 0; // the duration of the last process in MS
    
    public static Configurator configurator = new Configurator("."+File.separator+"config.xml");
    public  CharacterRecognizer chrRecog;
    public  Parser parser;
    public boolean enableReportGeneration;
    
    public Intelligence(boolean enableReportGeneration) throws Exception {
        this.enableReportGeneration = enableReportGeneration;
        int classification_method = Intelligence.configurator.getIntProperty("intelligence_classification_method");
        
        if (classification_method == 0)
        	
        {
            this.chrRecog = new KnnPatternClassificator();
           // System.out.println("In KNN ");
        }
        else
        { 
        	//this.chrRecog = new NeuralPatternClassificator();
        	System.out.println(" Error : Need Neural");
        }
        
       this.parser = new Parser();
    }
    
    // returns how long in milliseconds it took last recognition
   /* public long lastProcessDuration() {
        return this.lastProcessDuration;
    }*/
 
    public String recognize(CarSnapshot carSnapshot) throws Exception {
        //TimeMeter time = new TimeMeter();
        int syntaxAnalysisMode = Intelligence.configurator.getIntProperty("intelligence_syntaxanalysis");
        int skewDetectionMode = Intelligence.configurator.getIntProperty("intelligence_skewdetection");
        
       
        
        for (Band b : carSnapshot.getBands()) { //recomended 3
            
            for (Plate plate : b.getPlates()) {//recomended 3

                // SKEW-RELATED
                Plate notNormalizedCopy = null;
                BufferedImage renderedHoughTransform = null;
                HoughTransformation hough = null;
                if (enableReportGeneration || skewDetectionMode!=0) { // Detection was doing but one) any report generator U2) to correct
                    notNormalizedCopy = plate.clone();
                    notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
                    hough = notNormalizedCopy.getHoughTransformation(); 
                    renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
                }
                if (skewDetectionMode!=0) { // correction is made only when it is on
                    AffineTransform shearTransform = AffineTransform.getShearInstance(0,-(double)hough.dy/hough.dx);
                    BufferedImage core = plate.createBlankBi(plate.getBi());
                    core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
                    plate = new Plate(core);
                }
                
                plate.normalize();
                
                float plateWHratio = (float)plate.getWidth() / (float)plate.getHeight();
                if (plateWHratio < Intelligence.configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio")
                ||  plateWHratio > Intelligence.configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio")
                ) continue;
                
                Vector<Char> chars = plate.getChars();
                
                
                // Heuristic analysis have brand uniformity and number of letters:
                //Recognizer.configurator.getIntProperty("intelligence_minimumChars")
                if (chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars") ||
                        chars.size() > Intelligence.configurator.getIntProperty("intelligence_maximumChars")
                        ) continue;
                
                if (plate.getCharsWidthDispersion(chars) > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthDispersion")
                ) continue;
                
                // BRAND income, beginning of normalization and heuristics Letter 

                if (enableReportGeneration) {
                    Plate plateCopy = plate.clone();
                    plateCopy.linearResize(450, 90);
                }
      
                RecognizedPlate recognizedPlate = new RecognizedPlate();
                                
                for (Char chr : chars) chr.normalize();
                
                float averageHeight = plate.getAveragePieceHeight(chars);
                float averageContrast = plate.getAveragePieceContrast(chars);
                float averageBrightness = plate.getAveragePieceBrightness(chars);
                float averageHue = plate.getAveragePieceHue(chars);
                float averageSaturation = plate.getAveragePieceSaturation(chars);
                
                for (Char chr : chars) {
                    // heuristicka analyza jednotlivych pismen
                    boolean ok = true;
                    String errorFlags = "";
                    
                    // pri normalizovanom pisme musime uvazovat pomer
                    float widthHeightRatio = (float)(chr.pieceWidth);
                    widthHeightRatio /= (float)(chr.pieceHeight);
                    
                    if (widthHeightRatio < Intelligence.configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio") ||
                            widthHeightRatio > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthHeightRatio")
                            ) {
                        errorFlags += "WHR ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    
                    
                    if ((chr.positionInPlate.x1 < 2 ||
                            chr.positionInPlate.x2 > plate.getWidth()-1)
                            && widthHeightRatio < 0.12
                            ) {
                        errorFlags += "POS ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    
                    
                    //float similarityCost = rc.getSimilarityCost();
                    
                    float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
                    float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
                    float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
                    float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
                    float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;
                    
                    if (brightnessCost > Intelligence.configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
                        errorFlags += "BRI ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (contrastCost > Intelligence.configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
                        errorFlags += "CON ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (hueCost > Intelligence.configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
                        errorFlags += "HUE ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (saturationCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
                        errorFlags += "SAT ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (heightCost < -Intelligence.configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
                        errorFlags += "HEI ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    
                    float similarityCost = 0;
                    RecognizedChar rc = null;
                    if (ok==true) {
                        rc = this.chrRecog.recognize(chr);
                        similarityCost = rc.getPatterns().elementAt(0).getCost();
                        if (similarityCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
                            errorFlags += "NEU ";
                            ok = false;
                            if (!enableReportGeneration) continue;
                        }
                        
                    }
                    
                    if (ok==true) {
                        recognizedPlate.addChar(rc);
                    }  
                } // end for each char
                
                // The following line for processing the another candidate to sign, in the case of too little charrecognizingu Letter Recognition
                if (recognizedPlate.chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars")) continue;
                
               // this.lastProcessDuration = time.getTime();
                String parsedOutput = parser.parse(recognizedPlate, syntaxAnalysisMode);
                
                return parsedOutput;
            
            } // end for each  plate

        }
        
       // this.lastProcessDuration = time.getTime();
        //return new String("not available yet ;-)");
        return null;
    }
}