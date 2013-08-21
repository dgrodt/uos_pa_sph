package main;

import sph.SPH;

public class Main 
{
    public static void main(String[] args)
    {
        SPH app = SPH.getInstance();
//        SettingsFrame settings = new SettingsFrame();
//        
//        
//        settings.setVisible(true);
        app.run();
        System.exit(0);
    }
}
