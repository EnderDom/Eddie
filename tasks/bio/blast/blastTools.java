package tasks.bio.blast;

import java.io.File;

import org.apache.log4j.Logger;

import tasks.taskTools;
import tools.systemTools;

public abstract class blastTools {

	/* This from Eddie3 */
	public static StringBuffer[] runLocalBlast(File blastquery, String blastprg, String blastbin, String blastdb, String blastparams, File output){
		if(systemTools.isWindows()){
			if(blastprg.toLowerCase().indexOf("exe") == -1){
				blastprg = blastprg + ".exe"; /*
				Hmm... this seems a bit crap and may not be necessary
				but i'm unfamiliar with cmd line in windows systems 
				*/
			}
		}
		String exec = blastbin + blastprg+ " " + blastparams + " -db " + blastdb + " -query " + blastquery.getPath() + " -out " +output.getPath();
		Logger.getRootLogger().debug("blast_executable");
		StringBuffer[] buffer = taskTools.runProcess(exec, true);
		System.out.println("Complete");
		return buffer;
	}
}
