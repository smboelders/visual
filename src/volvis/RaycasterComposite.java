package volvis;

import java.awt.image.BufferedImage;
import volume.Volume;

/** 
 * @author Stan Roelofs
 */
public class RaycasterComposite extends Raycaster {
    
    public RaycasterComposite(int startRow, int endRow, int delta, 
            double[] viewMatrix, BufferedImage image, boolean phong, 
            boolean lowRes, Volume volume, TransferFunction tFunc) {
        
        super(startRow, endRow, delta, viewMatrix, image, phong, lowRes, volume);
        
        this.tFunc = tFunc;
    }
    
    @Override
    public void run() {
        init();
        for (int j = this.startRow; j <= this.endRow - step; j+=step) {
            for (int i = 0; i <= image.getWidth() - step; i+=step) {
                // Initialize compositecolor
                TFColor compositeColor = new TFColor(0,0,0,1);
                
                for (double k = -diagonal/2; k <= diagonal/2 - renderDelta; k += renderDelta) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                            + volumeCenter[0] + k * viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                            + volumeCenter[1] + k * viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                            + volumeCenter[2] + k * viewVec[2];             
                    
                    // Ignore values outside volume
                    if (pixelCoord[0] < 0 || pixelCoord[0] >= volume.getDimX() || pixelCoord[1] < 0 || pixelCoord[1] >= volume.getDimY()
                        || pixelCoord[2] < 0 || pixelCoord[2] >= volume.getDimZ()) {
                        // Skip rest, go to next iteration
                        continue;
                    }
                    
                    // Calculate value at pixelCoord using interpolation
                    int val = TripleInterpolation(pixelCoord, false);                    

                    // Get color corresponding to this value from transfer function
                    voxelColor = tFunc.getColor(val);    
                    
                    // If phong shading is enabled, call phong function to obtain new color
                    if (!this.lowRes && this.phong) {
                        voxelColor = phong(pixelCoord, voxelColor);
                    }
                    
                    // Update compositeColor                   
                    TFColor temp = new TFColor(compositeColor.r, compositeColor.g, compositeColor.b, compositeColor.a);
                    compositeColor.r = voxelColor.r * voxelColor.a + (1 - voxelColor.a) * temp.r;
                    compositeColor.g = voxelColor.g * voxelColor.a + (1 - voxelColor.a) * temp.g;
                    compositeColor.b = voxelColor.b * voxelColor.a + (1 - voxelColor.a) * temp.b;
                }                

                // Set pixel i,j to compositeColor in this.image
                super.setPixel(i, j, compositeColor);
            }
        }
    }
}
