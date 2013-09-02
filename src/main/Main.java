package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sph.SPH;
import sph.helper.Settings;

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
        //System.exit(0);
        if(Settings.GENERATE_VIDEO) {
        	try {
        		String filename = null;
        		int cnt = 0;
        		while(filename == null) {
        			File file = new File(Settings.OUTPUT_FOLDER+"/"+Settings.OUTPUT_FILENAME+"_"+cnt+".flv");
        			if(!file.exists()) {
        				filename = Settings.OUTPUT_FILENAME+"_"+cnt+".flv";
        			} else {
        				++cnt;
        			}
        		}
        		System.out.println("saving video to "+Settings.OUTPUT_FOLDER+"/"+filename+"...");
				Process ffmpeg = Runtime.getRuntime().exec("lib/ffmpeg -start_number 0 -i "+Settings.OUTPUT_FOLDER+"/frame_%d.png -vcodec flashsv2 "+Settings.OUTPUT_FOLDER+"/"+filename);
				BufferedReader in = new BufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
				String line;
				while((line = in.readLine()) != null) {
					System.out.println(line);
				}
				File file = new File(Settings.OUTPUT_FOLDER);
				File[] files = file.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".png") && name.startsWith("frame");
					}
				});
				for(File f: files) {
					f.delete();
				}
				System.out.println("saving complete.");
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
