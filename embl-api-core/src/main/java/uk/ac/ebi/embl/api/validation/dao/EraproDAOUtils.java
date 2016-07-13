package uk.ac.ebi.embl.api.validation.dao;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.reference.Reference;

public interface EraproDAOUtils
{
	Reference getSubmitterReference(String analysisId) throws SQLException,UnsupportedEncodingException;
	
}
