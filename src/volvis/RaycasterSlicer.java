package volvis;

/**
 * @author Stan Roelofs
 */
public class RaycasterSlicer extends Raycaster {
    
    public RaycasterSlicer(int delta) {
        super(delta);
    }
    
    @Override
    protected void method() {
        for (int j = 0; j <= image.getHeight() - step; j+=step) {
            for (int i = 0; i <= image.getWidth() - step; i+=step) {
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                int val = getVoxel(pixelCoord);

                // Map the intensity to a grey value by linear scaling
                voxelColor.r = val / max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                // voxelColor = tFunc.getColor(val);

                super.setPixel(i, j, voxelColor);
            }
        }
    }
}
