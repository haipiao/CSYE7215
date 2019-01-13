package cao;


import java.util.*;

/**
 * Simulation is the main class used to run the simulation.  You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// Make sure singleton
	public static Simulation service;


	// List to track simulation events during simulation
	public static List<SimulationEvent> events;

	private int numCustomers, numCooks, numTables, machineCapacity;
	private boolean randomOrders;

	private Thread[] cooks;
	private Thread[] customers;

	private HashMap<Food,Machine> machineForFood;
	private List<Customer> tables;

	private HashMap<Integer,List<Food>> orderByOrderNum;
	private HashMap<Integer,Object> locksByOrderNum;

	private HashSet<Integer> newOrders;
	private HashSet<Integer> inProgressOrders;
	private HashSet<Integer> finishedOrders;

	private List<Integer> orderPriority; // The order priority list.
	private Integer firstPriorityBoundPointer;
	private Integer secondPriorityBoundPointer;

	private HashMap<Customer,Long> customerStartTimes;
	private HashMap<Customer,Long> customerEndTimes;
	private HashMap<Customer,Object> locksByCustomer;

	private HashMap<Integer,Integer> priorityCounters;
	private HashMap<Integer,Long> priorityTotalTimes;
	private HashMap<Integer,Object> locksByPriority;

	private int finishedOrdersCounter;
	private Object finishedLock;

	// INVARIANT: Each threads of Cook, Machine and Customer should have its own separate work.
	//            The counter of the finished order should not exceed the number of orders.

	/**
	 * Used by other classes in the simulation to log events
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * 	Function responsible for performing the simulation. Returns a List of 
	 *  SimulationEvent objects, constructed any way you see fit. This List will
	 *  be validated by a call to Validate.validateSimulation. This method is
	 *  called from Simulation.main(). We should be able to test your code by 
	 *  only calling runSimulation.
	 *  
	 *  Parameters:
	 *	@param numCustomers the number of customers wanting to enter the coffee shop
	 *	@param numCooks the number of cooks in the simulation
	 *	@param numTables the number of tables in the coffe shop (i.e. coffee shop capacity)
	 *	@param machineCapacity the capacity of all machines in the coffee shop
	 *  @param randomOrders a flag say whether or not to give each customer a random order
	 *
	 */
	public static List<SimulationEvent> runSimulation(
			int numCustomers, int numCooks,
			int numTables, 
			int machineCapacity,
			boolean randomOrders
			) {

		//This method's signature MUST NOT CHANGE.


		//We are providing this events list object for you.  
		//  It is the ONLY PLACE where a concurrent collection object is 
		//  allowed to be used.
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());


		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers,
				numCooks,
				numTables,
				machineCapacity));



		// Set things up you might need
		service = new Simulation();
		service.numCustomers = numCustomers;
		service.numCooks = numCooks;
		service.numTables = numTables;
		service.machineCapacity = machineCapacity;
		service.randomOrders = randomOrders;

		service.cooks = new Thread[numCooks];

		service.machineForFood = new HashMap<Food,Machine>();

		service.tables = new ArrayList<Customer>(numTables);

		service.orderByOrderNum = new HashMap<Integer, List<Food>>();
		service.locksByOrderNum = new HashMap<Integer, Object>();

		service.newOrders = new HashSet<Integer>();
		service.inProgressOrders = new HashSet<Integer>();
		service.finishedOrders = new HashSet<Integer>();

		service.orderPriority = new ArrayList<Integer>();
		service.firstPriorityBoundPointer = 0;
		service.secondPriorityBoundPointer = 0;


		service.customerStartTimes = new HashMap<Customer, Long>();
		service.customerEndTimes = new HashMap<Customer, Long>();
		service.locksByCustomer = new HashMap<Customer, Object>();

		service.priorityCounters = new HashMap<Integer, Integer>();
		service.priorityTotalTimes = new HashMap<Integer, Long>();
		service.locksByPriority = new HashMap<Integer, Object>();


		service.finishedOrdersCounter = 0;

		service.finishedLock = new Object();


		// Start up machines
		service.machineForFood.put(FoodType.burger, new Machine("Grill",FoodType.burger,service.machineCapacity));
		service.machineForFood.put(FoodType.fries, new Machine("Frier",FoodType.fries,service.machineCapacity));
		service.machineForFood.put(FoodType.coffee, new Machine("Star", FoodType.coffee,service.machineCapacity));


		// Let cooks in
		for(int i=0; i< service.numCooks; i++){
			service.cooks[i] = new Thread(new Cook("Cook"+(i+1), service.machineForFood));
		}

		// Initialize the customer priority counter and the timer of total time, 0 at initial.
		for(int i=1; i<=3;i++){
			service.priorityCounters.put(i,0);
			service.priorityTotalTimes.put(i,0L);
			service.locksByPriority.put(i,new Object());
		}


		// Build the customers.
		service.customers = new Thread[service.numCustomers];
		LinkedList<Food> order;
		if (!service.randomOrders) {
			System.out.println("The orders are not random!");
			order = new LinkedList<Food>();
			// Order 2 Burgers, 2 Fries and 1 Coffee.
			order.add(FoodType.burger);
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			for(int i = 0; i < service.customers.length; i++) {
				Customer customer = new Customer("Customer " + (i), order, (int)((Math.random()*3)+1));
				service.customers[i] = new Thread(customer);
				service.locksByCustomer.put(customer,new Object());
			}
		}
		else {
			System.out.println("The orders are random!");
			for(int i = 0; i < service.customers.length; i++) {
				int burgerCount = (int)((Math.random()*3)+1);
				int friesCount = (int)((Math.random()*3)+1);
				int coffeeCount = (int)((Math.random()*3)+1);
				order = new LinkedList<Food>();
				for (int b = 0; b < burgerCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int c = 0; c < coffeeCount; c++) {
					order.add(FoodType.coffee);
				}
				Customer customer = new Customer("Customer " + (i), order, (int)((Math.random()*3)+1));
				service.customers[i] = new Thread(customer);
				service.locksByCustomer.put(customer,new Object());
			}
		}

		for (int i = 0;i<service.cooks.length;i++){
			service.cooks[i].start();
		}


		// Now "let the customers know the shop is open" by
		//    starting them running in their own thread.
		for(int i = 0; i < service.customers.length; i++) {
			service.customers[i].start();
			//NOTE: Starting the customer does NOT mean they get to go
			//      right into the shop.  There has to be a table for
			//      them.  The Customer class' run method has many jobs
			//      to do - one of these is waiting for an available
			//      table...
		}


		// Precondition: Orders are still not finished.
		// Postcondition: Return wait() to wait for the orders finished.
		// Exception: Return InterruptedException as all orders finished.
		try {
			// Wait for customers to finish
			//   -- you need to add some code here...
			for (int i = 0;i <service.customers.length;i++){
				service.customers[i].join();
			}

			while (!service.ifAllOrderFinished()){
				synchronized (service.finishedLock){
					try{
						service.finishedLock.wait();
					}catch (InterruptedException e){
						e.printStackTrace();
					}
				}
			}
			
			

			// Then send cooks home...
			// The easiest way to do this might be the following, where
			// we interrupt their threads.  There are other approaches
			// though, so you can change this if you want to.
			for(int i = 0; i < service.cooks.length; i++)
				service.cooks[i].interrupt();
			for(int i = 0; i < service.cooks.length; i++)
				service.cooks[i].join();

		}
		catch(InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		Simulation.logEvent(SimulationEvent.machineEnding(service.machineForFood.remove(FoodType.coffee)));
		Simulation.logEvent(SimulationEvent.machineEnding(service.machineForFood.remove(FoodType.fries)));
		Simulation.logEvent(SimulationEvent.machineEnding(service.machineForFood.remove(FoodType.burger)));


		// Calculate the average waiting time based on priority.
		for(int i=1; i<=3; i++){
			if(service.priorityCounters.get(i)==0){
				System.out.println("Priority "+i+", No that kind of customer!");
			}
			else{
				System.out.println("Priority "+i+", Average waiting time: "+ service.priorityTotalTimes.get(i)/service.priorityCounters.get(i)+"ms");
			}
		}

		// Done with simulation		
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation.  There
	 * should be exactly four arguments: the first is the number of customers,
	 * the second is the number of cooks, the third is the number of tables
	 * in the coffee shop, and the fourth is the number of items each cooking
	 * machine can make at the same time.  
	 */
	public static void main(String args[]) throws InterruptedException {
		// Parameters to the simulation
		/*
		if (args.length != 4) {
			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <capacity> <randomorders");
			System.exit(1);
		}
		int numCustomers = new Integer(args[0]).intValue();
		int numCooks = new Integer(args[1]).intValue();
		int numTables = new Integer(args[2]).intValue();
		int machineCapacity = new Integer(args[3]).intValue();
		boolean randomOrders = new Boolean(args[4]);
		 */
		int numCustomers = 10;
		int numCooks = 2;
		int numTables = 5;
		int machineCapacity = 4;
		boolean randomOrders = false;


		// Run the simulation and then 
		//   feed the result into the method to validate simulation.
		System.out.println("Did it work? " + 
				Validate.validateSimulation(
						runSimulation(
								numCustomers, numCooks, 
								numTables, machineCapacity,
								randomOrders
								)
						)
				);

		System.out.println("------------------------------------------------------------------------------------------------------------------------------------");

		randomOrders = true;
		// To test the result that randomOrders is true.
		System.out.println("Did it work? " +
				Validate.validateSimulation(
						runSimulation(
								numCustomers, numCooks,
								numTables, machineCapacity,
								randomOrders
						)
				)
		);


	}

	void enterTable(Customer customer) {
		synchronized (tables){
			while (numTables <= tables.size()){
				try{
					tables.wait();
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			tables.add(customer);

		}
	}

	void leaveTable(Customer customer){
		synchronized (tables){
			tables.remove(customer);
			tables.notifyAll();
		}
	}

	boolean placeOrder(Customer customer, List<Food> order, int orderNum){
		if(customer==null || order==null || newOrders == null || orderByOrderNum ==null){
			return false;
		}

		synchronized (orderByOrderNum){
			orderByOrderNum.put(orderNum,order);
		}

		synchronized (locksByOrderNum){
			locksByOrderNum.put(orderNum,new Object());
		}

		synchronized (orderPriority){
			if(customer.getPriority()==1){ // The 1st priority
				orderPriority.add(firstPriorityBoundPointer,orderNum);
				firstPriorityBoundPointer++;
				secondPriorityBoundPointer++;
			}else if(customer.getPriority()==2){ // The 2nd priority
				orderPriority.add(secondPriorityBoundPointer,orderNum);
				secondPriorityBoundPointer++;
			}else if(customer.getPriority()==3){ // The 3rd priority
				orderPriority.add(orderNum);
			}
			orderPriority.notifyAll();
		}

		synchronized (newOrders){
			newOrders.add(orderNum);
			synchronized (this){
				this.notifyAll();
			}
			newOrders.notifyAll();
		}
		return true;
	}

	void startOrder(Cook cook, int orderNum){
		synchronized (getOrderLock(orderNum)){
			synchronized (inProgressOrders){
				inProgressOrders.add(orderNum);
				getOrderLock(orderNum).notifyAll();
			}
		}
	}

	void finishOrder(Cook cook, int orderNum){
		synchronized (getOrderLock(orderNum)){
			synchronized (inProgressOrders){
				inProgressOrders.remove(orderNum);
				synchronized (finishedOrders){
					finishedOrders.add(orderNum);
				}
				synchronized (finishedLock){
					finishedOrdersCounter = finishedOrdersCounter +1;
				}
			}
			getOrderLock(orderNum).notifyAll();
		}
	}

	Object getOrderLock(int orderNum){
		synchronized (locksByOrderNum){
			return locksByOrderNum.get(orderNum);
		}
	}

	Object getCustomerLock(Customer customer){
		synchronized (locksByCustomer){
			return locksByCustomer.get(customer);
		}
	}

	Object getPriorityLock(int priority){
		synchronized (locksByPriority){
			return locksByPriority.get(priority);
		}
	}

	private boolean ifAllOrderFinished(){
		System.out.println("Order Finished: "+finishedOrders);
		synchronized (finishedLock){
			return finishedOrdersCounter == numCustomers;
		}
	}

	HashSet<Integer> getNewOrders(){
		synchronized (newOrders){
			return newOrders;
		}
	}

	boolean ifHasNewOrder(){
		synchronized (newOrders){
			return !newOrders.isEmpty();
		}
	}

	Integer getNextOrder() {
		synchronized (orderPriority){
			synchronized (newOrders){
				if(!service.orderPriority.isEmpty()) {
					System.out.println("Order priority queue: "+service.orderPriority);
				}

				while(newOrders.isEmpty() && !ifAllOrderFinished()){
					if(ifAllOrderFinished()){
						return null;
					}
					try{
						newOrders.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(ifAllOrderFinished()){
						return null;
					}
				}

				Integer orderNum = -1;
				Iterator<Integer> iterator = orderPriority.iterator();

				while (iterator.hasNext()){
					orderNum = iterator.next();
					break;
				}

				orderPriority.remove(orderNum);
				if(firstPriorityBoundPointer >0) firstPriorityBoundPointer--;
				if(secondPriorityBoundPointer >0) secondPriorityBoundPointer--;

				newOrders.remove(orderNum);
				newOrders.notifyAll();
				orderPriority.notifyAll();

				if(orderNum!=-1) System.out.println("Next order is "+orderNum);

				return orderNum;

			}
		}

	}

	boolean ifInProgress(int orderNum){
		synchronized (getOrderLock(orderNum)){
			synchronized (finishedOrders){
				return !finishedOrders.contains(orderNum);
			}
		}
	}

	List<Food> getOrder(int orderNum) {
		synchronized (orderByOrderNum){
			return orderByOrderNum.get(orderNum);
		}
	}

	void recordStartTime(Customer customer){
		synchronized (getCustomerLock(customer)){
			synchronized (customerStartTimes){
				customerStartTimes.put(customer,System.currentTimeMillis());
				customerStartTimes.notifyAll();
			}
			getCustomerLock(customer).notifyAll();
		}
	}

	void recordEndTime(Customer customer){
		synchronized (getCustomerLock(customer)){
			synchronized (customerEndTimes){
				customerEndTimes.put(customer,System.currentTimeMillis());
				customerEndTimes.notifyAll();
			}
			getCustomerLock(customer).notifyAll();
		}

	}

	Long calculateTime(Customer customer){
		synchronized (getCustomerLock(customer)){
			Long startTime = 0L;
			Long endTime = 0L;
			synchronized (customerStartTimes){
				startTime = customerStartTimes.get(customer);
			}
			synchronized ((customerEndTimes)){
				endTime = customerEndTimes.get(customer);
			}
			getCustomerLock(customer).notifyAll();
			return endTime-startTime;
		}
	}

	void addTotalTime(Customer customer,Long time){
		int priority = customer.getPriority();
		synchronized (getPriorityLock(priority)){
			synchronized (priorityTotalTimes){
				priorityTotalTimes.replace(priority,priorityTotalTimes.get(priority)+time);
			}
			synchronized (priorityCounters){
				priorityCounters.replace(priority,priorityCounters.get(priority)+1);
			}
			getPriorityLock(priority).notifyAll();
		}
	}


}



