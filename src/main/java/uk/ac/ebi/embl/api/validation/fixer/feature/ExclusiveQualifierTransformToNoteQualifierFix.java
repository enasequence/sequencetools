package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class ExclusiveQualifierTransformToNoteQualifierFix extends
		FeatureValidationCheck
{

	private final static String REMOVE_QUALIFIER_MESSAGE_ID = "ExclusiveQualifierTransformToNoteQualifierFix";

	public ExclusiveQualifierTransformToNoteQualifierFix()
	{
	}

	public ValidationResult check(Feature feature)
	{
		DataSet exclusiveQualifierSet = GlobalDataSets.getDataSet(GlobalDataSetFile.EXCLUSIVE_QUALIFIERS_TO_REMOVE);
		result = new ValidationResult();

		if (feature == null || feature.getQualifiers().isEmpty())
		{
			return result;
		}

		for (DataRow dataRow : exclusiveQualifierSet.getRows())
		{
			String qualifiertoRemove = dataRow.getString(0);
			String exclusiveQualifier = dataRow.getString(1);
			if (SequenceEntryUtils.isQualifierAvailable(qualifiertoRemove, feature) && SequenceEntryUtils.isQualifierAvailable(exclusiveQualifier, feature))
			{
				Qualifier qualifier = feature.getSingleQualifier(qualifiertoRemove);
				String texttoAppend = qualifier.getValue();
				if (SequenceEntryUtils.isQualifierAvailable(Qualifier.NOTE_QUALIFIER_NAME, feature))
				{
					feature.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).setValue(feature.getSingleQualifierValue(Qualifier.NOTE_QUALIFIER_NAME) + ";" + texttoAppend);
				} else
				{
					QualifierFactory qualifierFactory = new QualifierFactory();
					Qualifier noteQualifier = qualifierFactory.createQualifier(Qualifier.NOTE_QUALIFIER_NAME, texttoAppend);
					feature.addQualifier(noteQualifier);

				}
				feature.removeQualifier(qualifier);
				reportMessage(Severity.FIX, feature.getOrigin(), REMOVE_QUALIFIER_MESSAGE_ID, qualifiertoRemove);

			}
		}

		return result;
	}

}
