package enderdom.eddie.ui;

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
	
	public ExtendedExecutor(int i, UI ui) {
		super(i);
	}

	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof Future<?>) {
		    try {
		    	@SuppressWarnings("unused")
				Object result = ((Future<?>) r).get();
		    } 
		    catch (InterruptedException ie) { 
		    	Logger.getRootLogger().error("Task was interrupted",ie);
		    }
		    catch (ExecutionException ee) {
		    	Throwable realThrowable = ee.getCause();
		    	Logger.getRootLogger().error("Scheduler threw an exception", realThrowable);
		    }
		    catch (CancellationException ce) {
		    	Logger.getRootLogger().error("Task was cancelled",ce);
		    }
		}
    }

}
