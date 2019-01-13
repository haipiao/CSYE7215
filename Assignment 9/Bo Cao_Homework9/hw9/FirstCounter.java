package hw9;

import akka.actor.UntypedActor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * this actor reads the file, counts the vowels and sends the result to
 * EstimatorFirst.
 *
 * @author akashnagesh
 *
 */
public class FirstCounter extends UntypedActor {

	private static int countOfreceiving = 0;
	private static int indexCounter = 0;
	private List<Double> percent;// P(t)

	public FirstCounter() {
		// TODO Auto-generated constructor stub
		this.percent = new ArrayList<>();
	}

	@Override
	public void onReceive(Object msg) throws Throwable {

		if (msg instanceof List<?>){// Reading file from user actor
			System.out.println("Counter received message x " + ++countOfreceiving + " times");
			List<String> file = (List<String> )msg;
			Integer vowelsCounter = 0; // V(t)
			Integer wordsCounter = 0; // L(t)
			for(String s: file){
				wordsCounter = wordsCounter + s.length();
				for(int i=0; i<s.length(); i++){
					if(isVowel(s.charAt(i))) vowelsCounter++;

				}
			}

			//System.out.println(vowelsCounter);
			//System.out.println(wordsCounter);
			double result = (double) vowelsCounter * 100 / wordsCounter;

			percent.add(result); // P(t)=V(t)/L(t) %

			//System.out.println("p("+countOfreceiving+")="+percent.get(countOfreceiving-1));

			// Sending query to Estimator1 and Estimator2
			User.actorRefFirstEstimator.tell(new Message(indexCounter,0.0,"getPercent",null),getSelf());
			User.actorRefSecondEstimator.tell(new Message(indexCounter,0.0,"getPercent",null),getSelf());
			indexCounter++;

		}else if(msg instanceof Message){
			Message message = (Message) msg;

			if(message.getString().equals("ct")){// Reading C(t) from estimator;
				if(getSender() != null && getSender().path().name().equals("Estimator1")){
					System.out.println("Receiving t="+(message.getIndex()+1)+" from "+getSender().path().name());
					Double feedback = 0.0;
					feedback = percent.get(message.getIndex()) - message.getValue();

					//Sending feedback to Estimator1
					System.out.println("Sending feedback to Estimator1: "+ feedback);
					User.actorRefFirstEstimator.tell(new Message(message.getIndex(),feedback,"sendFeedback",null),getSelf());
				}else if(getSender() != null && getSender().path().name().equals("Estimator2")){
					System.out.println("Receiving t="+(message.getIndex()+1)+" from "+getSender().path().name());
					Double feedback = 0.0;
					feedback = percent.get(message.getIndex()) - message.getValue();

					//Sending feedback to Estimator2
					System.out.println("Sending feedback to Estimator2: "+ feedback);
					User.actorRefSecondEstimator.tell(new Message(message.getIndex(),feedback,"sendFeedback",null),getSelf());
				}
			}


		}


	}

	private boolean isVowel(char c) {
		char[] vowels = {'A', 'E', 'I', 'O', 'U', 'Y','a','e','i','o','u','y'};
		for (char vowel : vowels) {
			if (c == vowel) return true;
		}
		return false;
	}
}
