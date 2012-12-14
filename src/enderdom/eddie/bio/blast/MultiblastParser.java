package enderdom.eddie.bio.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;

import enderdom.eddie.bio.objects.BlastObject;
import enderdom.eddie.bio.objects.BlastOneBaseException;
import enderdom.eddie.bio.objects.GeneralBlastException;

public class MultiblastParser implements Iterator<BlastObject>{

	private Logger logger = RootLogger.getRootLogger();
	private static String iteration = "Iteration";
	private XMLStreamReader stream;
	private BlastObject current;
	private BlastObject last;
	int i =0;
	int hits =0;
	int hsps =0;
	String buffer;
	int itercount = 0;
	
	public MultiblastParser(File xml) throws FileNotFoundException, XMLStreamException{
		XMLInputFactory f = XMLInputFactory.newInstance();
		stream = f.createXMLStreamReader(new FileInputStream(xml));
		try {
			parseNext();
		} catch (GeneralBlastException e) {
			logger.error(e);
		} catch (BlastOneBaseException e) {
			logger.error(e);
		}
	}

	public MultiblastParser(InputStream str)throws XMLStreamException{
		XMLInputFactory f = XMLInputFactory.newInstance();
		stream = f.createXMLStreamReader(str);
		try {
			parseNext();
		} catch (GeneralBlastException e) {
			logger.error(e);
		} catch (BlastOneBaseException e) {
			logger.error(e);
		}
	}
	
	/*
	 * Sets the current BlastObject to 
	 * last and parse the next one, this means the
	 * parser is always one ahead of where it claims to be
	 * 
	 */
	private void parseNext() throws GeneralBlastException, BlastOneBaseException{
		last = current;
		current = null;
		String tag = "";
		hits=0;
		try {
			while(stream.hasNext()){
			    i= stream.next();
				if(stream.isStartElement()){
					tag = stream.getName().toString();
					if(tag.equals(iteration)){
						current = new BlastObject();
					}
					else if(tag.startsWith("Hit")){
						if(tag.equals("Hit"))hits++;
						else if(tag.equals("Hit_hsps"))hsps=0;//Do nothing, picked up by hsp
						else if(this.checkElementHasText())current.putHitTag(hits, tag, getElementText());
					}
					else if(tag.startsWith("Hsp")){
						if(tag.equals("Hsp"))hsps++;
						else if(this.checkElementHasText())current.putHspTag(hsps, hits, tag, getElementText());
					}
					else if(tag.startsWith("Statistics_")){
						if(this.checkElementHasText())current.putIterationTag(tag, getElementText());
					}
					else if(tag.equals("Iteration_iter-num") || tag.equals("Iteration_query-ID") || tag.equals("Iteration_query-def") || tag.equals("Iteration_query-len")){
						if(this.checkElementHasText())current.putIterationTag(tag, getElementText());
					}
				}
				if(stream.isEndElement()){
					if(stream.getName().toString().equals(iteration)){
						itercount++;
						logger.debug("Parsed Iteration " + itercount);
						return;
					}
				}
			}
		} 
		catch (XMLStreamException e) {
			logger.error("Failed to load XML as stream",e);
		}
	}
	
	/**
	 * There is probably an easier way of doing this but I can't think of it
	 * right now. This basically does that thing that shouldn't be done
	 * and gobbles errors.
	 * 
	 * @return whether or not the tag was parsed
	 * as I'm not 100% sure the various tags which have 
	 * no text in blast xml will be caught, this will return
	 * true if the element has text in it. ie <Hit_num> rather than no text
	 * ie <\Hit> 
	 */
	public boolean checkElementHasText(){
		buffer = new String();
		try{
			buffer = stream.getElementText();
			return true;
		}
		catch(XMLStreamException e){
			//nom nom nom
			logger.trace("This exception was ignored",e);
			return false;
		}
	}
	
	public String getElementText(){
		return buffer;
	}

	public boolean hasNext() {
		return current!=null;
	}

	public BlastObject next(){
		try {
			parseNext();
			return last;
		} catch (GeneralBlastException e) {
			logger.error(e);
		} catch (BlastOneBaseException e) {
			logger.error(e);
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public int getCurrentPosition(){
		return i;
	}
	
	public void close(){
		if(this.stream != null){
			try{	
				this.stream.close();
			}
			catch(XMLStreamException e){
				logger.warn("Failed to close XML stream",e);
			}
		}
	}
}
