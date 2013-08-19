package visualize.gl;


import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import pa.util.IOUtil;
import pa.util.IOUtil.TextureData;

public class Texture 
{
    public static class TextureDescription
    {
        public int internalFormat;
        public int format;
        public int target;
        public int type = GL11.GL_FLOAT;
        public int width;
        public int height;
        public int depth;
        public boolean genMipMap = false;
        public FloatBuffer data = null;
    }
    
    private int m_id;
    private int m_unit;
    private TextureDescription m_desc;

    public Texture(int unit)
    {
        m_id = -1;
        m_unit = unit;
    }
    
    private void bind()
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
    
    public static Texture create2DTexture(int format, int internalFormat, int w, int h, int unit, FloatBuffer data)
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
        return create2DTexture(GL11.GL_RGBA, GL30.GL_RGBA16F, w, h, unit, data);
    }
    
    public static Texture createRGBA2DTexture(int w, int h, int unit, FloatBuffer data)
    {
        return create2DTexture(GL11.GL_RGBA, GL11.GL_RGBA8, w, h, unit, data);
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
        
        return create2DTexture(format, internalFormat, td.w, td.h, unit, td.data);
    }
}
