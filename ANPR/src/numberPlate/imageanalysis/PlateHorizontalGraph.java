

package numberPlate.imageanalysis;

//import java.util.Collections;
//import java.util.Comparator;
import java.util.Vector;

import numberPlate.intelligence.Intelligence;


public class PlateHorizontalGraph extends Graph {
    private static double peakFootConstant =// 0.1;  /* CONSTANT*/
            Intelligence.configurator.getDoubleProperty("platehorizontalgraph_peakfootconstant");
    private static int horizontalDetectionType = 
            Intelligence.configurator.getIntProperty("platehorizontalgraph_detectionType");
    
    Plate handle;
    
    public PlateHorizontalGraph(Plate handle) {
        this.handle = handle;
    }
    
    public float derivation(int index1, int index2) {
        return this.yValues.elementAt(index1) - this.yValues.elementAt(index2);
    }
    
    public Vector<Peak> findPeak(int count) {
        if (horizontalDetectionType==1) return findPeak_edgedetection(count);
        return findPeak_derivate(count); 
    }
    
    public Vector<Peak> findPeak_derivate(int count) {  // RIESENIE DERIVACIOU
        int a,b;
        float maxVal = this.getMaxValue();
        
        for (a=2; -derivation(a,a+4) < maxVal*0.2 && a < this.yValues.size()-2-2-4; a++);
        for (b=this.yValues.size()-1-2; derivation(b-4,b) < maxVal*0.2 && b>a+2; b--);

        Vector<Peak> outPeaks = new Vector<Peak>();
       
        outPeaks.add(new Peak(a,b));
        super.peaks = outPeaks;
        return outPeaks;
    }
    
    public Vector<Peak> findPeak_edgedetection (int count) {
        float average = this.getAverageValue();
        int a,b;
        for (a=0; this.yValues.elementAt(a) < average; a++);
        for (b=this.yValues.size()-1; this.yValues.elementAt(b) < average; b--);
        
        Vector<Peak> outPeaks = new Vector<Peak>();
        a = Math.max(a-5,0);
        b = Math.min(b+5,this.yValues.size());
        
        outPeaks.add(new Peak(a,b));
        super.peaks = outPeaks;
        return outPeaks;
    }
}

