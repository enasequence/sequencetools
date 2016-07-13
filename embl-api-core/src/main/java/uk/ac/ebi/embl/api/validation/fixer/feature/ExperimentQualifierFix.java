package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

public class ExperimentQualifierFix extends FeatureValidationCheck
{
	private static final String experiment_legacy_value = "experimental evidence, no additional details recorded";
	private static final String ExperimentQualifierFix_ID = "ExperimentQualifierFix-1";
	
	public ExperimentQualifierFix()
	{
	}
	
	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();
		if (feature == null)
		{
			return result;
		}
		
		List<Qualifier> experimentQualifiers = feature.getQualifiers(Qualifier.EXPERIMENT_QUALIFIER_NAME);
		if (experimentQualifiers == null)
			return result;
		int experimentQualifierSize = experimentQualifiers.size();
		if (experimentQualifierSize == 0)
		{
			return result;
		}
		
		if (experimentQualifierSize > 1)
		{
			for (Qualifier qualifier : experimentQualifiers)
			{
				if (experiment_legacy_value.equals(qualifier.getValue()))
				{
					feature.removeQualifier(qualifier);
					reportMessage(Severity.FIX, feature.getOrigin(), ExperimentQualifierFix_ID, Qualifier.EXPERIMENT_QUALIFIER_NAME,
							experiment_legacy_value, feature.getName());
					return result;
				}
			}
		}
		
		return result;
		
	}
}
