package visualize;

import visualize.gl.Program;
import visualize.gl.GeometryFactory.Geometry;
import visualize.gl.GeometryFactory.GeometryCreator;

public class GeometryDraw extends FrameWork
{
    protected Geometry m_geometry;
    protected GeometryCreator m_creator;
    protected Program m_program;
    
    public GeometryDraw(GeometryCreator creator) 
    {
        this(creator, false);
    }
    
    public GeometryDraw(GeometryCreator creator, boolean vsync) 
    {
        this(800, 600, creator, vsync);
    }
    
    public GeometryDraw(int w, int h, GeometryCreator creator, boolean vsync) 
    {
        super(w, h, true, vsync);
        m_creator = creator;
    }

    @Override
    public void render() 
    {
        m_geometry.draw();
    }

    @Override
    public void init() 
    {
        m_program = new Program();
        m_program.create("shader/Default_VS.glsl", "shader/Default_FS.glsl");
        m_program.bindAttributeLocation("vs_in_pos", 0);
        m_program.bindAttributeLocation("vs_in_normal", 1);
        m_program.bindAttributeLocation("vs_in_tc", 2);
        m_program.bindAttributeLocation("vs_in_instance", 3);
        m_program.linkAndValidate();
        m_program.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
        m_program.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
        m_program.use();
        m_geometry = m_creator.CreateGeometry();
    }

    @Override
    public void close() 
    {
        m_program.delete();
        m_geometry.delete();
    }
}
