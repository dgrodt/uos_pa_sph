package visualize.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import visualize.FrameWork;
import visualize.gl.Buffer.VertexBuffer;
import visualize.gl.GeometryFactory.Geometry;

public class FrameBuffer {

	public static FrameBuffer createFrameBuffer(Program program, String name, boolean depthStencil, Texture... textures) {
		
        if(textures.length == 0) {
        	System.out.printf("No textures for FrameBuffer %s given!", name);
            return null;
        }
        int width = -1;
        int height = -1;
        Texture copiedTextures[] = new Texture[textures.length];
        int i = 0;
        for(Texture tex : textures) {
            copiedTextures[i++] = tex;
            if(tex == null) {
            	System.out.printf("Null texture for FrameBuffer %s not allowed!", name);
                return null;
            } else if(width == -1) {
                width = tex.getDest().width;
                height = tex.getDest().height;
            } else if(width != tex.getDest().width || height != tex.getDest().height) {
                System.out.printf("Size mismatch for textures for FrameBuffer %s!", name);
                return null;
            }
        }
        FrameBuffer buffer = new FrameBuffer(program ,name, depthStencil, copiedTextures);
       
        if(!buffer.init()) {
            buffer.delete();
            System.out.printf("Initialization of FrameBuffer %s failed!\n", name);
            return null;
        }
        return buffer;
    }
	
	//Frame buffer Parameters
	protected int ID;
	protected String name;
	protected boolean deptStencil;
	protected int deptStencilID;
	protected Texture textures[];
	protected Program program;
	
    private int m_sLocation = -1;
    private LinkedList<Integer> uniformTexturesIDs = new LinkedList<Integer>();
    private LinkedList<Integer> uniformTexturesUnits = new LinkedList<Integer>();
    private final FloatBuffer quadFloatbuffer = BufferUtils.createFloatBuffer(20);
    private ByteBuffer quadByteBuffer = BufferUtils.createByteBuffer(20 * 4);
	
    
	
	
	private FrameBuffer(Program program, String name, boolean depthStencil, Texture[] textures) {
		this.program = program;
		this.name = name;
		this.deptStencil = depthStencil;
		this.textures = textures;
	}
	
	private boolean init()
	{
		this.ID = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.ID);
        if(this.ID== 0) {
            return false;
        }
        GLUtil.checkError();
        this.bind();
		if(this.deptStencil)
		{
			this.deptStencilID = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.deptStencilID);
	    	GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, FrameWork.instance().getWidth(), FrameWork.instance().getHeight());
	    	GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.deptStencilID);
		}
		 GLUtil.checkError();
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(this.textures.length+1);
        for(int i=0; i < this.textures.length; ++i) {
            this.textures[i].bind();
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, this.textures[i].getId(), 0);
            drawBuffers.put(i, GL30.GL_COLOR_ATTACHMENT0 + i);
            if(this.program != null) {
            	//addUniformTexture(this.program ,textures[i].getDest().name,textures[i].getUInt());
            }
            if(!this.checkError()) {
                System.out.printf("Framebuffer %s texture %d failed.\n", this.name, i);
            }                
        }
        GLUtil.checkError();
        GL20.glDrawBuffers(drawBuffers);

        
        return this.checkError();
	}
	
	public int getId(){
		return this.ID;
	}
	public void bind() {
	    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.ID);
	}
	
	public void unbind(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
    public void renderToBackbuffer() {
        unbind();    }
	
    public void renderToFramebuffer(){
    	this.bind();
    	this.clear();
    }
    public boolean checkError() {
        this.bind();
        int error = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        String errorString;
        switch(error) {
            case GL30.GL_FRAMEBUFFER_COMPLETE:
                return true;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
                break;
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED:
                errorString = "GL_FRAMEBUFFER_UNSUPPORTED";
                break;
            case GL30.GL_FRAMEBUFFER_UNDEFINED:
                errorString = "GL_FRAMEBUFFER_UNDEFINED";
                break;
            case GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                errorString = "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
                break;
            default:
                errorString = "Unknown status";
                break;
        }
        System.out.printf("There is something wrong with framebuffer %s: %s.\n", this.name, errorString);
        return false;
    }
    
    public void clear() {
        if(this.deptStencil) {
            GL30.glClearBufferfi(GL30.GL_DEPTH_STENCIL, 0, 1.0f, 0);
        }
      //GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
    
    public void delete() {
        for(int i=0; i < this.textures.length; ++i) {
            if(this.textures[i] != null) {
                this.textures[i].delete();
                this.textures[i] = null;
            }
        }
        if(this.deptStencilID != 0) {
            GL30.glDeleteRenderbuffers(this.deptStencilID);
            this.deptStencilID = 0;
        }
        if(this.ID != 0) {
            GL30.glDeleteFramebuffers(this.ID);
            this.ID = 0;
        }
    }
    
    public void drawTexture(){
    	drawTexture(textures[0].getUInt());
    }
    
    public void drawTexture(int unit)
    {
        for(int i = 0; i < uniformTexturesIDs.size() && i < uniformTexturesUnits.size(); ++i) {
        	GL20.glUniform1i(uniformTexturesIDs.get(i), uniformTexturesUnits.get(i));
        }  
        checkError();
    }
    
    public void transformFloatToByte(FloatBuffer b0, ByteBuffer b1)
    {
        for(int i = 0; i < b0.capacity(); ++i)
        {
            b1.putFloat(b0.get(i));
        }
        b1.position(0);
    }
    
    public void addUniformTexture(Program program, String variableName, int unit) {
    	int variableID = program.getUniformLocation(variableName);
    	uniformTexturesIDs.add(variableID);
    	uniformTexturesUnits.add(unit);
    }
}
