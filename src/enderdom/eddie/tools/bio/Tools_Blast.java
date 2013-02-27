package enderdom.eddie.tools.bio;

import java.io.File;

import org.apache.log4j.Logger;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;

import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.Tools_Task;

public abstract class Tools_Blast {

	/* This from Eddie3 */
	public static StringBuffer[] runLocalBlast(File blastquery, String blastprg, String blastbin, String blastdb, String blastparams, File output, boolean notremote){
		if(BlastProgramEnum.valueOf(blastprg.toLowerCase()) == null){
			Logger.getRootLogger().warn("Warning, " + blastprg + " is not a standard blast program");
		}
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
		String exec = blastbin + blastprg+ " " + blastparams + " -db " + blastdb + 
				" -query " + blastquery.getPath() + " -out " +output.getPath();
		if(!exec.contains("-num_threads") && notremote){
			exec+=" -num_threads "+ Tools_System.getCPUs();
		}
		if(!notremote){
			exec+=" -remote";
		}

		StringBuffer[] buffer = Tools_Task.runProcess(exec, true);
		return buffer;
	}
	
}
