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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Invalid Location:Complement ignored in transl_except")
public class Transl_exceptLocationFix extends FeatureValidationCheck
{

	private static final String Transl_ExceptValueFix_ID = "transl_exceptLocationFix";
	private static final Pattern COMPATTERN = Pattern
			.compile("^\\s*\\(\\s*pos\\s*:\\s*(?:complement\\s*\\(\\s*(?:(\\d+)(?:\\s*\\.\\.\\s*(\\d+)){0,1})\\s*\\))\\s*,\\s*aa\\s*:\\s*([^)\\s]+)\\s*\\)\\s*$");

	public Transl_exceptLocationFix()
	{
	}

	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}

		for (Qualifier tequalifier : feature.getQualifiers(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME))
		{

			TranslExceptQualifier tQualifier = (TranslExceptQualifier) tequalifier;
			String teValue = tequalifier.getValue();
			Matcher compmatcher = COMPATTERN.matcher(teValue);
			if (compmatcher.matches())
			{
				StringBuffer fixedValue = new StringBuffer("(pos:");

				try
				{
					Location location = tQualifier.getLocation();
					String begin = location.getBeginPosition().toString();
					String end = location.getEndPosition().toString();
					fixedValue.append(begin);
					if (!begin.equals(end))
					{
						fixedValue.append("..");
						fixedValue.append(end);
					}
					fixedValue.append(",aa:" + tQualifier.getAminoAcid().getAbbreviation() + ")");
					tQualifier.setValue(fixedValue.toString());
					reportMessage(Severity.FIX, tequalifier.getOrigin(), Transl_ExceptValueFix_ID);
				} catch (ValidationException e)
				{

				}
			}

		}

		return result;
	}

}
