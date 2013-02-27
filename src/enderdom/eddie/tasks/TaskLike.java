package enderdom.eddie.tasks;

import java.util.concurrent.Future;

public interface TaskLike extends Runnable, Future<Object> {

	public void setCore(boolean core);
	
	
	
}
