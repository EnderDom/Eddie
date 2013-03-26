package enderdom.eddie.tasks.internal;

import java.sql.SQLException;
import java.util.Properties;

import enderdom.eddie.database.updates.MySQL_Update;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.BasicTask;
import enderdom.eddie.tasks.TaskState;

public class Task_DatabaseUpdate extends BasicTask{

	public double upfrom;
	public double upto;
	private DatabaseManager manager;
	
	public Task_DatabaseUpdate(double from, double to, DatabaseManager manager){
		core=false;
		upfrom = from;
		upto = to;
		this.manager = manager;
	}
	
	public void parseOpts(Properties props){
		logger.debug("Parse Options From props");
	}
	
	
	public void run() {
		setCompleteState(TaskState.STARTED);
		boolean nobreak = true;
		while(upfrom != upto && nobreak){
			if(upfrom < 2.3){
				logger.error("Cannot update versions of database from eddie3, you will need to" +
						"re-upload the data");
				setCompleteState(TaskState.ERROR);
				nobreak=false;
			}
			else if(upfrom == 2.3){
				try{
					dbto24();
				} catch (SQLException e) {
					logger.error(e);
					setCompleteState(TaskState.ERROR);
					return;	
				}	
			}
			else{
				logger.error("Can't update any further");
			}
		}
		setCompleteState(TaskState.FINISHED);
	}
	
	
	public void dbto24() throws SQLException{
		if(manager.getDBTYPE().equals("mysql"))  MySQL_Update.updbto24(manager);
		else{
			System.out.println("There was no support for none mysql before 2.4, eh?");
		}
		System.out.println("This update was due to a bug which lead to data such, " +
				"as such all blast upload data will need to be re-uploaded");
		upfrom=2.4;
	}
}