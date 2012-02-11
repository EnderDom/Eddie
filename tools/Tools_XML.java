package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Tools_XML {
	
	final public static Document inputStreamToDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();    
        Document doc = db.parse(inputStream);
        doc.getDocumentElement().normalize();
        return doc;
	}
	
	final public static boolean xmlWeb2File(String Url, String filename){
		FileWriter fstream;
		BufferedWriter out;
		boolean complete = false;
	
		try {
			fstream = new FileWriter(filename);
			out = new BufferedWriter(fstream);
			try{
				URLEncoder.encode(Url, "UTF-8");
			}
			catch(UnsupportedEncodingException encod){
				System.out.println("Encoding Exception in method urlReader." + encod.getMessage());
				
			}
			URL site = new URL(Url);
			BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				out.write(inputLine);
			}
			in.close();
			out.close();
			complete = true;
		}
		catch (IOException e) {		
			e.printStackTrace();
		}
		return complete;
 	}
 
 	public static void Xml2File(Document document, File file){
        Source src = new DOMSource(document);
        try {
        	FileOutputStream outStream = new FileOutputStream(file);
        	Result result = new StreamResult(outStream);
        	Transformer xformer;
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(src, result);
			outStream.close();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
}
