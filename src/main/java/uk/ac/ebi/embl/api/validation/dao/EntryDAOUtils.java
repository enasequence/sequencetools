package uk.ac.ebi.embl.api.validation.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public interface EntryDAOUtils
{
	public boolean isValueExists(String tableName, String constraintKey,
			String constraintValue) throws SQLException;

	boolean isEntryExists(String accession) throws SQLException;

	Long getSequenceLength(String accession) throws SQLException;

	ArrayList<Qualifier> getChromosomeQualifiers(String analysisId,
			String submitterAccession,SourceFeature source) throws SQLException;


	public boolean isProjectValid(String project) throws SQLException;

	public HashSet<String> getProjectLocutagPrefix(String project) throws SQLException;

	public String isEcnumberValid(String ecNumber) throws SQLException;

	Entry getEntryInfo(String primaryAcc) throws SQLException;

	public String getDbcode(String prefix) throws SQLException;

	public boolean isChromosomeValid(String analysisId, String chromosomeName) throws SQLException;





}
