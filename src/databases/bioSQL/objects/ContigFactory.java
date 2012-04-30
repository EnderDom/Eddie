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
		ContigXT contig = new ContigXT();
		BioSQLExtended bsxt = manager.getBioSQLXT();
		BioSQL bs = manager.getBioSQL();
		int[] reads = bsxt.getReads(manager.getCon(), contig_id);
		contig.setReadCount(reads.length, true, false, true);
		contig.setReadIDs(reads);
		int programid = bs.getTerm(manager.getCon(), null, division);
		
		try{
			int type_term_id = bsxt.getDefaultAssemblyTerm(bs, manager.getCon());
			if(type_term_id < 0)return null;
			PreparedStatement ment = manager.getCon().prepareStatement("SELECT bioentry_id.name, location.start_pos, location.stop_pos FROM bioentry JOIN segfeature" +
					" ON bioentry.bioentry_id=seqfeature.bioentry_id JOIN location ON seqfeature.seqfeature_id=location.seqfeature_id" +
					" WHERE seqfeature.type_term_id=? AND seqfeature.source_term_id=? AND seqfeature.bioentry_id=?");
			ment.setInt(1, type_term_id);
			ment.setInt(2, programid);
			for(int i =0; i < reads.length; i++){
				ment.setInt(3, reads[i]);
				ResultSet set = ment.executeQuery();
				while(set.next()){
					contig.addRead(set.getString(0), null, set.getInt(1), set.getInt(2), (short)0);
				}
			}
			ment.close();
		}
		catch(SQLException sql){
			logger.error("Failed to retrieve read data" , sql);
		}
		return null;
	}
	
	public BasicContig buildContig(ACERecord record){
		logger.warn("Method is stub!");
		return null;
	}
}
