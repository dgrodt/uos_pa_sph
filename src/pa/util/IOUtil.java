package pa.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

public class IOUtil 
{
    public static String readFileContent(String filepath)
    {
        String source = "";
        List<String> lines = readFileLines(filepath);
        for(String line : lines)
        {
            if(line.startsWith("#include"))
            {
                String[] inc = line.split(" ");
                String file = "null";
                if(inc.length == 2)
                {
                    file = inc[1].replace("\"", "");
                }
                source += readFileContent(file);
            }
            else
            {
                source += line + "\n";
            }
        }
        return source;
    }
    
    public static List<String> readFileLines(String filenpath) 
    {
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        String errorMessage = null;
        try 
        {
            reader = new BufferedReader(new FileReader(filenpath));
            String line;
            while((line = reader.readLine()) != null) 
            {
                lines.add(line);
            }
        } catch (IOException ex) 
        {
            errorMessage = ex.getMessage();
        } finally 
        {
            try 
            {
                if(reader != null)
                {
                    reader.close();   
                }
            } catch (IOException ex) 
            {
                errorMessage = ex.getMessage();
            }
        }
        if(errorMessage != null)
        {
            throw new Error(errorMessage);
        }
        return lines;
    }
    
    public static class TextureData
    {
        public FloatBuffer data;
        public int components;
        public int w;
        public int h;
    }
    
    public static TextureData readTextureData(String file)
    {
        File f = new File(file);
        
        BufferedImage image;
        
        try 
        {
            image = ImageIO.read(f);
        } catch (IOException e) 
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        int w = image.getWidth();
        int h = image.getHeight();
        
        int color[] = new int[image.getColorModel().getComponentSize().length];
        FloatBuffer data = BufferUtils.createFloatBuffer(w * h * color.length);

        for(int y = 0; y < h; ++y) 
        {
            for(int x = 0; x < w; ++x) 
            {
                image.getRaster().getPixel(x, y, color);
                for(int i = 0; i < color.length; ++i)
                {
                    data.put(color[i] / 255.0f);
                }
            }
        }
        
        data.position(0);
        TextureData td = new TextureData();
        td.data = data;
        td.components = image.getColorModel().getComponentSize().length;
        td.h = h;
        td.w = w;
        return td;
    }
}
