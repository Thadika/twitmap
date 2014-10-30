package app;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudsearchv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;


public class DatabaseHelper
{
	private AmazonDynamoDBClient amazonDynamoDBClient = null;
	public static final String tableName = "Tweets";
	
	public DatabaseHelper withCredentials(AWSCredentials awsCredentials)
	{
		this.amazonDynamoDBClient = new AmazonDynamoDBClient(awsCredentials);
		return this;
	}
	
	public void removeTable(){
		DeleteTableRequest dtr = new DeleteTableRequest().withTableName(DatabaseHelper.tableName);
		this.amazonDynamoDBClient.deleteTable(dtr);
		
		System.out.println("Waiting for " + tableName + " while status DELETING...");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (10 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			try {
				DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
				TableDescription tableDescription = this.amazonDynamoDBClient.describeTable(request).getTable();
				String tableStatus = tableDescription.getTableStatus();
				System.out.println("  - current state: " + tableStatus);
				if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
			} catch (ResourceNotFoundException e) {
				System.out.println("Table " + tableName + " is not found. It was deleted.");
				return;
			}
			try {Thread.sleep(1000 * 20);} catch (Exception e) {}
		}
		throw new RuntimeException("Table " + tableName + " was never deleted");
	    
	}
	
	public void initialize()
	{
		System.out.println("Creating table: " + DatabaseHelper.tableName);
		try {
			ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
			attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("S"));
			//attributeDefinitions.add(new AttributeDefinition().withAttributeName("Location").withAttributeType("S"));
			//attributeDefinitions.add(new AttributeDefinition().withAttributeName("Keyword").withAttributeType("S"));

			
			ArrayList<KeySchemaElement> ks = new ArrayList<KeySchemaElement>();
			ks.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH));
			  
			ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
						    .withReadCapacityUnits(10L)
						    .withWriteCapacityUnits(10L);
			        
			CreateTableRequest request = new CreateTableRequest()
						    .withTableName(DatabaseHelper.tableName)
						    .withAttributeDefinitions(attributeDefinitions)
						    .withKeySchema(ks)
						    .withProvisionedThroughput(provisionedThroughput);
			    
			this.amazonDynamoDBClient.createTable(request);
			System.out.println("Request sent.");
			
			while (true)
			{
				System.out.println("Checking state ...");
				TableDescription tableDescription = this.amazonDynamoDBClient.describeTable(new DescribeTableRequest()
																				.withTableName(DatabaseHelper.tableName)).getTable();
				String status = tableDescription.getTableStatus();
				if (status.equals(TableStatus.ACTIVE.toString()))
				{
					System.out.println("State = " + status);
					break;
				}
				else
				{
					System.out.println("State = " + status + ". Sleeping for 10s ...");
					try{Thread.sleep(10 * 1000);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
			System.out.println("Table " + DatabaseHelper.tableName + " created.");
		}
		catch(AmazonServiceException e)
		{
			e.printStackTrace();
		}
		catch (AmazonClientException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveTweet(Tweet tweet)
	{
		//System.out.println("Saving tweet ...");
		try
		{			
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			mapper.save(tweet);			
			//System.out.println("Saved.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void batchSaveTweets(List<Tweet> tweets){
		try{
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			mapper.batchSave(tweets);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	

	public List<Tweet> getTweetsByTopic(String topic)
	{
		System.out.println("Getting all tweets by topic " + topic + "...");
		List<Tweet> scannedTweets = new ArrayList<Tweet>();
		List<Tweet> tweets = new ArrayList<Tweet>();

		try
		{
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			scanExpression.addFilterCondition("Topic", 
					new Condition()
					.withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withS(topic)));

			scannedTweets = mapper.scan(Tweet.class, scanExpression);
			System.out.println("Retrieved " + scannedTweets.size() + " record(s).");
			tweets.addAll(scannedTweets);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tweets;
	}
	
	public List<Tweet> getAllTweets()
	{
		System.out.println("Getting all tweets ...");
		List<Tweet> scannedTweets = new ArrayList<Tweet>();
		List<Tweet> tweets = new ArrayList<Tweet>();

		try
		{			
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
						
			scannedTweets = mapper.scan(Tweet.class, scanExpression);
			System.out.println("Retrieved " + scannedTweets.size() + " record(s).");
			
			tweets.addAll(scannedTweets);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tweets;
	}
	
	public List<String> getTweetLocations(List<Tweet> tweets) {
		List<String> locations = new ArrayList<String>();
		for(int i=0;i<tweets.size();i++) {
			locations.add(tweets.get(i).getLocation());
		}
		return locations;
	}
	
}