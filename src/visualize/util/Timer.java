package visualize.util;

public class Timer 
{
    private long m_lastNanos, m_currentNanos, m_start;
    private float m_fps = 0;
    private long m_ticks = 0;
    private long m_framesTime = 0;
    
    public void reset() 
    {
        this.m_lastNanos = System.nanoTime();
        this.m_currentNanos = System.nanoTime();
        this.m_start = System.nanoTime();
    }
    
    public long getLastNanos() 
    {
        return this.m_currentNanos - this.m_lastNanos;
    }
    
    public long getLastMillis() 
    {
        return getLastNanos() / (long)1e6;
    }
    
    public float getFps() 
    {
        return this.m_fps;
    }
    
    public long getTime() 
    {
        return System.nanoTime() - this.m_start;
    }
    
    public long getTicks()
    {
        return m_ticks;
    }
    
    public void tick() 
    {
        this.m_lastNanos = this.m_currentNanos;
        this.m_currentNanos = System.nanoTime();
        this.m_framesTime += this.getLastNanos();
        if((++m_ticks) % 100 == 0) 
        {
            this.m_fps = 100.f/this.m_framesTime * 1e9f;
            this.m_framesTime = 0;
        }
    }
}
