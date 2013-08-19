package pa.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

public class BufferHelper 
{
    public static final ByteBuffer BYTE = BufferUtils.createByteBuffer(1);
    public static final IntBuffer INT = BufferUtils.createIntBuffer(1);
    public static final LongBuffer LONG = BufferUtils.createLongBuffer(1);
    public static final ShortBuffer SHORT = BufferUtils.createShortBuffer(1);
    public static final DoubleBuffer DOUBLE = BufferUtils.createDoubleBuffer(1);
    public static final FloatBuffer FLOAT = BufferUtils.createFloatBuffer(1);
    
    public static void printArray(int vals[], int stride)
    {
        IntBuffer buffer = BufferUtils.createIntBuffer(vals.length);
        buffer.put(vals);
        printBuffer(buffer, stride);
    }
    
    public static void printArray(float vals[], int stride)
    {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vals.length);
        buffer.put(vals);
        printBuffer(buffer, stride);
    }
    
    public static void printBuffer(Buffer buffer, int stride)
    {
        printBuffer(buffer, stride, null);
    }
    
    public static void printBuffer(Buffer buffer, int stride, String text)
    {
        printBuffer(buffer, stride, text, " ");
    }
    
    public static void printBuffer(Buffer buffer, String seperator, int stride)
    {
        printBuffer(buffer, stride, null, seperator);
    }
    
    public static void printBuffer(Buffer buffer, int stride, String text, String seperator)
    {
        int[] le = getLongestEntryPerCol(buffer, stride);

        if(text != null)
        {
            System.out.println(text);
        }
        
        for(int i = 0; i < buffer.capacity(); ++i)
        {
            Number v = getVal(buffer, i);
            int length = v.toString().length();
            boolean isRowEnd = (i+1) % stride == 0;
            
            System.out.print(v + (isRowEnd ? "\n" : ""));
            
            for(int k = le[i % stride] - length; k > 0 && !isRowEnd; --k)
            {
                System.out.print(" ");
            }
            if(!isRowEnd)
            {
                System.out.print(seperator);   
            }
        }
    }
    
    private static int[] getLongestEntryPerCol(Buffer buffer, int stride)
    {
        int[] e = new int[stride];
        for(int i = 0; i < stride; ++i)
        {
            e[i] = 0;
        }
        
        for(int i = 0; i < buffer.capacity(); ++i)
        {
            int length = getVal(buffer, i).toString().length();
            int index = i % stride;
            e[index] = length > e[index] ? length : e[index];
        }
        return e;
    }
    
    private static Number getVal(Buffer b, int index)
    {
        if(b instanceof FloatBuffer)
        {
            return ((FloatBuffer)(b)).get(index);
        }
        else if(b instanceof IntBuffer)
        {
            return ((IntBuffer)(b)).get(index);
        }
        else if(b instanceof ByteBuffer)
        {
            return ((ByteBuffer)(b)).get(index);
        }
        else if(b instanceof LongBuffer)
        {
            return ((LongBuffer)(b)).get(index);
        }
        else if(b instanceof ShortBuffer)
        {
            return ((ShortBuffer)(b)).get(index);
        }
        else if(b instanceof DoubleBuffer)
        {
            return ((DoubleBuffer)(b)).get(index);
        }
        throw new IllegalArgumentException("wrong buffer format");
    }
}
