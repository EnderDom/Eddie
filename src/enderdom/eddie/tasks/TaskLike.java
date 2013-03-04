package enderdom.eddie.tasks;

import java.util.concurrent.Future;

import enderdom.eddie.ui.UI;

public interface TaskLike extends Runnable, Future<Object> {

	public static int unstarted = -1;
	public static int started = 0;
	public static int finished = 1;
	public static int cancelled = 2;
	public static int error = 3;
	
	public void setCore(boolean core);
	
	public boolean isCore();
	
	public void update();
	
	public boolean isStart();
	
	public boolean cancel(boolean arg0);
	
	public boolean isCancelled();
	
	public boolean isDone();
	
	public int getComplete();
	
	public boolean isTry2Close();

	public void setTry2Close(boolean try2Close);

	public void setID(int taskcounter);
	
	public int getID();
	
	public boolean wantsUI();
	
	public void addUI(UI ui);
	
	public boolean isHelpmode();
	
}
