package enderdom.eddie.bio.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;

import enderdom.eddie.bio.objects.BlastObject;

public class MultiblastParser implements Iterator<BlastObject>{

	private Logger logger = RootLogger.getRootLogger();
	private static String iteration = "Iteration";
	private String tagname;
	private XMLStreamReader stream;
	private boolean started;
	private BlastObject current;
	private BlastObject last;
	int i =-1;
	
	public MultiblastParser(File xml) throws FileNotFoundException, XMLStreamException{
		XMLInputFactory f = XMLInputFactory.newInstance();
		stream = f.createXMLStreamReader(new FileInputStream(xml));
		int i =stream.next();
	}

	public boolean hasNext() {
		return last!=null;
	}

	public BlastObject next(){
//		try {
//			while(stream.hasNext()){
//				if(stream.isStartElement()){
//					tagname = stream.getAttributeLocalName(i);
//					if(tagname.contentEquals(iteration)){
//						current = new BlastObject();
//					}
//				}
//				else if(stream.isEndElement()){
//					tagname = stream.getAttributeLocalName(i);
//					if(tagname.contentEquals(iteration)){
//						return current;
//					}
//				}
//				stream.next();
//			}
//		} catch (XMLStreamException e) {
//			logger.error("Failed to load XML as stream",e);
//		}
		return null;//TODO
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
