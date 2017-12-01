package volvis;

/**
 * @author Stan Roelofs
 */
public class RaycasterMIP extends Raycaster {
    
    public RaycasterMIP(int delta) {
        super(delta);
    }
    
    @Override
    protected void method() {
        for (int j = 0; j <= image.getHeight() - step; j+=step) {
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
