package lostVictories.messageHanders;



public class CustomID {

	private byte[] bs;

	public CustomID(byte[] bs) {
		this.bs = bs;
	}
	
	public static long bytesToLong(byte[] id) {
		long res = 0;
	    for (int i = 0; i < 8; i++) {
	        res <<= 8;
	        res |= (id[i] & 0xFF);
	    }
	    return res;
	}
	
	@Override
	public String toString() {		
		
		byte[] t = new byte[8];
		System.arraycopy(bs, 0, t, 0, 8);
		long timestamp = bytesToLong(t);

		byte[] c = new byte[8];
		System.arraycopy(bs, 8, c, 4, 4);
		long counter = bytesToLong(c);
		
		byte[] g = new byte[8];
		System.arraycopy(bs, 12, g, 4, 4);
		long generator = bytesToLong(g); 
		
		return String.format("%016d-%08d-%08d", timestamp, counter, generator);
	}

}
