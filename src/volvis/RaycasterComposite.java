package volvis;

/** 
 * @author Stan Roelofs
 */
public class RaycasterComposite extends Raycaster {
    
    public RaycasterComposite(int delta) {
        super(delta);
    }
    
    @Override
    protected void method() {
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                TFColor compositeColor = new TFColor(0,0,0,1);
                
                for (int k = -imageCenter / delta; k < imageCenter / delta; k++) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                            + volumeCenter[0] + k * delta * viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                            + volumeCenter[1] + k * delta * viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                            + volumeCenter[2] + k * delta * viewVec[2];                
                    
                    int val = getInterpolatedVoxel(pixelCoord);                    

                    voxelColor = tFunc.getColor(val);    
                    
                    if (this.phong) {
                        voxelColor = phong(pixelCoord, voxelColor);
                    }
                    
                    TFColor temp = new TFColor(compositeColor.r, compositeColor.g, compositeColor.b, compositeColor.a);
                    compositeColor.r = voxelColor.r * voxelColor.a + (1 - voxelColor.a) * temp.r;
                    compositeColor.g = voxelColor.g * voxelColor.a + (1 - voxelColor.a) * temp.g;
                    compositeColor.b = voxelColor.b * voxelColor.a + (1 - voxelColor.a) * temp.b;
                }                

                super.setPixel(i, j,compositeColor);
            }
        }
    }
}
