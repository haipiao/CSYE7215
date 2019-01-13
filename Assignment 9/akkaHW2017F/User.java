package akkaHW2017F;

import akka.actor.ActorSystem;

/**
 * Main class for your estimation actor system.
 *
 * @author akashnagesh
 *
 */
public class User {

	public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create("EstimationSystem");

		/*
		 * Create the Estimator Actor and send it the StartProcessingFolder
		 * message. Once you get back the response, use it to print the result.
		 * Remember, there is only one actor directly under the ActorSystem.
		 * Also, do not forget to shutdown the actorsystem
		 */

	}

}
