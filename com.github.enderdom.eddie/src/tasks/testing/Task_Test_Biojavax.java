package tasks.testing;

import java.util.Iterator;
import java.util.List;

import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.db.biosql.BioSQLCrossReferenceResolver;
import org.biojavax.bio.db.biosql.BioSQLRichObjectBuilder;
import org.biojavax.bio.db.biosql.BioSQLRichSequenceHandler;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import tools.Tools_XML;

public class Task_Test_Biojavax extends Task_Test_Basic {
	public Task_Test_Biojavax(){
		super();
	}
	
	public void runTest(){
		/*
		 * Testing 
		 */
		try{
			session();
			System.out.println("Running test");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//This is going to break my database :(
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public void session(){
		try{
		Tools_XML.inputStreamToDocument(this.getClass().getResourceAsStream("databases.biojavax.mappings.mysql.hibernate.cfg.xml"));
		}
		catch
		SessionFactory sessionFactory = new Configuration().addPackage("databases.biojavax.mappings.mysql").buildSessionFactory();
		//SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		
		Session session = sessionFactory.openSession();                                     
		connectToBioSQL(session);
		 
		Transaction tx = session.beginTransaction();
		try {
		 
		    // print out all the namespaces in the database
		 
		    Query q = session.createQuery("from Namespace");
		    List namespaces = q.list();               // retrieve all the namespaces from the db
		    for (Iterator i = namespaces.iterator(); i.hasNext(); ) {
		        Namespace ns = (Namespace)i.next();
		        System.out.println(ns.getName());     // print out the name of the namespace
		 
		        // print out all the sequences in the namespace
		        Query sq = session.createQuery("from BioEntry where namespace= :nsp");
		        // set the named parameter "nsp" to ns
		        sq.setParameter("nsp",ns);
		        List sequences = sq.list();
		 
		        for (Iterator j = sequences.iterator(); j.hasNext(); ) {
		            BioEntry be = (BioEntry)j.next();        // RichSequences are BioEntrys too
		            System.out.println("   "+be.getName());  // print out the name of the sequence
		 
		            // if the sequence is called bloggs, change its description to XYZ
		 
		            if (be.getName().equals("bloggs")) {
		                be.setDescription("XYZ");
		            }
		        }
		 
		    }
		 
		    // commit and tidy up
		    tx.commit();         
		    System.out.println("Changes committed.");
		 
		    // all sequences called bloggs now have a description "XYZ" in the database
		 
		} catch (Exception e) {
		    tx.rollback();       
		    System.out.println("Changes rolled back.");
		    e.printStackTrace(); 
		}
		 
		session.close();
	}
	
	public static void connectToBioSQL(Object session) {
		RichObjectFactory.clearLRUCache();
		RichObjectFactory.setRichObjectBuilder(new BioSQLRichObjectBuilder(session));
		RichObjectFactory.setDefaultCrossReferenceResolver(new BioSQLCrossReferenceResolver(session));      
		RichObjectFactory.setDefaultRichSequenceHandler(new BioSQLRichSequenceHandler(session));
	}
	
}
