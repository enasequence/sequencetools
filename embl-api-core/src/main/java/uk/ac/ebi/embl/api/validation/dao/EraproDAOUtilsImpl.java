package uk.ac.ebi.embl.api.validation.dao;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;

public class EraproDAOUtilsImpl implements EraproDAOUtils 
{
	private Connection connection;
	HashMap<String,Reference> submitterReferenceCache=new HashMap<String, Reference>();

	public EraproDAOUtilsImpl (Connection connection)
	{
		this.connection=connection;
	}
	
	@Override
	public Reference getSubmitterReference(String analysisId) throws SQLException, UnsupportedEncodingException
    {
		if(submitterReferenceCache.get(analysisId)!=null)
    	{
    		return submitterReferenceCache.get(analysisId);
    	}
    	Publication publication = new Publication();
		ReferenceFactory referenceFactory = new ReferenceFactory();
		Reference reference = referenceFactory.createReference();
		HashSet<String> consortium=new HashSet<String>();
		String pubConsortium="";
		String submitterReferenceQuery = "select sc.consortium, sc.surname, sc.middle_initials, sc.first_name, s.center_name, to_char(s.first_created, 'DD-MON-YYYY') first_created, sa.laboratory_name, sa.address, sa.country "
				              + "from analysis a "
				              + "join submission s on(s.submission_id=a.submission_id) "
				              + "join submission_account sa on(s.submission_account_id=sa.submission_account_id) "
				              + "join submission_contact sc on(sc.submission_account_id=s.submission_account_id) "
				              + "where a.analysis_id =?";
		
		PreparedStatement submitterReferenceStmt = null;
		ResultSet submitterReferenceRs = null;
				
		try
		{
			submitterReferenceStmt = connection.prepareStatement(submitterReferenceQuery);
			submitterReferenceStmt.setString(1, analysisId);
			submitterReferenceRs = submitterReferenceStmt.executeQuery();
			while (submitterReferenceRs.next())
			{
				String pConsrtium=submitterReferenceRs.getString("consortium"); 
				consortium.add(pConsrtium);
					
				if(pConsrtium==null)//ignore first_name,middle_name and last_name ,if consortium is given : WAP-126
				{
				Person person =null;
				
				person = referenceFactory.createPerson(
						EntryUtils.concat(" ",WordUtils.capitalizeFully(EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("surname")),'-',' '),  EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("middle_initials"))),
						getFirstName(EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("first_name"))));
				
				publication.addAuthor(person);
				reference.setAuthorExists(true);
				}

				Submission submission = referenceFactory.createSubmission(publication);				
				submission.setSubmitterAddress(EntryUtils.concat(", ", EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("center_name")),
				EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("laboratory_name")), EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("address")), EntryUtils.convertNonAsciiStringtoAsciiString(submitterReferenceRs.getString("country"))));
				Date date = EntryUtils.getDay(submitterReferenceRs.getString("first_created"));
				submission.setDay(date);
				publication = submission;
				reference.setPublication(publication);
				reference.setLocationExists(true);
				reference.setReferenceNumber(1);
				
				}
		}
		finally
		{
			DbUtils.closeQuietly(submitterReferenceRs);
			DbUtils.closeQuietly(submitterReferenceStmt);
		}
		
		for(String refCons:consortium)
		{
			if(refCons!=null)
			pubConsortium+=refCons+", ";
		}
		if(pubConsortium!=null&&pubConsortium.endsWith(", "))
		{
			pubConsortium = pubConsortium.substring(0, pubConsortium.length()-2);
		}
		if(reference.getPublication()==null)
			return null;
		reference.getPublication().setConsortium(pubConsortium);
		
		return reference;
	}
	
	private String getFirstName(String firstName)
	{
		StringBuilder nameBuilder= new StringBuilder();
		if(StringUtils.containsNone(firstName,"@")&&StringUtils.contains(firstName, "-"))
		{
			String[] names=StringUtils.split(firstName,"-");
			List<String> fnames=Arrays.asList(names);
			int i=0;
			for(String n: fnames)
			{
				i++;
				nameBuilder.append(WordUtils.initials(n).toUpperCase());
				if(i==fnames.size())
					nameBuilder.append(".");
				else
					nameBuilder.append(".-");
			}
	   }
		else
		{
			nameBuilder.append(firstName.toUpperCase().charAt(0));
			nameBuilder.append(".");
		}
		return nameBuilder.toString();
	}
	
}
