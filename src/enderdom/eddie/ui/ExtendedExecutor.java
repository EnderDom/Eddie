package enderdom.eddie.ui;

import java.util.concurrent.*;

/**
 * Workaround to catch Exceptions thrown within 
 * run() of Task see Java BUG discussion 6459119
 * @ http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459119
 * 
 * @author dominic
 *
 */

public class ExtendedExecutor extends ScheduledThreadPoolExecutor {
	
	private UI ui;
	
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
		    	ui.error("Task was interrupted",ie);
		    }
		    catch (ExecutionException ee) {
		    	Throwable realThrowable = ee.getCause();
		    	ui.error("Scheduler threw an exception",realThrowable);
		    	
		    }
		    catch (CancellationException ce) {
		    	ui.error("Task was cancelled",ce);
		    }
		}
    }

}
