package uk.ac.ebi.embl.api.validation.dao;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SampleInfo;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.dao.model.*;
import uk.ac.ebi.embl.api.validation.fixer.entry.DivisionFix;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.helper.MasterSourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.helper.ReferenceUtils;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EraproDAOUtilsImpl implements EraproDAOUtils 
{
	private Connection connection;
	private final ReferenceUtils referenceUtils = new ReferenceUtils();
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

	/**
	 * - Builds <Reference> object from the submission_contact and submission_account information from database.
	 * - Used in pipelines to build <Reference> object if authors and address information is not available in analysis.xml
	 */
	@Override
	public Reference getSubmitterReference(String analysisId) throws SQLException, ValidationEngineException {
		try {
			SubmitterReference submitterReference = fetchSubmissionAccountAndAnalysisCreated(analysisId);
			if(submitterReference != null) {
				submitterReference.setSubmissionContacts(fetchSubmissionContacts(submitterReference.getSubmissionAccountId()));
				return new ReferenceUtils().constructSubmitterReference(submitterReference);
			} else {
				throw new ValidationEngineException("Could not retrieve submission account information");
			}
		} catch (UnsupportedEncodingException e) {
			throw new ValidationEngineException(e);
		}
	}

	/**
	 * called only from submission pipeline, not from Webin-CLI.
	 * builds <Reference> object from the authors and address information fetched from analysis.xml(we add manifest information
	 * into analysis.xml when the submitter makes a submission)
	 */
	@Override
	public Reference getReference(Entry entry, String analysisId, AnalysisType analysisType) throws SQLException , ValidationEngineException {

		String analysisQuery = "select submission_account_id, first_created, " +
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
				if (StringUtils.isNotBlank(author) ) {
					if(StringUtils.isBlank(address)) {
						address = referenceUtils.getAddressFromSubmissionAccount(fetchSubmissionAccountAndAnalysisCreated(analysisId).getSubmissionAccount());
					}
					return new ReferenceUtils().getSubmitterReferenceFromManifest(author, address, firstCreated, analysisRs.getString("submission_account_id"));
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

	private SubmitterReference fetchSubmissionAccountAndAnalysisCreated(String analysisId) throws SQLException {

		String addressQuery = "select a.first_created,submission_account_id,broker_name, sa.center_name, sa.laboratory_name, sa.address, sa.country "
				+ "from analysis a "
				+ "join submission_account sa using(submission_account_id) "
				+ "where analysis_id =?";

		PreparedStatement addressStmt = null;
		ResultSet addressRs = null;

		try {
			addressStmt = connection.prepareStatement(addressQuery);
			addressStmt.setString(1, analysisId);
			addressRs = addressStmt.executeQuery();
			if (addressRs.next()) {
				SubmitterReference submitterReference = new SubmitterReference();
				submitterReference.setSubmissionAccountId(addressRs.getString("submission_account_id"));
				submitterReference.setFirstCreated(addressRs.getDate("first_created"));
				SubmissionAccount subAccount = new SubmissionAccount();
				subAccount.setBrokerName(addressRs.getString("broker_name"));
				subAccount.setCenterName(addressRs.getString("center_name"));
				subAccount.setLaboratoryName(addressRs.getString("laboratory_name"));
				subAccount.setAddress(addressRs.getString("address"));
				subAccount.setCountry(addressRs.getString("country"));
				submitterReference.setSubmissionAccount(subAccount);
				return submitterReference;
			}
		} finally {
			DbUtils.closeQuietly(addressRs);
			DbUtils.closeQuietly(addressStmt);
		}

		return null;
	}

	private List<SubmissionContact> fetchSubmissionContacts(String submissionAccountId) throws SQLException {
		List<SubmissionContact> SubmissionContactList = new ArrayList<>();

		String submissionContactQuery = "select consortium, surname, middle_initials, first_name from submission_contact where submission_account_id =?";
		PreparedStatement submitterReferenceStmt = null;
		ResultSet submitterReferenceRs = null;

		try {
			submitterReferenceStmt = connection.prepareStatement(submissionContactQuery);
			submitterReferenceStmt.setString(1, submissionAccountId);
			submitterReferenceRs = submitterReferenceStmt.executeQuery();
			while (submitterReferenceRs.next()) {
				SubmissionContact submissionContact = new SubmissionContact();
				submissionContact.setConsortium(submitterReferenceRs.getString("consortium"));
				submissionContact.setSurname(submitterReferenceRs.getString("surname"));
				submissionContact.setMiddleInitials(submitterReferenceRs.getString("middle_initials"));
				submissionContact.setFirstName(submitterReferenceRs.getString("first_name"));
				SubmissionContactList.add(submissionContact);
			}
		} finally {
			DbUtils.closeQuietly(submitterReferenceRs);
			DbUtils.closeQuietly(submitterReferenceStmt);
		}
		return SubmissionContactList;
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
	public Set<String> getLocusTags(String projectId) throws SQLException {
		Set<String> locusTags= new HashSet<>();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select upper(locus_tag) from locus_tag where project_id =?");
			ps.setString(1,projectId);

			rs = ps.executeQuery();
			while (rs.next()) {
				locusTags.add(rs.getString(1));
			}

		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
		return locusTags;
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

	@Override
	public boolean isProjectValid(String project) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from project where project_id=? or ncbi_project_id=?");
			ps.setString(1, project);
			ps.setString(2, project);
			rs = ps.executeQuery();
			if (rs.next())
			{
				return true;
			}
			return false;
		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
	}

	@Override
	public SourceFeature getSourceFeature(String sampleId) throws Exception {
		SampleInfo sampleInfo = getSampleInfo(sampleId);
		return new MasterSourceFeatureUtils().constructSourceFeature(getSampleAttributes(sampleId), new TaxonHelperImpl(), sampleInfo);
	}

	@Override
	public boolean isIgnoreErrors(String submissionAccountId, String context, String name) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement("select 1 from webin_cli_ignore_errors where submission_account_id =? and context =? and name =?")) {
			ps.setString(1, submissionAccountId);
			ps.setString(2, context);
			ps.setString(3, name);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	@Override
	public Analysis getAnalysis(String analysisId) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement("select submission_account_id,unique_alias from analysis where analysis_id =?")) {
			ps.setString(1, analysisId);

			try (ResultSet rs = ps.executeQuery()) {
				if(rs.next()) {
					Analysis analysis = new Analysis();
					analysis.setSubmissionAccountId(rs.getString("submission_account_id"));
					analysis.setUniqueAlias(rs.getString("unique_alias"));
					return analysis;
				}
			}
		}
		return null;
	}

    public Entry createMasterEntry(String analysisId, AnalysisType analysisType) throws SQLException, ValidationEngineException
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

		SequenceFactory sequenceFactory = new SequenceFactory();
		SourceFeature sourceFeature = null;
		TaxonHelper taxonHelper=new TaxonHelperImpl();
		String sampleId = null;
		String projectId;
		SampleInfo sampleInfo = null;
		String prevSampleId = null;
		String prevProjectId = null;
		String author = null;
		String address = null;
		Date firstCreated = null;
		String submissionAccountId = null;
		boolean isTpa = false;

		String masterQuery = "select s.submission_account_id, a.first_created, a.bioproject_id, p.status_id, sam.sample_id, sam.biosample_id, " +
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
				submissionAccountId = masterInfoRs.getString("submission_account_id");
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

				author = masterInfoRs.getString("authors");
				address = masterInfoRs.getString("address");
				firstCreated = masterInfoRs.getDate("first_created");

				String molType=masterInfoRs.getString("mol_type");
				String tpa=masterInfoRs.getString("tpa");
				if("true".equalsIgnoreCase(tpa))
				{
					isTpa = true;
					EntryUtils.setKeyWords(masterEntry);
				}

				sampleInfo = getSampleInfo(sampleId);
				sampleInfo.setSampleId(sampleId);

				if(molType!=null&&taxonHelper.isChildOf(sampleInfo.getScientificName(), "Viruses"))
				{
					masterEntry.getSequence().setMoleculeType(molType);
				}

				String desc = masterInfoRs.getString("description");
				if(StringUtils.isNotBlank(desc)) {
					masterEntry.setComment(new Text(desc));
				}

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

		if (StringUtils.isNotBlank(author)) {
			if (StringUtils.isBlank(address)) {
				address = referenceUtils.getAddressFromSubmissionAccount(fetchSubmissionAccountAndAnalysisCreated(analysisId).getSubmissionAccount());
			}
			masterEntry.addReference(new ReferenceUtils().getSubmitterReferenceFromManifest(author, address, firstCreated, submissionAccountId));
		} else {
			masterEntry.addReference(getSubmitterReference(analysisId));
		}

		sourceFeature = new MasterSourceFeatureUtils().constructSourceFeature(getSampleAttributes(sampleId), taxonHelper, sampleInfo);

		masterEntry.addFeature(sourceFeature);
		String description = SequenceEntryUtils.generateMasterEntryDescription(sourceFeature, analysisType, isTpa);
		masterEntry.setDescription(new Text(description));
		masterCache.put(analysisId,masterEntry);
		return masterEntry;
	}


	private SampleInfo getSampleInfo(String sampleId) throws SQLException {

		SampleInfo sampleInfo = new SampleInfo();
		String sampleTaxonQuery = "select sample_alias, nvl(fixed_tax_id, tax_id) tax_id, nvl(fixed_scientific_name, scientific_name) scientific_name" +
				" from sample where sample_id=?";

		PreparedStatement selectTaxonPstmt = null;
		ResultSet selectTaxonRs = null;

		try {
			selectTaxonPstmt = connection.prepareStatement(sampleTaxonQuery);
			selectTaxonPstmt.setString(1, sampleId);
			selectTaxonRs = selectTaxonPstmt.executeQuery();
			if (selectTaxonRs.next()) {
				sampleInfo.setUniqueName(selectTaxonRs.getString("sample_alias"));
				sampleInfo.setScientificName(selectTaxonRs.getString("scientific_name"));
				sampleInfo.setTaxId(Long.valueOf(selectTaxonRs.getString("tax_id")));
			}
		} finally {
			DbUtils.closeQuietly(selectTaxonRs);
			DbUtils.closeQuietly(selectTaxonPstmt);
		}
		sampleInfo.setSampleId(sampleId);
		return sampleInfo;
	}

	/**
	 * @param sampleId
	 * @return SampleEntity constructed using sample.xml, SourceFeature qualifiers will be constructed later
	 * from SAMPLE_ATTRIBUTE in sample.xml
	 * @throws SQLException
	 */
	private SampleEntity getSampleAttributes( String sampleId) throws SQLException {

		SampleEntity sample =new SampleEntity();
		String select_sourcefeature_Query = "select t1.tag, t1.value from sample,XMLTable('//SAMPLE_ATTRIBUTE'PASSING sample_xml COLUMNS tag varchar2(4000) PATH 'TAG/text()'," +
				"value varchar2(4000) PATH 'VALUE/text()') t1 where sample_id =?";
		PreparedStatement select_sourcequalifiers_pstmt = null;
		ResultSet select_sourcequalifers_rs = null;

		try {
			select_sourcequalifiers_pstmt = connection.prepareStatement(select_sourcefeature_Query);
			select_sourcequalifiers_pstmt.setString(1, sampleId);
			select_sourcequalifers_rs = select_sourcequalifiers_pstmt.executeQuery();
			Map<String,String> attributes = new HashMap<>();
			while (select_sourcequalifers_rs.next()) {
				String tag = select_sourcequalifers_rs.getString(1);
				if(StringUtils.isNotBlank(tag) )
					attributes.put(tag,select_sourcequalifers_rs.getString(2));
			}
			sample.setAttributes(attributes);
		} finally {
			DbUtils.closeQuietly(select_sourcequalifers_rs);
			DbUtils.closeQuietly(select_sourcequalifiers_pstmt);
		}
		return sample;
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
