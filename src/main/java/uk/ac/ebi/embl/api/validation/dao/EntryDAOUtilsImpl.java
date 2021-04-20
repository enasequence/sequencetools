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
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.dbutils.DbUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;

public class EntryDAOUtilsImpl implements EntryDAOUtils
{
	private Connection connection=null;
	
	public EntryDAOUtilsImpl(Connection connection) throws SQLException
	{
		this(connection,false);
	}
	
	public EntryDAOUtilsImpl(Connection connection,boolean cvTable) throws SQLException
	{
		this.connection=connection;
	}
	@Override
	public byte[] getSequence(String primaryAcc)
			throws SQLException, IOException
	{
		String sql = "select p.seqtext from dbentry d join bioseq b on(d.bioseqid=b.seqid) join physicalseq p on(b.physeq=p.physeqid) where d.primaryacc#=?";
		
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

	@Override
	public boolean isChromosomeValid(String analysisId,String chromosomeName) throws SQLException
	{
         String sql = "select 1 from gcs_chromosome where assembly_id = ? and chromosome_name = ?";
		
 		try(PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setString(1, analysisId);
			ps.setString(2, chromosomeName);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
			{
				return false;		
			}
           return true;
		}
	}

	

  
}
