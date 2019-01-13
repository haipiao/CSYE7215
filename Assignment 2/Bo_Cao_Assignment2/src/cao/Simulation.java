package cao;


/**
 * Class provided for ease of test. This will not be used in the project 
 * evaluation, so feel free to modify it as you like.
 */ 
public class Simulation
{
    // Invariants: Each threads of seller and bidder should have its own separate work and not.

    // Bias: When the seller takes the advantage, his selling list will be shown to the buyer at the top markable position
    // and the list sequence is based on the point of the bias point.
    public static void main(String[] args)
    {                
        int nrSellers = 50;
        int nrBidders = 20;
        
        Thread[] sellerThreads = new Thread[nrSellers];
        Thread[] bidderThreads = new Thread[nrBidders];
        Seller[] sellers = new Seller[nrSellers];
        Bidder[] bidders = new Bidder[nrBidders];
        
        // Start the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            sellers[i] = new Seller(
            		AuctionServer.getInstance(), 
            		"Seller"+i, 
            		100, 50, i
            );
            sellerThreads[i] = new Thread(sellers[i]);
            sellerThreads[i].start();
        }
        
        // Start the buyers
        for (int i=0; i<nrBidders; ++i)
        {
            bidders[i] = new Bidder(
            		AuctionServer.getInstance(), 
            		"Buyer"+i, 
            		1000, 20, 150, i
            );
            bidderThreads[i] = new Thread(bidders[i]);
            bidderThreads[i].start();
        }
        
        // Join on the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // Join on the bidders
        for (int i=0; i<nrBidders; ++i)
        {
            try
            {
                bidderThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // TODO: Add code as needed to debug

        AuctionServer server = AuctionServer.getInstance();

        int bidderAmount =0;
        for(Bidder b : bidders){
            bidderAmount = bidderAmount + b.cashSpent();
        }

		System.out.println("Bidder Amount: "+bidderAmount);
		System.out.println("Total Revenue: "+server.revenue());

        if(bidderAmount == server.revenue()){
            System.out.println("Auction Server: Revenue Success!");
        }else{
            System.out.println("Auction Server: Revenue Failure!");
        }

        System.out.println("Sold item count: "+server.soldItemsCount());



    }
}