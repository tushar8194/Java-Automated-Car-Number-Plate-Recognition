package numberPlate.imageanalysis;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Vector;

import numberPlate.intelligence.Intelligence;


public class Band extends Photo {
    static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0,0,25,25);
    static private int numberOfCandidates = Intelligence.configurator.getIntProperty("intelligence_numberOfPlates");
            
    private BandGraph graphHandle = null;
    
    /** Creates a new instance of Band */
    public Band() {
        image = null;
    }
    
    public Band(BufferedImage bi) {
        super(bi);
    }
    
    public BufferedImage renderGraph() {
        this.computeGraph();
        return graphHandle.renderHorizontally(this.getWidth(), 100);
    }
    
    private Vector<Graph.Peak> computeGraph() {
        if (graphHandle != null) return graphHandle.peaks; // Chart already been calculated
        BufferedImage imageCopy = duplicateBufferedImage(this.image);
        fullEdgeDetector(imageCopy);
        graphHandle = histogram(imageCopy);
        graphHandle.rankFilter(image.getHeight());
        graphHandle.applyProbabilityDistributor(distributor);
        graphHandle.findPeaks(numberOfCandidates);
        return graphHandle.peaks;
    }
    
    public Vector<Plate> getPlates() {
        Vector<Plate> out = new Vector<Plate>();
        
        Vector<Graph.Peak> peaks = computeGraph();
        
        for (int i=0; i<peaks.size(); i++) {
            // get out of the flood! Brand image and save it to the vector. CAUTION !!!!!! Die-cut from the original, so
            // the coordinates calculated from imagecopy we need to apply inversion transformation
            Graph.Peak p = peaks.elementAt(i);
            out.add(new Plate(
                    image.getSubimage(  p.getLeft()  ,
                    0  ,
                    p.getDiff()  ,
                    image.getHeight()  )
                    ))
                    ;
        }
        return out;
    }
    
//    public void horizontalRankBi(BufferedImage image) {
//        BufferedImage imageCopy = duplicateBi(image);
//        
//        float data[] = new float[image.getHeight()];
//        for (int i=0; i<data.length; i++) data[i] = 1.0f/data.length;
//        
//        new ConvolveOp(new Kernel(data.length,1, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
//    }
    
    public BandGraph histogram(BufferedImage bi) {
        BandGraph graph = new BandGraph(this);
        for (int x=0; x<bi.getWidth(); x++) {
            float counter = 0;
            for (int y=0; y<bi.getHeight();y++)
                counter += getBrightness(bi,x,y);
            graph.addPeak(counter);
        }
        return graph;
    }
    
   public void fullEdgeDetector(BufferedImage source) {
        float verticalMatrix[] = {
            -1,0,1,
            -2,0,2,
            -1,0,1,
        };
        float horizontalMatrix[] = {
            -1,-2,-1,
            0, 0, 0,
            1, 2, 1
        };
        
        BufferedImage i1 = createBlankBi(source);
        BufferedImage i2 = createBlankBi(source);
        
        new ConvolveOp(new Kernel(3, 3, verticalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i1);
        new ConvolveOp(new Kernel(3, 3, horizontalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i2);
        
        int w = source.getWidth();
        int h = source.getHeight();
        
        for (int x=0; x < w; x++)
            for (int y=0; y < h; y++) {
            float sum = 0.0f;
            sum += this.getBrightness(i1,x,y);
            sum += this.getBrightness(i2,x,y);
            this.setBrightness(source, x, y, Math.min(1.0f, sum));
            }
        
    }    
    
}
