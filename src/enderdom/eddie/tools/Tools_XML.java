package enderdom.eddie.tools;

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
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Tools_XML {
	
	public static int indent = 2;
	
	
	//public static String[] getTagContentsfromURL(URL site, String[] listoftags){
	//}
	
	/**
	 * Very simple loads xml from URL
	 * and spits out the first contents
	 * of a tag which matches param tag
	 * 
	 * simples.
	 * 
	 * @param site
	 * @param tag
	 * @return
	 * @throws XMLStreamException
	 */
	public static String getSingleTagFromURL(URL site, String tag) throws XMLStreamException{
		XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
	    XMLStreamReader2 stream = (XMLStreamReader2) f.createXMLStreamReader(site);
		while(stream.hasNext()){
		    stream.next();
		    if(stream.isStartElement()){
		    	String xmtag = stream.getName().toString();
				if(xmtag.equals(tag)){
					String ret = stream.getElementText();
					stream.close();
					return ret;
				}
		    }
		}
		return null;
	}
	
	/**
	 * Specify both tag, and a attribute value, though 
	 * it will not discern the actual qname
	 * 
	 * @param site
	 * @param tag
	 * @param value
	 * @return
	 * @throws XMLStreamException
	 */
	public static String getSingleTagFromURL(URL site, String tag, String value) throws XMLStreamException{
		XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
	    XMLStreamReader2 stream = (XMLStreamReader2) f.createXMLStreamReader(site);
		while(stream.hasNext()){
		    stream.next();
		    if(stream.isStartElement()){
		    	String xmtag = stream.getName().toString();
				if(xmtag.equals(tag)){
					for(int i = 0; i < stream.getAttributeCount();i++){
						if(stream.getAttributeValue(i).equals(value)){
							String ret = stream.getElementText();
							stream.close();
							return ret;
						}
					}				
				}
		    }
		}
		return null;
	}
	
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
 
 	public static boolean Xml2File(Document document, File file){
        Source src = new DOMSource(document);
        try{
        	FileOutputStream outStream = new FileOutputStream(file);
        	Result result = new StreamResult(outStream);
        	Transformer xformer;
        	TransformerFactory factory = TransformerFactory.newInstance();
        	factory.setAttribute("indent-number", indent);
        	xformer = factory.newTransformer();
        	xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        	xformer.transform(src, result);
        	outStream.close();
        	return true;
        }
        catch (TransformerConfigurationException e) {
        	e.printStackTrace();
        	return false;
        }
        catch (IOException e) {
        	e.printStackTrace();
        	return false;
        }
        catch (TransformerFactoryConfigurationError e) {
        	e.printStackTrace();
        	return false;
        }
        catch (TransformerException e) {
        	e.printStackTrace();
        	return false;
        }
 	}
 	 	
}
