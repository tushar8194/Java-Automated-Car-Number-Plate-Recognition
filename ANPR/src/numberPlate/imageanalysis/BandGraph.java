package numberPlate.imageanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import numberPlate.intelligence.Intelligence;


    public class BandGraph extends Graph {/* TODO - BEGIN */
        Band handle;
        
        private static double peakFootConstant = 
                Intelligence.configurator.getDoubleProperty("bandgraph_peakfootconstant"); //0.75
        private static double peakDiffMultiplicationConstant = 
                Intelligence.configurator.getDoubleProperty("bandgraph_peakDiffMultiplicationConstant");  // 0.2

        
        public BandGraph(Band handle) {
            this.handle = handle; // carries a link to this image (band), to which the chart applies
        }
        
        public class PeakComparer implements Comparator {
            Vector<Float> yValues = null;
            
            public PeakComparer(Vector<Float> yValues) {
                this.yValues = yValues;
            }
            
            private float getPeakValue(Object peak) {
                //return ((Peak)peak).center(); // left > right
                
                return this.yValues.elementAt( ((Peak)peak).getCenter()  ); // size parachute
            }
            
            public int compare(Object peak1, Object peak2) { 
                double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
                if (comparison < 0) return -1;
                if (comparison > 0) return 1;
                return 0;
            }
        }
        
        public Vector<Peak> findPeaks(int count) {
            Vector<Graph.Peak> outPeaks = new Vector<Peak>();
            
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
                
                // We are the largest found a peek // do first Punching
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
            

            
            // necessary to filter candidates who match these proportions brands
            Vector<Peak> outPeaksFiltered = new Vector<Peak>();
            for (Peak p : outPeaks) {
                if (p.getDiff() > 2 * this.handle.getHeight() && // if not mark too narrow
                    p.getDiff() < 15 * this.handle.getHeight() // or not too wide
                    ) outPeaksFiltered.add(p);// the ok, I take it
               // else outPeaksFiltered.add(p);// the ok, I take it
            }
            
            Collections.sort(outPeaksFiltered, (Comparator<? super Graph.Peak>)
                                               new PeakComparer(this.yValues));
            super.peaks = outPeaksFiltered;
            return outPeaksFiltered;
            
        }
        public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
            int index=peak;
            int counter = 0;
            for (int i=peak; i>=0; i--) {
                index = i;
                if (yValues.elementAt(index) < peakFootConstantAbs  ) break;
            }
            return Math.max(0,index);
        }
        public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
            int index=peak;
            int counter = 0;
            for (int i=peak; i<yValues.size(); i++) {
                index = i;
                if (yValues.elementAt(index) < peakFootConstantAbs ) break;
            }
            return Math.min(yValues.size(), index);
        }
        /* TODO - END */
    }
