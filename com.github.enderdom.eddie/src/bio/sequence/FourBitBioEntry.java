package bio.sequence;

import java.util.Set;

import org.biojava.utils.ChangeVetoException;
import org.biojavax.Comment;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotation;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.taxa.NCBITaxon;

import databases.manager.DatabaseManager;

public class FourBitBioEntry extends FourBitDNA implements BioEntry{
	
	public FourBitBioEntry(int nlength) {
		super(nlength);
	}
	
	public FourBitBioEntry(String s) {
		super(s);
	}

	DatabaseManager manager;

	public RichAnnotation getRichAnnotation() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Set getNoteSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void setNoteSet(Set notes) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("rawtypes")
	public Set getRankedCrossRefs() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void setRankedCrossRefs(Set crossrefs) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void addRankedCrossRef(RankedCrossRef crossref)
			throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void removeRankedCrossRef(RankedCrossRef crossref)
			throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Namespace getNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccession() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIdentifier(String identifier) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public String getDivision() {
		return null;
	}

	public void setDivision(String division) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDescription(String description) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public NCBITaxon getTaxon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTaxon(NCBITaxon taxon) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("rawtypes")
	public Set getRankedDocRefs() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Set getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Set getRelationships() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addRankedDocRef(RankedDocRef docref) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void removeRankedDocRef(RankedDocRef docref)
			throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void addComment(Comment comment) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void removeComment(Comment comment) throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void addRelationship(BioEntryRelationship relation)
			throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}

	public void removeRelationship(BioEntryRelationship relation)
			throws ChangeVetoException {
		// TODO Auto-generated method stub
		
	}
	
}
