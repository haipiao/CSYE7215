package cao;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	// INVARIANT: The name of cook must be unique.
	//			  The number of cooking should not exceed the number of orders.
	private final String name;

	private HashMap<Food, Machine> machinesForFood;
	private HashMap<Integer, HashMap<Food, LinkedList<Thread>>> foodThreadsForOrderNumber;

	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name, HashMap<Food, Machine> machinesForFood) {
		this.name = name;
		this.machinesForFood = machinesForFood;
		this.foodThreadsForOrderNumber = new HashMap<Integer, HashMap<Food, LinkedList<Thread>>>();
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while(true) {
				//YOUR CODE GOES HERE...
				// Precondition: New order is not available.
				// Postcondition: Turn into wait status waiting for new order.
				// Exception: Return InterruptedException as all orders done.
				// Locking: Locking based on the object of order lock.

				Integer orderNum;
				synchronized (Simulation.service.getNewOrders()){
					while(!Simulation.service.ifHasNewOrder()){
						Simulation.service.getNewOrders().wait();
					}
					orderNum = Simulation.service.getNextOrder();
				}


				if(orderNum!=-1){
					List<Food> order = Simulation.service.getOrder(orderNum);
					doOrder(order,orderNum);
				}else{
					throw new InterruptedException("No new order to cook!");
				}




			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}

	private void doOrder(List<Food> order, int orderNum) throws InterruptedException{
		synchronized (Simulation.service.getOrderLock(orderNum)) {
			Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNum));
			foodThreadsForOrderNumber.put(orderNum, new HashMap<Food, LinkedList<Thread>>());

			Food[] foods = {FoodType.burger, FoodType.fries, FoodType.coffee};
			int[] foodcounter = new int[3];

			for (Food food : order) {
				if (food == FoodType.burger)
					foodcounter[0]++;
				else if (food == FoodType.fries)
					foodcounter[1]++;
				else if (food == FoodType.coffee)
					foodcounter[2]++;

			}

			for(int i = 0; i<3; i++){
				Food food = foods[i];
				foodThreadsForOrderNumber.get(orderNum).put(food,new LinkedList<Thread>());

				for(int j=0; j<foodcounter[i];j++){
					Machine machine = machinesForFood.get(food);
					Simulation.logEvent(SimulationEvent.cookStartedFood(this,food,orderNum));
					Simulation.service.startOrder(this,orderNum);
					Thread thread = machine.makeFood(food);
					foodThreadsForOrderNumber.get(orderNum).get(food).add(thread);
				}
			}

			for (Food food: foodThreadsForOrderNumber.get(orderNum).keySet()) {
				for (Thread thread : foodThreadsForOrderNumber.get(orderNum).get(food)){
					thread.join();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(this,food,orderNum));
				}
			}

			Simulation.service.finishOrder(this,orderNum);
			Simulation.logEvent(SimulationEvent.cookCompletedOrder(this,orderNum));
		}
	}
}