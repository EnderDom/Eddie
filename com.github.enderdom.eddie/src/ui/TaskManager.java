package ui;

import java.util.Stack;

import org.apache.log4j.Logger;

import databases.legacy.DBInterface;

import tasks.Task;
import tools.Tools_Fun;


public class TaskManager extends Thread{

	Logger logger = Logger.getRootLogger();
	DBInterface db;
	int corepoollimit;
	int auxilpoollimit;
	private Stack<Task> CoreTasks; // Holds Awaiting Tasks
	private Stack<Task> AuxilTasks; // Holds Awaiting Tasks
	public Task[] coretasklist; // Holds Current running tasks
	public Task[] auxiltasklist; //Holds Current running tasks
	/*
	 * Note make sure priority of Tasks is set to the correct pool, 
	 * else will start too many intensive task
	 */
	ExtendedExecutor Core; // Pool for Core tasks liking running blasts, so not too many threads run at the same time
	ExtendedExecutor Auxil; //Pool for Auxiliary tasks, things like database accession and waiting on web requests
	Stack<Task> futures;
	private boolean started;
	int taskcounter;
	UI top;
	
	public TaskManager(int i, int j){
		this.corepoollimit = i;
		this.auxilpoollimit =j;
		taskcounter = 0;
		CoreTasks = new Stack<Task>();
		AuxilTasks = new Stack<Task>();
	}
	
	public void addTask(Task task) {
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
					logger.debug(Tools_Fun.getFunnyMessage());
				}
			}
			if(!started){
				run();
			}
		}
	}
	
	private int pushTasks2Executor(ExtendedExecutor exe, Stack<Task> pops, Task[] currentTask, int limit){
		//While Poollimit below number of working tasks
		int submitted = 0;
		for(int i =0; i < currentTask.length; i++){
			if(currentTask[i] == null){
				currentTask[i] = pops.pop();
				exe.submit(currentTask[i]);
				submitted++;
			}
			else{
				if(currentTask[i].isDone()){
					//Send task to logger
					logTask(currentTask[i]);
					//Add new task
					currentTask[i] = pops.pop();
					//Submit
					exe.submit(currentTask[i]);
					
					submitted++;
				}
			}
			if(pops.size() < 1)break;
		}
		return submitted;
	}
	
	public void logTask(Task task){
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
	public synchronized void update(Task task){
		top.update(task);
	}
	
	public void run(){
		started = true;
		logger.debug("Task Manager has been started");
		coretasklist = new Task[corepoollimit];
		auxiltasklist = new Task[auxilpoollimit];
		Core = new ExtendedExecutor(corepoollimit);
		Auxil = new ExtendedExecutor(auxilpoollimit);
		
		while(AuxilTasks.size() > 0 || CoreTasks.size() > 0){
			int newsubmit = 0;
			if(CoreTasks.size() > 0){
				newsubmit = pushTasks2Executor(Core, CoreTasks, coretasklist, corepoollimit);
			}
			if(AuxilTasks.size() > 0){
				newsubmit = newsubmit + pushTasks2Executor(Auxil, AuxilTasks, auxiltasklist, auxilpoollimit);
			}
			if(newsubmit==0){
				try {
					sleep(10000);
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
	
}