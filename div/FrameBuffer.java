/*
 * This software is the proprietary information of nico3000.
 */
package graphics.framebuffer;

import graphics.texture.Texture;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

/**
 * Ein Framebuffer bietet die Moeglichkeit anstatt auf den Backbuffer in eine
 * beliebige Anzahl von Texturen zu rendern. Auf diese Weise koennen eine
 * Vielzahl von Effekten realisiert werden. Beispielsweise kann man die Textur
 * auf ein Objekt in der Szene legen und somit eine Art Bildschirm im Spiel
 * erzeugen. Oder das gerenderte Bild kann mittels diverse Posteffects
 * bearbeitet werden.
 * @author nico3000
 */
public class FrameBuffer {
    /**
     * Eine Liste aller bekannten Framebuffer Objekte.
     */
    private static final List<FrameBuffer> INSTANCES = new LinkedList<>();
    
    /**
     * Erzeugt und initialisiert einen neuen Framebuffer.
     * @param name Der Name des Framebuffers
     * @param depthStencil Falls {@code true}, enthaelt der Framebuffer einen
     *                     eigenen Stencil- und z-Buffer
     * @param textures Die Texturen, auf die gerendert werden soll. Diese
     *                 <i>muessen</i> alle gleich gross sein!
     * @return Der Framebuffer oder {@code null}, falls ein Fehler aufgetreten
     *         ist
     */
    public static FrameBuffer createFrameBuffer(String name, boolean depthStencil, Texture... textures) {
        if(textures.length == 0) {
            Logger.INSTANCE.error("No textures for FrameBuffer %s given!", name);
            return null;
        }
        int width = -1;
        int height = -1;
        Texture copiedTextures[] = new Texture[textures.length];
        int i = 0;
        for(Texture tex : textures) {
            copiedTextures[i++] = tex;
            if(tex == null) {
                Logger.INSTANCE.error("Null texture for FrameBuffer %s not allowed!", name);
                return null;
            } else if(width == -1) {
                width = tex.getWidth();
                height = tex.getHeight();
            } else if(width != tex.getWidth() || height != tex.getHeight()) {
                Logger.INSTANCE.error("Size mismatch for textures for FrameBuffer %s!", name);
                return null;
            }
        }
        FrameBuffer buffer = new FrameBuffer(name, depthStencil, copiedTextures);
        if(!buffer.init()) {
            buffer.release();
            Logger.INSTANCE.error("Initialization of FrameBuffer %s failed!", name);
            return null;
        }
        INSTANCES.add(buffer);
        return buffer;
    }
    
    /**
     * Gibt den Status der bekannten FrameBuffer zurueck.
     * @return Der Status
     */
    public static String getDebugStatus() {
        String textures = "";
        for(FrameBuffer buffer : INSTANCES) {
            if(buffer.isInitialized()) {
                textures += String.format("\n- %s", buffer.name);
            }
        }
        return String.format("Active FrameBuffer instances: %s", textures.isEmpty() ? "none" : textures);
    }

    /**
     * Deaktiviert den momentan gebundenen Framebuffer.
     */
    public static void renderToBackbuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
    
    /**
     * Der Name des Framebuffers.
     */
    private final String name;
    /**
     * Gibt an, ob der Framebuffer einen Stencil- und z-Buffer besitzt.
     */
    private final boolean depthStencil;
    /**
     * Die Rendertargets des Framebuffers.
     */
    private final Texture textures[];
    /**
     * Die ID des Framebuffers.
     */
    private int frameBufferId = 0;
    /**
     * Die ID des Depth-Stencil-Renderbuffers, sofern vorhanden.
     */
    private int depthStencilId = 0;
    /**
     * Die Farben, mit denen die einzelnen Rendertargets gecleart werden sollen.
     */
    private final FloatBuffer clearColor[];
    
    /**
     * Erzeugt einen neuen Framebuffer.
     * @param name Der Name des Framebuffers
     * @param depthStencil Falls {@code true}, enthaelt der Framebuffer einen
     *                     eigenen Stencil- und z-Buffer
     * @param textures Die Texturen, auf die gerendert werden soll. Diese
     *                 <i>muessen</i> alle gleich gross sein!
     */
    private FrameBuffer(String name, boolean depthStencil, Texture textures[]) {
        this.name = name;
        this.depthStencil = depthStencil;
        this.textures = textures;
        this.clearColor = new FloatBuffer[4];
        for(int i=0; i < textures.length; ++i) {
            this.textures[i].addRef();
            this.clearColor[i] = BufferUtils.createFloatBuffer(4);
            this.clearColor[i].put(new float[] { 0.0f, 0.0f, 0.0f, 0.0f, });
            this.clearColor[i].position(0);
        }
    }
    
    /**
     * Initialisiert den Framebuffer.
     * @return {@code true}, falls erfolgreich
     */
    private boolean init() {
        this.frameBufferId = GL30.glGenFramebuffers();
        if(this.frameBufferId == 0) {
            Logger.INSTANCE.error("glGenFramebuffers() failed!");
            return false;
        }
        this.bind();
        if(this.depthStencil) {
            this.depthStencilId = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.depthStencilId);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, this.textures[0].getWidth(), this.textures[0].getHeight());
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.depthStencilId);
        }
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(this.textures.length);
        for(int i=0; i < this.textures.length; ++i) {
            this.textures[i].bind();
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, this.textures[i].getId(), 0);
            drawBuffers.put(i, GL30.GL_COLOR_ATTACHMENT0 + i);
            if(!this.checkError()) {
                Logger.INSTANCE.info("Framebuffer %s texture %d failed.", this.name, i);
            }                
        }
        GL20.glDrawBuffers(drawBuffers);
        return this.checkError();
    }
    
    /**
     * Gibt den Framebuffer und alle Texturen frei.
     */
    public void release() {
        for(int i=0; i < this.textures.length; ++i) {
            if(this.textures[i] != null) {
                this.textures[i].release();
                this.textures[i] = null;
            }
        }
        if(this.depthStencilId != 0) {
            GL30.glDeleteRenderbuffers(this.depthStencilId);
            this.depthStencilId = 0;
        }
        if(this.frameBufferId != 0) {
            GL30.glDeleteFramebuffers(this.frameBufferId);
            this.frameBufferId = 0;
        }
    }
    
    /**
     * Bindet den Framebuffer. Alle nachfolgenden Drawaufrufe werden an diesen
     * Framebuffer weitergeleitet.
     */
    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
    }
    
    /**
     * Prueft, ob der Framebuffer bereits initialisiert ist.
     * @return {@code true}, falls er bereits erfolgreich initialisiert wurde
     */
    private boolean isInitialized() {
        return this.frameBufferId != 0;
    }
    
    /**
     * Cleart den gesamten Framebuffer.
     */
    public void clear() {
        for(int i=0; i < this.textures.length; ++i) {
            GL30.glClearBuffer(GL11.GL_COLOR, i, this.clearColor[i]);
        }
        if(this.depthStencil) {
            GL30.glClearBufferfi(GL30.GL_DEPTH_STENCIL, 0, 1.0f, 0);
        }
    }
    
    /**
     * Liest einen einzelnen Pixel aus diesem Framebuffer.
     * @param texture Die Textur
     * @param x Die x-Koordinate des Pixels
     * @param y Die y-Koordinate des Pixels
     * @param target Zielbuffer; darf <tt>null</tt> sein
     * @return Der Zielbuffer
     */
    public FloatBuffer getValue(int texture, int x, int y, FloatBuffer target) {
        this.bind();
        if(target == null) {
            target = BufferUtils.createFloatBuffer(4);
        }
        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_FLOAT, target);
        Logger.INSTANCE.info("Read: %.2f %.2f %.2f %.2f.", target.get(target.position()), target.get(target.position()+1), target.get(target.position()+2), target.get(target.position()+3));
        return target;
    }
    
    /**
     * Prueft den Framebuffer auf Completeness.
     * @return {@code false}, falls der Framebuffer incomplete, d.h. ungueltig
     *         ist
     */
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
        Logger.INSTANCE.error("There is something wrong with framebuffer %s: %s.", this.name, errorString);
        return false;
    }
}
