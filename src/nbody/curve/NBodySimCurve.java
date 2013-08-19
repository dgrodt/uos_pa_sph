package nbody.curve;

import static pa.cl.OpenCL.CL_FALSE;
import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
import static pa.cl.OpenCL.clBuildProgram;
import static pa.cl.OpenCL.clCreateBuffer;
import static pa.cl.OpenCL.clCreateCommandQueue;
import static pa.cl.OpenCL.clCreateContext;
import static pa.cl.OpenCL.clCreateKernel;
import static pa.cl.OpenCL.clCreateProgramWithSource;
import static pa.cl.OpenCL.clEnqueueNDRangeKernel;
import static pa.cl.OpenCL.clEnqueueReadBuffer;
import static pa.cl.OpenCL.clFinish;
import static pa.cl.OpenCL.clReleaseCommandQueue;
import static pa.cl.OpenCL.clReleaseContext;
import static pa.cl.OpenCL.clReleaseKernel;
import static pa.cl.OpenCL.clReleaseMemObject;
import static pa.cl.OpenCL.clReleaseProgram;
import static pa.cl.OpenCL.clSetKernelArg;

import java.nio.FloatBuffer;

import nbody.curve.helper.NBodyDataCurve;
import nbody.curve.helper.VisualizerCurve;
import nbody.curve.helper.VisualizerCurve.NBodyBuffers;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Display;

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.util.BufferHelper;
import pa.util.IOUtil;


public class NBodySimCurve 
{
    private VisualizerCurve vis;
    private PlatformDevicePair pair;
    private CLKernel nBody_CalcNewV;
    private CLKernel nBody_CalcNewPos;
    private CLKernel nBody_PassPositionOn;
    private CLKernel nBody_setTrailParticle;
    private CLProgram program;
    private CLCommandQueue queue;
    private CLContext context;
    private PointerBuffer gws_BodyCnt = new PointerBuffer(1);
    private final int N = 10000;//4096;//
    private NBodyBuffers buffers;    
    
    private final int VERTICES_PER_CURVE = 25;
    private final int TRAIL_PARTICLES_PER_CURVE = 10;    
    private final float dSC = 1.0f / (VERTICES_PER_CURVE - 1);

    /**
     * - Muss >= 0 sein
     * 1  : Jeder Punkt springt immer genau einen Ehamaligen Punkt der Kurve weiter -> Steht still im Raum
     * > 1: Jeder Ring springt mehr als einen ehemaligen Punkt zurück -> läuft die Kurve rückwärts
     * <1 : Jeder Ring springt weniger als einen ehemaligen Punkt zurück -> läuft die Kurve vorwärts
     *  0 : Jeder Ring behält seinen Kurvenparameter: Bewegt sich wie das Teilchen selbst
     */
    private final float TRAIL_PARTICLE_SPEED_FACT = 0.75f; 
    
    private final float dSTP = dSC * TRAIL_PARTICLE_SPEED_FACT;
   
    private CLMem body_Pos; 
    private CLMem body_V;
    private CLMem curve_Pos;
    private CLMem trailParticle_Pos;
    private CLMem trailParticle_Dir;
    private CLMem trailParticle_S;

    public void init()
    {
        vis = new VisualizerCurve(1024, 768);
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
        program = clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/nbody.cl"));
        clBuildProgram(program, pair.device, "", null);
        
        nBody_CalcNewV = clCreateKernel(program, "nBody_CalcNewV");
        nBody_CalcNewPos = clCreateKernel(program, "nBody_CalcNewPos");
        nBody_PassPositionOn = clCreateKernel(program, "passPositionOn");
        nBody_setTrailParticle = clCreateKernel(program, "setTrailParticle");
        
        gws_BodyCnt.put(0, N);
        
        vis.setKernelAndQueue(nBody_CalcNewV, queue);        
        vis.setTrailAndVertCnt(TRAIL_PARTICLES_PER_CURVE, VERTICES_PER_CURVE);
        
        float p[] = new float[N * 4];
        float v[] = new float[N * 4];
        
        NBodyDataCurve.createBodys(N, vis, p, v);
         
        float c[] = new float[N * 4 * TRAIL_PARTICLES_PER_CURVE];        
        buffers = vis.createPositionsAndVelos(p, c, c, context);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(N * 4);
        buffer.put(v);
        buffer.rewind();
        
        body_V = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);        
        curve_Pos = clCreateBuffer(context, CL_MEM_READ_WRITE, N * VERTICES_PER_CURVE * 4 * 4);

        trailParticle_Pos = buffers.trailPositions;
        trailParticle_Dir =  buffers.trailDirs;
        body_Pos = buffers.particlePositions;
        
        FloatBuffer tps = BufferUtils.createFloatBuffer(N * TRAIL_PARTICLES_PER_CURVE);
        for(int i=0; i < N; ++i)
        {
            for(int j=0; j < TRAIL_PARTICLES_PER_CURVE; ++j)
            {
                tps.put(i * TRAIL_PARTICLES_PER_CURVE + j, 1 - ((float)j)  / TRAIL_PARTICLES_PER_CURVE);  
            }
        }
        trailParticle_S = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, tps);

        clSetKernelArg(nBody_CalcNewV, 0, body_Pos);
        clSetKernelArg(nBody_CalcNewV, 1, body_V);
        clSetKernelArg(nBody_CalcNewV, 2, 0.01f);
        clSetKernelArg(nBody_CalcNewV, 3, 20f);
        
        clSetKernelArg(nBody_CalcNewPos, 0, body_Pos);
        clSetKernelArg(nBody_CalcNewPos, 1, body_V);
        clSetKernelArg(nBody_CalcNewPos, 2, vis.getCurrentParams().m_timeStep);
        
        clSetKernelArg(nBody_PassPositionOn, 0, body_Pos);
        clSetKernelArg(nBody_PassPositionOn, 1, curve_Pos);
        clSetKernelArg(nBody_PassPositionOn, 2, VERTICES_PER_CURVE);
        
        clSetKernelArg(nBody_setTrailParticle, 0, trailParticle_Pos);
        clSetKernelArg(nBody_setTrailParticle, 1, trailParticle_S);
        clSetKernelArg(nBody_setTrailParticle, 2, trailParticle_Dir);
        clSetKernelArg(nBody_setTrailParticle, 3, curve_Pos);
        clSetKernelArg(nBody_setTrailParticle, 4, TRAIL_PARTICLES_PER_CURVE);
        clSetKernelArg(nBody_setTrailParticle, 5, VERTICES_PER_CURVE);
        clSetKernelArg(nBody_setTrailParticle, 6, dSC);
        clSetKernelArg(nBody_setTrailParticle, 7, dSTP);
    }
    
    public void run()
    {
        init();
        while(!vis.isDone())
        {   
            if(!vis.isPause())
            {
                //simulate here
                clEnqueueNDRangeKernel(queue, nBody_CalcNewV, 1, null, gws_BodyCnt, null, null, null);
                clEnqueueNDRangeKernel(queue, nBody_CalcNewPos, 1, null, gws_BodyCnt, null, null, null);
                clEnqueueNDRangeKernel(queue, nBody_PassPositionOn, 1, null, gws_BodyCnt, null, null, null);
                clEnqueueNDRangeKernel(queue, nBody_setTrailParticle, 1, null, gws_BodyCnt, null, null, null);
                
                clFinish(queue);
            }
            vis.visualize();
        }
        close();
    }

    public void close()
    {
        vis.close();
        
        if(trailParticle_S != null)
        {
            clReleaseMemObject(trailParticle_S);
            trailParticle_S = null;
        }
        
        if(body_V != null)
        {
            clReleaseMemObject(body_V);
            body_V = null;
        }
        
        if(curve_Pos != null)
        {
            clReleaseMemObject(curve_Pos);
            curve_Pos = null;
        }
        
        if(nBody_CalcNewV != null)
        {
            clReleaseKernel(nBody_CalcNewV);
            nBody_CalcNewV = null;
        }
        
        if(nBody_CalcNewPos != null)
        {
            clReleaseKernel(nBody_CalcNewPos);
            nBody_CalcNewPos = null;
        }
        if(nBody_PassPositionOn != null)
        {
        	clReleaseKernel(nBody_PassPositionOn);
        	nBody_PassPositionOn = null;
        }
        if(nBody_setTrailParticle != null)
        {
        	clReleaseKernel(nBody_setTrailParticle);
        	nBody_setTrailParticle = null;
        }
        
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
