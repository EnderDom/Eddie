package enderdom.eddie.bio.homology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import enderdom.eddie.databases.bioSQL.psuedoORM.custom.IPRPredicates;
import enderdom.eddie.databases.bioSQL.psuedoORM.custom.IPRTermRelationship;

/**
 * 
 * In looking for documentation 
 * on how to store IPR/GO terms
 * in biosql I found a script in bioperl
 * for uploading iprterms straight
 * to bioSQL. So they only task should
 * be to link bioentries to IPR terms
 * 
 * @author dominic
 *
 */
public class InterproXML implements Iterator<InterproObject>{

	private Logger logger = RootLogger.getRootLogger();
	private XMLStreamReader2 stream;
	private InterproObject current;
	private InterproObject last;
	private String filename;
	private static String interpro_tag = "interpro_matches";
	private static String protein_tag = "protein";
	private boolean isInterpro;
	private String current_protein;
	private InterproMatch currentmatch;
	private IPRPredicates currentship;
	private String prefix;
	private String suffix;
	private int run_id;
	
	public InterproXML(File xml, String prefix, String suffix, int run_id)
			throws FileNotFoundException, XMLStreamException{
		this.prefix=prefix;
		this.suffix=suffix;
		this.filename=xml.getName();
		this.run_id = run_id;
		InputStream str = new FileInputStream(xml);
		setup(str);
	}
	
	public InterproXML(InputStream str,String prefix, String suffix, int run_id) 
			throws XMLStreamException{
		this.prefix=prefix;
		this.suffix=suffix;
		this.run_id = run_id;
		setup(str);
	}
	
	
	public InterproXML(File xml) throws FileNotFoundException, XMLStreamException{
		this.filename=xml.getName();
		InputStream str = new FileInputStream(xml);
		setup(str);
	}
	
	public InterproXML(InputStream str) throws XMLStreamException{
		setup(str);
	}
	
	public void setup(InputStream str)throws XMLStreamException{
		XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newInstance();
	    f.setProperty(XMLInputFactory2.SUPPORT_DTD, Boolean.FALSE);
		stream = (XMLStreamReader2) f.createXMLStreamReader(str);
		parseNext();
	}
	
	private void parseNext() throws XMLStreamException{
		last = current;
		current = null;
		String tag = new String();
		while(stream.hasNext()){
		    stream.next();
		    if(stream.isStartElement()){
		    	tag = stream.getName().toString();
		    	if(tag.equals(interpro_tag)){
		    		isInterpro=true;
		    	}
		    	else if(tag.equals(protein_tag)){
		    		for(int i = 0; i < stream.getAttributeCount();i++){
						if(stream.getAttributeName(i).toString().equals("id")){
							current_protein = stream.getAttributeValue(i);
							if(prefix!=null)current_protein = prefix+current_protein;
							if(suffix!=null)current_protein = current_protein+suffix;
							if(current_protein.contains("Seqman_Digest")){
								current_protein = current_protein.replace("Seqman_Digest", "Seqman_Digest_");
							}
						}
					}	
		    	}
		    	else if(tag.equals("interpro")){
		    		current = new InterproObject(run_id);
		    		current.setProtein(current_protein);
		    		for(int i = 0; i < stream.getAttributeCount();i++){
						if(stream.getAttributeName(i).toString().equals("id")){
							current.setIdentifier(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("name")){
							current.setName(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("type")){
							current.setType(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("parent_id")){
							current.addParent(stream.getAttributeValue(i));
						}
					}	
		    	}
		    	else if(tag.equals("match")){
		    		currentmatch = new InterproMatch();
		    		for(int i = 0; i < stream.getAttributeCount();i++){
						if(stream.getAttributeName(i).toString().equals("id")){
							currentmatch.setIdentifier(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("name")){
							currentmatch.setName(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("dbname")){
							currentmatch.setDbname(stream.getAttributeValue(i));
						}
					}	
		    	}
		    	else if(tag.equals("location")){
		    		InterproLocation loc = new InterproLocation();
		    		for(int i = 0; i < stream.getAttributeCount();i++){
						if(stream.getAttributeName(i).toString().equals("start")){
							loc.setStart(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("end")){
							loc.setEnd(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("score")){
							loc.setScore(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("status")){
							loc.setStatus(stream.getAttributeValue(i));
						}
						else if(stream.getAttributeName(i).toString().equals("evidence")){
							loc.setEvidence(stream.getAttributeValue(i));
						}
					}
					this.currentmatch.addLocation(loc);
		    	}
		    	else if(tag.equals("child_list")){
		    		/*
		    		 * Bioperl skips this one. I guess it will
		    		 * be covered by children's predicates
		    		 */
		    	}
		    	else if(tag.equals("found_in")){
		    		currentship = IPRPredicates.FOUND_IN;
		    	}
		    	else if(tag.equals("contains")){
		    		currentship = IPRPredicates.CONTAINS;
		    	}
		       	else if(tag.equals("is_a")){
		    		currentship = IPRPredicates.IS_A;
		    	}
		       	else if(tag.equals("part_of")){
		    		currentship = IPRPredicates.PART_OF;
		    	}
		    	else if(tag.equals("rel_ref")){
		    		IPRTermRelationship ship = new IPRTermRelationship();
		    		ship.setIPRsubject_id(current.getIdentifier());
		    		ship.setIPRPredicate(currentship);
		    		for(int i = 0; i < stream.getAttributeCount();i++){
		    			if(stream.getAttributeName(i).toString().equals("ipr_ref")){
							ship.setIPRobject_id(stream.getAttributeValue(i));
						}
		    		}
		    		current.addRelationship(ship);
		    	}
		    	//NOT dealng with GO terms as yet
		    	else if(tag.equals("classification")){
		    		
		    	}
		    	else if(tag.equals("category")){
		    		
		    	}
		    	else if(tag.equals("description")){
		    		
		    	}
		    }
		    else if(stream.isEndElement()){
		    	tag = stream.getName().toString();
		    	if(tag.equals("match")){
		    		current.addInterproMatch(currentmatch);
		    		currentmatch = null;
		    	}
		    }
		}
	}
	
	public boolean hasNext() {
		return current!=null;
	}

	public InterproObject next() {
		try {
			parseNext();
			return last;
		} catch (XMLStreamException e) {
			logger.error("Failed to parse next in "+ this.filename,e);			
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public boolean isInterpro() {
		return isInterpro;
	}

	public void setInterpro(boolean isInterpro) {
		this.isInterpro = isInterpro;
	}

	
	
}
