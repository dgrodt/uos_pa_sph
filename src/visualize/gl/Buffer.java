package visualize.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

public class Buffer 
{
    public static class GLBuffer
    {
        int m_id;
        int m_target;
        int m_size;
        
        public GLBuffer(int target)
        {
            m_id = -1;
            m_target = target;
        }
        
        public void create()
        {
            delete();
            m_id = GL15.glGenBuffers();
        }
        
        public void bind()
        {
            assert m_id != -1 : "Buffer not created";
            GL15.glBindBuffer(m_target, m_id);
        }
        
        public void loadFloatData(FloatBuffer data, int usage)
        {
            bind();
            GL15.glBufferData(m_target, data, usage);
            m_size = data.capacity();
        }
        
        public void updateFloatData(FloatBuffer data, long offset)
        {
            bind();
            GL15.glBufferSubData(m_target, offset, data);
        }
        
        public void loadIntData(IntBuffer data, int usage)
        {
            bind();
            GL15.glBufferData(m_target, data, usage);
            m_size = data.capacity();
        }
        
        public int getId()
        {
            return m_id;
        }
        
        public int getTarget()
        {
            return m_target;
        }
        
        public void delete()
        {
            if(m_id != -1)
            {
                GL15.glDeleteBuffers(m_id);
            }
        }
    }
    
    public static class VertexBuffer extends GLBuffer
    {
        public VertexBuffer()
        {
            super(GL15.GL_ARRAY_BUFFER);
        }
    }
    
    public static class IndexBuffer extends GLBuffer
    {
        protected int m_topology;
        protected int m_elements;
        public IndexBuffer(int topology)
        {
            super(GL15.GL_ELEMENT_ARRAY_BUFFER);
            m_topology = topology;
        }
    }
    
    public static class UniformBuffer extends GLBuffer
    {
        private int m_bindingPoint;
        
        public UniformBuffer(int bindingPoint)
        {
            super(GL31.GL_UNIFORM_BUFFER);
            m_bindingPoint = bindingPoint;
        }
        
        public void create()
        {
            super.create();
        }

        public void bindBufferBase()
        {
            GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, m_bindingPoint, m_id);
        }
    }
}
