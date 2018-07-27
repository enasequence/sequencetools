/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.AminoAcid;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Anticodon validation")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class AntiCodonQualifierCheck extends FeatureValidationCheck
	{
		private final static String ANTICODON_LOCATION_RANGE_MESSAGE_ID = "AntiCodonQualifierCheck_1";
		private final static String ANTICODON_SEGMENT_MESSAGE_ID = "AntiCodonQualifierCheck_2";
		private final static String ANTICODON_SEGMENT_START_MESSAGE_ID = "AntiCodonQualifierCheck_3";
		private final static String ANTICODON_SEGMENT_END_MESSAGE_ID = "AntiCodonQualifierCheck_4";
		private final static String ANTICODON_AMINO_ACID_MESSAGE_ID = "AntiCodonQualifierCheck_5";
		private final static String ANTICODON_AMINO_ACID_NOT_MATCH_MESSAGE_ID = "AntiCodonQualifierCheck_6";

		public AntiCodonQualifierCheck()
			{
			}

		public ValidationResult check(Feature feature)
			{
				result = new ValidationResult();
				if (feature == null)
					{
						return result;
					}
				try
					{
						AminoAcid aminoAcid = null;
						Long start = null, end = null;
						Qualifier antiCodonQualifier = feature
								.getSingleQualifier(Qualifier.ANTICODON_QUALIFIER_NAME);
						if (antiCodonQualifier == null)
							{
								return result;
							}
						CompoundLocation<Location> featureLocation = feature.getLocations();
						if(featureLocation!=null)
						{
							for(Location location : featureLocation.getLocations())
							{
								if(location instanceof RemoteLocation)
									return result;
							}
						}
						AnticodonQualifier antiCodon = new AnticodonQualifier(
								antiCodonQualifier.getValue());
					CompoundLocation<Location> antiCodonLocation = antiCodon.getLocations();
						Long beginPosition = featureLocation.getMinPosition();
						Long endPosition = featureLocation.getMaxPosition();
						start = antiCodonLocation.getMinPosition();
						end = antiCodonLocation.getMaxPosition();
						Long anticodonLocationLength=antiCodonLocation.getLength();
						aminoAcid = antiCodon.getAminoAcid();
						String aminoAcidString=antiCodon.getAminoAcidString();
						if (aminoAcid == null)
							{
								reportError(antiCodonQualifier.getOrigin(),
										ANTICODON_AMINO_ACID_MESSAGE_ID);
							}
						if(aminoAcid!=null&&aminoAcid.getAbbreviation()!=null)
						{
							if(!aminoAcid.getAbbreviation().equals(aminoAcidString))
							{
								reportWarning(feature.getOrigin(),ANTICODON_AMINO_ACID_NOT_MATCH_MESSAGE_ID,aminoAcidString,aminoAcid.getAbbreviation());
							}
						}
						if (start < beginPosition || end > endPosition)
							{
								reportError(antiCodonQualifier.getOrigin(),
										ANTICODON_LOCATION_RANGE_MESSAGE_ID, start, end);
							}
						if (start > end)
							{
								reportError(antiCodonQualifier.getOrigin(),
										ANTICODON_SEGMENT_MESSAGE_ID, start, end);
							}
						if (start == 0)
							{
								reportError(antiCodonQualifier.getOrigin(),
										ANTICODON_SEGMENT_START_MESSAGE_ID, start);
							}
						if (anticodonLocationLength!=3)
							{
								reportError(antiCodonQualifier.getOrigin(),
										ANTICODON_SEGMENT_END_MESSAGE_ID,
										3);
							}
					} catch (ValidationException exception)
					{
						reportException(result, exception,feature);
					}
				return result;
			}
		private void reportException(ValidationResult result, ValidationException exception,Feature feature)
		{
			ValidationMessage<Origin> message = exception.getValidationMessage();
			message.append(feature.getOrigin());
			result.append(message);
		}
	}
