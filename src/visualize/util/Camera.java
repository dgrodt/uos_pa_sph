package visualize.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import pa.util.math.MathUtil;
import visualize.util.math.MatrixUtil;

public class Camera 
{
    protected float m_far = 1e3f;
    protected float m_near = 1e-2f;
    protected float m_fov = MathUtil.PI_DIV2;
    protected float m_aspect;
    protected float m_phi = 0;
    protected float m_theta = 0;
    protected float m_speed = 1;
    
    protected Vector3f m_eyePos = new Vector3f(0, 0, -1);
    protected Vector3f m_viewDir = new Vector3f(0, 0, 1);
    protected Vector3f m_upDir = new Vector3f(0, 1, 0);
    protected Vector3f m_sideDir = new Vector3f(1, 0, 0);
    
    private Matrix4f m_view = new Matrix4f();
    private Matrix4f m_proj = new Matrix4f();
    
    public Camera(int width, int height) 
    {
        m_view.setIdentity();
        m_proj.setIdentity();
        setAspect(width, height);
        updateProjection();
        updateView();
    }
    
    public void setSpeed(float speed)
    {
        m_speed = speed;
    }
    
    public float getSpeed()
    {
        return m_speed;
    }
    
    public float getNear() 
    {
        return m_near;
    }
    
    public float getFar() 
    {
        return m_far;
    }
    
    public void setAspect(int width, int height)
    {
        setAspect(width / (float)height);
    }
    
    public void setAspect(float aspect) 
    {
        m_aspect = aspect;
        updateProjection();
    }
    
    public void setFOV(float fov)
    {
        m_fov = fov;
        updateProjection();
    }
    
    public void setNearFar(float near, float far)
    {
        m_near = near;
        m_far = far;
        updateProjection();
    }
    
    public final Matrix4f getView()
    {
        return m_view;
    }
    
    public final Matrix4f getProjection() 
    {
        return m_proj;
    }
    
    public final Vector3f getSideDir() 
    {
        return m_sideDir;
    }
    
    public final Vector3f getViewDir() 
    {
        return m_viewDir;
    }
    
    public float getPhi()
    {
        return m_phi;
    }
    
    public float getTheta()
    {
        return m_theta;
    }
    
    public final Vector3f getUpDir() 
    {
        return m_upDir;
    }
    
    public final Vector3f getEyePos() 
    {
        return m_eyePos;
    }
    
    public void lookAt(Vector3f eyePos, Vector3f lookAt)
    {
        m_eyePos.set(eyePos);
        Vector3f dir = new Vector3f();
        Vector3f.sub(lookAt, eyePos, dir);
        float xz = (float)Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float phi = -MathUtil.PI_MUL2 + (float)Math.asin(dir.x / xz);
        float theta = -MathUtil.PI_DIV2 + (float)Math.acos(dir.y);

        m_phi = 0;
        m_theta = 0;
        rotate(phi, theta);
    }
    
    public void move(Vector3f vector) 
    {
        m_eyePos.x += (vector.z * m_viewDir.x + vector.x * m_sideDir.x) * m_speed;
        m_eyePos.y += (vector.z * m_viewDir.y + vector.x * m_sideDir.y + vector.y) * m_speed;
        m_eyePos.z += (vector.z * m_viewDir.z + vector.x * m_sideDir.z) * m_speed;
        updateView();
    }
    
    public void rotate(float dphi, float dtheta) 
    {
        m_phi = (m_phi + dphi) % MathUtil.PI_MUL2;
        m_theta += dtheta;
        m_theta = MathUtil.clamp(m_theta, -MathUtil.PI_DIV2, MathUtil.PI_DIV2);
        
        float sinPhi = MathUtil.sin(m_phi);
        float cosPhi = MathUtil.cos(m_phi);
        float sinTheta = MathUtil.sin(m_theta);
        float cosTheta = MathUtil.cos(m_theta);
        
        m_sideDir.set(cosPhi, 0, -sinPhi);
        m_upDir.set(sinPhi*sinTheta, cosTheta, cosPhi*sinTheta);
        m_viewDir.set(sinPhi*cosTheta, -sinTheta, cosPhi*cosTheta);
        
        updateView();
    }
    
    public void updateView() 
    {
        MatrixUtil.lookToLH(m_eyePos, m_viewDir, m_upDir, m_view);
    }
    
    public void updateProjection() 
    {
        MatrixUtil.perspectiveFovLH(m_fov, m_aspect, m_near, m_far, m_proj);
    }
}
