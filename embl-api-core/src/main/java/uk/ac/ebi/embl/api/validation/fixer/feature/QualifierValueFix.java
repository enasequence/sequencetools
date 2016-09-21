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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table;

@Description("Qualifier \"{0}\" Value has been changed from \"{1}\" to \"{2}\"" +
		"Deleted Qualifiers from feature \"{0}\" having value \"DELETED\"")
public class QualifierValueFix extends FeatureValidationCheck
{

	private static final String QualifierValueFix_ID_1 = "QualifierValueFix_1";
	private static final String QualifierValueFix_ID_2 = "QualifierValueFix_2";
	private static final String QualifierValueFix_ID_3 = "QualifierValueFix_3";
	private static final String QualifierValueFix_ID_4 = "QualifierValueFix_4";

    private Set<String> qualifierswithFixedValue=new HashSet<String>();

	public QualifierValueFix()
	{
		
	}

	public ValidationResult check(Feature feature)
	{
		ArrayList<Qualifier> deleteQualifierList = new ArrayList<Qualifier>();
		ArrayList<Qualifier> invalidEcnumberList = new ArrayList<Qualifier>();
		try
		{ 
			result = new ValidationResult();
			if (feature == null )
			{
				return result;
			}
			for (Qualifier qual : feature.getQualifiers())
			{
				String qName = qual.getName();
				String qValue = qual.getValue();
				if(qValue!=null&&qValue.contains("\""))
				{
					qValue=qValue.replaceAll("\"", "\'");
					qual.setValue(qValue);
					reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_3,qName);
				}
				if (qName.equals(Qualifier.EC_NUMBER_QUALIFIER_NAME))
				{
					String ec[] = qValue.split("\\.");
					qValue = ec[0] + "\\." + ec[1] + "\\." + ec[2] + "\\." + ec[3];
					if((qValue.equals("-\\.-\\.-\\.-")))
					{
						invalidEcnumberList.add(qual);
					}
				}
				if(qName.equals(Qualifier.ALTITUDE_QUALIFIER_NAME)&&qValue.endsWith("m."))
				{
					qual.setValue(qValue.substring(0, qValue.length()-1));
					reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_1,qName,qValue,qual.getValue());

				}
				if (getEntryDAOUtils() != null)
				{
					cv_fqual_value_fix_table cv_fqual_value_fix_table=getEntryDAOUtils().get_cv_fqual_value_fix();
					if(cv_fqual_value_fix_table!=null)
					{
					if(qualifierswithFixedValue.isEmpty())
					qualifierswithFixedValue=cv_fqual_value_fix_table.getUniqueNames();
					
					if (qualifierswithFixedValue.contains(qName))
					{
							HashMap<String, String> regexMap = cv_fqual_value_fix_table.getQualifierValueMap(qName);
							if (regexMap.containsKey(qValue))
							{
								qual.setValue(regexMap.get(qValue));
								if((qValue.equals("-\\.-\\.-\\.-")&&qName.equals(Qualifier.EC_NUMBER_QUALIFIER_NAME)))
								{
									invalidEcnumberList.add(qual);
								}

								if ("DELETED".equals(regexMap.get(qValue)))
									deleteQualifierList.add(qual);
								reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_1, qName,qValue, regexMap.get(qValue));
							}
						
					}
				}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		if (deleteQualifierList.size() != 0 && SequenceEntryUtils.deleteDeletedValueQualifiers(feature, deleteQualifierList))
		{
			reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_2, feature.getName());
		}
		if(invalidEcnumberList.size()!=0 && SequenceEntryUtils.deleteDeletedValueQualifiers(feature, invalidEcnumberList))
		{
			reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_4, feature.getName());

		}
		return result;
	}




}
