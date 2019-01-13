package akkaHW2017F;

import java.io.File;

import akka.actor.UntypedActor;

/**
 * This is the main actor and the only actor that is created directly under the
 * {@code ActorSystem} This actor creates more child actors
 * {@code WordCountInAFileActor} depending upon the number of files in the given
 * directory structure
 * 
 * @author akashnagesh
 *
 */
public class WordCountActor extends UntypedActor {

	public WordCountActor() {

	}

	@Override
	public void onReceive(Object msg) throws Throwable {

	}

}
