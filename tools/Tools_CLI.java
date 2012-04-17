package tools;

import java.util.Scanner;

import org.apache.log4j.Logger;

public class Tools_CLI {
	
	public static String showInternalInputDialog(String title, String message){
		System.out.println("--"+title+"--");
		System.out.println();
		System.out.println(message+":");
		Scanner sc = new Scanner(System.in);
		String answer;
		answer = sc.next();
		return answer;
	}
	
	public static int showInternalConfirmDialog(String title, String message){
		System.out.println("--"+title+"--");
		System.out.println();
		System.out.println(message + " (yes/no/cancel)");
		Scanner sc = new Scanner(System.in);
		int timeout = 0;
		String answer;
		answer = sc.next();
		while(!answer.toLowerCase().startsWith("y") && !answer.toLowerCase().startsWith("n")  && !answer.toLowerCase().startsWith("c")  && timeout < 3){
			System.out.print("\nInput not recognised please try again ("+(3-timeout)+" more retries). (y/n/c)");
			answer = sc.next();
			timeout++;
		}
		if(timeout >= 3){
			System.out.println("Too many attempts, assumed to be cancel");
			answer = "c";
		}
		if(answer.toLowerCase().startsWith("y")){
			Logger.getRootLogger().trace("User answered yes");
			return 0;
		}
		if(answer.toLowerCase().startsWith("n")){
			Logger.getRootLogger().trace("User answered no");
			return 1;
		}
		else{
			Logger.getRootLogger().trace("Cancelled");
			return 2;
		}
	}
	
}
