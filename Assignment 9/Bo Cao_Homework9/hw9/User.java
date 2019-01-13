package hw9;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


/**
 * Main class for your estimation actor system.
 *
 * @author akashnagesh
 *
 */
public class User extends UntypedActor {

	static ActorRef actorRefUser;
	static ActorRef actorRefFirstEstimator;
	static ActorRef actorRefSecondEstimator;
	static ActorRef actorRefCounter;

	public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create("EstimationSystem");

		/*
		 * Create the EstimatorFirst Actor and send it the StartProcessingFolder
		 * message. Once you get back the response, use it to print the result.
		 * Remember, there is only one actor directly under the ActorSystem.
		 * Also, do not forget to shutdown the actorsystem
		 */

		Props propsUser = Props.create(User.class);
		Props propsFirstEstimator = Props.create(EstimatorFirst.class);
		Props propsSecondEstimator = Props.create(EstimatorSecond.class);
		Props propsCounter = Props.create(FirstCounter.class);

		actorRefUser = system.actorOf(propsUser,"userActor");
		actorRefFirstEstimator = system.actorOf(propsFirstEstimator,"Estimator1");
		actorRefSecondEstimator = system.actorOf(propsSecondEstimator,"Estimator2");
		actorRefCounter = system.actorOf(propsCounter,"Counter");

		actorRefUser.tell("start",null);
		Thread.sleep(5000);// Wait to terminate until all process done
		system.terminate();
	}

	@Override
	public void onReceive(Object msg) throws Throwable{
		if (msg instanceof String) {
			String curMsg = (String) msg;
			if (curMsg.equals("start")) {
				System.out.println("Program started from: " + getSelf().path().name());

				for(int i=0;i<10;i++){// Reading file one by one
					System.out.println("Current testing file: " + Messages.getSingleton().fileRoute.get(i));

					try (BufferedReader br = new BufferedReader(new FileReader(Messages.getSingleton().fileRoute.get(i)))) {
						String line;
						Messages.getSingleton().fileContent.add(new ArrayList<>());
						while ((line = br.readLine()) != null) {
							Messages.getSingleton().fileContent.get(i).add(line);
						}
					} catch (Exception e) {
						System.out.println("Reading File Error: " + e);
					}
					// Send file to Counter
					System.out.println("User Sending file"+(i+1)+" to Counter");
					actorRefCounter.tell(Messages.getSingleton().fileContent.get(i),getSelf());
					Thread.sleep(100);// Sleep for a while
				}

			}

		}else if(msg instanceof Message){
			Message message = (Message) msg;

			if(message.getString().equals("result")){//Get result and print out
				System.out.println("From "+ getSender().path().name() +", Learning rate:"+message.getList().get(0)+", C(0)="+message.getList().get(1)+"%, Estimator's error over 10 steps is: "+message.getValue());
			}
		}
	}

}
