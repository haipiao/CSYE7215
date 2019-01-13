package hw9;

import akka.actor.UntypedActor;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the main actor and the only actor that is created directly under the
 * {@code ActorSystem} This actor creates more child actors
 * {@code WordCountInAFileActor} depending upon the number of files in the given
 * directory structure
 *
 *
 */
public class EstimatorSecond extends UntypedActor {

    private List<Double> feedbackList; // U(t)
    private List<Double> estPercent;// C(t)
    private List<Double> accPercent; // C'(t)
    private double learningRate; // g

    public EstimatorSecond() {
        feedbackList = new ArrayList<>();
        estPercent = new ArrayList<>();
        accPercent = new ArrayList<>();
        estPercent.add(34.0); // In percentage
        learningRate = 0.0;// Setting learning rate

    }


    @Override
    public void onReceive(Object msg) throws Throwable {

        if(msg instanceof Message){
            Message message = (Message) msg;

            if(message.getString().equals("getPercent")){// Sending C(t) to Counter
                //System.out.println("Estimator2-Ct size: "+estPercent.size()+" index: "+message.getIndex());
                User.actorRefCounter.tell(new Message(message.getIndex(),estPercent.get(message.getIndex()),"ct",null),getSelf());
            }else if(message.getString().equals("sendFeedback")){
                feedbackList.add(message.getValue());
                Double accP = estPercent.get(message.getIndex())+ message.getValue();
                System.out.println("From Estimator2, C'(t="+(message.getIndex()+1)+")="+accP+"%");
                accPercent.add(accP);// Get C'(t)
                Double newCt = estPercent.get(message.getIndex())* learningRate + (1-learningRate)*accPercent.get(message.getIndex());// Calculate C(t+1)
                System.out.println("From Estimator2, C(t="+(message.getIndex()+2)+")="+newCt+"%");
                estPercent.add(newCt);

                if(feedbackList.size()==10) calculateS();// Calculate S(n=10)

            }
        }

    }

    private void calculateS(){
        Double sum =0.0;
        for( Double f: feedbackList){
            sum = sum + f;
        }
        Double s = sum/feedbackList.size();

        List<Double> list =new ArrayList<>();
        list.add(learningRate);
        list.add(estPercent.get(0));
        // Send result from Estimator2 to User
        User.actorRefUser.tell(new Message(10,s,"result",list),getSelf());

    }
}
