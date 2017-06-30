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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

@Description("Feature qualifier \\\"{0}\\\" is not recognized\\Feature qualifier \\\"{0}\\\" does not have a value (mandatory for this type)\\" +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" is invalid. Refer to the feature documentation or ask a curator for guidance." +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" does not comply to the qualifier specifications. Refer to the feature documentation or ask a curator for guidance.\"")
public class QualifierCheck extends FeatureValidationCheck {

    /**
     * A list of the valid feature qualifier values. ALso whether the qualifier is allowed to be null, and whether it
     * is 'new'. Also the regex that the value must subscribe to. Also the quoted value and the qualifier order.
     */
    @CheckDataSet("feature-qualifier-values.tsv")
    private DataSet qualifierSet;

    @CheckDataSet("artemis-qualifiers.tsv")
    private DataSet artemisQualifiers;

    /**
     * A list of the regex groups of particular qualifiers that need to be a particular value.
     * File generated using SQL on Webin database:
     */
    @CheckDataSet("feature-regex-groups.tsv")
    private DataSet regexSet;

    private HashMap<String, QualifierInfo> qualifierMap = new HashMap<String, QualifierInfo>();
    private Set<String> artemisQualifiersSet = new TreeSet<String>();

    private final static String NO_QUALIFIER_FOUND_ID = "QualifierCheck-1";
    private final static String NO_VALUE_ID = "QualifierCheck-2";
    private final static String NO_VALUE_ID_2 = "QualifierCheck-5";
    private final static String REGEX_FAIL_ID = "QualifierCheck-3";
    private final static String REGEX_GROUP_FAIL_ID = "QualifierCheck-4";
    private final static String COLLECTION_DATE_ID = "QualifierCheck-6";
	private final static String LAT_LON_MESSAGE_ID1 = "QualifierCheck-7";
	private final static String LAT_LON_MESSAGE_ID2 = "QualifierCheck-8";
	private final static String PROTEIN_ID_VERSION_MESSAGE_ID = "QualifierCheck-9";
    
    /**
     * if a list of permitted values exceeds this limit, they will be displayed in a separate page, rather than a tooltip
     */
    private static final int MAX_VALID_VALUES_SIZE = 20;   
    /*
     * for /lat_lon qualifier value: ex: 30.13 N 6.13 E
     * latitude value i.e. 30.13 must not be greater than 90.00
     * longitude value i.e. 6.13 must not be grater than 180.00
     */
    private static final double MAX_LATITUDE_VALUE = 90.00;
    private static final double MAX_LONGITUDE_VALUE = 180.00;

    
    

    public QualifierCheck() {
    }

    QualifierCheck(DataSet dataSet, DataSet regexSet, DataSet artemisQualifiers) {
        this.qualifierSet = dataSet;
        this.regexSet = regexSet;
        this.artemisQualifiers = artemisQualifiers;
    }

    public void setPopulated() {
        init();
        super.setPopulated();
    }

    private void init() {

        if (qualifierSet != null) {
            try {
                for (DataRow dataRow : qualifierSet.getRows()) {
                    String qualifierName = Utils.parseTSVString(dataRow.getString(0));
                    String noValue = Utils.parseTSVString(dataRow.getString(1));
                    String newField = Utils.parseTSVString(dataRow.getString(2));
                    String regex = Utils.parseTSVString(dataRow.getString(4));
                    String comments = Utils.parseTSVString(dataRow.getString(6));

                    if (regex != null) {
                        Pattern.compile(regex);
                    }

                    QualifierInfo qualifierInfo = new QualifierInfo(qualifierName, regex, noValue.equals("Y"), newField.equals("Y"), comments);
					if (!qualifierName.equals("EC_number")) { //EMD-2496
                    for (DataRow regexpRow : regexSet.getRows()) {//look at all the qualifier values associatesd with a regexp group
                        if (regexpRow.getString(0).equals(qualifierName)) {
                        	String regexGroupId = Utils.parseTSVString(regexpRow.getString(1));
                            boolean caseInsensitive = Utils.parseTSVString(regexpRow.getString(2)).equals("TRUE");
                            String[] regexpGroupValues = regexpRow.getStringArray(3);

                            RegexGroupInfo groupInfo = new RegexGroupInfo(regexGroupId);
                            groupInfo.addValues(Arrays.asList(regexpGroupValues));
                            groupInfo.setCaseInseneitive(caseInsensitive);
                            qualifierInfo.addRegexGroupInfo(groupInfo);
                        }
                    }
					}

                    qualifierMap.put(qualifierName, qualifierInfo);
                }

                for (DataRow dataRow : artemisQualifiers.getRows()) {
                    String artemisQualifier = Utils.parseTSVString(dataRow.getString(0));
                    artemisQualifiersSet.add(artemisQualifier);
                }

                setNullGroupTolerance();

            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid pattern while instantiating QualifierCheck! " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Failed to set qualifier values in QualifierCheck!");
        }
    }

    /**
     * Sets the regex group ids for a particular qualifier type that are permitted to not match. For example, the
     * "collection_date" qualifier has a regular expression where group '3' can be the month, which should match a CV
     * ("21-Oct-1952") but could also be just the year ("1952"), in which case group 3 will not match (i.e. exist at all)
     * but this does not represent a validation failure. This map keeps track of the qualifier names that tolerate a
     * does-not-exist for a particular group number. Maintaining by hand for now.
     */
    private void setNullGroupTolerance() {

        QualifierCheck.QualifierInfo qualifierInfo = qualifierMap.get(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
        for(RegexGroupInfo regexInfo : qualifierInfo.getRegexGroupInfos()){
            if(regexInfo.getGroupNumber() == 3){
                regexInfo.setNonMatch(true);
            }
        }
     
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        for (Qualifier qualifier : feature.getQualifiers()) {

            String qualifierName = qualifier.getName();

            if (qualifierMap.containsKey(qualifierName)) {

                QualifierInfo qualifierInfo = qualifierMap.get(qualifierName);
               

                //check the NOVALUE requirement
                String value = qualifier.getValue();
                boolean noValue = qualifierInfo.isNoValue();
                if (!noValue && (value == null || value.equals(""))) {
                    reportError(qualifier.getOrigin(), NO_VALUE_ID, qualifierName, feature.getName());
                }
				if (noValue && value != null && !value.isEmpty()) {
					reportError(qualifier.getOrigin(), NO_VALUE_ID_2,
							qualifierName, feature.getName());
				}

                /**
                 * do a check to see if the date is a legitimate one for collection date
                 * Added separate check for the collection date
                 */
				 /*if (qualifierName.equals(Qualifier.COLLECTION_DATE_QUALIFIER_NAME) && value != null) {

                   String dateFormat1 = "^(\\w){3}\\s*(-)\\s*(\\d{4})$";//MMM-yyy
                    String dateFormat2 = "^(\\d{1,2})\\s*(-)\\s*(.*)\\s*(-)\\s*(\\d{4})$";//dd-MMM-yyyy

                    if (value.matches(dateFormat1) || value.matches(dateFormat2)) {

                        SimpleDateFormat sdf;

                        if (value.matches(dateFormat1)) {
                            sdf = new SimpleDateFormat("MMM-yyyy");
                        } else {
                            sdf = new SimpleDateFormat("dd-MMM-yyyy");
                        }
                        sdf.setLenient(false);

                        try {
                            sdf.parse(qualifier.getValue());
                        } catch (ParseException e) {
                            reportError(qualifier.getOrigin(), COLLECTION_DATE_ID,
                                    qualifierName, qualifier.getValue());

                        }
                    }
                }*/

                //todo check the 'NEW' field

                String regex = qualifierInfo.getRegex();
                checkRegexValueRange(qualifierInfo,qualifier);
                if (value != null && regex != null) {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(value);
                    if (!matcher.matches()) {
                    	if(!isAlternateRegexMatch(qualifier))
                    	{
                        ValidationMessage<Origin> message =
                                reportError(qualifier.getOrigin(), REGEX_FAIL_ID, qualifierName, value, regex);
                        if (qualifierInfo.getCuratorComments() != null) {
                            message.setCuratorMessage(qualifierInfo.getCuratorComments());
                        }
                        }
                    } else if (qualifierInfo.getRegexGroupInfos().size() > 0) {//there are specific group requirements
                        for (RegexGroupInfo groupInfo : qualifierInfo.getRegexGroupInfos()) {
                            int groupNumber = groupInfo.getGroupNumber();
                            String group = matcher.group(groupNumber);
                            List<String> validValues = groupInfo.getValues();
                            

                            if ((group == null && !groupInfo.canBeNonMatch()) || (group != null && !valuesContains(validValues, group, groupInfo.isCaseInseneitive()))) {
                            	if("inference".equals(qualifierName)&&value!=null&&value.equalsIgnoreCase("non-experimental evidence, no additional details recorded"))
                            	{
                            		//do not show any message
                            	}
                            	else
                            	{
                                ValidationMessage<Origin> message =
                                        reportError(qualifier.getOrigin(), REGEX_GROUP_FAIL_ID, qualifierName, group);

                                if (validValues.size() > MAX_VALID_VALUES_SIZE) {
                                    message.setReportMessage(Utils.paramArrayToCuratorReportString(validValues.toArray()));
                                } else {
                                    String permittedValues = Utils.paramArrayToCuratorTipString(validValues.toArray());
                                    message.setCuratorMessage("Permitted values are : " + permittedValues);
                                }
                                }
                            }
                        }
                    }
                }

            } else {//the qualifier is not in the CV
                ValidationMessage<Origin> message =
                        reportError(qualifier.getOrigin(), NO_QUALIFIER_FOUND_ID, qualifierName);
                if(artemisQualifiersSet.contains(qualifierName)) {
                    message.setCuratorMessage("If you are using Artemis to create this file, select the 'EMBL submission' format");
                }
            }

        }

        return result;
    }
    
    private boolean isAlternateRegexMatch(Qualifier qualifier)
    {
    	if(qualifier!=null&&qualifier.getName().equals(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME))
    	{
    		Matcher matcher = TranslExceptQualifier.PATTERN.matcher(qualifier.getValue());
    		if(matcher.matches())
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }

    private boolean valuesContains(List<String> validValues, String toMatch, boolean caseInsensitive) {
        for (String value : validValues) {
            if (caseInsensitive) {
                if (value.equalsIgnoreCase(toMatch)) {
                    return true;
                }
            } else {
                if (value.equals(toMatch)) {
                    return true;
                }
            }
        }
        return false;
    }

	public void checkRegexValueRange(QualifierInfo qualifierInfo,
			Qualifier qualifier) {
		if (qualifierInfo.getName().equals(Qualifier.LAT_LON_QUALIFIER_NAME)) {
			Double lat = null, lon=null;
			String direcSN,direcWE,latValue,lonValue;
			String regex = qualifierInfo.getRegex();
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(qualifier.getValue());
			if (matcher.find()) {
				latValue = matcher.group(1);
				lonValue = matcher.group(4);
				direcSN = matcher.group(3);
				direcWE = matcher.group(6);
				if (latValue != null) {
					lat = new Double(latValue);
					lat = Math.floor(lat * 10000 + 0.5) / 10000;
				}

				if (lonValue != null) {
					lon = new Double(lonValue);
					lon = Math.floor(lon * 10000 + 0.5) / 10000;
				}
				// System.out.println("lat" + lat + "lon" + lon);
				if (lat < 0) { // need to do fixing for the direction direcSN
					lat = -lat;
				}
				if (lon < 0) { // need to do fixing for the direction direcWE

					lon = -lon;

				}
				if (lat > MAX_LATITUDE_VALUE) {
					reportError(qualifier.getOrigin(), LAT_LON_MESSAGE_ID1,
							qualifier.getName(), lat, MAX_LATITUDE_VALUE);
				}
				if (lon > MAX_LONGITUDE_VALUE) {
					reportError(qualifier.getOrigin(), LAT_LON_MESSAGE_ID2,
							qualifier.getName(), lon, MAX_LONGITUDE_VALUE);
				}
			}
		}
		if (qualifierInfo.getName().equals(Qualifier.PROTEIN_ID_QUALIFIER_NAME)) {
			Integer version = 0;
			String regex = qualifierInfo.getRegex();
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(qualifier.getValue());
			if (matcher.find()) {
				version = new Integer(matcher.group(3));
				if (version < 1) {
					reportError(qualifier.getOrigin(),
							PROTEIN_ID_VERSION_MESSAGE_ID, qualifier.getName());
				}
			}
		}
	}

    private class QualifierInfo {
        private String name;
        private String regex;
        private String curatorComments;
        private boolean noValue;
        private boolean newField;
        private List<RegexGroupInfo> regexGroupInfos = new ArrayList<RegexGroupInfo>();

        private QualifierInfo(String name, String regex, boolean noValue, boolean newField, String curatorComments) {
            this.name = name;
            this.regex = regex;
            this.noValue = noValue;
            this.newField = newField;
            this.curatorComments = curatorComments;
        }

        public String getName() {
            return name;
        }

        public String getRegex() {
            return regex;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isNewField() {
            return newField;
        }

        public void addRegexGroupInfo(RegexGroupInfo info) {
            regexGroupInfos.add(info);
        }

        public List<RegexGroupInfo> getRegexGroupInfos() {
            return regexGroupInfos;
        }

        public String getCuratorComments() {
            return curatorComments;
        }
    }

    class RegexGroupInfo {
        private int groupNumber;
        private List<String> values;
        private boolean nonMatch = false;//default
        private boolean caseInseneitive = false;//default

        RegexGroupInfo(String groupId) {
            this.groupNumber = new Integer(groupId);
            this.values = new ArrayList<String>();
        }

        int getGroupNumber() {
            return groupNumber;
        }

        public List<String> getValues() {
            return values;
        }

        public void addValues(List<String> strings) {
            values.addAll(strings);
        }

        public void setNonMatch(boolean nonMatch) {
            this.nonMatch = nonMatch;
        }

        /**
         * Is permissible for this group to not match in the regex and still be valid.
         * @return true if non-match permitted
         */
        public boolean canBeNonMatch() {
            return nonMatch;
        }

        /**
         * whether the regexp cares about case
         * @return
         */
        public boolean isCaseInseneitive() {
            return caseInseneitive;
        }

        public void setCaseInseneitive(boolean caseInseneitive) {
            this.caseInseneitive = caseInseneitive;
        }
       }
    
 }
