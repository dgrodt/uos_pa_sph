package visualize.input;

public class Input 
{
    public static interface InputHandler 
    {
        void processKeyDown(int key);
        
        void processKeyUp(int key);
        
        void processKeyPressed(int key);
        
        void processKeyRepeat(int key);
        
        void processMouseButtonDown(int x, int y, int button);
        
        void processMouseButtonUp(int x, int y, int button);
        
        void processMouseDragged(int x, int y, int dx, int dy, int button);
        
        void processMouseMoved(int x, int y, int dx, int dy);
        
        void processMouseWheel(int x, int y, int dWheel);
    }
    
    public static class InputAdapter implements InputHandler
    {
        public void processKeyDown(int key)
        {
            
        }
        
        public void processKeyUp(int key)
        {
            
        }
        
        public void processKeyPressed(int key)
        {
            
        }
        
        public void processKeyRepeat(int key)
        {
            
        }
        
        public void processMouseButtonDown(int x, int y, int button)
        {
            
        }
        
        public void processMouseButtonUp(int x, int y, int button)
        {
            
        }
        
        public void processMouseDragged(int x, int y, int dx, int dy, int button)
        {
            
        }
        
        public void processMouseWheel(int x, int y, int dWheel)
        {
            
        }
        
        public void processMouseMoved(int x, int y, int dx, int dy)
        {
            
        }
    }
}
