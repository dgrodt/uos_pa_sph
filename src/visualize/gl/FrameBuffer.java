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

	public static FrameBuffer createFrameBuffer(String name, boolean depthStencil, Texture... textures) {
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
        FrameBuffer buffer = new FrameBuffer(name, depthStencil, copiedTextures);
     
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
	
	//Program Variables
	private Program frameBufferProgram;
	private Geometry dynamicScreenSquad;
    private int m_sLocation = -1;
    private LinkedList<Integer> uniformTexturesIDs = new LinkedList<Integer>();
    private LinkedList<Integer> uniformTexturesUnits = new LinkedList<Integer>();
    private final FloatBuffer quadFloatbuffer = BufferUtils.createFloatBuffer(20);
    private ByteBuffer quadByteBuffer = BufferUtils.createByteBuffer(20 * 4);
    
	
	
	private FrameBuffer(String name, boolean depthStencil, Texture[] textures) {
		this.name = name;
		this.deptStencil = depthStencil;
		this.textures = textures;
	}
	
	private boolean init()
	{
        //Setup Program Variables
		frameBufferProgram = new Program();
		frameBufferProgram.create("shader/ScreenQuad_VS.glsl", "shader/ScreenQuad_FS.glsl");
		frameBufferProgram.bindAttributeLocation("vs_in_position", 0);
		frameBufferProgram.bindAttributeLocation("vs_in_tc", 1);
		frameBufferProgram.linkAndValidate();
        //m_sLocation = frameBufferProgram.getUniformLocation("g_quadTexture");
		frameBufferProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
		frameBufferProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
		frameBufferProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
		
        dynamicScreenSquad = GeometryFactory.createDynamicScreenQuad();
//        transformScreenQuad(0, 0, this.textures[0].getDest().width, this.textures[0].getDest().height);
        
		
		this.ID = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.ID);
        if(this.ID== 0) {
            return false;
        }
        this.bind();
		if(this.deptStencil)
		{
			this.deptStencilID = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.deptStencilID);
	    	GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, FrameWork.instance().getWidth(), FrameWork.instance().getHeight());
	    	GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.deptStencilID);
		}
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(this.textures.length+1);
        for(int i=0; i < this.textures.length; ++i) {
            this.textures[i].bind();
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, this.textures[i].getId(), 0);
            drawBuffers.put(i, GL30.GL_COLOR_ATTACHMENT0 + i);
            addUniformTexture(textures[i].getDest().name,textures[i].getUInt());
            if(!this.checkError()) {
                System.out.printf("Framebuffer %s texture %d failed.\n", this.name, i);
            }                
        }
        GL20.glDrawBuffers(drawBuffers);

        
        return this.checkError();
	}
	
	public void bind() {
	    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.ID);
	}
	
	public void unbind(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
    public void renderToBackbuffer(int uint) {
        unbind();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
        drawTexture(uint);
    }
	
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
    	FloatBuffer clearColor = BufferUtils.createFloatBuffer(4);
    	clearColor.put(new float[]{ 0.0f, 0.0f, 0.0f, 0.0f, });
    	clearColor.position(0);
        for(int i=0; i < this.textures.length; ++i) {
            GL30.glClearBuffer(GL11.GL_COLOR, i, clearColor);
        }
        if(this.deptStencil) {
            GL30.glClearBufferfi(GL30.GL_DEPTH_STENCIL, 0, 1.0f, 0);
        }
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
    	frameBufferProgram.delete();
    	dynamicScreenSquad.delete();
    }
    
    public void drawTexture(){
    	drawTexture(textures[0].getUInt());
    }
    
    public void drawTexture(int unit)
    {
    	frameBufferProgram.use();
        GL20.glUniform1i(m_sLocation, unit);
        for(int i = 0; i < uniformTexturesIDs.size() && i < uniformTexturesUnits.size(); ++i) {
        	GL20.glUniform1i(uniformTexturesIDs.get(i), uniformTexturesUnits.get(i));
        }
        dynamicScreenSquad.draw();
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
    
    public void addUniformTexture(String variableName, int unit) {
    	int variableID = frameBufferProgram.getUniformLocation(variableName);
    	uniformTexturesIDs.add(variableID);
    	uniformTexturesUnits.add(unit);
    }
}
