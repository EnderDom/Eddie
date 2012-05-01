package databases.bioSQL.objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;
import bio.assembly.ACERecord;
import bio.objects.BasicContig;
import bio.objects.ContigXT;

public class ContigFactory {
	
	Logger logger = Logger.getRootLogger();
	
	public BasicContig buildContig(DatabaseManager manager){
		logger.warn("Method is stub!");
		return null;
	}
	
	public ContigXT getContigXT(DatabaseManager manager, int contig_id, String division){
		logger.debug("Building Contig");
		ContigXT contig = new ContigXT();
		BioSQLExtended bsxt = manager.getBioSQLXT();
		BioSQL bs = manager.getBioSQL();
		logger.debug("Retrieving Read Count");
		int[] reads = bsxt.getReads(manager.getCon(), contig_id);
		if(reads == null){
			logger.error("Failed to retrieve reads associated with contig id " + contig_id + " for division " + division);
			return null;
		}
		logger.debug("Read Count @ "+reads.length);
		contig.setReadCount(reads.length);
		contig.setReadIDs(reads);
		logger.debug("Program Term Id for division " + division);
		int programid = bs.getTerm(manager.getCon(), null, division);
		logger.debug("Term is "+programid);
		if(programid < 0){
			logger.error("Could not get that Program ID");
			return null;
		}
		try{
			int type_term_id = bsxt.getDefaultAssemblyTerm(bs, manager.getCon());
			if(type_term_id < 0)return null;
			PreparedStatement ment = manager.getCon().prepareStatement("SELECT bioentry.name, location.start_pos, location.end_pos FROM bioentry JOIN seqfeature" +
					" ON bioentry.bioentry_id=seqfeature.bioentry_id JOIN location ON seqfeature.seqfeature_id=location.seqfeature_id" +
					" WHERE seqfeature.type_term_id=? AND seqfeature.source_term_id=? AND seqfeature.bioentry_id=?");
			ment.setInt(1, type_term_id);
			ment.setInt(2, programid);
			logger.debug("Retrieving Read Data from Database");
			for(int i =0; i < reads.length; i++){
				ment.setInt(3, reads[i]);
				ResultSet set = ment.executeQuery();
				while(set.next()){
					contig.addRead(set.getString(1), null, set.getInt(2), set.getInt(3), (short)0);
					break;
				}
			}
			System.out.println();
			ment.close();
		}
		catch(SQLException sql){
			logger.error("Failed to retrieve read data" , sql);
		}
		/*
		 * REMEMBER TO SET THE F***ING return value as something you idiot
		 * I know eclipse whinges whilst you're trying to code, but setting return to
		 * null always leads to epic fail as you will forget about it!!!
		 */
		return contig;
	}
	
	public BasicContig buildContig(ACERecord record){
		logger.warn("Method is stub!");
		return null;
	}
}
