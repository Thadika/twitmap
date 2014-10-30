package app;
import java.util.ArrayList;
import java.util.List;

import models.Tweet;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.api.TrendsResources;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterApi {
	private static final String CONSUMER = "****";
	private static final String CONSUMER_SECRET = "***";
	private static final String ACCESS_TOKEN = "****";
	private static final String ACCESS_TOKEN_SECRET = "****";
	
	private Twitter twitter;
	private TwitterStream twitterStream;
	private final Object lock = new Object();
	
	public TwitterApi(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
		  .setOAuthConsumerKey(CONSUMER)
		  .setOAuthConsumerSecret(CONSUMER_SECRET)
		  .setOAuthAccessToken(ACCESS_TOKEN)
		  .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
		Configuration confg = cb.build();
		TwitterFactory tf = new TwitterFactory(confg);
		TwitterStreamFactory tsf = new TwitterStreamFactory(confg);
		
		twitter = tf.getInstance();
		twitterStream = tsf.getInstance();
	}
	
	
	public List<String> getTrendsByLocation(int woeid){ // use 1 for global
		TrendsResources trendResource = this.twitter.trends();
		List<String> result = new ArrayList<String>();
		try{
			Trends trends = trendResource.getPlaceTrends(woeid);
			for(Trend t : trends.getTrends()){
				result.add(t.getName());
			}
		}catch(Exception e){
			e.printStackTrace(); //Change this later
		}
		return result;
	}
	
	public List<String> getTweetsByHashtag(String hashtag){
		Query searchq = new Query();
		searchq.setQuery(hashtag);
		//searchq.setResultType(ResultType.recent);
		searchq.setCount(100);
		List<String> result = new ArrayList<String>();
		
		try{
			QueryResult queryResult = this.twitter.search(searchq);
			for(Status s : queryResult.getTweets()){
				if(s.getGeoLocation() != null){
					result.add(s.getGeoLocation().getLatitude() + "," + s.getGeoLocation().getLongitude());
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public List<Tweet> getTweetsByTrendStreaming(String[] trends){
		final List<Tweet> tweets = new ArrayList<Tweet>();
		System.out.println("size of statuses: " + tweets.size());
		MyStatusListener listener = new MyStatusListener(){
			private int count = 0;
			private String keyword;
			
			@Override
			public void onException(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
			}

			@Override
			public void onScrubGeo(long userId, long userStatusId) {
			}

			@Override
			public void onStallWarning(StallWarning sw) {
			}

			@Override
			public void onStatus(Status status) {
				//System.out.println(status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude());
				if(status.getGeoLocation() != null){
					tweets.add(new Tweet().withKeyword(this.keyword).withLocation(status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude()).withCreated(status.getCreatedAt()));
					count++;
				}
				if(count > 300){
					System.out.println(tweets.size());
					synchronized (lock){
						lock.notify();
					}
					count = 0;
					System.out.println("unlocked");
				}
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}
			
			@Override
			public void setKeyword(String key){
				this.keyword = key;
			}
		};
		
		twitterStream.addListener(listener);

		FilterQuery filter = new FilterQuery();
		double[][] locations = {{-180.0d,-90.0d},{180.0d,90.0d}};
		filter.locations(locations);
		
		for(String trend: trends){
			listener.setKeyword(trend);
			
			String[] keywords = {trend};
			filter.track(keywords);
	
			twitterStream.filter(filter);
			try {
				System.out.println("waiting for lock");
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		twitterStream.removeListener(listener);
		twitterStream.shutdown();
		
		//System.out.println("Got Statuses (" + statuses.size() +")");
		return tweets;
	}
	
	public interface MyStatusListener extends StatusListener{
		public void setKeyword(String key);
		
	}
	
}
