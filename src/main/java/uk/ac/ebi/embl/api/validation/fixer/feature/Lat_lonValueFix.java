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

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("")
public class Lat_lonValueFix extends FeatureValidationCheck {

	private static final String Lat_lonValueFix_ID = "Lat_lonValueFix";

	public Lat_lonValueFix() {
	}

	public ValidationResult check(Feature feature) {
		result = new ValidationResult();

		if (feature == null) {
			return result;
		}
		List<Qualifier> latlonQualifiers=feature.getQualifiers(Qualifier.LAT_LON_QUALIFIER_NAME);
		
		if(latlonQualifiers.isEmpty())
		{
			return result;
		}
		String regex="^\\s*(-{0,1}\\s*\\d+(\\.\\d+){0,1})\\s+(S|N)\\s*,{0,1}\\s+(-{0,1}\\s*\\d+(\\.\\d+){0,1})\\s+(W|E)\\s*$";
		Pattern pattern = Pattern.compile(regex);

		for (Qualifier qualifier : latlonQualifiers) {
			if(qualifier.getValue()==null)
				continue;
			Double lat = null, lon=null;
			String direcSN,direcWE,latValue,lonValue;
			Matcher matcher = pattern.matcher(qualifier.getValue());
			if (matcher.find()) {
				latValue = matcher.group(1);
				lonValue = matcher.group(4);
				direcSN = matcher.group(3);
				direcWE = matcher.group(6);
				DecimalFormat df = new DecimalFormat("#.####");
				String newValue="";
				if (latValue != null) {
					lat = new Double(latValue);
					lat = Double.parseDouble(df.format(lat));
					newValue += lat+" ";
					if(direcSN!=null)
					newValue+=direcSN+ " ";
				}

				if (lonValue != null) {
					lon = new Double(lonValue);
					lon =Double.parseDouble(df.format(lon));
					newValue+=lon+" ";
					if(direcWE!=null)
						newValue+=direcWE;
				}
				if(!qualifier.getValue().equals(newValue))
				{
					reportMessage(Severity.FIX, qualifier.getOrigin(),
						Lat_lonValueFix_ID,qualifier.getValue(), newValue);
					qualifier.setValue(newValue);
				}
			}
				
			}
		
		return result;
	}

}
