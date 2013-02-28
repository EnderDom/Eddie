package enderdom.eddie.ui;

import java.util.Stack;

import org.apache.log4j.Logger;

import enderdom.eddie.databases.legacy.DBInterface;

import enderdom.eddie.tasks.BasicTaskStack;
import enderdom.eddie.tasks.TaskLike;
import enderdom.eddie.tasks.TaskStack;
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
	DBInterface db;
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
	Stack<TaskLike> futures;
	private boolean started;
	int taskcounter;
	UI top;
	public TaskStack tasker;
	
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
					logger.debug(Tools_Fun.getFunnyMessage());
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

	public TaskStack getTasker() {
		if(this.tasker == null){
			this.tasker = new BasicTaskStack();
		}
		return this.tasker;	
	}
	
	public void setTasker(TaskStack stack){
		this.tasker = stack;
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
	
}