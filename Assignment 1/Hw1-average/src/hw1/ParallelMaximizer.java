package hw1;


import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the maximum
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelMaximizer {
	
	int numThreads;
	ArrayList<ParallelMaximizerWorker> workers;

	public ParallelMaximizer(int numThreads) {
		workers = new ArrayList<ParallelMaximizerWorker>(numThreads);
		this.numThreads = numThreads;
		// System.out.println("初始化值： " + workers.size());
	}



	public static void main(String[] args) {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 1000; // number of integers in the list


		ParallelMaximizer maximizer = new ParallelMaximizer(numThreads);
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		// populate the list
		// TODO: change this implementation to test accordingly
		for (int i=0; i<numElements; i++) 
			list.add(i);

		// run the maximizer
		try {
			System.out.println("Average in total: "+maximizer.max(list));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Finds the maximum by using <code>numThreads</code> instances of
	 * <code>ParallelMaximizerWorker</code> to find partial maximums and then
	 * combining the results.
	 * @param list <code>LinkedList</code> containing <code>Integers</code>
	 * @return Maximum element in the <code>LinkedList</code>
	 * @throws InterruptedException
	 */
	public double max(LinkedList<Integer> list) throws InterruptedException {
		int max = Integer.MIN_VALUE; // initialize max as lowest value
		double sum = 0;
		int times = 0;
		
		System.out.println("Number of workers: " + numThreads);
		// run numThreads instances of ParallelMaximizerWorker
		for (int i=0; i<numThreads; i++) {
			workers.add(new ParallelMaximizerWorker(list));
		}
		for (int i=0; i < numThreads; i++) {
			workers.get(i).start();
		}
		// wait for threads to finish
		for (int i=0; i< numThreads; i++){
			workers.get(i).join();
			System.out.println("Average in "+workers.get(i).getName()+": "+workers.get(i).getAverage());
		}

		
		// take the highest of the partial maximums
		// TODO: IMPLEMENT CODE HERE
		for (int i=0; i< numThreads; i++){
			sum = sum + workers.get(i).getSum();
			times = times + workers.get(i).getTimes();
		}

		return sum / times;
	}
	
}
