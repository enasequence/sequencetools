package uk.ac.ebi.embl.api.validation.dao;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
	HashMap<String,AssemblySubmissionInfo> assemblySubmissionInfocache= new HashMap<String, AssemblySubmissionInfo>();

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
		if(firstName==null)
			return null;
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
	
	@Override
	public List<String> isSampleHasDifferentProjects(String analysisId) throws SQLException
	{
		
		List<String> analysisIdList= new ArrayList<String>();
			
        //check sample has different study_id
		String  differentStudyidSQL= "select analysis_id "
				+ "from analysis "
				+ "join analysis_sample "
				+ "using (analysis_id) "
				+ "where study_id <> ? "
				+ "and sample_id = ? "
				+ "and analysis.submission_account_id = ? "
				+ "and analysis_id <> ? "
				+ "and analysis_type = 'SEQUENCE_ASSEMBLY' "
				+ "and status_id in (2, 4, 7, 8)";

		
		ResultSet differentStudyidSQLrs = null;

		try(PreparedStatement differentStudyidSQLstmt = connection.prepareStatement(differentStudyidSQL);)
		{
			AssemblySubmissionInfo assemblySubmissionInfo=getAssemblySubmissionInfo(analysisId);
			
			if(assemblySubmissionInfo.getStudyId()==null)
				return analysisIdList;
			
			differentStudyidSQLstmt.setString(1,assemblySubmissionInfo.getStudyId());
			differentStudyidSQLstmt.setString(2,assemblySubmissionInfo.getSampleId());
			differentStudyidSQLstmt.setString(3,assemblySubmissionInfo.getSubmissionAccountId());
			differentStudyidSQLstmt.setString(4,analysisId);
			differentStudyidSQLrs=differentStudyidSQLstmt.executeQuery();
			while(differentStudyidSQLrs.next())
			{
				analysisIdList.add(differentStudyidSQLrs.getString(1));
			}
			return analysisIdList;
			/*
			if(analysisIdList.size()>0)
			{
				//throw new AssemblyUserException("Multiple assembly submissions found with a different project but the same sample "+ assemblySubmissionInfo.getBiosampleId() + ": "+String.join(",",analysisIdList));
				return true;
			}
			return false;
			*/
		}
	}

	@Override
	public List<String> isAssemblyDuplicate(String analysisId) throws SQLException
	{
		
		List<String> duplicateAnalysisIdList= new ArrayList<String>();
		
        //checks assembly is duplicate
		String  duplicateAssemblySQL= " select analysis_id "
				                     + "from analysis"
				                     + " join analysis_sample"
				                     + " using (analysis_id) "
				                     + "where study_id = ?  "
				                     + "and sample_id = ? "
				                     + "and analysis.submission_account_id = ? "
				                     + "and analysis_id <> ? "
				                     + "and analysis_type = 'SEQUENCE_ASSEMBLY' "
				                     + "and first_created between ? and ? "
				                     + "and status_id in (2, 4, 7, 8)";

		
		ResultSet duplicateAssemblySQLrs = null;

		try(PreparedStatement duplicateAssemblySQLstmt = connection.prepareStatement(duplicateAssemblySQL);)
		{
			AssemblySubmissionInfo assemblySubmissionInfo=getAssemblySubmissionInfo(analysisId);

			if(assemblySubmissionInfo.getStudyId()==null)
				return duplicateAnalysisIdList;
			
			duplicateAssemblySQLstmt.setString(1,assemblySubmissionInfo.getStudyId());
			duplicateAssemblySQLstmt.setString(2,assemblySubmissionInfo.getSampleId());
			duplicateAssemblySQLstmt.setString(3,assemblySubmissionInfo.getSubmissionAccountId());
			duplicateAssemblySQLstmt.setString(4,analysisId);
			duplicateAssemblySQLstmt.setDate(5,assemblySubmissionInfo.getBegindate());
			duplicateAssemblySQLstmt.setDate(6,assemblySubmissionInfo.getEnddate());

			duplicateAssemblySQLrs=duplicateAssemblySQLstmt.executeQuery();
			while(duplicateAssemblySQLrs.next())
			{
				duplicateAnalysisIdList.add(duplicateAssemblySQLrs.getString(1));
			}
			return duplicateAnalysisIdList;
		}
	}
	
	@Override
	public AssemblySubmissionInfo getAssemblySubmissionInfo(String analysisId) throws SQLException
	{
		if(assemblySubmissionInfocache.get(analysisId)!=null)
			return assemblySubmissionInfocache.get(analysisId);
		
		String sql = "select study_id, project_id, sample_id, biosample_id, analysis.submission_account_id, analysis.first_created-7 begindate ,analysis.first_created+7 enddate "
				                   + "from analysis "
				                   + "join analysis_sample "
				                   + "using (analysis_id) "
				                   + "join sample using (sample_id) "
				                   + "join study using (study_id) "
				                   + "where analysis_id = ?";
		
       	ResultSet rs = null;
		AssemblySubmissionInfo assemblyInfo= new AssemblySubmissionInfo();
		try(PreparedStatement stmt = connection.prepareStatement(sql);)
		{
			
			stmt.setString(1, analysisId);
			rs = stmt.executeQuery();
			if(rs.next())
			{
				assemblyInfo.setStudyId(rs.getString("study_id"));
				assemblyInfo.setProjectId(rs.getString("project_id"));
				assemblyInfo.setBiosampleId(rs.getString("biosample_id"));
				assemblyInfo.setSubmissionAccountId(rs.getString("submission_account_id"));
				assemblyInfo.setSampleId(rs.getString("sample_id"));
				assemblyInfo.setBegindate(rs.getDate("begindate"));
				assemblyInfo.setEnddate(rs.getDate("enddate"));
			}
			assemblySubmissionInfocache.put(analysisId, assemblyInfo);
			return assemblyInfo;
		}
		
	}
	

}
