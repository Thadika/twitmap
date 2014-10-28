import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;

public class Init
{
	AWSCredentials awsCredentials;
	TwitterApi api;

	public static void main(String[] args)
	{
		System.out.println("initializing app");
		Init init = new Init();
		
		init.doCredentials();
		//init.createDistribution();
		//init.createDatabase();
		//init.populateTable();
		init.initializeTwitterApi();
	}
	
	public void doCredentials()
	{
		this.awsCredentials = new AWSCredentialsProviderChain(
	            new InstanceProfileCredentialsProvider(),
	            new ClasspathPropertiesFileCredentialsProvider()).getCredentials();
	}
	
	
	public void createDistribution()
	{
		OnDemandDistributor distributor = new OnDemandDistributor()
							.withAWSCredentials(awsCredentials);
		distributor.createWebDistribution();
		distributor.createRtmpDistribution();
	}
	
	public void createDatabase()
	{
		DatabaseHelper helper = new DatabaseHelper()
							.withCredentials(awsCredentials);
		helper.initialize();
		
	}
	
	public void initializeTwitterApi(){
		this.api = new TwitterApi();
	}
	
	public void populateTable(){
		DatabaseHelper helper = new DatabaseHelper().withCredentials(awsCredentials);
		helper.saveTweet(new Tweet().withKeyword("Ebola").withLocation("-22.9083081,-43.1970258"));
		helper.saveTweet(new Tweet().withKeyword("Obama").withLocation("40.7056308,-73.9780035"));
		helper.saveTweet(new Tweet().withKeyword("Ebola").withLocation("48.8588589,2.3470599"));
		helper.saveTweet(new Tweet().withKeyword("NFL").withLocation("51.5073509,-0.1277583"));
		helper.saveTweet(new Tweet().withKeyword("NFL").withLocation("41.39479,2.1487679"));
		helper.saveTweet(new Tweet().withKeyword("NFL").withLocation("3.139003,101.686855"));
	}
	
	

}