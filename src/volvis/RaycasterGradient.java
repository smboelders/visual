package volvis;

/**
 * @author Stan Roelofs
 */
public class RaycasterGradient extends Raycaster {
    
    public RaycasterGradient(int delta) {
        super(delta);
    }   
    
    private short getInterpolatedGradient(double[] coord) {
        if (coord[0] < 0 || coord[0] >= volume.getDimX() || coord[1] < 0 || coord[1] >= volume.getDimY()
                || coord[2] < 0 || coord[2] >= volume.getDimZ()) {
            return 0;
        }
        
        double x = coord[0];
        double y = coord[1];
        double z = coord[2];
        
        int xFloor = (int) Math.floor(x);
        int yFloor = (int) Math.floor(y);
        int zFloor = (int) Math.floor(z);
        int xCeil = (int) Math.ceil(x);
        int yCeil = (int) Math.ceil(y);
        int zCeil = (int) Math.ceil(z);   
        
        if (xCeil >= volume.getDimX() || yCeil >= volume.getDimY() || zCeil >= volume.getDimZ()) {
            return 0;
        }
        
        float Sx0 = gradients.getGradient(xFloor, yFloor, zFloor).mag;
        float Sx1 = gradients.getGradient(xCeil, yFloor, zFloor).mag;
        float Sx2 = gradients.getGradient(xFloor, yCeil, zFloor).mag;
        float Sx3 = gradients.getGradient(xCeil, yCeil, zFloor).mag;       
        float Sx4 = gradients.getGradient(xFloor, yFloor, zCeil).mag;
        float Sx5 = gradients.getGradient(xCeil, yFloor, zCeil).mag;
        float Sx6 = gradients.getGradient(xFloor, yCeil, zCeil).mag;
        float Sx7 = gradients.getGradient(xCeil, yCeil, zCeil).mag;
        
        double alpha = (x - Math.floor(x)) / (Math.ceil(x) - Math.floor(x)); // (x - x0) / (x1 - x0)
        double beta = (y - Math.floor(y)) / (Math.ceil(y) - Math.floor(y));
        double gamma = (z - Math.floor(z)) / (Math.ceil(z) - Math.floor(z));
        
        double Sx = (1 - alpha) * (1 - beta) * (1 - gamma) * Sx0;
        Sx += alpha * (1 - beta) * (1 - gamma) * Sx1;
        Sx += (1 - alpha) * beta * (1 - gamma) * Sx2;
        Sx += alpha * beta * (1 - gamma) * Sx3;
        Sx += (1 - alpha) * (1 - beta) * gamma * Sx4;
        Sx += alpha * (1 - beta) * gamma * Sx5;
        Sx += (1 - alpha) * beta * gamma * Sx6;
        Sx += alpha * beta * gamma * Sx7;
        
        return (short) Sx;
    }
    
    @Override
    protected void method() {
        double baseIntensity = this.tfEditor2D.triangleWidget.baseIntensity;
        double radius = this.tfEditor2D.triangleWidget.radius;
        TFColor color = this.tfEditor2D.triangleWidget.color;    
        double lowerMag = this.tfEditor2D.triangleWidget.lowerMag;
        double upperMag = this.tfEditor2D.triangleWidget.upperMag;
        
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
                    double mag = getInterpolatedGradient(pixelCoord);  
                    
                    voxelColor = new TFColor(color.r, color.g, color.b, color.a);
                    
                    // Calculate opacity using the third function in Levoy's paper
                    if (mag == 0 && val == baseIntensity) {
                        voxelColor.a = 1;
                    } else if (mag >= lowerMag && mag <= upperMag && mag > 0 && (val - radius * Math.abs(mag) <= baseIntensity && baseIntensity <= val + radius * Math.abs(mag))) {
                        double temp = (baseIntensity - val) / Math.abs(mag);                     
                        voxelColor.a = 1 - ((1/radius) * Math.abs(temp));
                    } else {
                        voxelColor.a = 0;
                    }    
                    voxelColor.a = voxelColor.a * color.a;                   
                    
                    //TODO: phong
                    
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
