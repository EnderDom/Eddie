package enderdom.eddie.tools.bio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.EddiePropertyLoader;

@SuppressWarnings("deprecation")
public class Tools_TAIR {

	private static String genedecDatabase = "repgene";
	private static String genewebsite = "http://www.arabidopsis.org/cgi-bin/bulk/genes/gene_descriptions";
	
	/**
	 * 
	 * @param geneid of the form AT4G36430.1
	 * @return String description of the gene
	 */
	public static String getGeneDescription(String geneid){	
		StringBuffer buffer = new StringBuffer();
		String newl = Tools_System.getNewline();
		Logger logger = EddiePropertyLoader.logger;
		try{
			logger.debug("Building multipart entity");
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);			
			File fileToUpload = File.createTempFile("toupload", ".txt");
			FileBody fileBody = new FileBody(fileToUpload, "application/octet-stream");
			entity.addPart("search_for", new StringBody(geneid));
			entity.addPart("file", fileBody);
			entity.addPart("search_against", new StringBody(genedecDatabase));
			entity.addPart("output_type", new StringBody("text"));
	
			Logger.getRootLogger().debug("Accessing TAIR website...");
			HttpPost httpPost = new HttpPost(genewebsite);
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity result = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(result.getContent()));
			String line = null;
			while((line=reader.readLine())!=null){
				buffer.append(line);
				buffer.append(newl);
			}
			reader.close();
			httpClient.getConnectionManager().shutdown();
		}
		catch(UnsupportedEncodingException e){
			logger.error("Failed to retrieve data from TAIR database", e);
		} catch (ClientProtocolException e) {
			logger.error("Failed to retrieve data from TAIR database", e);
		} catch (IOException e) {
			logger.error("Failed to retrieve data from TAIR database", e);
		}
		return buffer.toString();
	}
	
}
