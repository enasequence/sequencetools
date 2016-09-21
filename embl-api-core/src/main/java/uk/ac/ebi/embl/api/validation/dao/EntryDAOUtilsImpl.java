package uk.ac.ebi.embl.api.validation.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.check.entry.PrimaryAccessionCheck;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table.cv_fqual_value_fix_record;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;

public class EntryDAOUtilsImpl implements EntryDAOUtils
{
	private Connection connection=null;
	private cv_fqual_value_fix_table cv_fqual_value_fix_table=null;
	private Entry masterEntry= null;
	private Map<String, Entry> masterEntryCache = Collections.synchronizedMap(new HashMap<String, Entry>());
	
	public EntryDAOUtilsImpl(Connection connection) throws SQLException
	{
		this(connection,false);
	}
	
	public EntryDAOUtilsImpl(Connection connection,boolean cvTable) throws SQLException
	{
		this.connection=connection;
		if(cvTable)
		cv_fqual_value_fix_table=get_cv_fqual_value_fix();
	}
	@Override
	public String getPrimaryAcc(String analysisId,
								String objectName,
								int assemblyLevel) throws SQLException
	{
		String sql = "select assigned_acc from gcs_sequence where assembly_id = ? and upper(object_name) = upper(?) and assembly_level = ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sequenceAccession=null;
		
		if(analysisId==null||objectName==null||connection==null)
			return null;
		try
		{
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, analysisId);
			stmt.setString(2, objectName);
			stmt.setInt(3, assemblyLevel);
			rs = stmt.executeQuery();
			
			if(rs.next())	
			{
				sequenceAccession = rs.getString(1);
			}
			
		  return sequenceAccession;
		}
		catch (SQLException ex)
		{
			throw new SQLException();
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
		
	}
	
	@Override
	public String getDataclass(String analysisId,
			                   String objectName,
			                   int assemblyLevel) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String primaryAcc = getPrimaryAcc(analysisId, objectName, assemblyLevel);

			if (primaryAcc == null)
				return null;

			else
			{
				String sql = "select dataclass from dbentry where primaryacc#=?";
				pstmt = connection.prepareStatement(sql);
				pstmt.setString(1, primaryAcc);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					return rs.getString(1);
				}
			}
		} finally
		{

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}

		return null;
	}
	
	@Override
	public byte[] getSequence(String submitterAccession,String analysisID,int assemblyLevel)
			throws SQLException, IOException
	{
		String sql = "select p.seqtext from dbentry d join bioseq b on(d.bioseqid=b.seqid) join physicalseq p on(b.physeq=p.physeqid) where d.primaryacc#=?";
		String objectName = submitterAccession;
		String analysisId = analysisID;
		String primaryAcc = getPrimaryAcc(analysisId, objectName, assemblyLevel);
		if (primaryAcc != null)
		{
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try
			{
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, primaryAcc);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					InputStream is = rs.getBinaryStream(1);
					int len;
					int size = 1024;
					byte[] buf = new byte[size];
					while ((len = is.read(buf, 0, size)) != -1)
					{
						bos.write(buf, 0, len);
					}
					buf = bos.toByteArray();
					return buf;
				} else
					return null;
			} finally
			{
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
			}
		}
		return null;

	}
	
	@Override
	public ContigSequenceInfo getSequenceInfoBasedOnEntryName(String entry_name,String analysisID, int assemblyLevel)
			throws SQLException
	{
		String sql =
				"select assigned_acc,sequence_length from gcs_sequence where upper(object_name) = upper(?) and gcs_sequence.assembly_id = ? and gcs_sequence.assembly_level < ?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.prepareStatement(sql);
			stmt.setString(	1,entry_name);
			stmt.setString(	2,analysisID);
			stmt.setInt(3,assemblyLevel);
			rs = stmt.executeQuery();

			if (!rs.next())
			{
				return null;
			}

			String accession = rs.getString(1);
			int seqLen = rs.getInt(2);
			ContigSequenceInfo sequenceInfo = new ContigSequenceInfo();
			sequenceInfo.setPrimaryAccession(accession);
			sequenceInfo.setSequenceVersion(1);
			sequenceInfo.setSequenceLength(seqLen);
			return sequenceInfo;
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}
	
	@Override
	public byte[] getSubSequence(String accession,Long beginPosition,Long length) throws SQLException, IOException
	{
		String sql = "select substr(seqtext,?,?) from bioseq b join physicalseq p on(b.physeq=p.physeqid) where sequence_acc =? or seq_accid=? ";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ByteBuffer segmentBuffer=ByteBuffer.wrap(new byte[length.intValue()]);
			try
			{
				stmt = connection.prepareStatement(sql);
				stmt.setLong(1, beginPosition);
				stmt.setLong(2, length);
				stmt.setString(3, accession);
				stmt.setString(4,accession);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					InputStream is = rs.getBinaryStream(1);
					
					int size = 1024;
					byte[] buf = new byte[size];
					int len ;
					while ((len=is.read(buf, 0, size)) != -1)
					{
						segmentBuffer.put(buf, 0, len);
					}
					buf = segmentBuffer.array();
					return buf;
				} else
					return null;
			}
			finally
			{
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
			}
		
	}
	
	@Override
	public ArrayList<Qualifier> getChromosomeQualifiers(String analysisId,String submitterAccession, SourceFeature source) throws SQLException
	{
		String sql = "select chromosome_name, chromosome_location, chromosome_type "
				+ "from gcs_chromosome where assembly_id = ? and upper(object_name) = upper(?)";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean virus=false;
		if(source!=null)
		{
		TaxonHelper taxonHelper= new TaxonHelperImpl();
		String scientificName=source.getScientificName();
		 virus=taxonHelper.isChildOf(scientificName, "Viruses");
		}
		ArrayList<Qualifier> qualifiers = new ArrayList<Qualifier>();
		
		QualifierFactory qualifierFactory = new QualifierFactory();
				
		try
		{
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, analysisId);
			stmt.setString(2, submitterAccession); // entry name
			rs = stmt.executeQuery();

			if (!rs.next())
			{
				return qualifiers;
			}
			String chromosomeType = rs.getString(3);
			String chromosomeLocation = rs.getString(2);
			String chromosomeName = rs.getString(1);
			
			if (chromosomeLocation != null && !chromosomeLocation.isEmpty()&& !virus&&!chromosomeLocation.equalsIgnoreCase("Phage"))
			{
				String organelleValue =  SequenceEntryUtils.getOrganelleValue(chromosomeLocation);
				if (organelleValue != null)
				{									
					qualifiers.add(qualifierFactory.createQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, SequenceEntryUtils.getOrganelleValue(chromosomeLocation)));
				}
			}	
			else if (chromosomeName != null && !chromosomeName.isEmpty())
			{
				if (Qualifier.PLASMID_QUALIFIER_NAME.equals(chromosomeType))
				{
					qualifiers.add(qualifierFactory.createQualifier(Qualifier.PLASMID_QUALIFIER_NAME, chromosomeName));
				}
				else if (Qualifier.CHROMOSOME_QUALIFIER_NAME.equals(chromosomeType))
				{
					qualifiers.add(qualifierFactory.createQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, chromosomeName));
				}
				else if("segmented".equals(chromosomeType)||"multipartite".equals(chromosomeType))
				{
					qualifiers.add(qualifierFactory.createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, chromosomeName));

				}
				else if("monopartite".equals(chromosomeType))
				{
					qualifiers.add(qualifierFactory.createQualifier(Qualifier.NOTE_QUALIFIER_NAME, chromosomeType));
				}
			}
			
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}		
		return qualifiers;
	}
	
	public boolean isAssemblyUpdate(String analysisId) throws SQLException
	{
        
		String query = "select gcs_pkg.is_update(?) from dual";

		PreparedStatement pstsmt = null;
		ResultSet rs = null;
		try
		{
			pstsmt = connection.prepareStatement(query);
			pstsmt.setString(1, analysisId);
			rs = pstsmt.executeQuery();
			
			if (rs.next())
		     { String status=rs.getString(1);
				return status.equals("Y");
			}
			
			return false;
			
		}finally
		{

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstsmt);
		}
	}
	
	
	public  String getAssemblyMaster(String analysisId) throws SQLException
	{
        String sql = "{call ? := gcs_pkg.get_assembly_master(?)}";
		CallableStatement cstmt = null;
		try
		{
			cstmt = connection.prepareCall(sql);
			cstmt.registerOutParameter (1, Types.VARCHAR);
			cstmt.setString(2, analysisId);
			cstmt.execute();
			String assemblyMaster=cstmt.getString(1);
			
			return assemblyMaster;
		}
		finally
		{
			DbUtils.closeQuietly(cstmt);
		}
	}
	
	@Override
	public Entry getMasterEntry(String analysisId) throws SQLException
	{
		if(isAssemblyUpdate(analysisId))
		{
			analysisId=getAssemblyMaster(analysisId);
		}
		
		if(masterEntryCache.get(analysisId)!=null)
		{
			return masterEntryCache.get(analysisId);
		}
		masterEntry= (new EntryFactory()).createEntry();
		String masterEntryQuery = "select d.study_id,d.sample_id,d.statusid,cf.fqual,"
	                              +"nvl(fq.text,(select text_value from cv_fqual_value c where c.FQUAL_VALUEID=fq.FQUAL_VALUEID and c.fqualid=fq.fqualid)) text,"
				                  +"n.leaf,n.tax_id from dbentry d "
                                  + "join seqfeature seq using (bioseqid) "
                                  + "join sourcefeature source on (source.featid = seq.featid) "
                                  + "join feature_qualifiers fq on (source.featid = fq.featid "
                                  + "and fq.fqualid in(83,47,40,80,81)) "
                                  + "join cv_fqual cf on (cf.fqualid = fq.fqualid) "
                                  + "join ntx_lineage n on (source.organism = n.tax_id) "
                                  + "where primaryacc# = ?" ;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		SourceFeature sourceFeature= (new FeatureFactory()).createSourceFeature();
		QualifierFactory qualifierFactory = new QualifierFactory();	
		String projectAccession=null;
		String sampleId=null;
		int statusId=0;
		String organism=null;
		Long taxId=null;
		try
		{
			ps = connection.prepareStatement(masterEntryQuery);
			ps.setString(1, analysisId);
			rs = ps.executeQuery();
			while (rs.next())
			{
				projectAccession=rs.getString("study_id");
				sampleId=rs.getString("sample_id");
				statusId=rs.getInt("statusid");
				String qual=rs.getString("fqual");
				String value=rs.getString("text");
				/*
				 * isolate,strain,environmental_sample,mol_type,organism
				 */
				sourceFeature.addQualifier(qualifierFactory.createQualifier(qual,value));
				organism=rs.getString("leaf");
				taxId=rs.getLong("tax_id");
			}
			if(organism!=null)
			sourceFeature.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, organism);
			if(taxId!=null)
			 sourceFeature.setTaxId(taxId);
			masterEntry.addFeature(sourceFeature);
			if(projectAccession!=null)
			masterEntry.getProjectAccessions().add(new Text(projectAccession));
			if(sampleId!=null)
			masterEntry.addXRef(new XRef("BioSample", sampleId));
			if(statusId!=0)
			masterEntry.setStatus(Entry.Status.getStatus(statusId));
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
		
		return masterEntry;
	}	

	@Override
	public boolean isValueExists(String tableName, String constraintKey, String constraintValue) throws SQLException
	{
		String sqlSearchStringTemp = "select 1 from %s where %s ='%s'";
		String sql = String.format(sqlSearchStringTemp, tableName, constraintKey, constraintValue);
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement(sql);
			if (ps.executeQuery(sql).next())
				return true;
			return false;

		} finally
		{
			DbUtils.closeQuietly(ps);

		}

	}
	
	@Override
	public cv_fqual_value_fix_table get_cv_fqual_value_fix()
			throws SQLException
	{
		if (cv_fqual_value_fix_table != null)
		{
			return cv_fqual_value_fix_table;
		}

		ResultSet rs = null;
		PreparedStatement ps = null;
		cv_fqual_value_fix_table=new cv_fqual_value_fix_table();
		try
		{
			ps = connection.prepareStatement("select fqual,regex,value from cv_fqual_value_fix fix,cv_fqual qual where fix.fqualid=qual.fqualid");
			rs = ps.executeQuery();
			while (rs.next())
			{
				cv_fqual_value_fix_record cv_fqual_value_fix_record =  cv_fqual_value_fix_table.create_cv_fqual_value_fix_record();
				cv_fqual_value_fix_record.setFqualName(rs.getString(1));
				cv_fqual_value_fix_record.setRegex(rs.getString(2));
				cv_fqual_value_fix_record.setValue(rs.getString(3));
				cv_fqual_value_fix_table.add(cv_fqual_value_fix_record);

			}

		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
		return cv_fqual_value_fix_table;
	}
	
	@Override
	public boolean isEntryExists(String accession) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from bioseq where sequence_acc=? or seq_accid=?");
			ps.setString(1, accession);
			ps.setString(2,accession);
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
	public Long getSequenceLength(String accession) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select seqlen from bioseq where sequence_acc=? or seq_accid=?");
			ps.setString(1,accession);
			ps.setString(2, accession);
			rs = ps.executeQuery();
			if (rs.next())
			{
				return rs.getLong(1);
			}

			return 0L;
		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
	}

	@Override
	public boolean isAssemblyLevelExists(String analysisId, int assembly_level) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from gcs_sequence where assembly_id=? and assembly_level=?");
			ps.setString(1, analysisId);
			ps.setInt(2,assembly_level);
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
	public boolean isAssemblyLevelObjectNameExists(String assembly_id,String entry_name,int assemblyLevel) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from gcs_sequence where assembly_id=? and object_name=? and assembly_level=?");
			ps.setString(1, assembly_id);
			ps.setString(2, entry_name);
			ps.setInt(3,assemblyLevel);
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
	public boolean isAssemblyEntryUpdate(String assembly_id,String entry_name,int assemblyLevel) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from dbentry where primaryacc# in (select assigned_acc from gcs_sequence where assembly_id=? and assembly_level=? and object_name=?)");
			ps.setString(1, assembly_id);
			ps.setInt(2,assemblyLevel);
			ps.setString(3, entry_name);
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
	public boolean isProjectValid(String project) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select 1 from mv_project where project_id=? or ncbi_project_id=?");
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
	public HashSet<String> getProjectLocutagPrefix(String project) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		HashSet<String> locusTagPrefixes = new HashSet<String>();
		try
		{
			ps = connection.prepareStatement("select upper(locus_tag) from mv_project where project_id=? or ncbi_project_id=?");
			ps.setString(1, project);
			ps.setString(2, project);
			rs = ps.executeQuery();
			while (rs.next())
			{
				locusTagPrefixes.add(rs.getString(1));
			}
			return locusTagPrefixes;
		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}	
		
	}
	
	@Override
	public String isEcnumberValid(String ecNumber) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement("select valid from cv_ec_numbers where ec_number=?");
			ps.setString(1, ecNumber);
			rs = ps.executeQuery();
			if (rs.next())
			{
				return rs.getString(1);
			}
			else
				return null;
		} finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}	
	}
	
	@Override
	public Entry 
	getEntryInfo( String primaryAcc) throws SQLException
	{
		Entry entry =(new EntryFactory()).createEntry();
		ResultSet rs = null;
		PreparedStatement ps = null;
		boolean isValid=false;
		try
		{
			ps = connection.prepareStatement("select entry_name,dataclass,keyword from dbentry "
					+ "left outer join keywords on(dbentry.dbentryid=keywords.dbentryid ) where dbentry.primaryacc#=?");
			ps.setString(1, primaryAcc);
			rs = ps.executeQuery();
			while(rs.next())
			{
				isValid=true;
				entry.setSubmitterAccession(rs.getString("entry_name"));
				entry.setDataClass(rs.getString("dataclass"));
				entry.addKeyword(new Text(rs.getString("keyword")));
			}
			if(!isValid)
			{
				return null;
			}
		}finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
		
		return entry;
		
	}

	@Override
	public String getAccessionDataclass(String prefix)	throws SQLException 
	{
	  String sql=	"select datclass from cv_database_prefix where prefix= ?";
	  ResultSet rs = null;
	  PreparedStatement ps = null;
		try
		{
			ps = connection.prepareStatement(sql);
			ps.setString(1, prefix);
			rs = ps.executeQuery();
			if(rs.next())
			{
    			return rs.getString(1);
			}
		}finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
		
		
	  return null;
	}

	@Override
	public String getDbcode(String prefix) throws SQLException {
		if(prefix==null)
			return null;
		 String sql=	"select dbcode from cv_database_prefix where prefix= ?";
		  ResultSet rs = null;
		  PreparedStatement ps = null;
			try
			{
				ps = connection.prepareStatement(sql);
				ps.setString(1, prefix);
				rs = ps.executeQuery();
				if(rs.next())
				{
	    			return rs.getString(1);
				}
			}finally
			{
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(ps);
			}
			
			
		  return null;
	}
}
