package visualize.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import visualize.FrameWork;
import visualize.gl.Buffer.VertexBuffer;
import visualize.gl.GeometryFactory.Geometry;

public class GLUtil
{   
    private static Program m_sQuadProgram;
    private static Geometry m_sDynamicScreenQuad;
    private static final FloatBuffer m_sQuadFloatbuffer = BufferUtils.createFloatBuffer(20);
    private static ByteBuffer m_sQuadByteBuffer = BufferUtils.createByteBuffer(20 * 4);
    private static int m_sLocation = -1;
    private static boolean m_sInit = false;
    
    public static void create()
    {
        if(m_sInit)
        {
            return;
        }
        m_sInit = true;

        m_sQuadProgram = new Program();
        m_sQuadProgram.create("shader/ScreenQuad_VS.glsl", "shader/ScreenQuad_FS.glsl");
        m_sQuadProgram.bindAttributeLocation("vs_in_position", 0);
        m_sQuadProgram.bindAttributeLocation("vs_in_tc", 1);
        m_sQuadProgram.linkAndValidate();
        m_sLocation = m_sQuadProgram.getUniformLocation("g_quadTexture");
        m_sDynamicScreenQuad = GeometryFactory.createDynamicScreenQuad();
    }
    
    public static void drawTexture(int unit)
    {
        m_sQuadProgram.use();
        GL20.glUniform1i(m_sLocation, unit);
        m_sDynamicScreenQuad.draw();
        GLUtil.checkError();
    }
    
    public static void destroy()
    {
        if(m_sInit)
        {
            m_sQuadProgram.delete();
            m_sDynamicScreenQuad.delete();
        }
    }
    
    public static Program getScreenQuadProgram()
    {
        return m_sQuadProgram;
    }
    
    public static void checkError()
    {
       int error = GL11.glGetError();
       switch(error)
       {
       case GL11.GL_NO_ERROR: return;
       case GL11.GL_INVALID_ENUM : System.err.println("GL_INVALID_ENUM"); break;
       case GL11.GL_INVALID_VALUE : System.err.println("GL_INVALID_VALUE"); break;
       case GL11.GL_INVALID_OPERATION : System.err.println("GL_INVALID_OPERATION"); break;
       default : System.err.println("Unknown error");
       }
    }
    
    public static void transformFloatToByte(FloatBuffer b0, ByteBuffer b1)
    {
        for(int i = 0; i < b0.capacity(); ++i)
        {
            b1.putFloat(b0.get(i));
        }
        b1.position(0);
    }
    
    public static Geometry transformScreenQuad(int x, int y, int w, int h)
    {
        VertexBuffer vb = m_sDynamicScreenQuad.getVertexBuffer();
        m_sQuadByteBuffer.position(0);
        m_sQuadByteBuffer = GL15.glMapBuffer(vb.getTarget(), GL15.GL_WRITE_ONLY, m_sQuadByteBuffer);
        
        float nx = x / (float)FrameWork.instance().getWidth();
        float ny = y / (float)FrameWork.instance().getHeight();
        float nx1 = nx + w / (float)FrameWork.instance().getWidth();
        float ny1 = ny + h / (float)FrameWork.instance().getHeight();
        
        nx = -1 + 2 * nx;
        ny = 1 - 2 * ny;
        nx1 = -1 + 2 * nx1;
        ny1 = 1 - 2 * ny1;
        //System.out.println(nx + ", " + ny + ", " + nx1 + ", " + ny1);
        float vertices[] = 
            {
        	  nx, ny1,  0, 0, 0,   //unten rechts
              nx1, ny1, 0, 1, 0,  //unten Links
              nx, ny,   0, 0, 1,    //oben rechts
              nx1, ny,  0, 1, 1,   //oben links
            };
        m_sQuadFloatbuffer.put(vertices);
        m_sQuadFloatbuffer.position(0);
        m_sQuadByteBuffer.position(0);
        GLUtil.transformFloatToByte(m_sQuadFloatbuffer, m_sQuadByteBuffer);
        
        GL15.glUnmapBuffer(vb.getTarget());
        
        return m_sDynamicScreenQuad;
    }
}
