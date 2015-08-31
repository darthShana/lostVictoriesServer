package lostVictories.messageHanders;


import static org.junit.Assert.*;

import org.junit.Test;

public class IDGeneratorTest {
	
//	long counter;
//	byte[] generatorID = new byte[] {
//            (byte)(34 >>> 8),
//            (byte)34};
	
	
	@Test
	public void testByteGeneration(){
		
		long sampleTimestamp = 1440695889550l;
		IDGenrarator idGenerator = new IDGenrarator(109);
		
		byte[] id = idGenerator.generateBytes(sampleTimestamp, 1019); 
		//[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] = 128 bit/16 byte ID
		//64 bit timestamp, 32 bit counter, 32 bit generator id
		
		
		assertEquals("id size", 16, id.length);
		byte[] tt = new byte[8];
		System.arraycopy(id, 0, tt, 0, 8);
		long t = CustomID.bytesToLong(tt);
		assertEquals("timestamp component", sampleTimestamp, t);
		
		byte[] cc = new byte[8];
		System.arraycopy(id, 8, cc, 4, 4);
		long c = CustomID.bytesToLong(cc);
		assertEquals("counter component", 1019, c);
		
		byte[] gg = new byte[8];
		System.arraycopy(id, 12, gg, 4, 4);
		long g = CustomID.bytesToLong(gg);
		assertEquals("generator id component", 109, g);
		
	}
	
	@Test(timeout=100)
	public void testIDGeneration(){
		IDGenrarator idGenerator = new IDGenrarator(109);
		for(int i=0;i<1000;i++){
			CustomID id = idGenerator.generate();
			System.out.println(id);
		}
	}
	
	@Test
	public void testBulkIDGeneration() throws InterruptedException{
		//16bytes*50,000,000 = 800,000,000 bytes = 0.8GB
		
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
		IDGenrarator idGenerator = new IDGenrarator(109);
		byte[] ids = idGenerator.generateBulk(50000000);
		runtime.gc();
		long memoryNow = runtime.totalMemory() - runtime.freeMemory();
		
		assertTrue("allowed memory usage exceeded:"+(memoryNow-memoryBefore), (memoryNow-memoryBefore)<1500000000);
		assertEquals(50000000*16, ids.length);
		byte[] id0 = new byte[16];
		System.arraycopy(ids, 0, id0, 0, 16);
		System.out.println(new CustomID(id0));
		byte[] id2 = new byte[16];
		System.arraycopy(ids, 2000000*16, id2, 0, 16);
		System.out.println(new CustomID(id2));
		byte[] id3 = new byte[16];
		System.arraycopy(ids, 39999999*16, id3, 0, 16);
		System.out.println(new CustomID(id3));
		byte[] id4 = new byte[16];
		System.arraycopy(ids, 49999999*16, id4, 0, 16);
		System.out.println(new CustomID(id4));
		
	}

	

}
