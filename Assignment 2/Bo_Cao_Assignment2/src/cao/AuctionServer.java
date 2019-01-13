package cao;

/**
 *  @author YOUR NAME SHOULD GO HERE
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected. 
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	private int soldItemsCount = 0;
	private int revenue = 0;

	public int soldItemsCount()
	{
		return this.soldItemsCount;
	}

	public int revenue()
	{
		return this.revenue;
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.


	/* Statistic variables and server constants: End code you should likely leave alone. */



	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */

	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();


	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1; 

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 




	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();



	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	private static final Object itemsUpForBiddingLock = new Object();


	/*
	 *  The code from this point forward can and should be changed to correctly and safely 
	 *  implement the methods as needed to create a working multi-threaded server for the 
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */


	private HashMap<String,Boolean> biaSeller = new HashMap<String,Boolean>();

	public HashMap<String, Boolean> getBiaSeller() {
		return biaSeller;
	}

	// INVARIANT: Listing ID must be unique, and each hash map must be unique.
	//            The size of itemsUpForBidding should not be more than serverCapacity.

	// Precondition: Checking if the listing ID is unique and checking if the seller exist and the seller item number limit.
	// Postcondition: Add item into list and return the listing ID if successfully.
	// Exception: Return -1 instead.
	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */
	public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   Make sure there's room in the auction site.
		//   If the seller is a new one, add them to the list of sellers.
		//   If the seller has too many items up for bidding, don't let them add this one.
		//   Don't forget to increment the number of things the seller has currently listed.

        //	 Locking: itemsUpForBidding should be locked when creating new item.
		synchronized (itemsUpForBiddingLock) {

			if (itemsUpForBidding.size() < serverCapacity){

				if(!itemsPerSeller.containsKey(sellerName)){
					// add the seller into the list of sellers
					itemsPerSeller.put(sellerName,0);


					int num = (int)((Math.random())*10);// create random number
					if (num<2){// set vip bias to seller randomly
						biaSeller.put(sellerName,true);// seller has vip
					}else{
						biaSeller.put(sellerName,false);// seller does not have vip
					}
				}

				if(itemsPerSeller.get(sellerName) < maxSellerItems){
					// put item into list
					// number of thins the seller has increased
					lastListingID++;
					Item item = new Item(sellerName,itemName,lastListingID,lowestBiddingPrice,biddingDurationMs);
					itemsUpForBidding.add(item);
					itemsAndIDs.put(lastListingID,item);

					highestBids.put(lastListingID,lowestBiddingPrice);
					itemsPerSeller.put(sellerName,itemsPerSeller.get(sellerName)+1);

					return lastListingID;

				}
			}

			return -1;
		}
	}

	// Precondition: None
	// Postcondition: Return the list of the items
	// Exception: None
	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//    Don't forget that whatever you return is now outside of your control.
		//	 Locking: itemsUpForBidding should be locked.

		synchronized (itemsUpForBiddingLock) {
			List<Item> itemTemp = new ArrayList<Item>();
			for (Item item : itemsUpForBidding){
				int listID = item.listingID();
				if(item.biddingOpen()){
					if(biaSeller.get(item.seller())){// If seller is vip, his item will be put at top
						itemTemp.add(0,new Item(item.seller(),item.name(),listID,item.lowestBiddingPrice(),item.biddingDurationMs()));
						System.out.println(item.name()+" has been put at the top, its seller: "+ item.seller());
					}else{// If seller is not vip, item will be put at the last position
						itemTemp.add(new Item(item.seller(),item.name(),listID,item.lowestBiddingPrice(),item.biddingDurationMs()));
					}

				}
			}
			return itemTemp;
		}
	}


	// Precondition: Checking if the item exist and if it can be bid.
	// Postcondition: Return true if it bid successfully.
	// Exception: Return false instead.
	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	public boolean submitBid(String bidderName, int listingID, int biddingAmount) {
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   See if the item exists.
		//   See if it can be bid upon.
		//   See if this bidder has too many items in their bidding list.
		//   Get current bidding info.
		//   See if they already hold the highest bid.
		//   See if the new bid isn't better than the existing/opening bid floor.
		//   Decrement the former winning bidder's count
		//   Put your bid in place
		//	 Locking: itemsUpForBidding should be locked.

		synchronized (itemsUpForBiddingLock) {
			if(itemsAndIDs.containsKey(listingID)){
				Item item = itemsAndIDs.get(listingID);

				if(item!=null&&item.biddingOpen()){
					if(!itemsPerBuyer.containsKey(bidderName)){
						itemsPerBuyer.put(bidderName,0);
					}

					if(itemsPerBuyer.get(bidderName) < maxBidCount){
						if(!highestBidders.containsKey(bidderName)||highestBidders.get(listingID).equals(bidderName)){
							if(highestBids.containsKey(listingID) && highestBids.get(listingID) < biddingAmount){
								String priviousBidder = highestBidders.get(listingID);

								if(priviousBidder != null && itemsPerBuyer.containsKey(priviousBidder)){
									itemsPerBuyer.put(priviousBidder,itemsPerBuyer.get(priviousBidder)-1);
								}

								highestBids.put(listingID,biddingAmount);
								highestBidders.put(listingID,bidderName);
								itemsPerBuyer.put(bidderName,itemsPerBuyer.get(bidderName)+1);

								return true;

							}
						}
					}
				}
			}


			return false;
		}
		

	}

	// Precondition: None.
	// Postcondition: Return status code.
	// Exception: Return status code 3 as failed.
	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 2 (open) if this <code>Item</code> is still up for auction<br>
	 * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public int checkBidStatus(String bidderName, int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   If the bidding is closed, clean up for that item.
		//     Remove item from the list of things up for bidding.
		//     Decrease the count of items being bid on by the winning bidder if there was any...
		//     Update the number of open bids for this seller

		int bidStatus = 3;

		synchronized (itemsUpForBiddingLock){
			Item item = itemsAndIDs.get(listingID);

			if(item != null){
				if(!item.biddingOpen()){// bidding is not in run
					itemsUpForBidding.remove(item);

					String sellerName = item.seller();
					itemsPerSeller.put(sellerName, itemsPerSeller.get(sellerName)-1);

					if(highestBidders.containsKey(listingID)){// bid exist
						String highestBidder = highestBidders.get(listingID);

						if(highestBidder.equals(bidderName)){
							revenue = revenue() + highestBids.get(listingID);
							soldItemsCount = soldItemsCount() + 1;
							bidStatus = 1;
						}else{ // bid not win
							bidStatus = 3;
						}
					}else{// bid does not exist
						bidStatus = 3;
					}

				}else{// bidding is open
					bidStatus = 2;
				}
			}
			else{// item does not exist
				bidStatus = 3;
			}
		}

		return bidStatus;
	}

	// Precondition: Listing ID should be exist.
	// Postcondition: Return the highest bid or the opening price if no bid.
	// Exception: Return -1.
	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if no bid has been made,
	 * -1 if no <code>Item</code> exists
	 */
	public int itemPrice(int listingID)
	{
		// TODO: IMPLEMENT CODE HERE

		synchronized (itemsUpForBiddingLock){
			Item item = itemsAndIDs.get(listingID);

			if (item != null){
				return highestBids.get(listingID);
			}else{
				return -1;
			}
		}

	}

	// Precondition: Listing ID should be exist.
	// Postcondition: Return true if there is no bid.
	// Exception: Return false.
	/**
	 * Check whether an <code>Item</code> has been bid upon yet
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public Boolean itemUnbid(int listingID)
	{
		// TODO: IMPLEMENT CODE HERE

		synchronized (itemsUpForBiddingLock){
			Item item = itemsAndIDs.get(listingID);

			if(item!= null && highestBidders.containsKey(listingID)){
				return false;
			}else{
				return true;
			}
		}

	}

	public int itemsUpForBiddingSize()
	{
		return itemsUpForBidding.size();
	}

}
 