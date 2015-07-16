import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Uploader {

	public static void main(String[] args) throws AuthenticationException, ClientProtocolException, IOException, ParseException, JSONException {
		String token="";
		
		//Setup the auth
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("yourUser", "yourPass");
		provider.setCredentials(AuthScope.ANY, credentials);
		HttpClient clientGet = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		
		//Get a license
		HttpGet getter = new HttpGet("http://10.211.55.3/square9api/api/licenses?format=json");
		HttpResponse licResponse = clientGet.execute(getter);
		
		JSONObject content = new JSONObject( EntityUtils.toString(licResponse.getEntity()));
		token = content.getString("Token");
		
		//Send a file to the server's WebPortal Cache
		HttpPost httpPost = new HttpPost("http://10.211.55.3/square9api/api/files");
		
		String pdfFileName = "/Users/DevUser/Downloads/salesInvoiceCustomized.pdf"; //Static test file
		InputStream inputStream = new FileInputStream(pdfFileName);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addPart("File", new FileBody(new File(pdfFileName)));
		HttpEntity entity = builder.build();
		httpPost.setEntity(entity);
		HttpResponse fileResponse = clientGet.execute(httpPost);
		
		//We need the server's file name, so get that out of the data returned.
		content = new JSONObject( EntityUtils.toString(fileResponse.getEntity()));
		JSONArray fileArray = content.getJSONArray("files");
		JSONObject fileElement = (JSONObject)fileArray.get(0);
		String fileNameString = fileElement.getString("name");
		
		//Create the JSON for indexing the file.
		StringEntity entity1 =  new StringEntity("{\"fields\":[{\"name\":\"7\",\"value\":\"Invoice 123\"}],\"files\":[{\"name\":\""+ fileNameString + "\"}]}");
		
		//Send it
		HttpPost indexPost = new HttpPost("http://10.211.55.3/square9api/api/dbs/27/archives/1005?token=" + token);
		indexPost.addHeader("content-type", "application/json");
		indexPost.setEntity(entity1);
		HttpResponse indexResponse = clientGet.execute(indexPost);
	}

}
