package nbody.curve.helper;

import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
import static pa.cl.OpenCL.clCreateFromGLBuffer;
import static pa.cl.OpenCL.clEnqueueAcquireGLObjects;
import static pa.cl.OpenCL.clEnqueueReleaseGLObjects;
import static pa.cl.OpenCL.clReleaseMemObject;
import static pa.cl.OpenCL.clSetKernelArg;
import nbody.Visualizer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import visualize.FrameWork;
import visualize.gl.GLUtil;
import visualize.gl.GeometryFactory;
import visualize.gl.Program;
import visualize.gl.Texture;

public class VisualizerCurve extends Visualizer 
{    
    private boolean m_pause = false;
    private CLMem m_oglBuffer2;
    private Program m_circleProgram;
    private Texture m_gd;
    private int m_vertsPerCurve;
    
    public VisualizerCurve(int w, int h)
    {
        super(w, h);
    }
    
    @Override
    public void init() 
    {
        m_program = new Program();
        m_program.create("shader/Particles_VS.glsl", "shader/Particles_FS.glsl");
        m_program.bindAttributeLocation("vs_in_pos", 0);
        m_program.bindAttributeLocation("vs_in_normal", 1);
        m_program.bindAttributeLocation("vs_in_tc", 2);
        m_program.bindAttributeLocation("vs_in_instance", 3);
        m_program.bindAttributeLocation("vs_in_velos", 4);
        m_program.linkAndValidate();
        m_program.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
        m_program.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
        
        m_program.use();
        m_invCameraAdress = m_program.getUniformLocation("invCamera");
        
        Matrix4f m = new Matrix4f();
        m.setIdentity();
        m.store(MATRIX4X4_BUFFER);
        MATRIX4X4_BUFFER.flip();
  
        GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER);
        
        m_circleProgram = new Program();
        m_circleProgram.create("shader/Circles_VS.glsl", "shader/Circles_FS.glsl");
        m_circleProgram.bindAttributeLocation("vs_in_pos", 0);
        m_circleProgram.bindAttributeLocation("vs_in_normal", 1);
        m_circleProgram.bindAttributeLocation("vs_in_tc", 2);
        m_circleProgram.bindAttributeLocation("vs_in_instance", 3);
        m_circleProgram.linkAndValidate();
        m_circleProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
        m_circleProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
        m_circleProgram.use();

        setColor(0.5f, 1f, 0f, 1f);
        
        m_camera.setSpeed(0.075f);
        
        m_camera.lookAt(new Vector3f(0,0, m_currentParams.m_z), new Vector3f());
        uploadCameraBuffer();
        
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        
        m_gd = Texture.createFromFile("textures/gd.png", 0);
        
        GLUtil.transformScreenQuad(
                getWidth() / 2 - m_gd.getDest().width / 2, 
                getHeight() / 2 - m_gd.getDest().height / 2, 
                m_gd.getDest().width, m_gd.getDest().height);
    }
    
    public void setTrailAndVertCnt(int tc, int vc)
    {
        GL20.glUniform1f(m_circleProgram.getUniformLocation("circleCnt"), (float)tc);
        m_vertsPerCurve = vc;
    }
    
    public static class NBodyBuffers
    {
        public CLMem particlePositions;
        public CLMem trailPositions;
        public CLMem trailDirs;
    }
    
    public NBodyBuffers createPositionsAndVelos(float[] pos, float[] circles, float[] circlesVelos, CLContext context)
    {
        m_buffer[0] = GeometryFactory.createParticles(pos, m_currentParams.m_pointSize * 0.3f, 4);
        
        for(int i = 0; i < circles.length / 4; ++i)
        {
            circles[4 * i + 2] = -1e4f;
        }
        
        m_buffer[1] = GeometryFactory.createParticles(circles, m_currentParams.m_pointSize * 0.3f, 4);
        m_buffer[1].addInstanceBuffer(circlesVelos, 4, 4, GL15.GL_STATIC_DRAW);
        
        m_oglBuffer0 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, m_buffer[0].getInstanceBuffer(0).getId());
        m_oglBuffer1 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, m_buffer[1].getInstanceBuffer(0).getId());
        m_oglBuffer2 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, m_buffer[1].getInstanceBuffer(1).getId());
        
        NBodyBuffers buffer = new NBodyBuffers();
        buffer.particlePositions = m_oglBuffer0;
        buffer.trailPositions = m_oglBuffer1;
        buffer.trailDirs = m_oglBuffer2;
        
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer2, null, null);
 
        return buffer;
    }
    
    public void setKernelAndQueue(CLKernel kernel, CLCommandQueue queue)
    {
        m_kernel = kernel;
        m_queue = queue;
        clSetKernelArg(m_kernel, 2, m_currentParams.m_timeStep);
        clSetKernelArg(m_kernel, 3, m_currentParams.m_softening);
    }
    
    @Override
    public void render() 
    {
        clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
        clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
        clEnqueueReleaseGLObjects(m_queue, m_oglBuffer2, null, null);
        
        updateInput();
        
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT); 
        
        if(m_vertsPerCurve < m_timer.getTicks())
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            setColor(0.1f, 0.1f, 0.8f, 0.5f);
            m_circleProgram.use();
            m_buffer[1].draw();
            
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);    

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            setColor(0.6f, 0.6f, 1.0f, 1f);
            m_program.use();
            m_buffer[0].draw();
        }
        else
        {
            GLUtil.drawTexture(0);
        }
        
        Display.update();
        
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
        clEnqueueAcquireGLObjects(m_queue, m_oglBuffer2, null, null);
        
        m_timer.tick();
    }
    
    @Override
    public void close() 
    {
        clEnqueueReleaseGLObjects(m_queue, m_oglBuffer2, null, null);
        if(m_oglBuffer2 != null)
        {
            clReleaseMemObject(m_oglBuffer2);
            m_oglBuffer2 = null;
        }
        m_circleProgram.delete();
        m_gd.delete();
        super.close();
    }
    
    public void processKeyPressed(int key)
    {
        super.processKeyPressed(key);
        if(key == Keyboard.KEY_P)
        {
            m_pause = !m_pause;
        }
    }
    
    public boolean isPause()
    {
        return m_pause;
    }
}
