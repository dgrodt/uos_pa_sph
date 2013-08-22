package visualize.gl;


import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import pa.util.IOUtil;
import pa.util.IOUtil.TextureData;
import visualize.FrameWork;

public class Texture 
{
    public static class TextureDescription
    {
        public int internalFormat;
        public int format;
        public int target = GL11.GL_TEXTURE_2D;
        public int type = GL11.GL_FLOAT;
        public int width;
        public int height;
        public int depth;
        public boolean genMipMap = false;
        public FloatBuffer data = null;
    }
    
    protected int m_id;
    protected int m_unit;
    protected TextureDescription m_desc;

    public Texture(int unit)
    {
        m_id = GL11.glGenTextures();
        m_unit = unit;
    }
    public Texture(int uint, TextureDescription desc)
    {
    	this(uint);
    	m_desc = desc;   
    }
    
    public void bind()
    {
        GL11.glBindTexture(m_desc.target, m_id);
    }
    
    public void activate()
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + m_unit);
        bind();
    }
    
    public void create(TextureDescription desc)
    {
        delete();
        m_id = GL11.glGenTextures();
        m_desc = desc;
        loadFloatData(desc.data);
    }
    
    public void loadFloatData(FloatBuffer data)
    {
        activate();
        switch(m_desc.target)
        {
        case GL11.GL_TEXTURE_2D:
        {
            GL11.glTexImage2D(m_desc.target, 0, m_desc.internalFormat, m_desc.width, m_desc.height, 0, m_desc.format, m_desc.type, data);
            GL30.glGenerateMipmap(m_desc.target);   
        } break;
        default : throw new IllegalArgumentException();
        }
    }
    
    public int getId()
    {
        return m_id;
    }
    public int getUInt()
    {
        return m_unit;
    }
    
    public TextureDescription getDest()
    {
        return m_desc;
    }
    
    public void delete()
    {
        if(m_id != -1)
        {
            GL11.glDeleteTextures(m_id);
        }
    }
    public static Texture createTexture(int uint, TextureDescription desc)
    {
    	Texture t = new Texture(uint, desc);
    	
        GL11.glTexParameteri(desc.target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(desc.target, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameterf(desc.target, GL11.GL_TEXTURE_WRAP_S, 	  GL11.GL_CLAMP );
        GL11.glTexParameterf(desc.target, GL11.GL_TEXTURE_WRAP_T,     GL11.GL_CLAMP );
    	GL11.glTexImage2D(   desc.target, 0, desc.internalFormat, desc.width, desc.height, 0, desc.format, desc.type, desc.data );
    	checkError();
    	return t;
    }
    
    public static Texture create2DTexture(int format, int internalFormat, int type, int w, int h, int unit, FloatBuffer data)
    {
        TextureDescription desc = new TextureDescription();
        desc.target = GL11.GL_TEXTURE_2D;
        desc.type = GL11.GL_FLOAT;
        desc.format = format;
        desc.internalFormat = internalFormat;
        desc.width = w;
        desc.height = h;
        desc.data = data;
        Texture t = new Texture(unit);
        t.create(desc);
        return t;
    }
    
    public static Texture createRGBA16F2DTexture(int w, int h, int unit, FloatBuffer data)
    {
        return create2DTexture(GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_FLOAT, w, h, unit, data);
    }
    
    public static Texture createRGBA2DTexture(int w, int h, int unit, FloatBuffer data)
    {
        return create2DTexture(GL11.GL_RGBA, GL11.GL_RGBA8,GL11.GL_FLOAT, w, h, unit, data);
    }
    
    public static Texture createRGBAFromX(TextureData data, int unit)
    {
        if(data.components == 4)
        {
            return createRGBA2DTexture(data.w, data.h, unit, data.data);
        }
        else
        {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(data.w * data.h * 4);
            for(int i = 0; i < data.w * data.h; ++i)
            {
                for(int j = 0; j < data.components; ++j)
                {
                    buffer.put(data.data.get());
                }
                for(int j = 0; j < 4 - data.components; ++j)
                {
                    buffer.put(0);
                }
            }
            buffer.position(0);
            return createRGBA2DTexture(data.w, data.h, unit, buffer);
        }
    }
    
    public static Texture createFromFile(String path, int unit)
    {
        TextureData td = IOUtil.readTextureData(path);
        int format = 0;
        int internalFormat = 0;
        switch(td.components) 
        {
            case 1: internalFormat = GL30.GL_R8; format = GL11.GL_RED; break;
            case 2: internalFormat = GL30.GL_RG8; format = GL30.GL_RG; break;
            case 3: internalFormat = GL11.GL_RGB8; format = GL11.GL_RGB; break;
            case 4: internalFormat = GL11.GL_RGBA8; format = GL11.GL_RGBA; break;
        }
        
        return create2DTexture(format, internalFormat,GL11.GL_FLOAT, td.w, td.h, unit, td.data);
    }
    
    public static Texture create3DTexture(){
    	//Setup Textures
    	int width = FrameWork.instance().getWidth();
    	int height = FrameWork.instance().getHeight();
        TextureDescription desc = new TextureDescription();
        desc.target = GL12.GL_TEXTURE_3D;
        desc.type = GL11.GL_FLOAT;
        desc.format = GL11.GL_RGBA;
        desc.internalFormat = GL30.GL_RGB16F;
        desc.width = width;
        desc.height = height;
        desc.data =  BufferUtils.createFloatBuffer(100*100*10*4);;

    	Texture t = new Texture(4,desc);
    	
    	GL11.glEnable(GL12.GL_TEXTURE_3D);
    	int texture3d = GL11.glGenTextures();
    	GL11.glBindTexture(GL12.GL_TEXTURE_3D, texture3d);
    	GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    	GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    	GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    	GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    	GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_REPEAT);
    	
    	GL12.glTexImage3D(desc.target, 0, desc.internalFormat, width, height, width, 0, desc.format, GL11.GL_FLOAT, desc.data);
    	GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
    	GL11.glBindTexture(GL12.GL_TEXTURE_3D, texture3d);
    	checkError();
    	return t;
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
}
