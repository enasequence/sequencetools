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
package uk.ac.ebi.embl.api.validation.check.entry;

import org.apache.commons.lang.math.NumberUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.ArrayList;
import java.util.Collection;

@Description("\"\\gap_type\" and \"\\linkage_evidence\" qualifiers are only allowed in assembly_gap feature."
		+ "\"assembly_gap\" and \"gap\" feature are mutually exclusive"
		+ "\"linkage_evidence\" qualifier must exists in feature \"assembly_gap\",if qualifier \"gap_type\" value equals to \"{0}\"."
		+ "\"linkage_evidence\" qualifier is  allowed in \"assembly_gap\" feature only when \"gap_type\" qualifier value equals to \"within scaffold\" or \"repeat within scaffold\"."
		+ "\"assembly_gap\" feature location must be a simple interval i.e X..Y.")
public class Assembly_gapFeatureCheck extends EntryValidationCheck
{

	protected final static String NO_ASSEMBLY_GAP_MESSAGE = "Assembly_gapFeatureCheck_1";
	protected static final String GAP_FEATURE_MESSAGE = "Assembly_gapFeatureCheck_2";
	protected static final String LINKAGE_EVIDENCE_MISSING_MESSAGE = "Assembly_gapFeatureCheck_5";
	protected static final String LINKAGE_EVIDENCE_MISSING_MESSAGE_TSA = "Assembly_gapFeatureCheck_9";
	protected static final String LINKAGE_EVIDENCE_INVALID_MESSAGE_TSA = "Assembly_gapFeatureCheck_11";
	protected static final String ESTIMATED_LENGTH__MESSAGE_TSA = "Assembly_gapFeatureCheck_10";
	protected static final String GAP_TYPE_MESSAGE_TSA = "Assembly_gapFeatureCheck_12";
	protected static final String LINKAGE_EVIDENCE_NOTALLOWED_MESSAGE = "Assembly_gapFeatureCheck_6";
	protected static final String INVALID_LOCATION_MESSAGE = "Assembly_gapFeatureCheck_7";
	protected static final String INVALID_QUALIFIER_MESSAGE = "Assembly_gapFeatureCheck_8";

	public Assembly_gapFeatureCheck()
	{

	}

	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		Collection<Feature> assemblygapFeatures = SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		Collection<Feature> gapFeatures = SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry);
		Collection<Qualifier> gaptypeQualifiers = SequenceEntryUtils.getQualifiers(Qualifier.GAP_TYPE_QUALIFIER_NAME, entry);
		Collection<Qualifier> linkageevidenceQualifiers = SequenceEntryUtils.getQualifiers(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, entry);
		ArrayList<String> permittedQualifiers = new ArrayList<String>();
		permittedQualifiers.add(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
		permittedQualifiers.add(Qualifier.GAP_TYPE_QUALIFIER_NAME);
		permittedQualifiers.add(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);

		if (assemblygapFeatures.isEmpty() && !(gaptypeQualifiers.isEmpty() && linkageevidenceQualifiers.isEmpty()))
		{
			reportError(entry.getOrigin(), NO_ASSEMBLY_GAP_MESSAGE);
		}
		if (!assemblygapFeatures.isEmpty())

		{
			if (!gapFeatures.isEmpty())
			{
				reportError(entry.getOrigin(), GAP_FEATURE_MESSAGE);
				return result;
			}

			/*
			 * if(entry.getContigs().isEmpty()) {
			 * reportError(entry.getOrigin(),CO_LINE_MESSAGE); return result; }
			 * if(entry.getSequence()!=null&&
			 * entry.getSequence().getSequence()!=null&&
			 * entry.getSequence().getSequence().length()!=0) {
			 * reportError(entry.getOrigin(),SEQUENCE_MESSAGE); return result; }
			 */
		}
		for (Feature assemblygapFeature : assemblygapFeatures)
		{
			Qualifier gap_typeQualifier = assemblygapFeature.getSingleQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
			boolean linkageEvidenceExists = (!assemblygapFeature.getQualifiers(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME).isEmpty());
			boolean isTsa = entry.getDataClass()!=null&&entry.getDataClass().equals(Entry.TSA_DATACLASS);
			/*
			 * mandatory qualifiers for assembly_gaps in TSAs :
			 * ====================================== 
			 * /estimated_length
			 * /gap_type 
			 * /linkage_evidence
			 *  permitted values : 
			 *  ==================
			 * /estimated_length =<integer> 
			 * /gap_type="within scaffold"
			 * /linkage_evidence="paired-ends", "align genus", "align xgenus",
			 * "align trnscpt", "within clone", "clone contig", "map",
			 * "strobe","pcr" BUT NOT "unspecified"
			 */
			// TSA Begin
			if (isTsa)
			{
				if (!NumberUtils.isNumber(assemblygapFeature.getSingleQualifierValue(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME)))
				{
					reportError(assemblygapFeature.getOrigin(), ESTIMATED_LENGTH__MESSAGE_TSA);
				}

				if (!linkageEvidenceExists)
				{
					reportError(assemblygapFeature.getOrigin(), LINKAGE_EVIDENCE_MISSING_MESSAGE_TSA);

				} else if (assemblygapFeature.getSingleQualifierValue(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME).equalsIgnoreCase("unspecified"))
				{
					reportError(assemblygapFeature.getOrigin(), LINKAGE_EVIDENCE_INVALID_MESSAGE_TSA);
				}
				if (gap_typeQualifier != null && !gap_typeQualifier.getValue().equals("within scaffold"))
				{
					reportError(assemblygapFeature.getOrigin(), GAP_TYPE_MESSAGE_TSA, gap_typeQualifier.getValue());
				}

			}// TSA End
				// CON Begin
			else if (gap_typeQualifier != null
					&& (gap_typeQualifier.getValue().equals("within scaffold") 
					    || gap_typeQualifier.getValue().equals("repeat within scaffold")))
			{
				if (!linkageEvidenceExists)
				{
					reportError(assemblygapFeature.getOrigin(), LINKAGE_EVIDENCE_MISSING_MESSAGE, gap_typeQualifier.getValue());
					return result;
				}
			} else
			{
				if (linkageEvidenceExists)
				{
					reportError(assemblygapFeature.getOrigin(), LINKAGE_EVIDENCE_NOTALLOWED_MESSAGE);
					return result;
				}

			}// End CON

			if (assemblygapFeature.getLocations() != null && !assemblygapFeature.getLocations().isSimpleLocation())
			{
				reportError(assemblygapFeature.getOrigin(), INVALID_LOCATION_MESSAGE);
				return result;
			}
			for (Qualifier assemblyQualifier : assemblygapFeature.getQualifiers())
			{
				if (!permittedQualifiers.contains(assemblyQualifier.getName()))
				{
					reportError(assemblygapFeature.getOrigin(), INVALID_QUALIFIER_MESSAGE, assemblyQualifier);
					return result;
				}
			}

		}
		return result;

	}

}
