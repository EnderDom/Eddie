package enderdom.eddie.bio.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import enderdom.eddie.bio.objects.BlastObject;

public class MultiblastParser implements Iterator<BlastObject>{

	private static String iteration = "Iteration";
	//private BlastObject last;
	//private BlastObject current;
	private String tagname; 
	
	public MultiblastParser(File xml) throws FileNotFoundException, XMLStreamException{
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLStreamReader r = f.createXMLStreamReader(new FileInputStream(xml));
		while(r.hasNext()){
			int i =r.next();
			if(r.isStartElement()){
				tagname = r.getAttributeLocalName(i);
				if(tagname.contentEquals(iteration)){
				//TODO
				}
			}
			else if(r.isEndElement()){
				tagname = r.getAttributeLocalName(i);
				if(tagname.contentEquals(iteration)){
				}
			}
		}
		
	}

	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public BlastObject next() {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}
