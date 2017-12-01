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
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                int maxVal = 0;
                for (int k = -imageCenter / delta; k < imageCenter / delta; k++) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                            + volumeCenter[0] + k * delta * viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                            + volumeCenter[1] + k * delta * viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                            + volumeCenter[2] + k * delta * viewVec[2];                
                    
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
