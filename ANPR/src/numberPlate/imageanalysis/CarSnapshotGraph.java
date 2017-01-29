package numberPlate.imageanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import numberPlate.intelligence.Intelligence;

public class CarSnapshotGraph extends Graph {
    // configuration for searching bands in image !
    private static double peakFootConstant = 
            Intelligence.configurator.getDoubleProperty("carsnapshotgraph_peakfootconstant"); //0.55
    private static double peakDiffMultiplicationConstant = 
            Intelligence.configurator.getDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant");//0.1
    
    CarSnapshot handle;
    
    public CarSnapshotGraph(CarSnapshot handle) {
        this.handle = handle;
    }
    
    public class PeakComparer implements Comparator {
        Vector<Float> yValues = null;
        
        public PeakComparer(Vector<Float> yValues) {
            this.yValues = yValues;
        }
        
        private float getPeakValue(Object peak) {
            return this.yValues.elementAt( ((Peak)peak).getCenter()  ); // Depending on the intensity
            //return ((Peak)peak).getDiff();
        }
        
        public int compare(Object peak1, Object peak2) { // Peak
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }
    
    public Vector<Peak> findPeaks(int count) {
        
        Vector<Peak> outPeaks = new Vector<Peak>();
        
        for (int c=0; c<count; c++) { // for count
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i=0; i<this.yValues.size(); i++) { // left to right
                if (allowedInterval(outPeaks, i)) { // If a potential peak is in "free" interval that does not fall within the other peaks
                    if (this.yValues.elementAt(i) >= maxValue) {
                        maxValue = this.yValues.elementAt(i);
                        maxIndex = i;
                    }
                }
            } // end for int 0->max
            // nasli sme najvacsi peak
            int leftIndex = indexOfLeftPeakRel(maxIndex,peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex,peakFootConstant);
            int diff = rightIndex - leftIndex;
            leftIndex -= peakDiffMultiplicationConstant * diff;   /*CONSTANT*/
            rightIndex+= peakDiffMultiplicationConstant * diff;   /*CONSTANT*/

                outPeaks.add(new Peak(
                    Math.max(0,leftIndex),
                    maxIndex,
                    Math.min(this.yValues.size()-1,rightIndex)
                    ));
        } // end for count
        
        Collections.sort(outPeaks, (Comparator<? super Graph.Peak>)
                                   new PeakComparer(this.yValues));
        
        super.peaks = outPeaks; 
        return outPeaks;
    }
//    public int indexOfLeftPeak(int peak, double peakFootConstant) {
//        int index=peak;
//        for (int i=peak; i>=0; i--) {
//            index = i;
//            if (yValues.elementAt(index) < peakFootConstant*yValues.elementAt(peak) ) break;
//        }
//        return Math.max(0,index);
//    }
//    public int indexOfRightPeak(int peak, double peakFootConstant) {
//        int index=peak;
//        for (int i=peak; i<yValues.size(); i++) {
//            index = i;
//            if (yValues.elementAt(index) < peakFootConstant*yValues.elementAt(peak) ) break;
//        }
//        return Math.min(yValues.size(), index);
//    }
    
}

