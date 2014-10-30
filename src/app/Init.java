package app;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import models.Tweet;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;

public class Init implements ServletContextListener
{
	AWSCredentials awsCredentials;
	private Thread thread = null;
	private TwitterProcessor tweetUpdater = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Cleaning up app");
		if(thread != null){
			tweetUpdater.terminate();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doCredentials();
		deleteDatabase();
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg)
	{
		System.out.println("initializing app");
		doCredentials();
		createDatabase();
		populateTable();
		
		tweetUpdater = new TwitterProcessor(new DatabaseHelper()
			.withCredentials(awsCredentials));
		thread = new Thread(tweetUpdater);
		thread.start();
		
	}
	
	private void deleteDatabase(){
		DatabaseHelper helper = new DatabaseHelper().withCredentials(awsCredentials);
		helper.removeTable();
	}
	
	private void doCredentials()
	{
		this.awsCredentials = new AWSCredentialsProviderChain(
	            new InstanceProfileCredentialsProvider(),
	            new ClasspathPropertiesFileCredentialsProvider()).getCredentials();
	}
	
	
	private void createDatabase()
	{
		DatabaseHelper helper = new DatabaseHelper()
							.withCredentials(awsCredentials);
		helper.initialize();
		
	}
	
	public void populateTable(){
		DatabaseHelper dbHelper = new DatabaseHelper().withCredentials(awsCredentials);
		String[] trends = {"#Ebola", "#Obama", "#NFL"};
		TwitterApi api = new TwitterApi();
		List<Tweet> tweets = api.getTweetsByTrendStreaming(trends);
		dbHelper.batchSaveTweets(tweets);
	}

}