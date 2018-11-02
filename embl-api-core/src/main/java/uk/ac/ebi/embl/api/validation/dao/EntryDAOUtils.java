package uk.ac.ebi.embl.api.validation.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table;

public interface EntryDAOUtils
{
	public String getPrimaryAcc(String analysisId, 
			                    String objectName,
			                    int assemblyLevel) throws SQLException;

	public String getDataclass(String analysisId, 
			                   String objectName,
			                   int assemblyLevel) throws SQLException;

	public byte[] getSequence(String primaryAcc)throws SQLException,IOException;
	
	public byte[] getSubSequence(String accession,Long beginPosition,Long length) throws SQLException, IOException;
	

	public boolean isValueExists(String tableName, String constraintKey,
			String constraintValue) throws SQLException;

	boolean isEntryExists(String accession) throws SQLException;

	Long getSequenceLength(String accession) throws SQLException;

	ContigSequenceInfo getSequenceInfoBasedOnEntryName(String entry_name,
												String analysisID,
												int assemblyLevel)	throws SQLException;

	ArrayList<Qualifier> getChromosomeQualifiers(String analysisId,
			String submitterAccession,SourceFeature source) throws SQLException;

	public boolean isAssemblyLevelExists(String analysisId,int assembly_level) throws SQLException;
	
	public boolean isAssemblyEntryUpdate(String analysisId,String entry_name,int assembly_level) throws SQLException;
	
	public boolean isAssemblyLevelObjectNameExists(String analysis_id,String entry_name,int assemblyLevel) throws SQLException;
	
	public boolean isProjectValid(String project) throws SQLException;
	
	public HashSet<String> getProjectLocutagPrefix(String project)throws SQLException;
	
	Entry getMasterEntry(String analysisId) throws SQLException;

	public String isEcnumberValid(String ecNumber) throws SQLException;

	Entry getEntryInfo(String primaryAcc) throws SQLException;
	
	public String getAccessionDataclass(String prefix) throws SQLException;
	
	public String getDbcode(String prefix) throws SQLException;
	
	public String getAssemblyEntryAccession(String remoteAccession,String assemblyId) throws SQLException;
	
	public boolean isAssemblyUpdateSupported(String analysisId) throws SQLException;
	public boolean isChromosomeValid(String analysisId, String chromosomeName) throws SQLException;
	public String associate_unlocalised_list_acc (String objectName, int assembly_level,String analysisId) throws SQLException;
	public void registerSequences(List<String> sequences,String analysisId,int assemblyLevel) throws SQLException;
	public ConcurrentHashMap<String,Integer> getAssemblysequences(String analysisId) throws SQLException;


	
}
