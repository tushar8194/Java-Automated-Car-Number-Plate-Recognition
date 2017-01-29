package numberPlate.imageanalysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class HoughTransformation {
    public static int RENDER_ALL = 1;
    public static int RENDER_TRANSFORMONLY = 0;
    public static int COLOR_BW = 0;
    public static int COLOR_HUE = 1;
    
    float[][] bitmap;
    Point maxPoint;
    private int width;
    private int height;
    
    public float angle = 0;
    public float dx = 0;
    public float dy = 0;
    
    public HoughTransformation(int width, int height) {
        this.maxPoint = null;
        this.bitmap = new float[width][height];
        this.width = width;
        this.height = height;
        for (int x=0; x<this.width; x++)
            for (int y=0; y<this.height; y++)
                this.bitmap[x][y] = 0;
    }

    public void addLine(int x, int y, float brightness) {
        // coordinate system will move out of the middle : -1 .. 1, -1 .. 1
        float xf = 2*((float)x)/this.width - 1;
        float yf = 2*((float)y)/this.height - 1;
        // y=ax + b
        // b = y - ax

        for (int a=0; a<this.width;a++) {     
            // We skip to the middle
            float af = 2*((float)a)/this.width - 1;
            // we calculate
            float bf = yf - af * xf;
            // shifted to the original coordinate system
            int b = (int)(  (bf+1)*this.height/2  );
            
            if (0 < b && b < this.height-1) {
                bitmap[a][b] += brightness;
            }
        }
    }
  
    private float getMaxValue() {
        float maxValue = 0;
        for (int x=0; x<this.width; x++) 
            for (int y=0; y<this.height; y++)
                maxValue=Math.max(maxValue, this.bitmap[x][y]);
        return maxValue;
    }
    
    private Point computeMaxPoint() {
        float max = 0;
        int maxX = 0, maxY = 0;
        for (int x=0; x<this.width; x++)  {
            for (int y=0; y<this.height; y++) {
                float curr = this.bitmap[x][y];
                if (curr >= max) {
                    maxX = x;
                    maxY = y;
                    max = curr;
                }
            }
        }
        return new Point(maxX, maxY);
    }
    public Point getMaxPoint() {
        if (this.maxPoint == null) this.maxPoint = this.computeMaxPoint();
        return this.maxPoint;
    }
    
    private float getAverageValue() {
        float sum = 0;
        for (int x=0; x<this.width; x++) 
            for (int y=0; y<this.height; y++)
                sum+=this.bitmap[x][y];
        return sum/(this.width*this.height);
    }    
    
    public BufferedImage render(int renderType, int colorType) {
        
        float average = this.getAverageValue();
        BufferedImage output = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        
        for (int x=0; x<this.width; x++) {
            for (int y=0; y<this.height; y++) {
                int value = (int)(255 * this.bitmap[x][y] / average/3);
                //int value = (int)Math.log(this.bitmap[x][y]*1000);
                value = Math.max(0,Math.min(value,255));
                if (colorType == HoughTransformation.COLOR_BW) {
                    output.setRGB(x,y, new Color(value,value,value).getRGB());
                } else {
                    output.setRGB(x,y, Color.HSBtoRGB(0.67f - ((float)value/255)*2/3, 1.0f, 1.0f));
                }
            }
        }
        this.maxPoint = computeMaxPoint();
        g.setColor(Color.ORANGE);
        
        
        float a = 2*((float)this.maxPoint.x)/this.width - 1;
        float b = 2*((float)this.maxPoint.y)/this.height - 1;
        //int b = this.maxPoint.y;
        float x0f = -1;
        float y0f = a * x0f + b;
        float x1f = 1;
        float y1f = a * x1f + b;
        
        
        int y0 = (int)( (y0f+1)*this.height/2  );
        int y1 = (int)( (y1f+1)*this.height/2  );

        int dx = this.width;
        int dy = y1 - y0;
        this.dx = dx;
        this.dy = dy;
        this.angle = (float) (180 * Math.atan(this.dy/this.dx) / Math.PI);
        
        if (renderType==HoughTransformation.RENDER_ALL) {
            g.drawOval(this.maxPoint.x-5, this.maxPoint.y-5, 10,10);
            g.drawLine(0,this.height/2-(int)dy/2-1,this.width,this.height/2+(int)dy/2-1);
            g.drawLine(0,this.height/2-(int)dy/2+0,this.width,this.height/2+(int)dy/2+0);
            g.drawLine(0,this.height/2-(int)dy/2+1,this.width,this.height/2+(int)dy/2+1);
        }
        
        return output;
    }
}
