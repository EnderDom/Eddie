package tools;

public class Tools_Bit {

	public static String LongAsBitString(long p){
		String st = new String();
		long mask = 0x0000000000000001L;
		long z = 0x0+p;
		for(int i =0; i < 64; i ++){
			st = (int)(mask&z)+st;
			z>>=1;
			if((i+1)%4==0)st=" "+st;
		}
		return st;
	}
	
	public static String LongAsHexString(long p){
		String st = new String();
		long mask = 0x000000000000000FL;
		for(int i =0; i < 64; i+=4){
			int a = (int)((p>>(60-i)) & mask);
			switch(a){
				case  10:st = st+"A";break;
				case  11:st = st+"B" ;break;
				case  12:st = st+ "C" ;break;
				case  13:st = st+"D";break;
				case  14:st = st+"E";break;
				case  15:st = st+"F" ;break;
				default: st = st+a ;break;
			}
		}
		return "0x"+st;
	}

}
