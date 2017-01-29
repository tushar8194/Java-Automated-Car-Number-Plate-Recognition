

package numberPlate.imageanalysis;

import java.awt.image.BufferedImage;
//import javaanpr.imageanalysis.Photo;

public class Statistics {
    public float maximum;
    public float minimum;
    public float average;
    public float dispersion;
    
    Statistics(BufferedImage bi) {
        this(new Photo(bi));
    }
    
    Statistics(Photo photo) {
        float sum = 0;
        float sum2 = 0;
        int w = photo.getWidth();
        int h = photo.getHeight();
        
        for (int x=0; x < w;x++) {
            for (int y=0; y < h;y++) {
                float pixelValue = photo.getBrightness(x,y);
                this.maximum = Math.max(pixelValue, this.maximum);
                this.minimum = Math.min(pixelValue, this.minimum);
                sum += pixelValue;
                sum2 += (pixelValue * pixelValue);
            }
        }
        int count = (w * h);
        this.average = sum / count;
        // rozptyl = priemer stvorcov + stvorec priemeru
        this.dispersion = ( sum2 / count ) - ( this.average * this.average);
    }
    
    public float thresholdBrightness(float value, float coef) {
        float out;
        if (value > this.average)
            out = coef + (1 - coef) * (value - this.average) / (this.maximum - this.average);
        else
            out =  (1 - coef) * (value - this.minimum) / (this.average - this.minimum);
        return out;
    }
}
