package hw1;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;

public class PublicTest {

	private int	threadCount = 10; // number of threads to run
	private ParallelMaximizer maximizer = new ParallelMaximizer(threadCount);
	
	@Test
	public void compareMax() {
		int size = 10000; // size of list
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		int serialMax = Integer.MIN_VALUE;
		double parallelMax = 0;
		double sum = 0;
		// populate list with random elements
		for (int i=0; i<size; i++) {
			int next = rand.nextInt();
			list.add(next);
			sum = sum + next;
			// serialMax = Math.max(serialMax, next); // compute serialMax
		}
		// try to find parallelMax
		try {
			parallelMax = maximizer.max(list);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail("The test failed because the max procedure was interrupted unexpectedly.");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
		}
		
		Assert.assertEquals("The serial max doesn't match the parallel max", sum/size, parallelMax, 0.1);
	}
}