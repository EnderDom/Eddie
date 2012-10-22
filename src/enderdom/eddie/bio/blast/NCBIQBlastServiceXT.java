package enderdom.eddie.bio.blast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.biojava3.ws.alignment.RemotePairwiseAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;

/**
 * 
 * This is just the NCBIQBlastService 
 *
 */

public class NCBIQBlastServiceXT extends NCBIQBlastService {

	private HashMap<String, Long> holder;
	private long step;
	private long start;
	
	private URL aUrl;
	private URLConnection uConn;
	private OutputStreamWriter fromQBlast;
	private BufferedReader rd;
	private String errorstring;
	private static String baseurl = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi";
	
	private String rid;
	
	public NCBIQBlastServiceXT() throws Exception {
		try {
			this.aUrl = new URL(baseurl);
			this.uConn = setQBlastServiceProperties(aUrl.openConnection());
			this.holder = new HashMap<String, Long>();
		}
		/*
		 * Needed but should never be thrown since the URL is static and known
		 * to exist
		 */
		catch (MalformedURLException e) {
			throw new Exception(
					"It looks like the URL for NCBI QBlast service is wrong.\n");
		}
		/*
		 * Intercept if the program can't connect to QBlast service
		 */
		catch (IOException e) {
			throw new Exception(
					"Impossible to connect to QBlast service at this time. Check your network connection.\n");
		}
	}
	
	private String sendActualAlignementRequest(String str,
			RemotePairwiseAlignmentProperties rpa) throws Exception {
		String rid = super.sendAlignmentRequest(str, rpa);
		holder.put(rid, start);
		return rid;
	}
	
	public String sendAlignmentRequest(String str,
			RemotePairwiseAlignmentProperties rpa) throws Exception {

		/*
		 * sending the command to execute the Blast analysis
		 */
		return setRid(sendActualAlignementRequest(str, rpa));
	}

	public int isReadyOrErrd(String id, long present) throws Exception {
		int isReady = 0;
		String check = "CMD=Get&RID=" + id;

		
		if (holder.containsKey(id)) {
			/*
			 * If present time is less than the start of the search added to
			 * step obtained from NCBI, just do nothing ;-)
			 * 
			 * This is done so that we do not send zillions of requests to the
			 * server. We do the waiting internally first.
			 */
			if (present < start) {
				isReady = 0;
			}
			/*
			 * If we are at least step seconds in the future from the actual
			 * call sendAlignementRequest()
			 */
			else {
				try {
					uConn = setQBlastServiceProperties(aUrl.openConnection());

					fromQBlast = new OutputStreamWriter(uConn.getOutputStream());
					fromQBlast.write(check);
					fromQBlast.flush();

					rd = new BufferedReader(new InputStreamReader(uConn
							.getInputStream()));

					String line = "";

					while ((line = rd.readLine()) != null) {
						if (line.contains("READY")) {
							isReady = 1;
						} 
						else if (line.contains("WAITING")) {
							/*
							 * Else, move start forward in time... for the next
							 * iteration
							 */
							start = present + step;
							holder.put(id, start);
						}
						else if (line.contains("ERROR")){
							isReady = -2;
							this.errorstring = line;
						}
						else if (line.contains("UNKNOWN")) {
							throw new IllegalArgumentException("Unknown request id - no results exist for it. Given id = " + id);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new Exception("Impossible to check for request ID named "
					+ id + " because it does not exists!\n");
		}
		return isReady;
	}
	
	public String getErrorString(){
		return errorstring;
	}
	
	private URLConnection setQBlastServiceProperties(URLConnection conn) {

		URLConnection tmp = conn;

		conn.setDoOutput(true);
		conn.setUseCaches(false);

		tmp.setRequestProperty("User-Agent", "Biojava/NCBIQBlastService");
		tmp.setRequestProperty("Connection", "Keep-Alive");
		tmp.setRequestProperty("Content-type",
				"application/x-www-form-urlencoded");
		tmp.setRequestProperty("Content-length", "200");

		return tmp;
	}

	public String getRid() {
		return rid;
	}

	public String setRid(String rid) {
		this.rid = rid;
		return rid;
	}
	
}
