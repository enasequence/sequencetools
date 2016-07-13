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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.Vector;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class FeatureLocationsMatcher extends FlatFileMatcher {
	
	private boolean isIgnoreLocationParseError=false;
		public FeatureLocationsMatcher(FlatFileLineReader reader,boolean ignoreParseError) {
		super(reader, PATTERN);
		this.reader = reader;
		this.isIgnoreLocationParseError=ignoreParseError;
	}
	
	private FlatFileLineReader reader;
	private static final Pattern simpleLocationPattern = Pattern.compile("(\\d+)(..)(\\d+)");

	private static final Pattern PATTERN = Pattern.compile(
		"(?:(\\s*complement\\s*\\()?\\s*((?:join)|(?:order)))?\\s*\\(?(.*)");	
	
	private static final int GROUP_COMPLEMENT = 1;
	private static final int GROUP_OPERATOR = 2;
	private static final int GROUP_ELEMENTS = 3;

	public CompoundLocation<Location> getCompoundLocation() {
 		CompoundLocation<Location> compoundLocation = new Join<Location>();		
		boolean isComplement = isValue(GROUP_COMPLEMENT);
		if (isValue(GROUP_OPERATOR)) {
			if (getString(GROUP_OPERATOR).equals("order")) {
				compoundLocation = new Order<Location>();
			} 
		}
		if (isComplement) {
			compoundLocation.setComplement(true);
		}
		
		Vector<String> element = FlatFileUtils.split(getString(GROUP_ELEMENTS), ",");
		int elementCount = element.size(); 
		if (elementCount == 0) {
			error("FT.4"); // Invalid feature location.
			return null;
		}
		
		if (elementCount == 1 && simpleLocationPattern.matcher(element.get(0)).matches())
		{
			compoundLocation.setSimpleLocation(true);
		}
		
		for (int i = 0 ; i < elementCount ; ++i) {
			FeatureLocationMatcher featureLocationMatcher = new FeatureLocationMatcher(reader);
			if(!featureLocationMatcher.match(element.get(i))) {
				error("FT.8", element.get(i)); // Invalid feature location.
				return null;
			}
			Location location = featureLocationMatcher.getLocation();
			boolean isComp=location.isComplement();
			if (featureLocationMatcher.isLeftPartial())
			{
				if (!isComp && i == 0)
				{
					compoundLocation.setLeftPartial(true);
				}
				else if (isComp && i == elementCount - 1)
				{
					compoundLocation.setRightPartial(true);
				}
				else if (!isIgnoreLocationParseError)
				{
					error("FT.8", element.get(i));
					return null;
				}

			}
			if (featureLocationMatcher.isRightPartial())
			{
				if (isComp && i == 0)
				{
					compoundLocation.setLeftPartial(true);
				}
				else if (!isComp && i == elementCount - 1)
				{
					compoundLocation.setRightPartial(true);
				}
				else if (!isIgnoreLocationParseError)
				{
						error("FT.8", element.get(i));
						return null;
				}
				
			}
            compoundLocation.addLocation(location);
        }

        //COMMENTED THIS OUT - MOVED THIS LOGIC INTO THE TRANSLATOR "CONFIGURE_FROM_FEATURE" method - this is where it matters
        /**
         * Finally, look at the compound location complement status. If is complement, reverse the left and right
         * partiallity as the whole thing is actually the other way round... This needs to be reversed when writing
         * the flat files out again
         */
/*
        if (compoundLocation.isComplement() &&
                compoundLocation.isLeftPartial() != compoundLocation.isRightPartial()) {

            compoundLocation.setLeftPartial(!compoundLocation.isLeftPartial());//reverse it
            compoundLocation.setRightPartial(!compoundLocation.isRightPartial());//reverse it
        }
*/

        return compoundLocation;
	}	
	
}
