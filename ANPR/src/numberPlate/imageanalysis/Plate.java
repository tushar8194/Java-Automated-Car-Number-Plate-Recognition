
package numberPlate.imageanalysis;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
//import java.io.IOException;
import java.util.Vector;

import numberPlate.intelligence.Intelligence;


public class Plate extends Photo {
    static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0,0,0,0);
    static private int numberOfCandidates = Intelligence.configurator.getIntProperty("intelligence_numberOfChars");
    private static int horizontalDetectionType = 
            Intelligence.configurator.getIntProperty("platehorizontalgraph_detectionType");    
    
    private PlateGraph graphHandle = null;
    public Plate plateCopy;
    
    /** Creates a new instance of Character */
    public Plate() {
        image = null;
    }
    
    public Plate(BufferedImage bi) {
        super(bi);
        this.plateCopy = new Plate(duplicateBufferedImage(this.image), true);
        this.plateCopy.adaptiveThresholding();
    }
    
    public Plate(BufferedImage bi, boolean isCopy) {
        super(bi);
    }
    
    public BufferedImage renderGraph() {
        this.computeGraph();
        return graphHandle.renderHorizontally(this.getWidth(), 100);
    }
    
    private Vector<Graph.Peak> computeGraph() {
        if (graphHandle != null) return graphHandle.peaks; // graf uz bol vypocitany

        graphHandle = histogram(plateCopy.getBi()); //PlateGraph graph = histogram(imageCopy); 
        graphHandle.applyProbabilityDistributor(distributor);
        graphHandle.findPeaks(numberOfCandidates);        
        
        return graphHandle.peaks;
    }    
    
    public Vector<Char> getChars() {
        Vector<Char> out = new Vector<Char>();

        Vector<Graph.Peak> peaks = computeGraph();
        
        for (int i=0; i<peaks.size(); i++) {
            // vyseknut z povodneho! obrazka znacky, a ulozit do vektora. POZOR !!!!!! Vysekavame z povodneho, takze
            // na suradnice vypocitane z imageCopy musime uplatnit inverznu transformaciu
            Graph.Peak p = peaks.elementAt(i);
            if (p.getDiff() <= 0) continue;
            out.add(new Char(
                        image.getSubimage(
                            p.getLeft()  ,
                            0  ,
                            p.getDiff()  ,
                            image.getHeight()
                        )  ,                         
                    this.plateCopy.image.getSubimage(
                            p.getLeft()  ,
                            0  ,
                            p.getDiff()  ,
                            image.getHeight()
                        )  ,  
                        new PositionInPlate(p.getLeft(), p.getRight())
                    )
                    );
        }
        
        return out;
    }

    public Plate clone() {
        return new Plate(this.duplicateBufferedImage(this.image));
    }   
         
    public void horizontalEdgeBi(BufferedImage image) {
        BufferedImage imageCopy = duplicateBufferedImage(image);
        float data[] = {
          -1,0,1
        };
        new ConvolveOp(new Kernel(1,3, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
    }    
    
    public void normalize() {
        // pre ucely orezania obrazka sa vytvori klon ktory sa normalizuje a prahuje s
        // koeficientom 0.999. funkcie cutTopBottom a cutLeftRight orezu originalny
        // obrazok na zaklade horizontalnej a vertikalnej projekcie naklonovaneho
        // obrazka, ktory je prahovany
        
        Plate clone1 = this.clone();
        clone1.verticalEdgeDetector(clone1.getBi());
        PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getBi());
        this.image = cutTopBottom(this.image, vertical);
        this.plateCopy.image = cutTopBottom(this.plateCopy.image, vertical);

        Plate clone2 = this.clone();
        if (horizontalDetectionType == 1) clone2.horizontalEdgeDetector(clone2.getBi());
        PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getBi());
        this.image = cutLeftRight(this.image, horizontal);        
        this.plateCopy.image = cutLeftRight(this.plateCopy.image, horizontal);
        
    }
    private BufferedImage cutTopBottom(BufferedImage origin, PlateVerticalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,0f,2,2));
        Graph.Peak p = graph.findPeak(3).elementAt(0);
        return origin.getSubimage(0,p.getLeft(),this.image.getWidth(),p.getDiff());
    }
    private BufferedImage cutLeftRight(BufferedImage origin, PlateHorizontalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,0f,2,2));
        Vector<Graph.Peak> peaks = graph.findPeak(3);
        
        if (peaks.size()!=0) {
            Graph.Peak p = peaks.elementAt(0);
            return origin.getSubimage(p.getLeft(),0,p.getDiff(),image.getHeight());
        }
        return origin;
    }

    
    public PlateGraph histogram(BufferedImage bi) {
        PlateGraph graph = new PlateGraph(this);
        for (int x=0; x<bi.getWidth(); x++) {
            float counter = 0;
            for (int y=0; y<bi.getHeight();y++)
                counter += getBrightness(bi,x,y);
            graph.addPeak(counter);
        }
        return graph;
    }
    
    private PlateVerticalGraph histogramYaxis(BufferedImage bi) {
        PlateVerticalGraph graph = new PlateVerticalGraph(this);
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int y=0; y<h; y++) {
            float counter = 0;
            for (int x=0; x<w;x++)
                counter += getBrightness(bi,x,y);
            graph.addPeak(counter);
        }
        return graph;        
    }
    private PlateHorizontalGraph histogramXaxis(BufferedImage bi) {
        PlateHorizontalGraph graph = new PlateHorizontalGraph(this);
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x<w; x++) {
            float counter = 0;
            for (int y=0; y<h;y++)
                counter += getBrightness(bi,x,y);
            graph.addPeak(counter);
        }
        return graph;
    }   

    public void verticalEdgeDetector(BufferedImage source) {
        
        float matrix[] = {
            -1,0,1
        };
        
        BufferedImage destination = duplicateBufferedImage(source);
        
        new ConvolveOp(new Kernel(3, 1, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);        

    } 
    
    public void horizontalEdgeDetector(BufferedImage source) {
        BufferedImage destination = duplicateBufferedImage(source);
        
        float matrix[] = {
            -1,-2,-1,
            0,0,0,
            1,2,1
        };

        new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
    }    
    
    public float getCharsWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAverageCharWidth(chars);
       
        for (Char chr : chars) 
            averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
        averageDispersion /= chars.size();
        
        return averageDispersion / averageWidth;
    }
    public float getPiecesWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAveragePieceWidth(chars);
       
        for (Char chr : chars) 
            averageDispersion += (Math.abs(averageWidth - chr.pieceWidth));
        averageDispersion /= chars.size();
        
        return averageDispersion / averageWidth;
    }    
    
    public float getAverageCharWidth(Vector<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) 
            averageWidth += chr.fullWidth;
        averageWidth /= chars.size();
        return averageWidth;
    }
    public float getAveragePieceWidth(Vector<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) 
            averageWidth += chr.pieceWidth;
        averageWidth /= chars.size();
        return averageWidth;
    }    

    public float getAveragePieceHue(Vector<Char> chars) throws Exception {
        float averageHue = 0;
        for (Char chr : chars) 
            averageHue += chr.statisticAverageHue;
        averageHue /= chars.size();
        return averageHue;
    }  
    public float getAveragePieceContrast(Vector<Char> chars) throws Exception {
        float averageContrast = 0;
        for (Char chr : chars) 
            averageContrast += chr.statisticContrast;
        averageContrast /= chars.size();
        return averageContrast;
    }    
    public float getAveragePieceBrightness(Vector<Char> chars) throws Exception {
        float averageBrightness = 0;
        for (Char chr : chars) 
            averageBrightness += chr.statisticAverageBrightness;
        averageBrightness /= chars.size();
        return averageBrightness;
    }     
    public float getAveragePieceMinBrightness(Vector<Char> chars) throws Exception {
        float averageMinBrightness = 0;
        for (Char chr : chars) 
            averageMinBrightness += chr.statisticMinimumBrightness;
        averageMinBrightness /= chars.size();
        return averageMinBrightness;
    }   
    public float getAveragePieceMaxBrightness(Vector<Char> chars) throws Exception {
        float averageMaxBrightness = 0;
        for (Char chr : chars) 
            averageMaxBrightness += chr.statisticMaximumBrightness;
        averageMaxBrightness /= chars.size();
        return averageMaxBrightness;
    }       
    
    public float getAveragePieceSaturation(Vector<Char> chars) throws Exception {
        float averageSaturation = 0;
        for (Char chr : chars) 
            averageSaturation += chr.statisticAverageSaturation;
        averageSaturation /= chars.size();
        return averageSaturation;
    }        
    
    public float getAverageCharHeight(Vector<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars)
            averageHeight += chr.fullHeight;
        averageHeight /= chars.size();
        return averageHeight;
    }
    public float getAveragePieceHeight(Vector<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars)
            averageHeight += chr.pieceHeight;
        averageHeight /= chars.size();
        return averageHeight;
    }    
    
//    public float getAverageCharSquare(Vector<Char> chars) {
//        float average = 0;
//        for (Char chr : chars)
//            average += chr.getWidth() * chr.getHeight();
//        average /= chars.size();
//        return average;
//    }
    

}
