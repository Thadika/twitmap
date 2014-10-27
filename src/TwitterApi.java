import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.api.TrendsResources;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterApi {
	private static final String CONSUMER = "5DVp2sh4PCJnib1X84P1DFSPM";
	private static final String CONSUMER_SECRET = "t7VwvP48WKVKyTfxdX3AG2OKG6l0EqmGnoF6shEmYtM1gRPjXA";
	private static final String ACCESS_TOKEN = "600561617-e9MMZLJUkra2SPNnjb7YtIMSyTx5GiKUdfE06EWM";
	private static final String ACCESS_TOKEN_SECRET = "N7PbwMUeYzUjhYNnIdqzKBTzygY1DUbkSiUa3U8L1kfeS";
	
	private Twitter twitter;
	
	
	public TwitterApi(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
		  .setOAuthConsumerKey(CONSUMER)
		  .setOAuthConsumerSecret(CONSUMER_SECRET)
		  .setOAuthAccessToken(ACCESS_TOKEN)
		  .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		
		twitter = tf.getInstance();
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
//				else{
//					//get location for the user who posted it
//					//TODO: scrape location of user, though mostly trash
//					result.add("N/A");
//				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	
}
