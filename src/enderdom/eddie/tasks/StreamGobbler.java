package enderdom.eddie.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

public class StreamGobbler extends Thread{
    
	InputStream is;
    String type;
    boolean cache; //Store output?
    StringBuffer buffer;
    boolean done;
    CountDownLatch latch;
	
    public StreamGobbler(InputStream is, String type, boolean cache, CountDownLatch latch){
        this.is = is;
        this.type = type;
        this.cache =cache;
        this.done=false;
        this.latch = latch;
    }
    
    public void run(){
    	String line=null;
        try{
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String newline = System.getProperty("line.separator");
            if(cache)buffer = new StringBuffer();
            while ( (line = br.readLine()) != null)
	            	if(cache){
	            		buffer.append(line + newline);
	            	}
	            	else{
	            		//Send to digital oblivion
	            	}
            } 
        catch (IOException ioe){
        	Logger.getRootLogger().warn("IO Issue last line parsed:"+line, ioe);
        }
        latch.countDown();
    }

    public boolean isDone(){return done;}
    
    public StringBuffer getOutput(){
    	return this.buffer;
    }
    
    public void close(){
    	try {
			this.is.close();
		} 
    	catch (IOException e) {
			Logger.getRootLogger().debug("Failed to close Inputstream, this may just be becuase it's already closed.");
		}
    }
}
