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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.helper.MasterSourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.flatfile.reader.ReferenceReader;

public class EraproDAOUtilsImpl implements EraproDAOUtils 
{
	private Connection connection;
	HashMap<String,Reference> submitterReferenceCache=new HashMap<String, Reference>();
	HashMap<String,AssemblySubmissionInfo> assemblySubmissionInfocache= new HashMap<String, AssemblySubmissionInfo>();
	HashMap<String, Entry> masterCache = new HashMap<String,Entry>();

	
	public enum MASTERSOURCEQUALIFIERS
	{
		ecotype, 
		cultivar,
		isolate,
		strain, 
		sub_species, 
		variety,
	    sub_strain, 
		cell_line, 
		serotype, 
		serovar, 
		environmental_sample,
		metagenome_source,
		isolation_source;
		
		public static boolean isValid(String qualifier)
		{
			try
			{
				if (!qualifier.equalsIgnoreCase("PCR_primers"))
				{
					valueOf(qualifier.toLowerCase());
				}
			}
			catch (IllegalArgumentException e)
			{
				return false;
			}
			return true;
		}

		public static boolean isNoValue(String qualifier)
		{
			List<String> noValueQualifiers = new ArrayList<String>();
			noValueQualifiers.add(Qualifier.GERMLINE_QUALIFIER_NAME);
			noValueQualifiers.add(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);
			noValueQualifiers.add(Qualifier.PROVIRAL_QUALIFIER_NAME);
			noValueQualifiers.add(Qualifier.REARRANGED_QUALIFIER_NAME);
			noValueQualifiers.add(Qualifier.FOCUS_QUALIFIER_NAME);
			noValueQualifiers.add(Qualifier.TRANSGENIC_QUALIFIER_NAME);
			noValueQualifiers
					.add(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);

			if (noValueQualifiers.contains(qualifier))
			{
				return true;
			}
			return false;
		}
		
		public static boolean isNullValue(String qualifierValue) {
			List<String> nullValueCV = new ArrayList<String>();
			nullValueCV.add("not applicable");
			nullValueCV.add("not collected");
			nullValueCV.add("not provided");
			nullValueCV.add("restricted access");
			nullValueCV.add("missing");
            if(qualifierValue==null||qualifierValue.isEmpty())
            	return true;
			if (nullValueCV.contains(qualifierValue.toLowerCase())) {
				return true;
			}

			return false;

		}
	}
	

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
	public String getTemplateId(String analysisId) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("SELECT template_id from analysis where analysis_id = ?");
			ps.setString(1,analysisId);

			rs = ps.executeQuery();
			if (rs.next())
			{
				return rs.getString(1);
			}

			return null;
		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
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

	public Reference getReference(Entry entry, String analysisId, AnalysisType analysisType) throws SQLException , ValidationEngineException {

		String analysisQuery = "select first_created, " +
				"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/" + analysisType.name() + "/AUTHORS/text()' PASSING analysis_xml RETURNING CONTENT)) authors, " +
				"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/" + analysisType.name() + "/ADDRESS/text()' PASSING analysis_xml RETURNING CONTENT)) address, " +
				"XMLSERIALIZE(CONTENT xmlquery('let $d :=for $i in /ANALYSIS_SET/ANALYSIS/RUN_REF   return $i/IDENTIFIERS/PRIMARY_ID return string-join($d, \",\")'PASSING analysis_xml  RETURNING CONTENT) ) AS run_ref, " +
				"XMLSERIALIZE(CONTENT xmlquery('let $d :=for $i in /ANALYSIS_SET/ANALYSIS/ANALYSIS_REF   return $i/IDENTIFIERS/PRIMARY_ID return string-join($d, \",\")'PASSING analysis_xml  RETURNING CONTENT) ) AS analysis_ref " +
				"from analysis a where a.analysis_id=?";
		PreparedStatement analysisStmt = null;
		ResultSet analysisRs = null;

		try {
			analysisStmt = connection.prepareStatement(analysisQuery);
			analysisStmt.setString(1, analysisId);
			analysisRs = analysisStmt.executeQuery();
			while (analysisRs.next()) {
				String author = analysisRs.getString("authors");
				String address = analysisRs.getString("address");
				Date firstCreated = analysisRs.getDate("first_created");
				if (StringUtils.isNotBlank(author) && StringUtils.isNotBlank(address)) {
					return new ReferenceReader().getReference(author, address, firstCreated);
				}
				String runRef = analysisRs.getString("run_ref");
				setXrefs(runRef, entry);
				String analysisRef = analysisRs.getString("analysis_ref");
				setXrefs(analysisRef, entry);
			}
		}  finally {
			DbUtils.closeQuietly(analysisRs);
			DbUtils.closeQuietly(analysisStmt);
		}
		return null;
	}

   public Entry getMasterEntry(String analysisId, AnalysisType analysisType) throws SQLException
	{
	   if(masterCache.containsKey(analysisId))
	   {
		   return masterCache.get(analysisId);
	   }
		Entry masterEntry = new Entry();
		if(analysisType == null) {
			return  masterEntry;
		}

		masterEntry.setPrimaryAccession(analysisId);
		masterEntry.setIdLineSequenceLength(1);
		FeatureFactory featureFactory = new FeatureFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		TaxonHelper taxonHelper=new TaxonHelperImpl();
		String sampleId = null;
		String projectId;
		String scientificName;
		String prevSampleId = null;
		String prevProjectId = null;
		String uniqueName=null;
		String author = null;
		String address = null;
		Date firstCreated = null;

		String masterQuery = "select a.first_created, a.bioproject_id, p.status_id, sam.sample_id, sam.biosample_id, sam.sample_alias, " +
			"nvl(sam.fixed_tax_id, sam.tax_id) tax_id, nvl(sam.fixed_scientific_name, sam.scientific_name) scientific_name, " +
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/"+analysisType.name()+"/NAME/text()' PASSING analysis_xml RETURNING CONTENT)) assembly_name, " +
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/"+analysisType.name()+"/MOL_TYPE/text()' PASSING analysis_xml RETURNING CONTENT)) mol_type, "+
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/"+analysisType.name()+"/TPA/text()' PASSING analysis_xml RETURNING CONTENT)) tpa, " +
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/"+analysisType.name()+"/AUTHORS/text()' PASSING analysis_xml RETURNING CONTENT)) authors, " +
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_TYPE/"+analysisType.name()+"/ADDRESS/text()' PASSING analysis_xml RETURNING CONTENT)) address, " +
			"XMLSERIALIZE(CONTENT xmlquery('/ANALYSIS_SET/ANALYSIS/RUN_REF/IDENTIFIERS/PRIMARY_ID' PASSING analysis_xml  RETURNING CONTENT) ) AS run_ref, " +
			"XMLSERIALIZE(CONTENT xmlquery('/ANALYSIS_SET/ANALYSIS/ANALYSIS_REF/IDENTIFIERS/PRIMARY_ID' PASSING analysis_xml  RETURNING CONTENT) ) AS analysis_ref, " +
			"XMLSERIALIZE(CONTENT XMLQuery('/ANALYSIS_SET/ANALYSIS/DESCRIPTION/text()' PASSING analysis_xml RETURNING CONTENT)) description " +
			"from analysis a " +
			"join analysis_sample asam on (asam.analysis_id=a.analysis_id) " +
			"join sample sam on(asam.sample_id=sam.sample_id) " +
			"join submission s on(s.submission_id=a.submission_id) " +
			"join project p on(a.bioproject_id=p.project_id) " +
			"where a.analysis_id=?";

		boolean masterExists = false;
				
		masterEntry.setDataClass(Entry.SET_DATACLASS);

		
		Sequence sequence = sequenceFactory.createSequence();
		//sequence.setLength(1); // Required by putff.
		masterEntry.setSequence(sequence);
		masterEntry.setIdLineSequenceLength(1);
		if(analysisType == AnalysisType.TRANSCRIPTOME_ASSEMBLY) {
			masterEntry.getSequence().setMoleculeType("transcribed RNA");
		} else {
			masterEntry.getSequence().setMoleculeType("genomic DNA");
		}
		masterEntry.getSequence().setTopology(Topology.LINEAR);		
		
		sourceFeature.setMasterLocation();
		
		PreparedStatement masterInfoStmt = null;
		ResultSet masterInfoRs = null;
				
		try
		{
			masterInfoStmt = connection.prepareStatement(masterQuery);
			masterInfoStmt.setString(1, analysisId);
			masterInfoRs = masterInfoStmt.executeQuery();
			while (masterInfoRs.next())
			{
				masterExists = true;
				//masterEntry.setHoldDate(masterInfoRs.getDate("hold_date"));//hold_date always should be null , as entry status depends on study_id
				masterEntry.setStatus(Entry.Status.getStatus(2));//assembly new entries status should always be private

				sampleId = masterInfoRs.getString("sample_id");

				projectId = masterInfoRs.getString("bioproject_id");
				if (projectId != null && !projectId.equals(prevProjectId))
				{
					masterEntry.addProjectAccession(new Text(projectId));
				}			
				prevProjectId = projectId;

				String bioSampleId = masterInfoRs.getString("biosample_id");
				if (bioSampleId != null && !bioSampleId.equals(prevSampleId))
				{
					masterEntry.addXRef(new XRef("BioSample", bioSampleId));
				}

				String runRefs = masterInfoRs.getString("run_ref");
				setXrefs(runRefs, masterEntry);
				String analysisRef = masterInfoRs.getString("analysis_ref");
				setXrefs(analysisRef, masterEntry);

				prevSampleId = bioSampleId;
				
				sourceFeature.setTaxId(Long.valueOf(masterInfoRs.getString("tax_id")));

				author = masterInfoRs.getString("authors");
				address = masterInfoRs.getString("address");
				firstCreated = masterInfoRs.getDate("first_created");
				scientificName = masterInfoRs.getString("scientific_name");
				String molType=masterInfoRs.getString("mol_type");
				String tpa=masterInfoRs.getString("tpa");
				if("true".equalsIgnoreCase(tpa))
				{
					EntryUtils.setKeyWords(masterEntry);
				}
				if(molType!=null&&taxonHelper.isChildOf(scientificName, "Viruses"))
				{
					masterEntry.getSequence().setMoleculeType(molType);
				}

				String desc = masterInfoRs.getString("description");
				if(StringUtils.isNotBlank(desc)) {
					masterEntry.setComment(new Text(desc));
				}

				sourceFeature.setScientificName(scientificName);
				uniqueName=masterInfoRs.getString("sample_alias");
			}
		}
		
		finally
		{
			DbUtils.closeQuietly(masterInfoRs);
			DbUtils.closeQuietly(masterInfoStmt);
		}
		
		if (!masterExists)
		{
			return null;
		}
		
			
		// SOURCE QUALIFIERS
		String select_sourcefeature_Query = "select t1.tag, t1.value from sample,XMLTable('//SAMPLE_ATTRIBUTE'PASSING sample_xml COLUMNS tag varchar2(4000) PATH 'TAG/text()',value varchar2(4000) PATH 'VALUE/text()') t1 where sample_id =?";
		PreparedStatement select_sourcequalifiers_pstmt = null;
		ResultSet select_sourcequalifers_rs = null;
		MasterSourceFeatureUtils sourceUtils= new MasterSourceFeatureUtils();
		try
		{
			select_sourcequalifiers_pstmt = connection.prepareStatement(select_sourcefeature_Query);
			select_sourcequalifiers_pstmt.setString(1, sampleId);
			select_sourcequalifers_rs = select_sourcequalifiers_pstmt.executeQuery();
			while (select_sourcequalifers_rs.next())
			{
				String tag = select_sourcequalifers_rs.getString(1);
				String value = select_sourcequalifers_rs.getString(2);
				sourceUtils.addSourceQualifier(tag, value, sourceFeature);
			}

			sourceUtils.addExtraSourceQualifiers(sourceFeature, taxonHelper, uniqueName);
			if(StringUtils.isNotBlank(author) && StringUtils.isNotBlank(address)) {
				masterEntry.addReference(new ReferenceReader().getReference(author, address, firstCreated));
			} else {
				masterEntry.addReference(getSubmitterReference(analysisId));
			}

		}
		catch (Exception ex)
		{
		}
		finally
		{
			DbUtils.closeQuietly(select_sourcequalifers_rs);
			DbUtils.closeQuietly(select_sourcequalifiers_pstmt);
		}
		masterEntry.addFeature(sourceFeature);
		String description = SequenceEntryUtils.generateMasterEntryDescription(sourceFeature, analysisType);
		masterEntry.setDescription(new Text(description));
		masterCache.put(analysisId,masterEntry);
		return masterEntry;
	}

	private void setXrefs(String refs, Entry masterEntry) {
		if (StringUtils.isNotBlank(refs)) {
			String patternS = "<PRIMARY_ID>(.*)<\\/PRIMARY_ID>";
			Pattern p = Pattern.compile(patternS);
			Matcher m = p.matcher(refs);
			while (m.find()) {
				masterEntry.addXRef(new XRef("ENA", m.group(1)));
			}
		}
	}
   
}
