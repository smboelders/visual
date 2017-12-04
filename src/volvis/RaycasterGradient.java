package volvis;

import gui.TransferFunction2DEditor;
import java.awt.image.BufferedImage;
import volume.GradientVolume;
import volume.Volume;

/**
 * @author Stan Roelofs
 */
public class RaycasterGradient extends Raycaster {
    
    public RaycasterGradient(int startRow, int endRow, int delta, double[] viewMatrix, BufferedImage image, 
            boolean phong, boolean lowRes, Volume volume, GradientVolume gradients, TransferFunction2DEditor tfEditor2D) {
        super(startRow, endRow, delta, viewMatrix, image, phong, lowRes, volume);

        this.tfEditor2D = tfEditor2D;
        this.gradients = gradients;
    }
    
    @Override
    public void run() {
        init();
        
        // Get parameters from triangleWidget
        double baseIntensity = this.tfEditor2D.triangleWidget.baseIntensity;
        double radius = this.tfEditor2D.triangleWidget.radius;
        TFColor color = this.tfEditor2D.triangleWidget.color;    
        double lowerMag = this.tfEditor2D.triangleWidget.lowerMag;
        double upperMag = this.tfEditor2D.triangleWidget.upperMag;
        
        for (int j = this.startRow; j <= this.endRow - step; j+=step) {
            for (int i = 0; i <= image.getWidth() - step; i+=step) {
                // Initialize color for pixel (i,j)
                TFColor compositeColor = new TFColor(0,0,0,1);
                
                // Cast ray back to front
                for (int k = -imageCenter / renderDelta; k < imageCenter / renderDelta; k++) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                            + volumeCenter[0] + k * renderDelta * viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                            + volumeCenter[1] + k * renderDelta * viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                            + volumeCenter[2] + k * renderDelta * viewVec[2];                
                    
                    // Calculate value at pixelCoord using interpolation
                    int val = TripleInterpolation(pixelCoord, false); 
                    
                    // Calculate magnitude at pixelCoord using interpolation
                    double mag = TripleInterpolation(pixelCoord, true); 
                    
                    // Initialize voxelcolor to color from triangleWidget
                    voxelColor = new TFColor(color.r, color.g, color.b, color.a);
                    
                    /* Calculate opacity using the third function in Levoy's paper
                     * While additionality checking whether the magnitude lies within the
                     * allowed range of magnitudes, defined by the extended triangle widget
                     */
                    if (mag == 0 && val == baseIntensity) {
                        voxelColor.a = 1;
                    } else if (mag >= lowerMag && mag <= upperMag && mag > 0 && (val - radius * Math.abs(mag) <= baseIntensity && baseIntensity <= val + radius * Math.abs(mag))) {
                        double temp = (baseIntensity - val) / Math.abs(mag);                     
                        voxelColor.a = 1 - ((1/radius) * Math.abs(temp));
                    } else {
                        voxelColor.a = 0;
                    }    
                    voxelColor.a = voxelColor.a * color.a;                   
                    
                    // If phong shading is enabled, call phong function to obtain new color
                    if (!this.lowRes && this.phong && voxelColor.a > 0) {
                        voxelColor = phong(pixelCoord, voxelColor);
                    }
                    
                    // Update compositeColor  
                    TFColor temp = new TFColor(compositeColor.r, compositeColor.g, compositeColor.b, compositeColor.a);
                    compositeColor.r = voxelColor.r * voxelColor.a + (1 - voxelColor.a) * temp.r;
                    compositeColor.g = voxelColor.g * voxelColor.a + (1 - voxelColor.a) * temp.g;
                    compositeColor.b = voxelColor.b * voxelColor.a + (1 - voxelColor.a) * temp.b;
                }                

                // Set pixel i,j to compositeColor in this.image
                super.setPixel(i, j,compositeColor);
            }
        }
    }
}
