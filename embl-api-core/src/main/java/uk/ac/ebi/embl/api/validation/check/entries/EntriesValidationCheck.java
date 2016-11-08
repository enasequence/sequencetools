package uk.ac.ebi.embl.api.validation.check.entries;

import java.sql.SQLException;
import java.util.ArrayList;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.EntryValidations;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationCheck;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class EntriesValidationCheck implements EmblEntryValidationCheck<ArrayList<Entry>>
{
	protected ValidationResult result;
	private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils=null;

	@Override
	public ValidationResult check(ArrayList<Entry> object)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPopulated()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPopulated()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Creates an error validation message for the entry and adds it to the
	 * validation result.
	 * 
	 * @param origin
	 * @param messageKey
	 *            a message key
	 * @param params
	 *            message parameters
	 */
	protected ValidationMessage<Origin> reportError(Origin origin, String messageKey, Object... params)
	{
		return reportMessage(Severity.ERROR, origin, messageKey, params);
	}

	/**
	 * Creates a warning validation message for the entry and adds it to the
	 * validation result.
	 * 
	 * @param origin
	 * @param messageKey
	 *            a message key
	 * @param params
	 *            message parameters
	 */
	protected ValidationMessage<Origin> reportWarning(Origin origin, String messageKey, Object... params)
	{
		return reportMessage(Severity.WARNING, origin, messageKey, params);
	}

	/**
	 * Creates a validation message for the entry and adds it to the validation
	 * result.
	 * 
	 * @param severity
	 *            message severity
	 * @param origin
	 * @param messageKey
	 *            a message key
	 * @param params
	 *            message parameters
	 */
	protected ValidationMessage<Origin> reportMessage(Severity severity, Origin origin, String messageKey, Object... params)
	{
		ValidationMessage<Origin> message = EntryValidations.createMessage(origin, severity, messageKey, params);
		result.append(message);
		return message;
	}

	@Override
	public void setEmblEntryValidationPlanProperty(
			EmblEntryValidationPlanProperty property) throws SQLException
	{
		this.property=property;
	}

	@Override
	public EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty()
	{
		return property;
	}

	@Override
	public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils)
	{
		this.entryDAOUtils=entryDAOUtils;
		
	}

	@Override
	public EntryDAOUtils getEntryDAOUtils()
	{
		// TODO Auto-generated method stub
		return entryDAOUtils;
	}

	@Override
	public EraproDAOUtils getEraproDAOUtils() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEraproDAOUtils(EraproDAOUtils daoUtils) {
		// TODO Auto-generated method stub
		
	}

	
}
