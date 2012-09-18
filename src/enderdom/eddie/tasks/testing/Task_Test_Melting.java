package enderdom.eddie.tasks.testing;


import melting.Main;

public class Task_Test_Melting extends Task_Test_Basic{
	
	public Task_Test_Melting(){
		super();
	}
	
	public void runTest(){
		/*
		 * Testing 
		 */
		try{
			Main.main(new String[]{"-h"});
			System.out.println("Running test");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
}
