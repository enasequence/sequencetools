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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class FeatureReader extends FlatFileLineReader {
	
	boolean skipSource=false;

    public FeatureReader(LineReader lineReader) {
    	super(lineReader);
    }

	public FeatureReader(LineReader lineReader,boolean skipSource) {
    	super(lineReader);
    	this.skipSource=skipSource;
    }
    private static final int LOCATION_BEGIN_POS = 21;
    private static final int QUALIFIER_BEGIN_POS = 21;
    int quotecount=0;

	protected void readLines() throws IOException {
		// The feature names must appear in the correct position.
		// The feature qualifiers must appear in the correct position.
		// The feature locations must appear in the correct position.
		// The feature locations are terminated by a feature name or
		// a feature location.
		// The qualifier value must continue in the correct position.
		// The qualifier value can only continue if the qualifier is double
		// quoted.
		boolean moltypeFound = false;
		Feature feature = readFeature();
		if (feature == null) {
			return;
		}

        if((Feature.SOURCE_FEATURE_NAME.equals(feature.getName()) && skipSource)
                || (!Feature.SOURCE_FEATURE_NAME.equals(feature.getName()) && lineReader.getReaderOptions() != null && lineReader.getReaderOptions().isParseSourceOnly()))
        {
            while(true)
            {
                lineReader.readLine();
                String nextLine = lineReader.getNextMaskedLine();

                if(isFeature(nextLine))
                {
                    break;
                }
            }
        }

		while (true) {
			Qualifier qualifier = readQualifier();
			if (qualifier != null) {
				if (qualifier.getName().equals("organism")) {
					if (!(feature instanceof SourceFeature)) {
						error("FT.6", qualifier.getName()); // Invalid feature qualifier.
					}
					else {
						SourceFeature sourceFeature = ((SourceFeature)feature);
						String scientificName = qualifier.getValue();
						sourceFeature.setScientificName(scientificName);
						String lineage = getCache().getLineage(scientificName);
						String commonName = getCache().getCommonName(scientificName);
						sourceFeature.setCommonName(commonName);
						Long taxId = getCache().getTaxId(scientificName);
						if (taxId != null) {
							sourceFeature.setTaxId(taxId);
						}
						if (lineage != null) {

							sourceFeature.getTaxon().setLineage(lineage);;

						}
					}
				}
				else if (qualifier.getName().equals("mol_type")) {
					if (!(feature instanceof SourceFeature)) {
						error("FT.6", qualifier.getName());
					}
					else {
						moltypeFound = true;
						String value = qualifier.getValue();
						getCache().setMolType(value);
						String oldValue = entry.getSequence().getMoleculeType();
						if (!FlatFileUtils.isBlankString(value) &&
								!FlatFileUtils.isBlankString(oldValue) &&
								!value.equals(oldValue)) {
							error("FT.7", qualifier.getName(), value, oldValue); // Inconsistent feature qualifier.
						}
						else {
							entry.getSequence().setMoleculeType(qualifier.getValue());
						}
					}

				}
				else if (qualifier.getName().equals("db_xref")) {
					XRefTaxonMatcher taxonXrefMatcher = new XRefTaxonMatcher(this);
					if (taxonXrefMatcher.match(qualifier.getValue())) {
						if (!(feature instanceof SourceFeature)) {
							error("FT.6", qualifier.getName()); // Invalid feature qualifier.
						}
						else {
							((SourceFeature)feature).setTaxId(taxonXrefMatcher.getTaxId());
						}
					}
					else {
						XRefQualifierMatcher xrefQualifierMatcher = new XRefQualifierMatcher(this);
						if (!xrefQualifierMatcher.match(qualifier.getValue())) {
							error("FT.6", qualifier.getName()); // Invalid feature qualifier.
						}
						else {
							feature.addXRef(xrefQualifierMatcher.getXref());
						}
					}
				}
				else {
					feature.addQualifier(qualifier);
				}
			} else {
				break;
			}
		}
		if((feature instanceof SourceFeature)&& !moltypeFound&&!skipSource)
		{
			error("FT.9");
		}
		entry.addFeature(feature);

	}

	private Feature readFeature() throws IOException {
		int firstLineNumber = lineReader.getCurrentLineNumber();
		String line = lineReader.getCurrentMaskedLine();
		if (line.length() <= LOCATION_BEGIN_POS) {
			error("FT.1"); // Invalid feature.
			return null;
		}
		String featureName = line.substring(0, LOCATION_BEGIN_POS).trim();
		if(StringUtils.contains(featureName," "))
		{
			error("FT.12",featureName); // FeatureName shouldn't contain spaces
			return null;
		}

		if (FlatFileUtils.isBlankString(featureName)) {
			error("FT.2"); // Missing feature name.
			return null;
		}
		featureName=Utils.getValidFeatureName(featureName);

		String locationString = line.substring(LOCATION_BEGIN_POS);
		CompoundLocation<Location> location = readLocation(locationString);
		if (location == null) {
			return null;
		}
		int lastLineNumber = lineReader.getCurrentLineNumber();

		Feature feature = (new FeatureFactory()).createFeature(featureName);
		feature.setOrigin(new FlatFileOrigin(lineReader.getFileId(), firstLineNumber, lastLineNumber));
		feature.setLocations(location);
		feature.getLocations().setOrigin(new FlatFileOrigin(lineReader.getFileId(), firstLineNumber, lastLineNumber));
		return feature;
	}
    
    private CompoundLocation<Location> readLocation(
    		String locationString) throws IOException {
		if (FlatFileUtils.isBlankString(locationString)) {
			error("FT.3"); // Missing feature location.
			return null;
		}    	
    	StringBuilder locationBuilder = new StringBuilder();
    	locationBuilder.append(locationString);
		while (true) {
			if (!lineReader.joinLine()) {
    			break;
    		}
    		String nextLine = lineReader.getNextMaskedLine();
    		if (isFeature(nextLine)) {
    			break;
    		}
    		if (isQualifier(nextLine)) {
    			break;
    		}
			if (nextLine.length() <= LOCATION_BEGIN_POS) {
    			error("FF.8"); // Invalid feature location.
    			return null;
    		}
			locationString = nextLine.substring(LOCATION_BEGIN_POS);
    		if (!FlatFileUtils.isBlankString(locationString)) {
    			locationBuilder.append(locationString);
    		}    			
			lineReader.readLine();			
		}
    	FeatureLocationsMatcher matcher = new FeatureLocationsMatcher(this,lineReader.getReaderOptions().isIgnoreParserErrors());
    	if (!matcher.match(locationBuilder.toString())) {
    		error("FT.4"); // Invalid feature location.
    		return null;
    	}
    	CompoundLocation<Location> location = matcher.getCompoundLocation();
    	return location;		
    }
          
    private Qualifier readQualifier() throws IOException {
		int firstLineNumber = 0;
		int lastLineNumber = 0;
		if (!lineReader.joinLine()) {
			return null;
		}
		String nextLine = lineReader.getNextMaskedLine();
		
		nextLine=StringEscapeUtils.unescapeHtml4(nextLine);
		
		if (!isQualifier(nextLine)) {
			return null;
		}
		if (nextLine.length() <= QUALIFIER_BEGIN_POS) {
			error("FT.5"); // Invalid feature qualifier.
			return null;
		}
    	StringBuilder qualifierBuilder = new StringBuilder();
		String qualifierString = nextLine.substring(QUALIFIER_BEGIN_POS); 
		qualifierBuilder.append(qualifierString);
		// Do not add space if the last character on the line is '-'.
		boolean noAddSpace =
			qualifierString.length() > 0 &&
			qualifierString.charAt(qualifierString.length() - 1) == '-';
    	boolean addSpace =
    			!qualifierString.startsWith("/replace") &&
    			!qualifierString.startsWith("/rpt_unit_seq") &&
    			!qualifierString.startsWith("/PCR_primers") &&
    			!qualifierString.startsWith("/translation");
		while (true) {
			String tempLine=nextLine ;
			lineReader.readLine();
			if (firstLineNumber == 0 ) {
				firstLineNumber = lineReader.getCurrentLineNumber();				
			}
			lastLineNumber = lineReader.getCurrentLineNumber();
			if (!lineReader.joinLine()) {
				break;
			}
			nextLine = lineReader.getNextMaskedLine();
						
    		if (isFeature(nextLine)) {
    			break;
    		}
    		if (isQualifier(nextLine)&isQuoteBalance(tempLine)) {
    			quotecount=0;
    			break;
    		}
			if (nextLine.length() <= QUALIFIER_BEGIN_POS) {
				error("FT.5"); // Invalid feature qualifier.
				break;
    		}
			if (addSpace && !noAddSpace) {
				qualifierBuilder.append(" ");
			}
			qualifierString = nextLine.substring(QUALIFIER_BEGIN_POS);
			qualifierBuilder.append(qualifierString);	
			noAddSpace =
				qualifierString.length() > 0 &&
				qualifierString.charAt(qualifierString.length() - 1) == '-';			
		}
		if (qualifierBuilder.length() == 0) {
			return null;
		}
		QualifierMatcher qualifierMatcher = new QualifierMatcher(this);
		if (!qualifierMatcher.match(qualifierBuilder.toString())) {
			error("FT.5", "Invalid feature qualifier.");
			return null;
		}		
		Qualifier qualifier = qualifierMatcher.getQualifier();
		if(qualifier!=null)
		qualifier.setOrigin(
				new FlatFileOrigin(lineReader.getFileId(), firstLineNumber, lastLineNumber));
		return qualifier;
    }
    
    private boolean isFeature(String line) {
    	for (int i = 0 ; i < LOCATION_BEGIN_POS && i < line.length() ; ++i) {
    		if (line.charAt(i) != ' ') {
    			return true;
    		}
    	}
    	return false;
    }    
    
	private static final Pattern QUALIFIER_PATTERN_1 = Pattern.compile(
			"^\\/[a-zA-Z1-9-_]+\\=.*");

	private static final Pattern QUALIFIER_PATTERN_2 = Pattern.compile(
			"^\\/[a-zA-Z1-9-_]+\\s*$");	
    
    private boolean isQualifier(String line) {
    	if (line.length() <= QUALIFIER_BEGIN_POS) {
    		return false; 
    	}
		if (line.charAt(QUALIFIER_BEGIN_POS) != '/') {
			return false;
		}
    	
    	String str = line.substring(21);
    	Matcher matcher = QUALIFIER_PATTERN_1.matcher(str);
    	if (matcher.matches()) {
    		return true;
    	}
    	matcher = QUALIFIER_PATTERN_2.matcher(str);
    	return matcher.matches();
   }  

	private boolean isQuoteBalance(String line)
	{
		quotecount += StringUtils.countMatches(line, "\"");
		return quotecount % 2 == 0;
	}
}

