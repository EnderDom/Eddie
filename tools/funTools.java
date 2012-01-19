package tools;

/*
 * because sometimes its fun to dick around
 */

public class funTools {

	public static String getExitMessage(){
		String[] messages = new String[]{
				";)",
				"For the Lolz",
				"I'm Sorry Dave, but I just can't do that.",
				"ciao",
				"bye bye",
				"bye"
		};
		
		return messages[(int)Math.round(Math.random()*messages.length)];
	}
}
