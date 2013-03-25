package enderdom.eddie.ui;

import java.util.concurrent.*;

import org.apache.log4j.Logger;

import enderdom.eddie.tasks.TaskLike;
import enderdom.eddie.tasks.TaskState;

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
	
	public ExtendedExecutor(int i, UI u) {
		super(i);
		this.ui = u;
	}

	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof Future<?>) {
		    try {
		    	@SuppressWarnings("unused")
				Object result = ((Future<?>) r).get();
		    } 
		    catch (InterruptedException ie) {
		    	Logger.getRootLogger().error("Task was interrupted",ie);
		    	setErrored(r);
		    }
		    catch (ExecutionException ee) {
		    	Throwable realThrowable = ee.getCause();
		    	Logger.getRootLogger().error("Scheduler threw an exception", realThrowable);
		    	setErrored(r);
		    }
		    catch (CancellationException ce) {
		    	Logger.getRootLogger().error("Task was cancelled",ce);
		    	setErrored(r);
		    }
		}
    }

	private void setErrored(Runnable r){
		TaskLike l = ui.getTaskManager().getTaskWithFuture(r.hashCode());
    	if(l != null) {
    		l.setCompleteState(TaskState.ERROR);
    		Logger.getRootLogger().info("Eddie is shutting down the errored task.");
    	}
    	else Logger.getRootLogger().error("Task could not be retrieved to cancel, thread will hang"); 
	}
}
