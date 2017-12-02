package volvis;

import java.awt.image.BufferedImage;
import volume.Volume;

/**
 * @author Stan Roelofs
 */
public class RaycasterMIP extends Raycaster {
    
    public RaycasterMIP(int startHeight, int endHeight, int delta, double[] viewMatrix, BufferedImage image, 
            boolean phong, boolean lowRes, Volume volume, TransferFunction tFunc) {
        super(startHeight, endHeight, delta, viewMatrix, image, phong, lowRes, volume);
        
        this.tFunc = tFunc;
    }
    
    @Override
    public void run() {
        for (int j = this.startHeight; j <= this.endHeight - step; j+=step) {
            for (int i = 0; i <= image.getWidth() - step; i+=step) {
                int maxVal = 0;
                for (int k = -imageCenter / renderDelta; k < imageCenter / renderDelta; k++) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                            + volumeCenter[0] + k * renderDelta * viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                            + volumeCenter[1] + k * renderDelta * viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                            + volumeCenter[2] + k * renderDelta * viewVec[2];                
                    
                    int val = getInterpolatedVoxel(pixelCoord);
                    if (val > maxVal){
                        maxVal = val;
                    }
                }

                voxelColor = tFunc.getColor(maxVal);
                
                if (this.phong) {
                    voxelColor = phong(pixelCoord, voxelColor);
                }
                
                super.setPixel(i, j, voxelColor);
            }
        }
    }
}
