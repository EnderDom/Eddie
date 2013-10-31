package enderdom.eddie.ui;

import java.util.Stack;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import enderdom.eddie.tasks.TaskLike;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tools.Tools_Fun;

/**
 * black box class, just add a task
 * and forget about it for now
 * originally it was meant to allow
 * multiple tasks to be run simoultaneously
 * but that is as yet untested
 */
public class TaskManager extends Thread{

	Logger logger = Logger.getRootLogger();
	int corepoollimit;
	int auxilpoollimit;
	private Stack<TaskLike> CoreTasks; // Holds Awaiting Tasks
	private Stack<TaskLike> AuxilTasks; // Holds Awaiting Tasks
	public TaskLike[] coretasklist; // Holds Current running tasks
	public TaskLike[] auxiltasklist; //Holds Current running tasks
	/*
	 * Note make sure priority of Tasks is set to the correct pool, 
	 * else will start too many intensive task
	 */
	ExtendedExecutor Core; // Pool for Core tasks liking running blasts, so not too many threads run at the same time
	ExtendedExecutor Auxil; //Pool for Auxiliary tasks, things like database accession and waiting on web requests
	private boolean started;
	int taskcounter;
	UI top;
	
	public TaskManager(UI ui, int i, int j){
		this.corepoollimit = i;
		this.auxilpoollimit =j;
		taskcounter = 0;
		CoreTasks = new Stack<TaskLike>();
		AuxilTasks = new Stack<TaskLike>();
		this.top = ui;
	}
	
	public void addTask(TaskLike task) {
		logger.debug("Task add to the task list");
		if(task.isHelpmode()){
			logger.debug("Task went to helpmode, not adding to Task Manager");
		}
		else{
			if(task.isCore()){
				CoreTasks.push(task);
				task.setID(taskcounter);
				taskcounter++;
				logTask(task);
			}
			else{
				AuxilTasks.push(task);
				task.setID(taskcounter);
				logTask(task);
				taskcounter++;
				if(taskcounter % 100 == 0){
					logger.debug(Tools_Fun.getRandomMessage());
				}
			}
			if(!started){
				run();
			}
		}
	}
	
	private int pushTasks2Executor(ExtendedExecutor exe, Stack<TaskLike> pops, TaskLike[] currentTask, int limit){
		//While Poollimit below number of working tasks
		int submitted = 0;
		for(int i =0; i < currentTask.length; i++){
			if(currentTask[i] == null){
				currentTask[i] = pops.pop();
				Future<?> t = exe.submit(currentTask[i]);
				currentTask[i].setFutureHash(t.hashCode());
				submitted++;
			}
			else{
				if(currentTask[i].isDone()){
					//Send task to logger
					logTask(currentTask[i]);
					//Add new task
					currentTask[i] = pops.pop();
					//Submit
					Future<?> t = exe.submit(currentTask[i]);
					currentTask[i].setFutureHash(t.hashCode());
					submitted++;
				}
			}
			if(pops.size() < 1)break;
		}
		return submitted;
	}
	
	public void logTask(TaskLike task){
		/*
		 * TODO
		 * 
		 *  Log should show return of complete variable 
		 *  to show any errors
		 *
		 */
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
	
	/*
	 * Work in progress, not yet completely familiar with thread safety yet
	 */
	public synchronized void update(TaskLike task){
		top.update(task);
	}
	
	public void run(){
		started = true;
		logger.debug("Task Manager has been started");
		coretasklist = new TaskLike[corepoollimit];
		auxiltasklist = new TaskLike[auxilpoollimit];
		Core = new ExtendedExecutor(corepoollimit, top);
		Auxil = new ExtendedExecutor(auxilpoollimit, top);
		
		while(AuxilTasks.size() > 0 || CoreTasks.size() > 0 || tasksUnfinished()){
			int newsubmit = 0;
			if(CoreTasks.size() > 0){
				newsubmit = pushTasks2Executor(Core, CoreTasks, coretasklist, corepoollimit);
			}
			if(AuxilTasks.size() > 0){
				newsubmit = newsubmit + pushTasks2Executor(Auxil, AuxilTasks, auxiltasklist, auxilpoollimit);
			}
			if(newsubmit==0){
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					logger.error("Interrupt",e);
				}
			}
		}
		Core.shutdown();
		Auxil.shutdown();
		logger.debug("Task Manager has no more tasks, shutting down");
		started = false;
	}
	
	public boolean tasksUnfinished(){
		for(int i =0; i < coretasklist.length;i++){
			if(coretasklist[i] != null){
				if(!coretasklist[i].isDone()){
					return true;
				}
			}
		}
		for(int i =0; i < auxiltasklist.length;i++){
			if(auxiltasklist[i] != null){
				if(!auxiltasklist[i].isDone()){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Attempts to retrieve a task based on hashcode of runnable
	 * @param hashCode
	 */
	public TaskLike getTaskWithFuture(int hashCode) {
		for(int i =0; i < coretasklist.length;i++){
			if(coretasklist[i] != null){
				if(coretasklist[i].getFutureHash() == hashCode){
					return coretasklist[i];
				}
			}
		}
		for(int i =0; i < auxiltasklist.length;i++){
			if(auxiltasklist[i] != null){
				if(auxiltasklist[i].getFutureHash() == hashCode){
					return auxiltasklist[i];
				}
			}
		}
		return null;
	}

	public Stack<TaskLike> getShutdowns() {
		Stack<TaskLike> toshut = new Stack<TaskLike>();
		for(int i =0; i < coretasklist.length;i++){
			if(coretasklist[i] != null){
				if(coretasklist[i].canBeShutdown())toshut.add(coretasklist[i]);
				else coretasklist[i].setCompleteState(TaskState.CANCELLED);
			}
		}
		for(int i =0; i < auxiltasklist.length;i++){
			if(auxiltasklist[i] != null){
				if(auxiltasklist[i].canBeShutdown())toshut.add(auxiltasklist[i]);
				else auxiltasklist[i].setCompleteState(TaskState.CANCELLED);
			}
		}
		return toshut;
	}
		

}