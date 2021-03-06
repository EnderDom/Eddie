package enderdom.eddie.bio.homology.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;


import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import enderdom.eddie.exceptions.BlastOneBaseException;
import enderdom.eddie.exceptions.GeneralBlastException;

/*
 * Had issues because of this:
 * bug report 6536111 {@link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6536111}
 * 
 * Switched to woodstox
 * 
 */

public class MultiblastParser implements Iterator<BlastObject>, BlastParser{

	private Logger logger = RootLogger.getRootLogger();
	private static String iteration = "Iteration";
	private XMLStreamReader2 stream;
	private BlastObject current;
	private BlastObject last;
	int i =0;
	int hits =0;
	int hsps =0;
	String buffer;
	int itercount = 0;
	public String classtype;
	public int blasttype = 0;
	public static int BASICBLAST =0;
	public static int UNIVEC =1;
	public Hashtable<String, String> parserCache;
	String filename;
	FileInputStream streamwrapper;
	XMLInputFactory2 factory;
	
	public MultiblastParser(int blastype, File xml) throws XMLStreamException, IOException, GeneralBlastException, BlastOneBaseException{
		this.filename=xml.getName();
		parserCache = new Hashtable<String, String>();
		streamwrapper = new FileInputStream(xml);
		this.blasttype = blastype;
		factory = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    factory.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		stream = (XMLStreamReader2) factory.createXMLStreamReader(streamwrapper);
		parseNext();
	}

	public MultiblastParser(int blastype, InputStream str)throws IOException, XMLStreamException, GeneralBlastException, BlastOneBaseException{
		parserCache = new Hashtable<String, String>();
		streamwrapper = null;
		this.blasttype = blastype;
		factory = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    factory.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		stream = (XMLStreamReader2) factory.createXMLStreamReader(str);
		parseNext();
	}

	
	/*
	 * Sets the current BlastObject to 
	 * last and parse the next one, this means the
	 * parser is always one ahead of where it claims to be
	 * 
	 */
	private void parseNext() throws  GeneralBlastException, XMLStreamException, IOException, BlastOneBaseException{
		last = current;
		current = null;
		String tag = "";
		hits=0;

		while(stream.hasNext()){
		    i= stream.next();
			if(stream.isStartElement()){
				tag = stream.getName().toString();
				if(tag.equals(iteration)){
					if(this.blasttype == UNIVEC) current = new UniVecBlastObject(this);
					else current = new BasicBlastObject(this);	
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
					if(this.checkElementHasText())this.put(tag, getElementText());
				}
				else if(tag.equals("Iteration_iter-num") || tag.equals("Iteration_query-ID") 
						|| tag.equals("Iteration_query-def") || tag.equals("Iteration_query-len")){
					if(this.checkElementHasText())current.put(tag, getElementText());
				}
				else if(tag.startsWith("BlastOutput_") && !tag.startsWith("BlastOutput_param") && !tag.startsWith("BlastOutput_iterations")){
					if(this.checkElementHasText()){
						this.put(tag, this.getElementText());
					}
				}
			}
			if(stream.isEndElement()){
				if(stream.getName().toString().equals(iteration)){
					itercount++;
					return;
				}
			}
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
			if(buffer == null) return false;
			else return true;
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

	/**
	 * NOTE returns null if an error
	 * as you can't throw error exception
	 * 
	 */
	public BlastObject next(){
		try {
			parseNext();
			return last;
		} catch (GeneralBlastException e) {
			logger.error("A general blast exception ",e);
		} catch (XMLStreamException e) {
			logger.error("XML can't be parsed ",e);
		} catch (IOException e) {
			logger.error("XML failed to be parsed ",e);
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
		try{
			if(this.streamwrapper != null)this.streamwrapper.close();
			if(stream != null)this.stream.close();
		}
		catch(XMLStreamException e){
			logger.warn("Failed to close XML stream",e);
		} catch (IOException e) {
			logger.warn("Failed to close stream politely", e);
		}
	}

	public void put(String key, String value) {
		this.parserCache.put(key, value);
	}

	public String get(String key) {
		return this.parserCache.get(key);
	}
}
