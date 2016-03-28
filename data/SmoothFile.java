import java.io.*;
import java.util.zip.*;

// http://www.xyzws.com/javafaq/how-to-make-a-gzip-file-in-java/215

public class SmoothFile {
  static BufferedWriter bufferedWriter = null;
    public static void main(String[] args) {
        System.out.println("Hello, World");
	Smooth smooth = new Smooth(100);
	String[] array;
	float x,y,z;
	long timestamp,offset = 0;
	double tl;

	String fname = "sensor-2016-03-28-10:04:59.txt";
	String line = null;

	try {
          FileReader fileReader = new FileReader(fname);
          BufferedReader bufferedReader = new BufferedReader(fileReader);
          bufferedWriter = new BufferedWriter(new FileWriter("liner_acceleration_smooth.dat"));
          // bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("liner_acceleration_smooth.dat.gz"))));
  
          while((line = bufferedReader.readLine()) != null) {
            array = line.split("\\s+");

	    if (array[1].matches("10")) {
	    timestamp = Long.parseLong(array[0]);

	    if (offset == 0 ) {
	      offset = timestamp;
	    }

	    tl = (timestamp - offset)/1000000000.0f;

	      x = Float.parseFloat(array[2]);
	      y = Float.parseFloat(array[3]);
	      z = Float.parseFloat(array[4]);

              // System.out.println(String.format("%d %.6f",timestamp,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	      bufferedWriter.append(String.format("%.6f %.6f\n",tl,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	    }
  	  }   
  	  bufferedReader.close();
  	  bufferedWriter.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + fname + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + fname + "'");                  
        }
    }

}

