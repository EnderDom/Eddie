package tasks;

import interfaces.DBInterface;
import interfaces.FileInterface;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ui.TaskManager;

public class Task implements Runnable, Future<Object> {

	private int id;
	private boolean core;
	private DBInterface database;
	private FileInterface files;
	private TaskManager manager;
	protected int complete;
	/*
	 * complete note:
	 * -1 == unstarted, but init
	 * 0 == started
	 * 1 == finished without error
	 * 2 == cancelled
	 * 3 == Error
	 * >3 == Task-Specific Error
	 */
	
	private boolean try2Close;
	
	public boolean isCore(){
		return core;
	}
	
	public void setCore(boolean core){
		this.core = core;
	}
	
	public void run() {
		
	}
	
	public void parseArgs(String args){
		
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void SetInterfaces(DBInterface database, FileInterface files){
		this.setFiles(files);
		this.setDatabase(database);
	}

	public DBInterface getDatabase() {
		return database;
	}

	public void setDatabase(DBInterface database) {
		this.database = database;
	}

	public FileInterface getFiles() {
		return files;
	}

	public void setFiles(FileInterface files) {
		this.files = files;
	}
	
	public String getHelpMessage(){
		return new String("--This is the Help Message of the Default Task--");
	}
	
	public synchronized void update(){
		this.manager.update(this);
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

	public void setComplete(int complete) {
		this.complete = complete;
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
	

}
