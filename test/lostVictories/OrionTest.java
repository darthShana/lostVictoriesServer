package lostVictories;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class OrionTest {

	@Test
	public void test() {
		int[] tes = new int[]{1, 2, 5, 9, 9};
		assertEquals(-1, solution(tes, 4));
		assertEquals(0, solution(tes, 1));
		assertEquals(1, solution(tes, 2));
		assertEquals(2, solution(tes, 5));
		assertEquals(3, solution(tes, 9));
	}
	
	int solution(int[] A, int X) {
        int N = A.length;
        if (N == 0) {
            return -1;
        }
        int l = 0;
        int r = N - 1;
        while (l < r) {
            int m = (l + r) / 2;
            if (A[m] >= X) {
                r = m;
            } else {
                l = m+1;
            }
        }
        if (A[l] == X) {
            return l;
        }
        return -1;
    }
	
	@Test
	public void test3(){
		assertEquals(4, solution2(1, 3));
	}
	
	public int solution2(int M, int N) {
		byte[] ret = getBits(M);
		for(int i=M+1;i<=N;i++){
			byte[] cur = getBits(i);
			ret = bitand(ret, cur);
		}
		return toInt(ret);
    }

	private int toInt(byte[] bitand) {
		return 	(bitand[0]<<24)&0xff000000|
				(bitand[1]<<16)&0x00ff0000|
				(bitand[2]<< 8)&0x0000ff00|
				(bitand[3]<< 0)&0x000000ff;
	}

	private byte[] bitand(byte[] bits, byte[] bits2) {
		byte[] ret = new byte[4];
		for(int i=0;i<bits.length;i++){
			ret[i] = (byte) (bits[i] & bits2[i]);
		}
		return ret;
	}

	private byte[] getBits(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}
	
	@Test
	public void testSolution4(){
		int[] a = new int[]{0, 1, 2, 3, 4, 5, 6};
		assertEquals(4, solution4(a));
	}
	
	public int solution4(int[] A) {
		Integer max = null;
		
		for(int i = 0;i<A.length;i++){
			int curr = getKset(i, A).size();
			if(max == null || curr>max){
				max = curr;
			}
		}
		return max;
    }

	private Set<Integer> getKset(int i, int[] a) {
		Set<Integer> kset = new HashSet<Integer>();
		if(a[i]==i){
			kset.add(a[i]);
			return kset;
		}
		while(a[i]!=i && !kset.contains(a[i])){
			kset.add(a[i]);
			i = a[i];
		}
		
        return kset;
	}
	
	
	

}
