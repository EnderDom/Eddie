package ui;

import java.util.Stack;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import tasks.Task;


import gui.EddieGUI;
import interfaces.DBInterface;
import interfaces.FileInterface;

public class TaskManager extends Thread{

	DBInterface db;
	FileInterface face;
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
	ScheduledThreadPoolExecutor Core; // Pool for Core tasks liking running blasts, so not too many threads run at the same time
	ScheduledThreadPoolExecutor Auxil; //Pool for Auxiliary tasks, things like database accession and waiting on web requests
	Stack<Task> futures;
	private boolean started;
	int taskcounter;
	EddieGUI desktop;
	
	public TaskManager(int i, int j){
		this.corepoollimit = i;
		this.auxilpoollimit =j;
		taskcounter = 0;
		CoreTasks = new Stack<Task>();
		AuxilTasks = new Stack<Task>();
	}
	
	public void addTask(Task task) {
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
		}
	}
	
	private int pushTasks2Executor(ScheduledThreadPoolExecutor exe, Stack<Task> pops, Task[] currentTask, int limit){
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
					logTask(currentTask[i]);
					currentTask[i] = pops.pop();
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

	public synchronized void update(Task task){
		desktop.updateTaskManagerGui(task);
	}
	
	public void run() {
		started = true;
		coretasklist = new Task[corepoollimit];
		auxiltasklist = new Task[auxilpoollimit];
		Core = new ScheduledThreadPoolExecutor(corepoollimit);
		Auxil = new ScheduledThreadPoolExecutor(auxilpoollimit);
		
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Core.shutdown();
		Auxil.shutdown();
		started = false;
	}
	
}