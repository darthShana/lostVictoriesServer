package lostVictories.messageHanders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class IDGenrarator {

	byte[] clientID;
	byte[] t = new byte[8];
	int counter = 0;
	long lastTimeStamp = 0;

	
	ThreadLocal<byte[]> temp = new ThreadLocal<byte[]>(){
		@Override
		protected byte[] initialValue() {
			return new byte[8];
		}
	};
	
	public IDGenrarator(int c) {
		this.clientID = new byte[4];

		longToByte(this.clientID, c, 4, 0, 4);
	}

	byte[] generateBytes(long timestamp, int counter) {
		byte[] bs = new byte[16];
		
		longToByte(bs, timestamp, 0, 0, 8);
		longToByte(bs, counter, 4, 8, 4);
		
		System.arraycopy(clientID, 0, bs, 12, 4);
		
		return bs;
	}
	
	private void longToByte(byte[] bs, long value, int ss, int ds, int size){
		
		byte[] res = temp.get();
		
		for(int i=7;i>=0;i--){
			res[i] = (byte)(value & 0xFF);
			value >>= 8;
		}
		System.arraycopy(res, ss, bs, ds, size);
		
		
	}

	public CustomID generate() {		
		long currentTimeStamp = System.currentTimeMillis();
		
		if(lastTimeStamp!=currentTimeStamp){
			lastTimeStamp = currentTimeStamp;
			counter = 0;
		}else{
			if(counter==Integer.MAX_VALUE){
				throw new RuntimeException("unable to generated ID load too heavy");
			}
			counter+=1;
		}
		
		return new CustomID(generateBytes(currentTimeStamp, counter));
	}
	
	public byte[] generateRaw() {		
		long currentTimeStamp = System.currentTimeMillis();
		
		if(lastTimeStamp!=currentTimeStamp){
			lastTimeStamp = currentTimeStamp;
			counter = 0;
		}else{
			if(counter==Integer.MAX_VALUE){
				throw new RuntimeException("unable to generated ID load too heavy");
			}
			counter+=1;
		}
		
		return generateBytes(currentTimeStamp, counter);
	}

	public byte[] generateBulk(int size) throws InterruptedException {
		final byte[] ret = new byte[size*16];
		ExecutorService executor = Executors.newFixedThreadPool(5);
		int index =0;
		while(index<size){
			int ii = index;
			int nextIndex = (index+1000000>=size)?size:index+1000000;
			executor.submit(() -> {
				System.out.println("starting run:"+ii);
				IDGenrarator generator = new IDGenrarator(ii);
				for(int i = ii;i<nextIndex;i++){
					copyID(ret, generator.generateRaw(), i*16);
				}
				System.out.println("finishing run:"+ii);
			});
			index = nextIndex;
		}
		executor.shutdown();
		executor.awaitTermination(60, TimeUnit.MINUTES);
		
		return ret;
		
	}

	private void copyID(final byte[] ret, byte[] generateRaw, int offset) {
		ret[offset] = generateRaw[0];
		ret[offset+1] = generateRaw[1];
		ret[offset+2] = generateRaw[2];
		ret[offset+3] = generateRaw[3];
		ret[offset+4] = generateRaw[4];
		ret[offset+5] = generateRaw[5];
		ret[offset+6] = generateRaw[6];
		ret[offset+7] = generateRaw[7];
		ret[offset+8] = generateRaw[8];
		ret[offset+9] = generateRaw[9];
		ret[offset+10] = generateRaw[10];
		ret[offset+11] = generateRaw[11];
		ret[offset+12] = generateRaw[12];
		ret[offset+13] = generateRaw[13];
		ret[offset+14] = generateRaw[14];
		ret[offset+15] = generateRaw[15];
	}
	
	
	
	

}
