package pa.cl;

import static org.lwjgl.opencl.CL10.CL_DEVICE_ADDRESS_BITS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_AVAILABLE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_COMPILER_AVAILABLE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_ENDIAN_LITTLE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_ERROR_CORRECTION_SUPPORT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_EXECUTION_CAPABILITIES;
import static org.lwjgl.opencl.CL10.CL_DEVICE_EXTENSIONS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_GLOBAL_MEM_CACHE_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_GLOBAL_MEM_CACHE_TYPE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_GLOBAL_MEM_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE2D_MAX_HEIGHT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE2D_MAX_WIDTH;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE3D_MAX_DEPTH;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE3D_MAX_HEIGHT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE3D_MAX_WIDTH;
import static org.lwjgl.opencl.CL10.CL_DEVICE_IMAGE_SUPPORT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_LOCAL_MEM_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_LOCAL_MEM_TYPE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_CLOCK_FREQUENCY;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_COMPUTE_UNITS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_CONSTANT_ARGS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_MEM_ALLOC_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_PARAMETER_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_READ_IMAGE_ARGS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_SAMPLERS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_WRITE_IMAGE_ARGS;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MEM_BASE_ADDR_ALIGN;
import static org.lwjgl.opencl.CL10.CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_NAME;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PLATFORM;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PROFILE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_PROFILING_TIMER_RESOLUTION;
import static org.lwjgl.opencl.CL10.CL_DEVICE_QUEUE_PROPERTIES;
import static org.lwjgl.opencl.CL10.CL_DEVICE_SINGLE_FP_CONFIG;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_VENDOR;
import static org.lwjgl.opencl.CL10.CL_DEVICE_VENDOR_ID;
import static org.lwjgl.opencl.CL10.CL_DEVICE_VERSION;
import static org.lwjgl.opencl.CL10.CL_DRIVER_VERSION;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_NAME;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_PROFILE;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VENDOR;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VERSION;
import static org.lwjgl.opencl.CL11.CL_DEVICE_HOST_UNIFIED_MEMORY;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_INT;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG;
import static org.lwjgl.opencl.CL11.CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT;
import static org.lwjgl.opencl.CL11.CL_DEVICE_OPENCL_C_VERSION;
import static org.lwjgl.opencl.CL11.CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.StringTokenizer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;

public class CLUtil 
{
    public static final boolean ERROR_CHECK = true;
    public static final int DESIRED_DEVICETYPE = OpenCL.CL_DEVICE_TYPE_GPU;
    
    public static class PlatformDevicePair
    {
        public CLPlatform platform;
        public CLDevice device;
    }
    
    public static class Logger
    {
        public static void logInfo(String format, Object... args)
        {
            log(String.format(format, args), System.out);
        }
        
        public static void logError(String format, Object... args)
        {
            log(String.format(format, args), System.err);
        }
        
        public static void log(String text, PrintStream writer)
        {
            writer.println(text);
        }
    }
    
    protected static class CLError extends Error
    {
        private static final long serialVersionUID = -2437782598092165889L;
        public CLError(String error)
        {
            super(error);
        }
    }
    
    protected static class CLCreationError extends CLError
    {
        private static final long serialVersionUID = -5440572086994579062L;
        protected CLCreationError(String error)
        {
            super(error);
        }
    }
    
    protected static class CLProgramBuildError extends CLError
    {
        private static final long serialVersionUID = -2437782598092165889L;
        protected CLProgramBuildError()
        {
            super("");
        }
    }
    
    public static PlatformDevicePair choosePlatformAndDevice()
    {
        return choosePlatformAndDevice(DESIRED_DEVICETYPE);
    }
    
    public static PlatformDevicePair choosePlatformAndDevice(int desiredDevice)
    {
        PlatformDevicePair pair = new PlatformDevicePair();
        printPlatformInfos();
        for(CLPlatform plf : CLPlatform.getPlatforms()) 
        { 
            List<CLDevice> devices = plf.getDevices(desiredDevice);
            //TODO: Find best way to get most powerfull GPU device
            //System.out.println(plf.getInfoString(CL_PLATFORM_NAME)+ "\t " + devices.get(0).getInfoLong(CL_DEVICE_GLOBAL_MEM_SIZE) /1024 /1024 +" MB");
            if(devices != null && devices.size() > 0 && plf.getInfoString(CL_PLATFORM_NAME).contains("NVIDIA")) 
            {
                pair.device = devices.get(0);
                pair.platform = plf;
                Logger.logInfo("Platform with the desired devicetype '%s' found.", getDevicetypeAsString(desiredDevice));
                return pair;
            }
        }
        
       throw new IllegalArgumentException(String.format("No Platform with the desired devicetype '%s' found.", getDevicetypeAsString(desiredDevice)));
    }
    
    public static void createCL()
    {
        if(!CL.isCreated())
        {
            try
            {
                CL.create();   
            } catch(LWJGLException e)
            {
                throw new CLCreationError(e.getMessage());
            }
        }
    }
    
    public static void destroyCL()
    {
        CL.destroy();
    }
    
    public static String[] getPlatformInfo(CLPlatform platform) 
    {
        String[] lines = new String[3];
        
        lines[0] = String.format("Version: %s", platform.getInfoString(CL_PLATFORM_VERSION)) 
                 + String.format(", Name: %s", platform.getInfoString(CL_PLATFORM_NAME));
        lines[1] = String.format(String.format("Vendor: %s", platform.getInfoString(CL_PLATFORM_VENDOR)) 
                 + String.format(", Profile: %s", platform.getInfoString(CL_PLATFORM_PROFILE)));
        
        List<CLDevice> d = platform.getDevices(CL_DEVICE_TYPE_GPU);
        String s = String.format("GPU Devices: %s", d != null ? d.size() : 0);
        d = platform.getDevices(CL_DEVICE_TYPE_CPU);
        lines[2] = s + String.format(", CPU Devices: %s", d != null ? d.size() : 0);
        return lines;
    }
    
    public static String getFormattedInfoBox(String[][] lines, String titel)
    {
        int longestString = 0;
        
        for(int i = 0; i < lines.length; ++i) 
        {
            String[] row = lines[i];
            for(String line : row) 
            {
                int l = line.length();
                longestString = l > longestString ? l : longestString;
            }
        }
        
        String info = titel;
        for(int i = 0; i < longestString-titel.length()+3; ++i) 
        {
            info += "-";
        }
        info += "+\n";
        for(int i = 0; i < lines.length; ++i) 
        {
            String[] rows = lines[i];
            if(i > 0) 
            {
                info += "|";
                for(int k = 0; k < longestString+2; ++k) 
                {
                    info += "*";
                } 
                info += "|\n";
            }
            for(String line : rows) 
            {
                info += "| ";
                info += line;
                for(int j = 0; j < (1+longestString - line.length()); ++j) 
                {
                    info += " ";
                }
                info += "|\n";
            }
        }
        info += "+";
        for(int i = 0; i < longestString+2; ++i) 
        {
            info += "-";
        }
        info += "+";
        
        return info;
    }
    
    public static void printPlatformInfos()
    {
        String[][] lines;
        int platformcount = CLPlatform.getPlatforms().size();
        
        if(platformcount != 0) 
        {
            lines = new String[platformcount][3];
            for(int i = 0; i < platformcount; ++i) 
            {
                lines[i] = getPlatformInfo(CLPlatform.getPlatforms().get(i));
            }
        } else 
        {
            lines = new String[1][1];
            lines[0][0] = "####No OpenCL Platform found####";
        }
        
        String info = getFormattedInfoBox(lines, "+-OpenCL Platform(s)");
        
        Logger.logInfo(info);
    }
    
    public static void printProgramInfo(CLProgram program, CLDevice device) 
    {
        String status = getProgramInfo(program, device, CL10.CL_PROGRAM_BUILD_STATUS);
        
        String log = getProgramInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG);

        if(log.length() > 0)
        {
            log = log.substring(0, log.length()-1);   
        }
        
        String options = getProgramInfo(program, device, CL10.CL_PROGRAM_BUILD_OPTIONS);
        if(options.length() > 0)
        {
            options = options.substring(0, options.length()-1); 
        }
        else
        {
            options = "\"\"";
        }

        Logger.logInfo("Program '0x%s' Status=%s, Options=%s", Long.toHexString(program.getPointer()), status, options);
        if(log.length() > 0)
        {
            if(status.equals("CL_BUILD_SUCCESS"))
            {
                Logger.logInfo("\nInfoLog:\n%s", log);    
            }
            else
            {
                Logger.logError("\nErrorLog:\n%s", log);    
            }
        }
    }

    public static void checkError(int errorCode) 
    {
        if(CLUtil.ERROR_CHECK && errorCode != CL10.CL_SUCCESS) 
        {
            throw new CLError(CLUtil.getErrorcodeAsString(errorCode));
        }
    }
    
    public static void printDeviceInfos(CLDevice device) 
    {
        Logger.logInfo("CL_DEVICE_TYPE: %s", getDevicetypeAsString(device.getInfoInt(CL_DEVICE_TYPE)));
        Logger.logInfo("CL_DEVICE_AVAILABLE: %d", device.getInfoInt(CL_DEVICE_AVAILABLE));
        Logger.logInfo("CL_DEVICE_OPENCL_C_VERSION: %s", device.getInfoString(CL_DEVICE_OPENCL_C_VERSION));
        Logger.logInfo("CL_DEVICE_NAME: %s", device.getInfoString(CL_DEVICE_NAME));
        Logger.logInfo("CL_DEVICE_VENDOR: %s", device.getInfoString(CL_DEVICE_VENDOR));
        Logger.logInfo("CL_DRIVER_VERSION: %s", device.getInfoString(CL_DRIVER_VERSION));
        Logger.logInfo("CL_DEVICE_PROFILE: %s", device.getInfoString(CL_DEVICE_PROFILE));
        Logger.logInfo("CL_DEVICE_VERSION: %s", device.getInfoString(CL_DEVICE_VERSION));
        Logger.logInfo("CL_DEVICE_EXTENSIONS: %s", getFormattedExtensionsAsString(device.getInfoString(CL_DEVICE_EXTENSIONS)));
        Logger.logInfo("CL_DEVICE_PLATFORM: %d", device.getInfoInt(CL_DEVICE_PLATFORM));
        Logger.logInfo("CL_DEVICE_VENDOR_ID: %d", device.getInfoInt(CL_DEVICE_VENDOR_ID));
        Logger.logInfo("CL_DEVICE_MAX_COMPUTE_UNITS: %d", device.getInfoInt(CL_DEVICE_MAX_COMPUTE_UNITS));
        Logger.logInfo("CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS: %d", device.getInfoInt(CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS));
        Logger.logInfo("CL_DEVICE_MAX_WORK_GROUP_SIZE: %d", device.getInfoInt(CL_DEVICE_MAX_WORK_GROUP_SIZE));
        Logger.logInfo("CL_DEVICE_MAX_WORK_ITEM_SIZES: %d", device.getInfoInt(CL_DEVICE_MAX_WORK_ITEM_SIZES));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE));
        Logger.logInfo("CL_DEVICE_MAX_CLOCK_FREQUENCY: %d", device.getInfoInt(CL_DEVICE_MAX_CLOCK_FREQUENCY));
        Logger.logInfo("CL_DEVICE_ADDRESS_BITS: %d", device.getInfoInt(CL_DEVICE_ADDRESS_BITS));
        Logger.logInfo("CL_DEVICE_MAX_READ_IMAGE_ARGS: %d", device.getInfoInt(CL_DEVICE_MAX_READ_IMAGE_ARGS));
        Logger.logInfo("CL_DEVICE_MAX_WRITE_IMAGE_ARGS: %d", device.getInfoInt(CL_DEVICE_MAX_WRITE_IMAGE_ARGS));
        Logger.logInfo("CL_DEVICE_MAX_MEM_ALLOC_SIZE: %d", device.getInfoInt(CL_DEVICE_MAX_MEM_ALLOC_SIZE));
        Logger.logInfo("CL_DEVICE_IMAGE2D_MAX_WIDTH: %d", device.getInfoInt(CL_DEVICE_IMAGE2D_MAX_WIDTH));
        Logger.logInfo("CL_DEVICE_IMAGE2D_MAX_HEIGHT: %d", device.getInfoInt(CL_DEVICE_IMAGE2D_MAX_HEIGHT));
        Logger.logInfo("CL_DEVICE_IMAGE3D_MAX_WIDTH: %d", device.getInfoInt(CL_DEVICE_IMAGE3D_MAX_WIDTH));
        Logger.logInfo("CL_DEVICE_IMAGE3D_MAX_HEIGHT: %d", device.getInfoInt(CL_DEVICE_IMAGE3D_MAX_HEIGHT));
        Logger.logInfo("CL_DEVICE_IMAGE3D_MAX_DEPTH: %d", device.getInfoInt(CL_DEVICE_IMAGE3D_MAX_DEPTH));
        Logger.logInfo("CL_DEVICE_IMAGE_SUPPORT: %d", device.getInfoInt(CL_DEVICE_IMAGE_SUPPORT));
        Logger.logInfo("CL_DEVICE_MAX_PARAMETER_SIZE: %d", device.getInfoInt(CL_DEVICE_MAX_PARAMETER_SIZE));
        Logger.logInfo("CL_DEVICE_MAX_SAMPLERS: %d", device.getInfoInt(CL_DEVICE_MAX_SAMPLERS));
        Logger.logInfo("CL_DEVICE_MEM_BASE_ADDR_ALIGN: %d", device.getInfoInt(CL_DEVICE_MEM_BASE_ADDR_ALIGN));
        Logger.logInfo("CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE: %d", device.getInfoInt(CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE));
        Logger.logInfo("CL_DEVICE_SINGLE_FP_CONFIG: %d", device.getInfoInt(CL_DEVICE_SINGLE_FP_CONFIG));
        Logger.logInfo("CL_DEVICE_GLOBAL_MEM_CACHE_TYPE: %d", device.getInfoInt(CL_DEVICE_GLOBAL_MEM_CACHE_TYPE));
        Logger.logInfo("CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE: %d", device.getInfoInt(CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE));
        Logger.logInfo("CL_DEVICE_GLOBAL_MEM_CACHE_SIZE: %d", device.getInfoInt(CL_DEVICE_GLOBAL_MEM_CACHE_SIZE));
        Logger.logInfo("CL_DEVICE_GLOBAL_MEM_SIZE: %d", device.getInfoInt(CL_DEVICE_GLOBAL_MEM_SIZE));
        Logger.logInfo("CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE: %d", device.getInfoInt(CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE));
        Logger.logInfo("CL_DEVICE_MAX_CONSTANT_ARGS: %d", device.getInfoInt(CL_DEVICE_MAX_CONSTANT_ARGS));
        Logger.logInfo("CL_DEVICE_LOCAL_MEM_TYPE: %d", device.getInfoInt(CL_DEVICE_LOCAL_MEM_TYPE));
        Logger.logInfo("CL_DEVICE_LOCAL_MEM_SIZE: %d", device.getInfoInt(CL_DEVICE_LOCAL_MEM_SIZE));
        Logger.logInfo("CL_DEVICE_ERROR_CORRECTION_SUPPORT: %d", device.getInfoInt(CL_DEVICE_ERROR_CORRECTION_SUPPORT));
        Logger.logInfo("CL_DEVICE_PROFILING_TIMER_RESOLUTION: %d", device.getInfoInt(CL_DEVICE_PROFILING_TIMER_RESOLUTION));
        Logger.logInfo("CL_DEVICE_ENDIAN_LITTLE: %d", device.getInfoInt(CL_DEVICE_ENDIAN_LITTLE));
        Logger.logInfo("CL_DEVICE_COMPILER_AVAILABLE: %d", device.getInfoInt(CL_DEVICE_COMPILER_AVAILABLE));
        Logger.logInfo("CL_DEVICE_EXECUTION_CAPABILITIES: %d", device.getInfoInt(CL_DEVICE_EXECUTION_CAPABILITIES));
        Logger.logInfo("CL_DEVICE_QUEUE_PROPERTIES: %d", device.getInfoInt(CL_DEVICE_QUEUE_PROPERTIES));
        Logger.logInfo("CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF: %d", device.getInfoInt(CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF));
        Logger.logInfo("CL_DEVICE_HOST_UNIFIED_MEMORY: %d", device.getInfoInt(CL_DEVICE_HOST_UNIFIED_MEMORY));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_INT: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_INT));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE));
        Logger.logInfo("CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF: %d", device.getInfoInt(CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF));
    }
      
    public static String getErrorcodeAsString(int errorCode) 
    {
        switch(errorCode) 
        {
        case 0xFFFFFFFF : return "CL_DEVICE_NOT_FOUND";
        case 0xFFFFFFFE : return "CL_DEVICE_NOT_AVAILABLE";
        case 0xFFFFFFFD : return "CL_COMPILER_NOT_AVAILABLE";
        case 0xFFFFFFFC : return "CL_MEM_OBJECT_ALLOCATION_FAILURE";
        case 0xFFFFFFFB : return "CL_OUT_OF_RESOURCES";
        case 0xFFFFFFFA : return "CL_OUT_OF_HOST_MEMORY";
        case 0xFFFFFFF9 : return "CL_PROFILING_INFO_NOT_AVAILABLE";
        case 0xFFFFFFF8 : return "CL_MEM_COPY_OVERLAP";
        case 0xFFFFFFF7 : return "CL_IMAGE_FORMAT_MISMATCH";
        case 0xFFFFFFF6 : return "CL_IMAGE_FORMAT_NOT_SUPPORTED";
        case 0xFFFFFFF5 : return "CL_BUILD_PROGRAM_FAILURE";
        case 0xFFFFFFF4 : return "CL_MAP_FAILURE";
        case 0xFFFFFFE2 : return "CL_INVALID_VALUE";
        case 0xFFFFFFE1 : return "CL_INVALID_DEVICE_TYPE";
        case 0xFFFFFFE0 : return "CL_INVALID_PLATFORM";
        case 0xFFFFFFDF : return "CL_INVALID_DEVICE";
        case 0xFFFFFFDE : return "CL_INVALID_CONTEXT";
        case 0xFFFFFFDD : return "CL_INVALID_QUEUE_PROPERTIES";
        case 0xFFFFFFDC : return "CL_INVALID_COMMAND_QUEUE";
        case 0xFFFFFFDB : return "CL_INVALID_HOST_PTR";
        case 0xFFFFFFDA : return "CL_INVALID_MEM_OBJECT";
        case 0xFFFFFFD9 : return "CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
        case 0xFFFFFFD8 : return "CL_INVALID_IMAGE_SIZE";
        case 0xFFFFFFD7 : return "CL_INVALID_SAMPLER";
        case 0xFFFFFFD6 : return "CL_INVALID_BINARY";
        case 0xFFFFFFD5 : return "CL_INVALID_BUILD_OPTIONS";
        case 0xFFFFFFD4 : return "CL_INVALID_PROGRAM";
        case 0xFFFFFFD3 : return "CL_INVALID_PROGRAM_EXECUTABLE";
        case 0xFFFFFFD2 : return "CL_INVALID_KERNEL_NAME";
        case 0xFFFFFFD1 : return "CL_INVALID_KERNEL_DEFINITION";
        case 0xFFFFFFD0 : return "CL_INVALID_KERNEL";
        case 0xFFFFFFCF : return "CL_INVALID_ARG_INDEX";
        case 0xFFFFFFCE : return "CL_INVALID_ARG_VALUE";
        case 0xFFFFFFCD : return "CL_INVALID_ARG_SIZE";
        case 0xFFFFFFCC : return "CL_INVALID_KERNEL_ARGS";
        case 0xFFFFFFCB : return "CL_INVALID_WORK_DIMENSION";
        case 0xFFFFFFCA : return "CL_INVALID_WORK_GROUP_SIZE";
        case 0xFFFFFFC9 : return "CL_INVALID_WORK_ITEM_SIZE";
        case 0xFFFFFFC8 : return "CL_INVALID_GLOBAL_OFFSET";
        case 0xFFFFFFC7 : return "CL_INVALID_EVENT_WAIT_LIST";
        case 0xFFFFFFC6 : return "CL_INVALID_EVENT";
        case 0xFFFFFFC5 : return "CL_INVALID_OPERATION";
        case 0xFFFFFFC4 : return "CL_INVALID_GL_OBJECT";
        case 0xFFFFFFC3 : return "CL_INVALID_BUFFER_SIZE";
        case 0xFFFFFFC2 : return "CL_INVALID_MIP_LEVEL";
        case 0xFFFFFFC1 : return "CL_INVALID_GLOBAL_WORK_SIZE";
        default : return "CL_UNKNOWN_ERROR";
        }
    }
    
    private static String getProgramInfo(CLProgram program, CLDevice device, int flag)
    {
        PointerBuffer buffer = BufferUtils.createPointerBuffer(1);
        checkError(CL10.clGetProgramBuildInfo(program, device, flag, null, buffer));
                
        if(buffer.get(0) > 2) 
        {
            ByteBuffer _log = BufferUtils.createByteBuffer((int)buffer.get(0));
            checkError(CL10.clGetProgramBuildInfo(program, device, flag, _log, buffer));
            if(flag == CL10.CL_PROGRAM_BUILD_STATUS)
            {
                return getBuildStatusAsString(_log.getInt());
            }
            else
            {
                byte bytes[] = new byte[_log.capacity()];
                _log.get(bytes);
                return new String(bytes);
            }
        }
        return "";
    }
    
    private static String getDevicetypeAsString(int type) 
    {
        switch(type) 
        {
        case CL10.CL_DEVICE_TYPE_DEFAULT: return "CL_DEVICE_TYPE_DEFAULT";
        case CL10.CL_DEVICE_TYPE_CPU: return "CL_DEVICE_TYPE_CPU";
        case CL10.CL_DEVICE_TYPE_GPU: return "CL_DEVICE_TYPE_GPU";
        case CL10.CL_DEVICE_TYPE_ACCELERATOR: return "CL_DEVICE_TYPE_ACCELERATOR";
        case 0xFFFFFFFF: return "CL_DEVICE_TYPE_ALL";
        default: return Integer.toString(type);
        }
    }
    
    private static String getBuildStatusAsString(int status)
    {
        switch(status)
        {
        case CL10.CL_BUILD_SUCCESS : return "CL_BUILD_SUCCESS";
        case CL10.CL_BUILD_NONE : return "CL_BUILD_NONE";
        case CL10.CL_BUILD_ERROR : return "CL_BUILD_ERROR";
        case CL10.CL_BUILD_IN_PROGRESS : return "CL_BUILD_IN_PROGRESS";
        default : return "Unknown status";
        }
    }
    
    private static String getFormattedExtensionsAsString(String ext) 
    {
        if(ext == null) return "";
        StringTokenizer st = new StringTokenizer(ext, " ");
        ext = "";
        while(st.hasMoreTokens()) 
        {
            ext += st.nextToken() + (st.hasMoreTokens() ? ", " : "");
        }
        return ext;
    }
}
