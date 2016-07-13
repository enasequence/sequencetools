package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class FeatureLengthCheck extends FeatureValidationCheck
{
	
	private final static String FEATURE_LENGTH_CHECK_ID = "FeatureLengthCheck-1";
	private final static long INTRON_FETURE_LENGTH = 10;
	private final static long EXON_FETURE_LENGTH = 15;
	
	@Override
	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();
		if (feature == null)
			return result;
		if (feature.getLocations() == null)
			return result;
		String featureName = feature.getName();
		Long length = feature.getLength();
		if (length == null)
			return result;
		if ((Feature.INTRON_FEATURE_NAME.equals(featureName) && length.longValue() < INTRON_FETURE_LENGTH))
		{
			reportWarning(feature.getOrigin(), FEATURE_LENGTH_CHECK_ID, featureName, INTRON_FETURE_LENGTH);
		}
		else if (Feature.EXON_FEATURE_NAME.equals(featureName) && length.longValue() < EXON_FETURE_LENGTH)
		{
			reportWarning(feature.getOrigin(), FEATURE_LENGTH_CHECK_ID, featureName, EXON_FETURE_LENGTH);
		}
		
		return result;
		
	}
}
