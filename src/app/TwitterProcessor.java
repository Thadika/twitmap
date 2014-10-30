import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;


public class TwitterProcessor implements Runnable {
	private final TwitterApi api = new TwitterApi();
	private volatile boolean running = true;
	private final DatabaseHelper dbHelper;
	private final String[] trends = {"#Obama", "#Ebola", "#NFL"};
	
	public TwitterProcessor(DatabaseHelper dbHelper){
		this.dbHelper = dbHelper;
	}
	
	public void terminate(){
		running = false;
	}
	
	@Override
	public void run() {
		while(running){
			try{
				//Sleep for 5 minutes before updating with most recent tweets again
				Thread.sleep(60*5*1000);
				//update dynamoDb
				for(String keyword : trends){
					List<Status> tweets = api.getTweetsByTrendStreaming(keyword);
					//System.out.println("Got tweets for trend" + s + " (" + tweets.size() +")");
					List<Tweet> tweetModels = new ArrayList<Tweet>();
					
					for(Status stts : tweets){
						
						tweetModels.add(new Tweet().withKeyword(keyword).withLocation(stts.getGeoLocation().getLatitude()+","+stts.getGeoLocation().getLongitude()).withCreated(stts.getCreatedAt()));
					}
					dbHelper.batchSaveTweets(tweetModels);
				}
				
			}catch(InterruptedException e){
				running = false;
			}
		}

	}

}
