package pa.cl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLBuildProgramCallback;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;

import pa.cl.CLUtil.CLError;
import pa.cl.CLUtil.CLProgramBuildError;
import pa.util.BufferHelper;

/**
 * @author Henning Wenke
 * @author Sascha Kolodzey
 */
public class OpenCL 
{
    private static final IntBuffer lastErrorCode = BufferUtils.createIntBuffer(1);
    
    static
    {
        lastErrorCode.put(0, CL10.CL_SUCCESS);
    }
    
    private OpenCL() {}

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, long host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, Buffer host_ptr) 
    {
        CLMem mem = null;
        if(host_ptr instanceof FloatBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (FloatBuffer)host_ptr, lastErrorCode); 
        }
        else if(host_ptr instanceof IntBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (IntBuffer)host_ptr, lastErrorCode); 
        }
        else if(host_ptr instanceof ByteBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (ByteBuffer)host_ptr, lastErrorCode); 
        }
        else if(host_ptr instanceof LongBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (LongBuffer)host_ptr, lastErrorCode); 
        }
        else if(host_ptr instanceof ShortBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (ShortBuffer)host_ptr, lastErrorCode); 
        }
        else if(host_ptr instanceof DoubleBuffer)
        {
            mem = CL10.clCreateBuffer(context, flags, (DoubleBuffer)host_ptr, lastErrorCode); 
        }
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateFromGLBuffer.html">clCreateFromGLBuffer</a>
     */
    public static CLMem clCreateFromGLBuffer(CLContext context, long flags, int bufobj) 
    {
        CLMem mem = CL10GL.clCreateFromGLBuffer(context, flags, bufobj, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateFromGLTexture2D.html">clCreateFromGLTexture2D</a>
     */
    public static CLMem clCreateFromGLTexture2D(CLContext context, long flags, int target, int mimaplevel, int texobj) 
    {
        CLMem mem = CL10GL.clCreateFromGLTexture2D(context, CL10.CL_MEM_READ_WRITE, GL11.GL_TEXTURE_2D, 0, texobj, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, ByteBuffer host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, IntBuffer host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateKernel.html">clCreateKernel</a>
     */
    public static CLKernel clCreateKernel(CLProgram program, String name) 
    {
        CLKernel kernel = CL10.clCreateKernel(program, name, lastErrorCode);
        checkError();
        return kernel;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clSetKernelArg.html">clSetKernelArg</a>
     */
    public static void clSetKernelArg(CLKernel kernel, int arg_index, Number arg_value) 
    {
        if(arg_value instanceof Float)
        {
            BufferHelper.FLOAT.put(0, arg_value.floatValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index, BufferHelper.FLOAT));   
        } 
        else if(arg_value instanceof Double)
        {
            BufferHelper.DOUBLE.put(0, arg_value.doubleValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index, BufferHelper.DOUBLE));   
        }
        else if(arg_value instanceof Integer)
        {
            BufferHelper.INT.put(0, arg_value.intValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index, BufferHelper.INT));   
        }
        else if(arg_value instanceof Short)
        {
            BufferHelper.SHORT.put(0, arg_value.shortValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index, BufferHelper.SHORT));   
        }
        else if(arg_value instanceof Long)
        {
            BufferHelper.LONG.put(0, arg_value.longValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index, BufferHelper.LONG));   
        }
        else if(arg_value instanceof Byte)
        {
            BufferHelper.BYTE.put(0, arg_value.byteValue());
            checkError(CL10.clSetKernelArg(kernel, arg_index,BufferHelper.BYTE));   
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clSetKernelArg.html">clSetKernelArg</a>
     */
    public static void clSetKernelArg(CLKernel kernel, int arg_index, CLMem arg_value) 
    {
        checkError(CL10.clSetKernelArg(kernel, arg_index, arg_value));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateProgramWithSource.html">clCreateProgramWithSource</a>
     */
    public static CLProgram clCreateProgramWithSource(CLContext context, String source) 
    {
        CLProgram program = CL10.clCreateProgramWithSource(context, source, lastErrorCode);
        checkError();
        return program;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueAcquireGLObjects.html">clEnqueueAcquireGLObjects</a>
     */
    public static void clEnqueueAcquireGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        checkError(CL10GL.clEnqueueAcquireGLObjects(command_queue, mem_object, event_wait_list, event));
        if(CLUtil.ERROR_CHECK)
        {
            clFinish(command_queue);
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueWriteBuffer.html">clEnqueueWriteBuffer</a>
     */
    public static void clEnqueueWriteBuffer(CLCommandQueue command_queue, CLMem mem_object, int blocking_writing, long offset, Buffer ptr, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        if(ptr instanceof ByteBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (ByteBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof FloatBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (FloatBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof IntBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (IntBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof LongBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (LongBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof DoubleBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (DoubleBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof ShortBuffer)
        {
            checkError(CL10.clEnqueueWriteBuffer(command_queue, mem_object, blocking_writing, offset, (ShortBuffer)ptr, event_wait_list, event));   
        }
        else
        {
            throw new IllegalArgumentException(String.format("BufferType '%s' not supported.", ptr.getClass().getSimpleName()));
        }
        if(CLUtil.ERROR_CHECK)
        {
            clFinish(command_queue);
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueReadBuffer.html">clEnqueueReadBuffer</a>
     */
    public static void clEnqueueReadBuffer(CLCommandQueue command_queue, CLMem mem_object, int blocking_read, long offset, Buffer ptr, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        if(ptr instanceof ByteBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (ByteBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof FloatBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (FloatBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof IntBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (IntBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof LongBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (LongBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof DoubleBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (DoubleBuffer)ptr, event_wait_list, event));   
        }
        else if(ptr instanceof ShortBuffer)
        {
            checkError(CL10.clEnqueueReadBuffer(command_queue, mem_object, blocking_read, offset, (ShortBuffer)ptr, event_wait_list, event));   
        }
        else
        {
            throw new IllegalArgumentException(String.format("BufferType '%s' not supported.", ptr.getClass().getSimpleName()));
        }
        if(CLUtil.ERROR_CHECK)
        {
            clFinish(command_queue);
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueReleaseGLObjects.html">clEnqueueReleaseGLObjects</a>
     */
    public static void clEnqueueReleaseGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        checkError(CL10GL.clEnqueueReleaseGLObjects(command_queue, mem_object, event_wait_list, event));
        if(CLUtil.ERROR_CHECK)
        {
            clFinish(command_queue);
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseKernel.html">clReleaseKernel</a>
     */
    public static void clReleaseKernel(CLKernel kernel) 
    {
        checkError(CL10.clReleaseKernel(kernel));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseProgram.html">clReleaseProgram</a>
     */
    public static void clReleaseProgram(CLProgram program) 
    {
        checkError(CL10.clReleaseProgram(program));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseCommandQueue.html">clReleaseCommandQueue</a>
     */
    public static void clReleaseCommandQueue(CLCommandQueue queue) 
    {
        checkError(CL10.clReleaseCommandQueue(queue));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseContext.html">clReleaseContext</a>
     */
    public static void clReleaseContext(CLContext context) 
    {
        checkError(CL10.clReleaseContext(context));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseMemObject.html">clReleaseMemObject</a>
     */
    public static void clReleaseMemObject(CLMem mem) 
    {
        checkError(CL10.clReleaseMemObject(mem));
    }

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueNDRangeKernel.html">clEnqueueNDRangeKernel</a>
     */
    public static void clEnqueueNDRangeKernel
    (
            CLCommandQueue command_queue, 
            CLKernel kernel, 
            int work_dim, 
            PointerBuffer global_work_offset, 
            PointerBuffer global_work_size, 
            PointerBuffer local_work_size, 
            PointerBuffer event_wait_list, 
            PointerBuffer event) 
    {
        checkError(
                
                CL10.clEnqueueNDRangeKernel(
                        command_queue, 
                        kernel, 
                        work_dim, 
                        global_work_offset, 
                        global_work_size, 
                        local_work_size, 
                        event_wait_list, 
                        event)
                        
                );
        
        if(CLUtil.ERROR_CHECK)
        {
            clFinish(command_queue);
        }
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFinish.html">clFinish</a>
     */
    public static void clFinish(CLCommandQueue command_queue) 
    {
        checkError(CL10.clFinish(command_queue));
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFlush.html">clFlush</a>
     */
    public static void clFlush(CLCommandQueue command_queue) 
    {
        checkError(CL10.clFlush(command_queue));
    }
  
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clBuildProgram.html">clBuildProgram</a>
     */
    public static void clBuildProgram(CLProgram program, CLDevice device, CharSequence options, CLBuildProgramCallback pfn_notify) 
    {
        int error = CL10.clBuildProgram(program, device, options, pfn_notify);
        CLUtil.printProgramInfo(program, device);
        if(error != CL10.CL_SUCCESS)
        {
            throw new CLProgramBuildError();
        }
    }
    
    /**
     * @see http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateContext.html
     */
    public static CLContext clCreateContext(CLPlatform platform, CLDevice device, CLContextCallback pfn_notify, Drawable share_drawable)
    {
        List<CLDevice> devices = new ArrayList<CLDevice>();
        devices.add(device);
        return clCreateContext(platform, devices, pfn_notify, share_drawable);
    }
    
    /**
     * @see http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateContext.html
     */
    public static CLContext clCreateContext(CLPlatform platform, List<CLDevice> devices, CLContextCallback pfn_notify, Drawable share_drawable)
    {
        CLContext context = null;
        try 
        {
            context = CLContext.create(platform, devices, null, share_drawable, lastErrorCode);
            checkError();
        } catch (LWJGLException e) 
        {
            throw new CLError(e.getMessage());
        }
        return context;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateCommandQueue.html">clCreateCommandQueue</a>
     */
    public static CLCommandQueue clCreateCommandQueue(CLContext context, CLDevice device, long properties) 
    {
        CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, properties, lastErrorCode);
        checkError();
        return queue;
    }
    
    private static void checkError() 
    {
        CLUtil.checkError(lastErrorCode.get(0));
    }
    
    private static void checkError(int error) 
    {
        CLUtil.checkError(error);
    }
    
    /***Constants***/
    //CL10
    
    public static final int CL_SUCCESS = 0x0,
            CL_DEVICE_NOT_FOUND = 0xFFFFFFFF,
            CL_DEVICE_NOT_AVAILABLE = 0xFFFFFFFE,
            CL_COMPILER_NOT_AVAILABLE = 0xFFFFFFFD,
            CL_MEM_OBJECT_ALLOCATION_FAILURE = 0xFFFFFFFC,
            CL_OUT_OF_RESOURCES = 0xFFFFFFFB,
            CL_OUT_OF_HOST_MEMORY = 0xFFFFFFFA,
            CL_PROFILING_INFO_NOT_AVAILABLE = 0xFFFFFFF9,
            CL_MEM_COPY_OVERLAP = 0xFFFFFFF8,
            CL_IMAGE_FORMAT_MISMATCH = 0xFFFFFFF7,
            CL_IMAGE_FORMAT_NOT_SUPPORTED = 0xFFFFFFF6,
            CL_BUILD_PROGRAM_FAILURE = 0xFFFFFFF5,
            CL_MAP_FAILURE = 0xFFFFFFF4,
            CL_INVALID_VALUE = 0xFFFFFFE2,
            CL_INVALID_DEVICE_TYPE = 0xFFFFFFE1,
            CL_INVALID_PLATFORM = 0xFFFFFFE0,
            CL_INVALID_DEVICE = 0xFFFFFFDF,
            CL_INVALID_CONTEXT = 0xFFFFFFDE,
            CL_INVALID_QUEUE_PROPERTIES = 0xFFFFFFDD,
            CL_INVALID_COMMAND_QUEUE = 0xFFFFFFDC,
            CL_INVALID_HOST_PTR = 0xFFFFFFDB,
            CL_INVALID_MEM_OBJECT = 0xFFFFFFDA,
            CL_INVALID_IMAGE_FORMAT_DESCRIPTOR = 0xFFFFFFD9,
            CL_INVALID_IMAGE_SIZE = 0xFFFFFFD8,
            CL_INVALID_SAMPLER = 0xFFFFFFD7,
            CL_INVALID_BINARY = 0xFFFFFFD6,
            CL_INVALID_BUILD_OPTIONS = 0xFFFFFFD5,
            CL_INVALID_PROGRAM = 0xFFFFFFD4,
            CL_INVALID_PROGRAM_EXECUTABLE = 0xFFFFFFD3,
            CL_INVALID_KERNEL_NAME = 0xFFFFFFD2,
            CL_INVALID_KERNEL_DEFINITION = 0xFFFFFFD1,
            CL_INVALID_KERNEL = 0xFFFFFFD0,
            CL_INVALID_ARG_INDEX = 0xFFFFFFCF,
            CL_INVALID_ARG_VALUE = 0xFFFFFFCE,
            CL_INVALID_ARG_SIZE = 0xFFFFFFCD,
            CL_INVALID_KERNEL_ARGS = 0xFFFFFFCC,
            CL_INVALID_WORK_DIMENSION = 0xFFFFFFCB,
            CL_INVALID_WORK_GROUP_SIZE = 0xFFFFFFCA,
            CL_INVALID_WORK_ITEM_SIZE = 0xFFFFFFC9,
            CL_INVALID_GLOBAL_OFFSET = 0xFFFFFFC8,
            CL_INVALID_EVENT_WAIT_LIST = 0xFFFFFFC7,
            CL_INVALID_EVENT = 0xFFFFFFC6,
            CL_INVALID_OPERATION = 0xFFFFFFC5,
            CL_INVALID_GL_OBJECT = 0xFFFFFFC4,
            CL_INVALID_BUFFER_SIZE = 0xFFFFFFC3,
            CL_INVALID_MIP_LEVEL = 0xFFFFFFC2,
            CL_INVALID_GLOBAL_WORK_SIZE = 0xFFFFFFC1;

        /**
         * OpenCL Version 
         */
        public static final int CL_VERSION_1_0 = 0x1;

        /**
         * cl_bool 
         */
        public static final int CL_FALSE = 0x0,
            CL_TRUE = 0x1;

        /**
         * cl_platform_info 
         */
        public static final int CL_PLATFORM_PROFILE = 0x900,
            CL_PLATFORM_VERSION = 0x901,
            CL_PLATFORM_NAME = 0x902,
            CL_PLATFORM_VENDOR = 0x903,
            CL_PLATFORM_EXTENSIONS = 0x904;

        /**
         * cl_device_type - bitfield 
         */
        public static final int CL_DEVICE_TYPE_DEFAULT = 0x1,
            CL_DEVICE_TYPE_CPU = 0x2,
            CL_DEVICE_TYPE_GPU = 0x4,
            CL_DEVICE_TYPE_ACCELERATOR = 0x8,
            CL_DEVICE_TYPE_ALL = 0xFFFFFFFF;

        /**
         * cl_device_info 
         */
        public static final int CL_DEVICE_TYPE = 0x1000,
            CL_DEVICE_VENDOR_ID = 0x1001,
            CL_DEVICE_MAX_COMPUTE_UNITS = 0x1002,
            CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS = 0x1003,
            CL_DEVICE_MAX_WORK_GROUP_SIZE = 0x1004,
            CL_DEVICE_MAX_WORK_ITEM_SIZES = 0x1005,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR = 0x1006,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT = 0x1007,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_ = 0x1008,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG = 0x1009,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT = 0x100A,
            CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE = 0x100B,
            CL_DEVICE_MAX_CLOCK_FREQUENCY = 0x100C,
            CL_DEVICE_ADDRESS_BITS = 0x100D,
            CL_DEVICE_MAX_READ_IMAGE_ARGS = 0x100E,
            CL_DEVICE_MAX_WRITE_IMAGE_ARGS = 0x100F,
            CL_DEVICE_MAX_MEM_ALLOC_SIZE = 0x1010,
            CL_DEVICE_IMAGE2D_MAX_WIDTH = 0x1011,
            CL_DEVICE_IMAGE2D_MAX_HEIGHT = 0x1012,
            CL_DEVICE_IMAGE3D_MAX_WIDTH = 0x1013,
            CL_DEVICE_IMAGE3D_MAX_HEIGHT = 0x1014,
            CL_DEVICE_IMAGE3D_MAX_DEPTH = 0x1015,
            CL_DEVICE_IMAGE_SUPPORT = 0x1016,
            CL_DEVICE_MAX_PARAMETER_SIZE = 0x1017,
            CL_DEVICE_MAX_SAMPLERS = 0x1018,
            CL_DEVICE_MEM_BASE_ADDR_ALIGN = 0x1019,
            CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE = 0x101A,
            CL_DEVICE_SINGLE_FP_CONFIG = 0x101B,
            CL_DEVICE_GLOBAL_MEM_CACHE_TYPE = 0x101C,
            CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE = 0x101D,
            CL_DEVICE_GLOBAL_MEM_CACHE_SIZE = 0x101E,
            CL_DEVICE_GLOBAL_MEM_SIZE = 0x101F,
            CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE = 0x1020,
            CL_DEVICE_MAX_CONSTANT_ARGS = 0x1021,
            CL_DEVICE_LOCAL_MEM_TYPE = 0x1022,
            CL_DEVICE_LOCAL_MEM_SIZE = 0x1023,
            CL_DEVICE_ERROR_CORRECTION_SUPPORT = 0x1024,
            CL_DEVICE_PROFILING_TIMER_RESOLUTION = 0x1025,
            CL_DEVICE_ENDIAN_LITTLE = 0x1026,
            CL_DEVICE_AVAILABLE = 0x1027,
            CL_DEVICE_COMPILER_AVAILABLE = 0x1028,
            CL_DEVICE_EXECUTION_CAPABILITIES = 0x1029,
            CL_DEVICE_QUEUE_PROPERTIES = 0x102A,
            CL_DEVICE_NAME = 0x102B,
            CL_DEVICE_VENDOR = 0x102C,
            CL_DRIVER_VERSION = 0x102D,
            CL_DEVICE_PROFILE = 0x102E,
            CL_DEVICE_VERSION = 0x102F,
            CL_DEVICE_EXTENSIONS = 0x1030,
            CL_DEVICE_PLATFORM = 0x1031;

        /**
         * cl_device_fp_config - bitfield 
         */
        public static final int CL_FP_DENORM = 0x1,
            CL_FP_INF_NAN = 0x2,
            CL_FP_ROUND_TO_NEAREST = 0x4,
            CL_FP_ROUND_TO_ZERO = 0x8,
            CL_FP_ROUND_TO_INF = 0x10,
            CL_FP_FMA = 0x20;

        /**
         * cl_device_mem_cache_type 
         */
        public static final int CL_NONE = 0x0,
            CL_READ_ONLY_CACHE = 0x1,
            CL_READ_WRITE_CACHE = 0x2;

        /**
         * cl_device_local_mem_type 
         */
        public static final int CL_LOCAL = 0x1,
            CL_GLOBAL = 0x2;

        /**
         * cl_device_exec_capabilities - bitfield 
         */
        public static final int CL_EXEC_KERNEL = 0x1,
            CL_EXEC_NATIVE_KERNEL = 0x2;

        /**
         * cl_command_queue_properties - bitfield 
         */
        public static final int CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE = 0x1,
            CL_QUEUE_PROFILING_ENABLE = 0x2;

        /**
         * cl_context_info 
         */
        public static final int CL_CONTEXT_REFERENCE_COUNT = 0x1080,
            CL_CONTEXT_DEVICES = 0x1081,
            CL_CONTEXT_PROPERTIES = 0x1082;

        /**
         * cl_context_info + cl_context_properties 
         */
        public static final int CL_CONTEXT_PLATFORM = 0x1084;

        /**
         * cl_command_queue_info 
         */
        public static final int CL_QUEUE_CONTEXT = 0x1090,
            CL_QUEUE_DEVICE = 0x1091,
            CL_QUEUE_REFERENCE_COUNT = 0x1092,
            CL_QUEUE_PROPERTIES = 0x1093;

        /**
         * cl_mem_flags - bitfield 
         */
        public static final int CL_MEM_READ_WRITE = 0x1,
            CL_MEM_WRITE_ONLY = 0x2,
            CL_MEM_READ_ONLY = 0x4,
            CL_MEM_USE_HOST_PTR = 0x8,
            CL_MEM_ALLOC_HOST_PTR = 0x10,
            CL_MEM_COPY_HOST_PTR = 0x20;

        /**
         * cl_channel_order 
         */
        public static final int CL_R = 0x10B0,
            CL_A = 0x10B1,
            CL_RG = 0x10B2,
            CL_RA = 0x10B3,
            CL_RGB = 0x10B4,
            CL_RGBA = 0x10B5,
            CL_BGRA = 0x10B6,
            CL_ARGB = 0x10B7,
            CL_INTENSITY = 0x10B8,
            CL_LUMINANCE = 0x10B9;

        /**
         * cl_channel_type 
         */
        public static final int CL_SNORM_INT8 = 0x10D0,
            CL_SNORM_INT16 = 0x10D1,
            CL_UNORM_INT8 = 0x10D2,
            CL_UNORM_INT16 = 0x10D3,
            CL_UNORM_SHORT_565 = 0x10D4,
            CL_UNORM_SHORT_555 = 0x10D5,
            CL_UNORM_INT_101010 = 0x10D6,
            CL_SIGNED_INT8 = 0x10D7,
            CL_SIGNED_INT16 = 0x10D8,
            CL_SIGNED_INT32 = 0x10D9,
            CL_UNSIGNED_INT8 = 0x10DA,
            CL_UNSIGNED_INT16 = 0x10DB,
            CL_UNSIGNED_INT32 = 0x10DC,
            CL_HALF_FLOAT = 0x10DD,
            CL_FLOAT = 0x10DE;

        /**
         * cl_mem_object_type 
         */
        public static final int CL_MEM_OBJECT_BUFFER = 0x10F0,
            CL_MEM_OBJECT_IMAGE2D = 0x10F1,
            CL_MEM_OBJECT_IMAGE3D = 0x10F2;

        /**
         * cl_mem_info 
         */
        public static final int CL_MEM_TYPE = 0x1100,
            CL_MEM_FLAGS = 0x1101,
            CL_MEM_SIZE = 0x1102,
            CL_MEM_HOST_PTR = 0x1103,
            CL_MEM_MAP_COUNT = 0x1104,
            CL_MEM_REFERENCE_COUNT = 0x1105,
            CL_MEM_CONTEXT = 0x1106;

        /**
         * cl_image_info 
         */
        public static final int CL_IMAGE_FORMAT = 0x1110,
            CL_IMAGE_ELEMENT_SIZE = 0x1111,
            CL_IMAGE_ROW_PITCH = 0x1112,
            CL_IMAGE_SLICE_PITCH = 0x1113,
            CL_IMAGE_WIDTH = 0x1114,
            CL_IMAGE_HEIGHT = 0x1115,
            CL_IMAGE_DEPTH = 0x1116;

        /**
         * cl_addressing_mode 
         */
        public static final int CL_ADDRESS_NONE = 0x1130,
            CL_ADDRESS_CLAMP_TO_EDGE = 0x1131,
            CL_ADDRESS_CLAMP = 0x1132,
            CL_ADDRESS_REPEAT = 0x1133;

        /**
         * cl_filter_mode 
         */
        public static final int CL_FILTER_NEAREST = 0x1140,
            CL_FILTER_LINEAR = 0x1141;

        /**
         * cl_sampler_info 
         */
        public static final int CL_SAMPLER_REFERENCE_COUNT = 0x1150,
            CL_SAMPLER_CONTEXT = 0x1151,
            CL_SAMPLER_NORMALIZED_COORDS = 0x1152,
            CL_SAMPLER_ADDRESSING_MODE = 0x1153,
            CL_SAMPLER_FILTER_MODE = 0x1154;

        /**
         * cl_map_flags - bitfield 
         */
        public static final int CL_MAP_READ = 0x1,
            CL_MAP_WRITE = 0x2;

        /**
         * cl_program_info 
         */
        public static final int CL_PROGRAM_REFERENCE_COUNT = 0x1160,
            CL_PROGRAM_CONTEXT = 0x1161,
            CL_PROGRAM_NUM_DEVICES = 0x1162,
            CL_PROGRAM_DEVICES = 0x1163,
            CL_PROGRAM_SOURCE = 0x1164,
            CL_PROGRAM_BINARY_SIZES = 0x1165,
            CL_PROGRAM_BINARIES = 0x1166;

        /**
         * cl_program_build_info 
         */
        public static final int CL_PROGRAM_BUILD_STATUS = 0x1181,
            CL_PROGRAM_BUILD_OPTIONS = 0x1182,
            CL_PROGRAM_BUILD_LOG = 0x1183;

        /**
         * cl_build_status 
         */
        public static final int CL_BUILD_SUCCESS = 0x0,
            CL_BUILD_NONE = 0xFFFFFFFF,
            CL_BUILD_ERROR = 0xFFFFFFFE,
            CL_BUILD_IN_PROGRESS = 0xFFFFFFFD;

        /**
         * cl_kernel_info 
         */
        public static final int CL_KERNEL_FUNCTION_NAME = 0x1190,
            CL_KERNEL_NUM_ARGS = 0x1191,
            CL_KERNEL_REFERENCE_COUNT = 0x1192,
            CL_KERNEL_CONTEXT = 0x1193,
            CL_KERNEL_PROGRAM = 0x1194;

        /**
         * cl_kernel_work_group_info 
         */
        public static final int CL_KERNEL_WORK_GROUP_SIZE = 0x11B0,
            CL_KERNEL_COMPILE_WORK_GROUP_SIZE = 0x11B1,
            CL_KERNEL_LOCAL_MEM_SIZE = 0x11B2;

        /**
         * cl_event_info 
         */
        public static final int CL_EVENT_COMMAND_QUEUE = 0x11D0,
            CL_EVENT_COMMAND_TYPE = 0x11D1,
            CL_EVENT_REFERENCE_COUNT = 0x11D2,
            CL_EVENT_COMMAND_EXECUTION_STATUS = 0x11D3;

        /**
         * cl_command_type 
         */
        public static final int CL_COMMAND_NDRANGE_KERNEL = 0x11F0,
            CL_COMMAND_TASK = 0x11F1,
            CL_COMMAND_NATIVE_KERNEL = 0x11F2,
            CL_COMMAND_READ_BUFFER = 0x11F3,
            CL_COMMAND_WRITE_BUFFER = 0x11F4,
            CL_COMMAND_COPY_BUFFER = 0x11F5,
            CL_COMMAND_READ_IMAGE = 0x11F6,
            CL_COMMAND_WRITE_IMAGE = 0x11F7,
            CL_COMMAND_COPY_IMAGE = 0x11F8,
            CL_COMMAND_COPY_IMAGE_TO_BUFFER = 0x11F9,
            CL_COMMAND_COPY_BUFFER_TO_IMAGE = 0x11FA,
            CL_COMMAND_MAP_BUFFER = 0x11FB,
            CL_COMMAND_MAP_IMAGE = 0x11FC,
            CL_COMMAND_UNMAP_MEM_OBJECT = 0x11FD,
            CL_COMMAND_MARKER = 0x11FE,
            CL_COMMAND_ACQUIRE_GL_OBJECTS = 0x11FF,
            CL_COMMAND_RELEASE_GL_OBJECTS = 0x1200;

        /**
         * command execution status 
         */
        public static final int CL_COMPLETE = 0x0,
            CL_RUNNING = 0x1,
            CL_SUBMITTED = 0x2,
            CL_QUEUED = 0x3;

        /**
         * cl_profiling_info 
         */
        public static final int CL_PROFILING_COMMAND_QUEUED = 0x1280,
            CL_PROFILING_COMMAND_SUBMIT = 0x1281,
            CL_PROFILING_COMMAND_START = 0x1282,
            CL_PROFILING_COMMAND_END = 0x1283;
        
        //CL11
        
        /**
         * Error Codes 
         */
        public static final int CL_MISALIGNED_SUB_BUFFER_OFFSET = 0xFFFFFFF3,
            CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST = 0xFFFFFFF2,
            CL_INVALID_PROPERTY = 0xFFFFFFC0;

        /**
         * OpenCL Version 
         */
        public static final int CL_VERSION_1_1 = 0x1;

        /**
         * cl_device_info 
         */
        public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF = 0x1034,
            CL_DEVICE_HOST_UNIFIED_MEMORY = 0x1035,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR = 0x1036,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT = 0x1037,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_INT = 0x1038,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG = 0x1039,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT = 0x103A,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE = 0x103B,
            CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF = 0x103C,
            CL_DEVICE_OPENCL_C_VERSION = 0x103D;

        /**
         * cl_device_fp_config - bitfield 
         */
        public static final int CL_FP_SOFT_FLOAT = 0x40;

        /**
         * cl_context_info 
         */
        public static final int CL_CONTEXT_NUM_DEVICES = 0x1083;

        /**
         * cl_channel_order 
         */
        public static final int CL_Rx = 0x10BA,
            CL_RGx = 0x10BB,
            CL_RGBx = 0x10BC;

        /**
         * cl_mem_info 
         */
        public static final int CL_MEM_ASSOCIATED_MEMOBJECT = 0x1107,
            CL_MEM_OFFSET = 0x1108;

        /**
         * cl_addressing_mode 
         */
        public static final int CL_ADDRESS_MIRRORED_REPEAT = 0x1134;

        /**
         * cl_kernel_work_group_info 
         */
        public static final int CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE = 0x11B3,
            CL_KERNEL_PRIVATE_MEM_SIZE = 0x11B4;

        /**
         * cl_event_info 
         */
        public static final int CL_EVENT_CONTEXT = 0x11D4;

        /**
         * cl_command_type 
         */
        public static final int CL_COMMAND_READ_BUFFER_RECT = 0x1201,
            CL_COMMAND_WRITE_BUFFER_RECT = 0x1202,
            CL_COMMAND_COPY_BUFFER_RECT = 0x1203,
            CL_COMMAND_USER = 0x1204;

        /**
         * cl_buffer_create_type 
         */
        public static final int CL_BUFFER_CREATE_TYPE_REGION = 0x1220;
}