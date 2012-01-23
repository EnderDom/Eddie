package tools;

/*
 * because sometimes its fun to dick around
 */

public abstract class funTools {

	public static String getExitMessage(){
		String[] messages = new String[]{
				";)",
				"I'm Sorry Dave, but I just can't do that.",
				"ciao",
				"bye bye",
				"bye"
		};
		
		return getRandom(messages);
	}
	
	public static String getRandom(String[] in){
		return in[(int)Math.round(Math.random()*in.length)];
	}
	
	public static void printAbout(){
		System.out.println("");
		System.out.println("");
		System.out.println("Author: EnderDom");
		System.out.println("About: A mindless jerk who'll be the first against the wall when the revolution comes.");
		System.out.println("Current Position: Recieving psychotherapy at the Walter Bishop Institute for Borked Scientists");
		System.out.println("Contact: Please speak to Dr. Smith at the Institute");
		System.out.println("");
		System.out.println("");
	}	
}
