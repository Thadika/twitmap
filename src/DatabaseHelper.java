import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;


public class DatabaseHelper
{
	private AmazonDynamoDBClient amazonDynamoDBClient = null;
	public static final String tableName = "bucketoftweets";
	
	public DatabaseHelper withCredentials(AWSCredentials awsCredentials)
	{
		this.amazonDynamoDBClient = new AmazonDynamoDBClient(awsCredentials);
		return this;
	}
	
	public void initialize()
	{
		System.out.println("Creating table: " + DatabaseHelper.tableName);
		try {
			ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
			attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("S"));
			
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
	
	public void saveTweet(String tweet)
	{
		System.out.println("Saving tweet ...");
		try
		{			
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			mapper.save(tweet);			
			System.out.println("Saved.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public List<String> getTweetsByTopic(String topic)
	{
		System.out.println("Getting all tweets by topic ...");
		List<String> scannedTweets = new ArrayList<String>();
		List<String> tweets = new ArrayList<String>();
		try
		{
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			Map<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition scanCondition = new Condition()
			.withComparisonOperator(ComparisonOperator.EQ.toString())
			.withAttributeValueList(new AttributeValue().withS(topic));

			scanFilter.put("Topic", scanCondition);

			scanExpression.setScanFilter(scanFilter);

			scannedTweets = mapper.scan(String.class, scanExpression);
			System.out.println("Retrieved " + scannedTweets.size() + " record(s).");
			tweets.addAll(scannedTweets);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tweets;
	}
	
	public List<String> getAllTweets()
	{
		System.out.println("Getting all tweets ...");
		List<String> scannedTweets = new ArrayList<String>();
		List<String> tweets = new ArrayList<String>();
		try
		{			
			DynamoDBMapper mapper = new DynamoDBMapper(this.amazonDynamoDBClient);
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			
			scannedTweets = mapper.scan(String.class, scanExpression);
			System.out.println("Retrieved " + scannedTweets.size() + " record(s).");
			
			tweets.addAll(scannedTweets);
			
			if (tweets.size() > 0)
			{
				Collections.sort(tweets);
				Collections.reverse(tweets);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tweets;
	}
	
}