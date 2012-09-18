package enderdom.eddie.tools;

import java.io.Console;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class Tools_CLI {
	
	public static int yes = 0;
	public static int no = 1;
	public static int cancel = 2;
	
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
			return yes;
		}
		if(answer.toLowerCase().startsWith("n")){
			Logger.getRootLogger().trace("User answered no");
			return no;
		}
		else{
			Logger.getRootLogger().trace("Cancelled");
			return cancel;
		}
	}
	
	public static String showInternalPasswordDialog(String message, String title){
		System.out.println("--"+title+"--");
		System.out.println();
		Console c = System.console();
		return new String(c.readPassword(message+": "));
	}
	
}


