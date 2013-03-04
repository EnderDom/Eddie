package enderdom.eddie.tasks.subtasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import enderdom.eddie.tasks.TaskLike;
import enderdom.eddie.ui.UI;

public class SubTask implements TaskLike{

	boolean core;
	private boolean try2Close;
	protected int complete;
	Logger logger = Logger.getRootLogger();
	int id;
	
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public boolean isCore(){
		return core;
	}
	
	public void setCore(boolean core){
		this.core = core;
	}
	public synchronized void update(){
		
	}
	
	public boolean isStart(){
		if(complete == -1){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean cancel(boolean arg0) {
		try2Close = arg0;
		return false;
	}

	public Object get() throws InterruptedException, ExecutionException {
		return (Object) this;
	}

	public Object get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		return (Object) this;
	}

	public boolean isCancelled() {
		if(complete== 2){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isDone() {
		if(complete > 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public int getComplete() {
		return complete;
	}

	protected void setComplete(int complete) {
		this.complete = complete;
		logger.info("Task was set to complete");
	}

	public boolean isTry2Close() {
		return try2Close;
	}

	public void setTry2Close(boolean try2Close) {
		this.try2Close = try2Close;
	}

	public void setID(int taskcounter) {
		this.id = taskcounter;
	}
	public int getID(){
		return this.id;
	}

	public boolean wantsUI() {
		return true;
	}

	public void addUI(UI ui) {
		
	}

	public boolean isHelpmode() {
		// TODO Auto-generated method stub
		return false;
	}

}
