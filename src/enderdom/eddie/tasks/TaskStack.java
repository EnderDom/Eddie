package enderdom.eddie.tasks;

public interface TaskStack {
	
	public String pop();
		
	public String push(String o); 

	public boolean empty();
	
}
