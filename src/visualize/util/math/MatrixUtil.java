package visualize.util.math;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class MatrixUtil 
{    
    public static Matrix4f lookToLH(Vector3f eye, Vector3f dir, Vector3f up, Matrix4f dst) 
    {
        if(dst == null) dst = new Matrix4f();

        Vector3f zAxis = new Vector3f(dir);
        zAxis.normalise();
        Vector3f yAxis = new Vector3f(up);
        Vector3f xAxis = new Vector3f();
        Vector3f.cross(yAxis, zAxis, xAxis);
        xAxis.normalise();
        Vector3f.cross(zAxis, xAxis, yAxis);
              
        dst.m00 = xAxis.x;
        dst.m10 = xAxis.y;
        dst.m20 = xAxis.z;
        dst.m30 = -(eye.x*xAxis.x+eye.y*xAxis.y+eye.z*xAxis.z);

        dst.m01 = yAxis.x;
        dst.m11 = yAxis.y;
        dst.m21 = yAxis.z;
        dst.m31 = -(eye.x*yAxis.x+eye.y*yAxis.y+eye.z*yAxis.z);
        
        dst.m02 = zAxis.x;
        dst.m12 = zAxis.y;
        dst.m22 = zAxis.z;
        dst.m32 = -(eye.x*zAxis.x+eye.y*zAxis.y+eye.z*zAxis.z);
        
        dst.m03 = 0;
        dst.m13 = 0;
        dst.m23 = 0;
        dst.m33 = 1;
        return dst;
    }
    
    public static Matrix4f perspectiveFovLH(float fov, float aspect, float znear, float zfar, Matrix4f dst)
    {
        if(dst == null) dst = new Matrix4f();
        
        double invTan = Math.tan(fov * 0.5);
        
        dst.m00 = (float)(1.0 / invTan);
        dst.m01 = 0;
        dst.m02 = 0;
        dst.m03 = 0;

        dst.m10 = 0;
        dst.m11 = (float)(aspect / invTan);
        dst.m12 = 0;
        dst.m13 = 0;

        dst.m20 = 0;
        dst.m21 = 0;
        dst.m22 = (zfar+znear) / (zfar-znear);
        dst.m23 = 1;

        dst.m30 = 0;
        dst.m31 = 0;
        dst.m32 = zfar * (1.0f-dst.m22);
        dst.m33 = 0;

        return dst;
    }
}
