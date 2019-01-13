package hw9;

import java.util.ArrayList;
import java.util.List;

/**
 * Messages that are passed around the actors are usually immutable classes.
 * Think how you go about creating immutable classes:) Make them all static
 * classes inside the Messages class.
 * 
 * This class should have all the immutable messages that you need to pass
 * around actors. You are free to add more classes(Messages) that you think is
 * necessary
 * 
 * @author akashnagesh
 *
 */
public class Messages {

    public static List<List<String>> fileContent;
    public List<String> fileRoute;
    public Integer counter;

    private static Messages singleton = null;

    public Messages() {
        fileContent = new ArrayList<>();
        fileRoute = new ArrayList<>();
        for(int i=1;i<=10;i++){
            fileRoute.add("data/Akka"+i+".txt");
        }
        counter = 0;
    }

    public static Messages getSingleton() {
        if(singleton == null){
            singleton = new Messages();
        }
        return singleton;
    }
}