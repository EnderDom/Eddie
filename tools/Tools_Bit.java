package tools;

public class Tools_Bit {

	public static String LongAsBitString(long p){
		String st = new String();
		long mask = 1;
		long z = 0x0+p;
		for(int i =0; i < 64; i ++){
			st = (int)(mask&z)+st;
			z>>=1;
			if((i+1)%4==0)st=" "+st;
		}
		return st;
	}

}
