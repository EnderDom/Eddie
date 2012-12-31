package enderdom.eddie.bio.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;


import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/*
 * Had issues because of this:
 * bug report 6536111 {@link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6536111}
 * 
 * Switched to woodstox
 * 
 */

public class MultiblastParser implements Iterator<BlastObject>{

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
	
	public MultiblastParser(int blastype, File xml) throws Exception{
		this.blasttype = blastype;
		XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		stream = (XMLStreamReader2) f.createXMLStreamReader(new FileInputStream(xml));
		try {
			parseNext();
		} catch (GeneralBlastException e) {
			logger.error(e);
		} catch (BlastOneBaseException e) {
			logger.error(e);
		}
	}

	public MultiblastParser(int blastype, InputStream str)throws Exception{
		this.blasttype = blastype;
		XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		stream = (XMLStreamReader2) f.createXMLStreamReader(str);
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
	private void parseNext() throws Exception{
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
						if(this.blasttype == UNIVEC) current = new UniVecBlastObject();
						else current = new BasicBlastObject();	
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
						System.out.print("\rParsed Iteration " + itercount+ "       ");
						return;
					}
				}
			}
		} 
		catch (XMLStreamException e) {
			logger.error("Failed to load XML as stream",e);
		}
		System.out.println();
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
		} catch (Exception e) {
			e.printStackTrace();
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
