package sph;

import static pa.cl.OpenCL.clBuildProgram;
import static pa.cl.OpenCL.clCreateCommandQueue;
import static pa.cl.OpenCL.clCreateContext;
import static pa.cl.OpenCL.clCreateProgramWithSource;
import static pa.cl.OpenCL.clFinish;
import static pa.cl.OpenCL.clReleaseCommandQueue;
import static pa.cl.OpenCL.clReleaseContext;
import static pa.cl.OpenCL.clReleaseProgram;

import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Display;

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.util.IOUtil;


public class SPH 
{
    private Visualizer vis;
    private PlatformDevicePair pair;
    private CLProgram program;
    private CLCommandQueue queue;
    private CLContext context;
    private PointerBuffer gws_BodyCnt = new PointerBuffer(1);

    public void init()
    {
        vis = new Visualizer(1024, 768);
        try 
        {
            vis.create();
        } catch (LWJGLException e) 
        {
            throw new RuntimeException(e.getMessage());
        }
        
        CLUtil.createCL();
        pair = CLUtil.choosePlatformAndDevice();
        context = clCreateContext(pair.platform, pair.device, null, Display.getDrawable());
        vis.initGL();
        queue = clCreateCommandQueue(context, pair.device, 0);
        program = clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/nbody.cl")); //TODO: richtiges programm
        clBuildProgram(program, pair.device, "", null);
        
        //vis.setKernelAndQueue(, queue);        //TODO
        
        //TODO kernel und speicher initialisieren
    }
    
    public void run()
    {
        init();
        while(!vis.isDone())
        {   
            //TODO simulieren
            
            clFinish(queue);
            vis.visualize();
        }
        close();
    }

    public void close()
    {
        vis.close();
        
        //TODO elemente freigeben
        
        if(program != null)
        {
            clReleaseProgram(program);
            program = null;
        }
        
        if(queue != null)
        {
            clReleaseCommandQueue(queue);
            queue = null;
        }
        
        if(context != null)
        {
            clReleaseContext(context);
            context = null;
        }
        
        CLUtil.destroyCL();
    }
}
