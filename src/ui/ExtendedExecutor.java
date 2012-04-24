package ui;

import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * Workaround to catch Exceptions thrown within 
 * run() of Task see Java BUG discussion 6459119
 * @ http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459119
 * 
 * @author dominic
 *
 */

public class ExtendedExecutor extends ScheduledThreadPoolExecutor {
	
	Logger logger = Logger.getRootLogger();
	
	public ExtendedExecutor(int i) {
		super(i);
	}

	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof Future<?>) {
		    try {
		    	@SuppressWarnings("unused")
				Object result = ((Future<?>) r).get();
		    } 
		    catch (InterruptedException ie) { 
		    	logger.error("Task was interrupted",ie);
		    }
		    catch (ExecutionException ee) {
		    	Throwable realThrowable = ee.getCause();
		    	logger.error("Scheduler threw an exception",realThrowable);
		    	
		    }
		    catch (CancellationException ce) {
		    	logger.error("Task was cancelled",ce);
		    }
		}
    }

}
