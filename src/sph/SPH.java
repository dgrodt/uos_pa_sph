package sph;

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
import pa.cl.OpenCL;
import pa.util.IOUtil;
import pa.util.math.MathUtil;


public class SPH 
{

    private final int n = 14;
    private final float vol = 1000000;
    private final int N = n*n*n;
    private float rho =  0.003f;
    private final float m = 5000 / ((float)N*vol);
    private final float c = 1500f;
    private final float gamma = 7;

	private static SPH sph = null;
	
    private Visualizer vis;
    private PlatformDevicePair pair;
    private CLProgram program;
    private CLCommandQueue queue;
    private CLContext context;
    private PointerBuffer gws_BodyCnt = new PointerBuffer(1);
    private FloatBuffer float_buffer = BufferUtils.createFloatBuffer(N);

    

    private CLKernel sph_calcNewV;
    private CLKernel sph_calcNewPos;
    private CLKernel sph_calcNewP;
    private CLKernel sph_calcNewRho;
    private CLKernel sph_calcNewN;
    
    private CLMem[] buffers;
    
    private CLMem body_Pos; 
    private CLMem body_V;
    private CLMem body_P;
    private CLMem body_rho;
    private CLMem body_n;
    
    
    private boolean initialized = false;


    public void init()
    {
    	
    	if (initialized) {
			return;
		}
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
        sph_calcNewRho = clCreateKernel(program, "sph_CalcNewRho");
        sph_calcNewN = clCreateKernel(program, "sph_CalcNewN");
        vis.setKernelAndQueue(sph_calcNewV, queue);  
        
        gws_BodyCnt.put(0, N);
        
        float p[] = new float[N * 4];
        float v[] = new float[N * 4];
        
        int cnt = 0;
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < n; j++) {
        		for (int k = 0; k < n; k++) {
        			p[cnt++] = 0.07f * j  - 0.1f;// + MathUtil.nextFloat(0.01f);
        			p[cnt++] = 0.07f * i - 0.9f;// + MathUtil.nextFloat(0.01f);
        			p[cnt++] = 0.07f * k  - 0.1f;
        			p[cnt++] = 0;
        			
        		}
        	}
        	
        }
 
     
        
        buffers = vis.createPositions(p, context);
        
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * N);
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

        float n_arr[] = new float[4 * N];
        
        buffer = BufferUtils.createFloatBuffer(N);
        buffer.put(rho_arr);
        buffer.rewind();
        
        body_n = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);
        
        clSetKernelArg(sph_calcNewRho, 0, body_Pos);
        clSetKernelArg(sph_calcNewRho, 1, body_rho);
        clSetKernelArg(sph_calcNewRho, 2, m);
        
        clSetKernelArg(sph_calcNewN, 0, body_Pos);
        clSetKernelArg(sph_calcNewN, 1, body_rho);
        clSetKernelArg(sph_calcNewN, 2, body_n);
        
        clSetKernelArg(sph_calcNewP, 0, body_P);
        clSetKernelArg(sph_calcNewP, 1, body_rho);
        clSetKernelArg(sph_calcNewP, 2, rho);
        
        clSetKernelArg(sph_calcNewV, 0, body_Pos);
        clSetKernelArg(sph_calcNewV, 1, body_V);
        clSetKernelArg(sph_calcNewV, 3, body_P);
        clSetKernelArg(sph_calcNewV, 4, body_rho);
        clSetKernelArg(sph_calcNewV, 5, body_n);
        clSetKernelArg(sph_calcNewV, 6, m);
        
        clSetKernelArg(sph_calcNewPos, 0, body_Pos);
        clSetKernelArg(sph_calcNewPos, 1, body_V);
        clSetKernelArg(sph_calcNewPos, 2, vis.getCurrentParams().m_timeStep);
    }
    
    public void run()
    {
        init();
        while(!vis.isDone())
        {   
        	
        	if (!vis.isPause()) {
        	clEnqueueNDRangeKernel(queue, sph_calcNewRho, 1, null, gws_BodyCnt, null, null, null);
        	clEnqueueNDRangeKernel(queue, sph_calcNewP, 1, null, gws_BodyCnt, null, null, null);
        	//clEnqueueNDRangeKernel(queue, sph_calcNewN, 1, null, gws_BodyCnt, null, null, null);
        	clEnqueueNDRangeKernel(queue, sph_calcNewV, 1, null, gws_BodyCnt, null, null, null);
            clEnqueueNDRangeKernel(queue, sph_calcNewPos, 1, null, gws_BodyCnt, null, null, null);
        	}
            vis.visualize();
        	
        }
        close();
    }

    public void close()
    {
        vis.close();
        
        if(sph_calcNewP != null)
        {
        	clReleaseKernel(sph_calcNewP);
            program = null;
        }
        if(sph_calcNewPos != null)
        {
        	clReleaseKernel(sph_calcNewPos);
            program = null;
        }
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
    public static SPH getInstance() {
		if (sph == null) {
			sph = new SPH();
		}
		return sph;
	}

	public static void destroy() {
		if (sph != null) {
			// System.out.println("destryoing");
			// Display.destroy();
			sph.requestClose();
		}
		sph = null;
	}

	public void requestClose() {
		vis.requestClose();
	}

	public void set_m(float m) {
	}

	public float get_m() {
		return m;
	}

	public void set_rho(float rho) {
	}

	public float get_rho() {
		return rho;
	}

	public void set_c(float c) {
	}

	public float get_c() {
		return c;
	}

	public void set_gamma(float gamma) {
	}

	public float get_gamma() {
		return gamma;
	}
	public void setPause(boolean pause) {
		vis.setPause(pause);
	}
}
