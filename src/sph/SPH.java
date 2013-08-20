package sph;

import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
import static pa.cl.OpenCL.clBuildProgram;
import static pa.cl.OpenCL.clCreateBuffer;
import static pa.cl.OpenCL.clCreateCommandQueue;
import static pa.cl.OpenCL.clCreateContext;
import static pa.cl.OpenCL.clCreateKernel;
import static pa.cl.OpenCL.clCreateProgramWithSource;
import static pa.cl.OpenCL.clEnqueueNDRangeKernel;
import static pa.cl.OpenCL.clFinish;
import static pa.cl.OpenCL.clReleaseCommandQueue;
import static pa.cl.OpenCL.clReleaseContext;
import static pa.cl.OpenCL.clReleaseProgram;
import static pa.cl.OpenCL.clReleaseKernel;
import static pa.cl.OpenCL.clSetKernelArg;

import java.nio.FloatBuffer;

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
import pa.util.IOUtil;
import pa.util.math.MathUtil;
import sph.helper.ParticleHelper;


public class SPH 
{
    private Visualizer vis;
    private PlatformDevicePair pair;
    private CLProgram program;
    private CLCommandQueue queue;
    private CLContext context;
    private PointerBuffer gws_BodyCnt = new PointerBuffer(1);
    
    private CLKernel sph_calcNewV;
    private CLKernel sph_calcNewPos;
    private CLKernel sph_calcNewP;
    private CLMem[] buffers;
    
    private CLMem body_Pos; 
    private CLMem body_V;
    private CLMem body_P;
    private CLMem body_rho;
    
    private final int n = 20;
    private final int N = n*n*n;
    private final float rho = 1;
    private final float m = (float)0.0003;
    private final float c = 100;
    private final float gamma = 7;

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
        program = clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/sph.cl")); 
        clBuildProgram(program, pair.device, "", null);
        
        sph_calcNewV = clCreateKernel(program, "sph_CalcNewV");
        sph_calcNewPos = clCreateKernel(program, "sph_CalcNewPos");
        sph_calcNewP = clCreateKernel(program, "sph_CalcNewP");
        vis.setKernelAndQueue(sph_calcNewV, queue);  
        
        gws_BodyCnt.put(0, N);
        
        float p[] = new float[N * 4];
        float v[] = new float[N * 4];
        
        //ParticleHelper.createBodys(N, vis, p, v);
        
        for (int i = 0; i < 4 * N; i++) v[i] = 5 - MathUtil.nextFloat(10);
        for (int i = 0; i < N; i++) v[4 * i+3] = 0;

        int cnt = 0;
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < n; j++) {
        		for (int k = 0; k < n; k++) {
        			p[cnt++] = 1.9f * j / (float)n  - 0.85f;
        			p[cnt++] = 1.9f * i / (float)n - 0.85f;
        			p[cnt++] = 1.9f * k / (float)n  - 0.85f;
        			p[cnt++] = 0;
        			
        		}
        	}
        	
        }
 
     
        
        buffers = vis.createPositions(p, context);
        
        FloatBuffer buffer = BufferUtils.createFloatBuffer(N * 4);
        buffer.put(v);
        buffer.rewind();
        
        body_V = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);   
        body_Pos = buffers[0];
        
        
        float P_arr[] = new float[N];
        
        buffer = BufferUtils.createFloatBuffer(N);
        buffer.put(P_arr);
        buffer.rewind();
        
        body_P = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);
        
        float rho_arr[] = new float[N];
        
        for (int i = 0; i < rho_arr.length; i++) rho_arr[i] = rho;
        
        buffer = BufferUtils.createFloatBuffer(N);
        buffer.put(rho_arr);
        buffer.rewind();
        
        body_rho = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);
        
        clSetKernelArg(sph_calcNewP, 0, body_P);
        clSetKernelArg(sph_calcNewP, 1, body_rho);
        clSetKernelArg(sph_calcNewP, 2, rho);
        clSetKernelArg(sph_calcNewP, 3, m);
        clSetKernelArg(sph_calcNewP, 4, c);
        clSetKernelArg(sph_calcNewP, 5, gamma);
        
        clSetKernelArg(sph_calcNewV, 0, body_Pos);
        clSetKernelArg(sph_calcNewV, 1, body_V);
        
        clSetKernelArg(sph_calcNewV, 4, body_P);
        clSetKernelArg(sph_calcNewV, 5, body_rho);
        clSetKernelArg(sph_calcNewV, 6, m);
         
        
        clSetKernelArg(sph_calcNewPos, 0, body_Pos);
        clSetKernelArg(sph_calcNewPos, 1, body_V);
        clSetKernelArg(sph_calcNewPos, 2, vis.getCurrentParams().m_timeStep);
        //TODO kernel und speicher initialisieren
    }
    
    public void run()
    {
        init();
        while(!vis.isDone())
        {   
            //TODO simulieren
        	clEnqueueNDRangeKernel(queue, sph_calcNewP, 1, null, gws_BodyCnt, null, null, null);
        	clEnqueueNDRangeKernel(queue, sph_calcNewV, 1, null, gws_BodyCnt, null, null, null);
            clEnqueueNDRangeKernel(queue, sph_calcNewPos, 1, null, gws_BodyCnt, null, null, null);
            
            clFinish(queue);
            vis.visualize();
            
        }
        close();
    }

    public void close()
    {
        vis.close();
        
        //TODO elemente freigeben
        
        if(sph_calcNewV != null)
        {
        	clReleaseKernel(sph_calcNewV);
            program = null;
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
