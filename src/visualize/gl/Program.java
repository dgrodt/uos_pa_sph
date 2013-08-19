package visualize.gl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import pa.util.IOUtil;

public class Program 
{
    protected class Shader
    {
        protected int m_id;
        private int m_type;
        
        public Shader(int type)
        {
            m_id = -1;
            m_type = type;
        }
        
        public void create()
        {
            delete();
            m_id = GL20.glCreateShader(m_type);
        }
        
        public void compile(String source)
        {
            GL20.glShaderSource(m_id, source);
            GL20.glCompileShader(m_id);
            int status = GL20.glGetShaderi(m_id, GL20.GL_COMPILE_STATUS);
            if(status == 0)
            {
                System.err.println((m_type == GL20.GL_VERTEX_SHADER ? "VertexShader Error:\n" : "FragmentShader Error\n") + 
                        GL20.glGetShaderInfoLog(m_id, 2048));
            }
        }
        
        public void delete()
        {
            if(m_id != -1)
            {
                GL20.glDeleteShader(m_id);   
            }
        }
    }
    
    protected class VertexShader extends Shader
    {
        public VertexShader()
        {
            super(GL20.GL_VERTEX_SHADER);
        }
    }
    
    protected class FragmentShader extends Shader
    {
        public FragmentShader()
        {
            super(GL20.GL_FRAGMENT_SHADER);
        }
    }
    
    private Shader m_vs;
    private Shader m_fs;
    private int m_program;
    
    public Program()
    {
        m_program = -1;
    }
    
    public void create(String vsFile, String fsFile)
    {
        if(m_vs == null)
        {
            m_vs = this.new VertexShader();   
        }
        m_vs.create();
        m_vs.compile(IOUtil.readFileContent(vsFile));
        
        if(m_fs == null)
        {
            m_fs = this.new FragmentShader();
        }
        m_fs.create();
        m_fs.compile(IOUtil.readFileContent(fsFile));
        
        if(m_program == -1)
        {
            m_program = GL20.glCreateProgram();
            GL20.glAttachShader(m_program, m_vs.m_id);
            GL20.glAttachShader(m_program, m_fs.m_id);
        }
        
        linkAndValidate();
    }
    
    public int getId()
    {
        return m_program;
    }
    
    public void bindAttributeLocation(String name, int index)
    {
        GL20.glBindAttribLocation(m_program, index, name);
    }
    
    public int getAttributeLocation(String name)
    {
        return GL20.glGetAttribLocation(m_program, name);
    }
    
    public int getUniformLocation(String name)
    {
        return GL20.glGetUniformLocation(m_program, name);
    }
    
    public int getUniformBlockIndex(String name)
    {
        return GL31.glGetUniformBlockIndex(m_program, name);
    }
    
    public void bindUniformBlock(String name, int unit)
    {
        GL31.glUniformBlockBinding(m_program, getUniformBlockIndex(name), unit); 
    }
    
    public void linkAndValidate()
    {
        GL20.glLinkProgram(m_program);
        GL20.glValidateProgram(m_program);
        int length = GL20.glGetProgrami(m_program, GL20.GL_INFO_LOG_LENGTH);
        String log = GL20.glGetProgramInfoLog(m_program, length);
        if(log.length() > 0)
        {
            System.out.println(log);   
        }
    }
    
    public int GetUniformLocation(String name)
    {
        return GL20.glGetUniformLocation(m_program, name);
    }
    
    public void use()
    {
        GL20.glUseProgram(m_program);
    }
    
    public void delete()
    {
        if(m_vs != null)
        {
            m_vs.delete();   
        }
        if(m_fs != null)
        {
            m_fs.delete();
        }
    }
}
