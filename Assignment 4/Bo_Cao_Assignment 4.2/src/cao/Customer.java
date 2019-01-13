package cao;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the 
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
	// INVARIANT: orderNum must be unique to each customer
	// 			  customer name must be unique
	// 			  the list of order cannot changed after ordered.
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private final int priority;
	
	private static int runningCounter = 0;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int priority) {
		this.name = name;
		this.order = order;
		this.priority = priority;
		this.orderNum = ++runningCounter;

		System.out.println(name+", priority: "+priority);
	}

	public int getPriority() {
		return priority;
	}

	public String toString() {
		return name;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the coffee shop (only successful when the coffee shop has a
	 * free table), place its order, and then leave the coffee shop
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...

		// Precondition: Order is in progress.
		// Postcondition: Turn into wait status.
		// Exception: Return InterruptedException.

		// Locking: Locking based on the object of order lock.

		Simulation.logEvent(SimulationEvent.customerStarting(this));
		Simulation.service.enterTable(this);
		Simulation.service.recordStartTime(this);
		Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
		Simulation.logEvent(SimulationEvent.customerPlacedOrder(this,order,orderNum));
		Simulation.service.placeOrder(this,order,orderNum);

		synchronized (Simulation.service.getOrderLock(orderNum)){
			while (Simulation.service.ifInProgress(orderNum)){
				try{
					Simulation.service.getOrderLock(orderNum).wait();
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}

		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this,order,orderNum));
		Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
		Simulation.service.leaveTable(this);
		Simulation.service.recordEndTime(this);
		Simulation.service.addTotalTime(this,Simulation.service.calculateTime(this));

	}
}