package pa.util.math;

import java.util.Random;



public class MathUtil 
{
    public static final float PI = (float)Math.PI;
    
    public static final float PI_DIV2 = PI * 0.5f;
    
    public static final float PI_DIV4 = PI_DIV2 * 0.5f;
    
    public static final float PI_MUL2 = 2.f * PI;
    
    public static final int RAND_SEED = 10;
    
    private static Random RANDOM = null;
    
    public static float sin(float f)
    {
        return (float)Math.sin(f);
    }
    
    public static float cos(float f) 
    {
        return (float)Math.cos(f);
    }
    
    public static float tan(float f)
    {
        return (float)Math.tan(f);
    }
    
    public static float nextFloat()
    {
        return _rand().nextFloat();
    }
    
    public static float nextCubeFloat()
    {
        return (MathUtil.nextBool() ? -1 : 1) * _rand().nextFloat();
    }
    
    public static float nextFloat(float f)
    {
        return f * _rand().nextFloat();
    }
    
    public static double nextDouble()
    {
        return _rand().nextDouble();
    }
    
    public static double nextDouble(double d)
    {
        return d * _rand().nextDouble();
    }
    
    public static int nextInt(int n)
    {
        return _rand().nextInt(n);
    }
    
    public static int nextInt()
    {
        return _rand().nextInt();
    }
    
    public static boolean nextBool()
    {
        return nextInt(2) < 1;
    }
    
    public static float clamp(float v, float min, float max) 
    {
        return Math.max(min, Math.min(max, v));
    }
    
    public static float lerp(float x, float min, float max) 
    {
        return min + (max - min) * x;
    }
    
    private static Random _rand()
    {
        if(RANDOM == null)
        {
            RANDOM = new Random(RAND_SEED);
        }
        return RANDOM;
    }
}
