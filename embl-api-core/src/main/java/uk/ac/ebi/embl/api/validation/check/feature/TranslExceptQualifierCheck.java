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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("transl_except validation")
public class TranslExceptQualifierCheck extends FeatureValidationCheck
	{
		private final static String TRANSL_EXCEPT_LOCATION_RANGE_MESSAGE_ID = "TranslExceptQualifierCheck_1";
		private final static String TRANSL_EXCEPT_SEGMENT_MESSAGE_ID = "TranslExceptQualifierCheck_2";
		private final static String TRANSL_EXCEPT_SEGMENT_START_MESSAGE_ID = "TranslExceptQualifierCheck_3";
		private final static String TRANSL_EXCEPT_SEGMENT_END_MESSAGE_ID = "TranslExceptQualifierCheck_4";
		private final static String TRANSL_EXCEPT_AMINO_ACID_MESSAGE_ID = "TranslExceptQualifierCheck_5";
		private final static String TRANSL_EXCEPT_AMINO_ACID_NOT_MATCH_MESSAGE_ID = "TranslExceptQualifierCheck_6";

		public void TranslationExceptionQualifierCheck()
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
						Qualifier translExceptQual = feature
								.getSingleQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME);
						if (translExceptQual == null)
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
						TranslExceptQualifier translExceptQualifier = new TranslExceptQualifier(
								translExceptQual.getValue());
					CompoundLocation<Location> antiCodonLocation = translExceptQualifier.getLocations();
						Long beginPosition = featureLocation.getMinPosition();
						Long endPosition = featureLocation.getMaxPosition();
						start = antiCodonLocation.getMinPosition();
						end = antiCodonLocation.getMaxPosition();
						Long anticodonLocationLength=antiCodonLocation.getLength();
						aminoAcid = translExceptQualifier.getAminoAcid();
						String aminoAcidString=translExceptQualifier.getAminoAcidString();
						if (aminoAcid == null)
							{
								reportError(translExceptQualifier.getOrigin(),
										TRANSL_EXCEPT_AMINO_ACID_MESSAGE_ID);
							}
						if(aminoAcid!=null&&aminoAcid.getAbbreviation()!=null)
						{
							if(!aminoAcid.getAbbreviation().equals(aminoAcidString))
							{
								reportWarning(feature.getOrigin(),TRANSL_EXCEPT_AMINO_ACID_NOT_MATCH_MESSAGE_ID,aminoAcidString,aminoAcid.getAbbreviation());
							}
						}
						if (start < beginPosition || end > endPosition)
							{
								reportError(translExceptQualifier.getOrigin(),
										TRANSL_EXCEPT_LOCATION_RANGE_MESSAGE_ID, start, end);
							}
						if (start > end)
							{
								reportError(translExceptQualifier.getOrigin(),
										TRANSL_EXCEPT_SEGMENT_MESSAGE_ID, start, end);
							}
						if (start == 0)
							{
								reportError(translExceptQualifier.getOrigin(),
										TRANSL_EXCEPT_SEGMENT_START_MESSAGE_ID, start);
							}
						if (anticodonLocationLength!=3)
							{
								reportError(translExceptQualifier.getOrigin(),
										TRANSL_EXCEPT_SEGMENT_END_MESSAGE_ID,
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
