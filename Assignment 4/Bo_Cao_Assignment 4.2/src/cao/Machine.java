package cao;

import java.util.concurrent.SynchronousQueue;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
	// INVARIANT: the name of machine and the name of machineFoodType must be unique.
	//            The number of cooking item should not exceed the capacity of the machine.
	public final String machineName;
	public final Food machineFoodType;

	//YOUR CODE GOES HERE...
	public int capacityIn;
	public int numCooking;

	/**
	 * The constructor takes at least the name of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		
		//YOUR CODE GOES HERE...
		this.capacityIn = capacityIn;
		this.numCooking = 0;

		Simulation.logEvent(SimulationEvent.machineStarting(this,foodIn,capacityIn));
	}
	

	

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public Thread makeFood(Food food) throws InterruptedException {
		//YOUR CODE GOES HERE...
		Thread thread = new Thread(new CookAnItem(this,food));
		thread.start();
		return thread;
	}

	private boolean ifAvailable(){
		synchronized (this){
			return numCooking < capacityIn;
		}
	}

	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		Machine machine;
		Food food;

		public CookAnItem(Machine machine, Food food) {
			this.machine = machine;
			this.food = food;
		}


		// Precondition: New order is not available.
		// Postcondition: Turn into wait status waiting for new order.
		// Exception: Return InterruptedException as all orders done.
		// Locking: The object of machine should be locked.
		public void run() {
			synchronized (machine){
				while (!ifAvailable()){
					try{
						machine.wait();
					}catch (InterruptedException e){
						e.printStackTrace();
					}
				}
				Simulation.logEvent(SimulationEvent.machineCookingFood(machine,food));
				numCooking++;
			}

			try {
				//YOUR CODE GOES HERE...
				Thread.sleep(food.cookTimeMS);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (machine){
				Simulation.logEvent(SimulationEvent.machineDoneFood(machine,food));
				numCooking--;
				machine.notifyAll();
			}
		}
	}
 

	public String toString() {
		return machineName;
	}
}