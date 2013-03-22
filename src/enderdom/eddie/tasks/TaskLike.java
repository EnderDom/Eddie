package enderdom.eddie.tasks;

import java.util.concurrent.Future;

import enderdom.eddie.ui.UI;

public interface TaskLike extends Runnable, Future<Object> {

	public void setCore(boolean core);
	
	public boolean isCore();
	
	public void update();
	
	public boolean isStart();
	
	public boolean isCancelled();
	
	public boolean isDone();
	
	public TaskState getCompleteState();

	public void setID(int taskcounter);
	
	public int getID();
	
	public boolean wantsUI();
	
	public void addUI(UI ui);
	
	public boolean isHelpmode();
	
	public void setCompleteState(TaskState state);
	
	//What fresh madness have I started here? 
	//Returning were as tedious going
	public void setFutureHash(int hash);
	
	public int getFutureHash();
	
}
