package enderdom.eddie.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import enderdom.eddie.ui.UI;

public class BasicTask implements TaskLike {

	protected boolean core;
	protected boolean try2Close;
	protected TaskState state;
	protected int futurehash;
	protected Logger logger = Logger.getRootLogger();
	protected int id;
	
	public boolean isCore(){
		return core;
	}
	
	public void setCore(boolean core){
		this.core = core;
	}
	public synchronized void update(){
		
	}

	public Object get() throws InterruptedException, ExecutionException {
		return (Object) this;
	}

	public Object get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		return (Object) this;
	}
	

	public boolean isStart(){
		return state != TaskState.UNSTARTED;
	}

	public boolean isCancelled() {
		return state == TaskState.CANCELLED;
	}

	public boolean isDone() {
		if(state == TaskState.STARTED || state == TaskState.UNSTARTED){
			return false;
		}
		else{
			return true;
		}
	}
	
	public TaskState getCompleteState(){
		return state;
	}
	
	public void setCompleteState(TaskState state){
		this.state = state;
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
	
	public boolean wantsUI(){
		return false;
	}
	
	public void addUI(UI ui){
		
	}

	public void setFutureHash(int hash) {
		this.futurehash = hash;
	}

	public int getFutureHash() {
		return this.futurehash;
	}

	public void run() {
		// TODO Auto-generated method stub
	}

	public boolean isHelpmode() {
		return false;
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		setCompleteState(TaskState.CANCELLED);
		return false;
	}
}
