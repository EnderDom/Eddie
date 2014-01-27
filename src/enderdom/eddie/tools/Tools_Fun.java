package enderdom.eddie.tools;

import enderdom.eddie.ui.EddiePropertyLoader;

/*
 * because sometimes its fun to dick around
 */

public abstract class Tools_Fun {
	
	public static String getRandomMessage(){
		String[] messages = new String[]{
				";)",
				"I'm Sorry Dave, but I just can't do that.",
				"Eddie protects the USER",
				"User intelligence test enabled, press computer power button to continue",
				"Row row row your boat, gently down the stream. Merrily merrily merrily merrily. Life is but a dream",
				"Warp 5 Lieutenant. Engage!",
				"This application is sponsored by Soylent Green, No artificial colors or preservatives. Made entirely from vegetables *who couldn't fight back*",
				"EnderDom is currently unavailable, as he remains mostly dead, in order to avoid paying taxes",
				"Casual Shirt Tuesday has been cancelled indefinitely",
				"This message was brought to you by the Nuka Cola Corporation, Feel those Rads",
				"And now for something completely different...",
				"I would like to tha..BRRR!!R!!RR!!RR!...All hail hypnotoad",
				"foobar",
				"Charizard, I chose you.",
				"Magicarp used splash, it was super effective",
				"Always bring a towel",
				"Insert yet another stupid reference here."
		};
		
		return getRandom(messages);
	}
	
	public static String getRandom(String[] in){
		return in[(int)Math.round(Math.random()*in.length-1)];
	}
	
	public static void printAbout(){
		System.out.println("--Eddie v"+EddiePropertyLoader.getFullVersion()+"--");
		System.out.println("");
		System.out.println("Author: Dominic Matthew Wood");
		System.out.println("About: A mindless jerk who'll be the first against the wall when the revolution comes.");
		System.out.println("Current Position: Recieving psychotherapy at the Walter Bishop Institute for Borked Scientists");
		System.out.println("Contact: Please speak to Lead Psychotherapist at the Institute");
		System.out.println("");
		//@COMPILE_INFO_INJECT
		System.out.println("Message of the Day:");
		System.out.println(getRandomMessage());
	}
	
	public static String rot13(String s){
		String[] alphabet = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		String[] alphabet2 = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		s=s.toLowerCase();
		for(int j =0; j < alphabet.length; j++){
			if(j+13 < 26){
				s=s.replaceAll(alphabet[j], alphabet2[j+13]);
			}
			else{
				s=s.replaceAll(alphabet[j], alphabet2[j+13-26]);
			}
		}
		s=s.toLowerCase();
		return s;
	}

}

