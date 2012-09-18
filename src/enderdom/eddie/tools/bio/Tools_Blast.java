package enderdom.eddie.tools.bio;

import java.io.File;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;

public abstract class Tools_Blast {

	/* This from Eddie3 */
	public static StringBuffer[] runLocalBlast(File blastquery, String blastprg, String blastbin, String blastdb, String blastparams, File output){
		if(Tools_System.isWindows()){
			if(blastprg.toLowerCase().indexOf("exe") == -1){
				blastprg = blastprg + ".exe"; /*
				Hmm... this seems a bit crap and may not be necessary
				but i'm unfamiliar with cmd line in windows systems 
				*/
				if(blastbin.indexOf("/")!=-1){
					Logger.getRootLogger().warn("This is a Windows system but the default directorys looked unix like");
				}
			}
		}
		String exec = blastbin + blastprg+ " " + blastparams + " -db " + blastdb + " -query " + blastquery.getPath() + " -out " +output.getPath();
		StringBuffer[] buffer = Tools_Task.runProcess(exec, true);
		return buffer;
	}
	
}
