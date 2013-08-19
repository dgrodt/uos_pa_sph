package visualize.util;

import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLProgram;

import pa.cl.CLUtil.PlatformDevicePair;
import pa.cl.OpenCL;

public class CLContainer 
{
    public PlatformDevicePair m_pair = null;
    public CLContext m_context = null;    
    public CLKernel m_kernel = null;
    public CLProgram m_program = null;
    public CLCommandQueue m_queue = null;
    
    public void destroy()
    {
        if(m_kernel != null)
        {
            OpenCL.clReleaseKernel(m_kernel);
            m_kernel = null;
        }
        
        if(m_program != null)
        {
            OpenCL.clReleaseProgram(m_program);
            m_program = null;
        }
        
        if(m_queue != null)
        {
            OpenCL.clReleaseCommandQueue(m_queue);
            m_queue = null;
        }
        
        if(m_context != null)
        {
            OpenCL.clReleaseContext(m_context);
            m_context = null;
        }
    }
}
