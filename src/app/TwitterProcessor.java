package app;

import java.util.List;

import models.Tweet;


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
				List<Tweet> tweets = api.getTweetsByTrendStreaming(trends);
				dbHelper.batchSaveTweets(tweets);
				
			}catch(InterruptedException e){
				running = false;
			}
		}

	}

}
