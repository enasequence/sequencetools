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
package uk.ac.ebi.embl.api.validation.helper;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.*;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.storage.CachedFileDataManager;
import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	private static Object FeatureFactory;
	private final static String UTILS_2= "Utility_shift_Location_2";
	private final static String UTILS_3 = "Utility_shift_Location_3";
	private final static String UTILS_4 = "Utility_shift_Location_4";
	private final static String UTILS_5 = "Utility_shift_Location_5";
	private final static String UTILS_6= "Utility_shift_Location_6";
	private static final Pattern SHRINK = Pattern.compile(" {2,}");
	
	private static final DataManager dataManager = new CachedFileDataManager();
	private static final CheckFileManager tsvFileManager = new CheckFileManager();
	
	
	//prevent instantiation
	private Utils() {
	}
	
	public static String paramArrayToString(Object[] array) {
		StringBuilder result = new StringBuilder();
		result.append("\"");
		if (array != null) {
			result.append(StringUtils.join(array, ", "));
		}
		result.append("\"");
		return result.toString();
	}

	public static String paramArrayToCuratorTipString(Object[] array) {
		StringBuilder result = new StringBuilder();
        /**
         * use " rather than ' as tooltip gizmo in webin does not like '
         */
        result.append("\"");
		if (array != null) {
			result.append(StringUtils.join(array, ", "));
		}
		result.append("\"");
		return result.toString();
	}

	public static String paramArrayToCuratorReportString(Object[] array) {
		StringBuilder result = new StringBuilder();
        result.append("\"");
		if (array != null) {
			result.append(StringUtils.join(array, ",\n"));
		}
		result.append("\"");
		return result.toString();
	}

	/**
	 * Converts list of strings to an array.
	 * 
	 * @param list a list of strings to be converted
	 * @return an array
	 */
	public static Object[] stringListToArray(List<String> list) {
		Object[] params = null;
		if (list != null) {
			params = list.toArray(new String[list.size()]);
		}
		return params;
	}	
	
	/**
	 * Check whether value NOT matches the pattern build with prefix, middle 
	 * part and post-fixer.
	 * 
	 * @param value a value to be checked
	 * @param prefix a prefix pattern
	 * @param middle a middle pattern
	 * @param postfix a post-fixer pattern
	 * @return true if value not matches pattern otherwise false
	 */
	public static boolean notMatches(String value, String prefix, String middle,
			String postfix) {
		return !matches(value, prefix, middle, postfix);
	}

	/**
	 * Check whether value matches the pattern build with prefix, middle part
	 * and post-fixer.
	 * 
	 * @param value a value to be checked
	 * @param prefix a prefix pattern
	 * @param middle a middle pattern
	 * @param postfix a post-fixer pattern
	 * @return true if value matches pattern otherwise false
	 */
	public static boolean matches(String value, String prefix, String middle, 
			String postfix) {
		String pattern = prefix + middle + postfix;
		boolean result = value.matches(pattern);
		return result;
	}

	/**
	 * It removes prefixes from values and compare remaining parts.
	 * 
	 * @param value1 first value to be checked
	 * @param prefix1 prefix of the first value
	 * @param value2 second value to be checked
	 * @param prefix2 prefix of the second value
	 * @return true when both values without their prefixes are equal
	 */
	public static boolean matchesWithoutPrefixes(String value1, String prefix1, 
			String value2, String prefix2) {
		if (!value1.startsWith(prefix1)) {
			return false;
		}
		value1 = value1.substring(prefix1.length());
		
		if (!value2.startsWith(prefix2)) {
			return false;
		}
		value2 = value2.substring(prefix2.length());
		
		return value1.equals(value2);
	}

	public static List<Text> stringsToTexts(List<String> strings, Origin origin){
        List<Text> texts = new ArrayList<Text>();
        for(String string : strings){
            texts.add(new Text(string, origin));
        }
        return texts;
    }

    public static FlattenedMessageResult flattenMessages(List<ValidationMessage<Origin>> validationMessages,
                                                          int messageFlattenThreshold) {

        Map<String, Integer> messageCounts = new HashMap<String, Integer>();
        List<ValidationMessage> flattenedMessages = new ArrayList<ValidationMessage>();
        List<ValidationMessage> unFlattenedMessages = new ArrayList<ValidationMessage>();

        for (ValidationMessage<Origin> message : validationMessages) {
            String messageKey = message.getMessageKey();
            if (messageCounts.containsKey(messageKey)) {
                Integer count = messageCounts.get(messageKey);
                count += 1;
                messageCounts.put(messageKey, count);
            } else {
                messageCounts.put(messageKey, 1);
            }
        }

        for (ValidationMessage<Origin> message : validationMessages) {
            String messageKey = message.getMessageKey();
            if (messageCounts.containsKey(messageKey)) {
                Integer count = messageCounts.get(messageKey);
                if (count > messageFlattenThreshold) {
                    String messageExample = ValidationMessageManager.getString(messageKey);//get the unformatted message string
                    message.setMessage(messageExample + " (" + count + " occurrences)");//swap the message to contain the example message (unformatted)
                    flattenedMessages.add(message);
                    messageCounts.remove(messageKey);
                } else {
                    unFlattenedMessages.add(message);
                }
            }
        }

        return new FlattenedMessageResult(flattenedMessages, unFlattenedMessages);
    }

    public static List<FlattenedValidationPlanResult> flattenValidationPlans(List<ValidationPlanResult> planResults) {
        Map<String,FlattenedValidationPlanResult> resultsMap = new HashMap<String, FlattenedValidationPlanResult>();
        for(ValidationPlanResult planResult : planResults){
            if(resultsMap.containsKey(planResult.getTargetOrigin())){
                FlattenedValidationPlanResult flattenedResult = resultsMap.get(planResult.getTargetOrigin());
                flattenedResult.append(planResult);
            }else{
                resultsMap.put(planResult.getTargetOrigin(), new FlattenedValidationPlanResult(planResult));
            }
        }

        return new ArrayList<FlattenedValidationPlanResult>(resultsMap.values());
    }

    public static String parseTSVString(String input){
        if(input.equals("(null)")){
            return null;
        }
        return input.trim();
    }
    
	/**
	 * Shifting the feature Locations according to the new sequence locations
	 * 
	 * @param entry
	 * @param deletedBeginNs
	 *            (number of deleted 'n's at the beginning of sequence)
	 * 
	 * @return ArrayList (Validation Messages)
	 */
	public static ArrayList<ValidationMessage> shiftLocation(Entry entry,
			int deletedBeginNs,boolean removeall) {
		ArrayList<Feature> gapFeatures = new ArrayList<Feature>();
		ArrayList<Feature> invalidFeatures = new ArrayList<Feature>();
		ArrayList<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
		if (entry == null) {
			return null;
		}
		List<Feature> features = entry.getFeatures();
		for (int i = 0; i < features.size(); i++) {

			Feature feature = features.get(i);
			boolean invalidFeature = false;
			// New Sequence String Length
			long newSequenceLength = entry.getSequence().getLength();

			for (Location location : feature.getLocations().getLocations()) {

				/*
				 * check for all feature locations exists in the entry are
				 * within range of sequence Begin and End positions and shifting
				 * the locations of the feature according to the new sequence
				 * positions
				 */

				if (location.getBeginPosition() <= deletedBeginNs
						&& location.getEndPosition() <= deletedBeginNs) {

					location.setBeginPosition(location.getBeginPosition()
							- deletedBeginNs);
					location.setEndPosition(location.getEndPosition()
							- deletedBeginNs);

					if (feature.getName().equals(Feature.GAP_FEATURE_NAME)) {
						validationMessages.add(ValidationMessage.message(
								Severity.FIX, UTILS_2, location
										.getBeginPosition().toString(),
								location.getEndPosition().toString()));
						gapFeatures.add(feature);
						invalidFeature = true;
					} else {
						if(feature!=null&&!feature.getName().equals(Feature.SOURCE_FEATURE_NAME))
						{
						invalidFeatures.add(feature);
                        invalidFeature = true;
						}
					}

				} else if (location.getBeginPosition() <= deletedBeginNs
						&& location.getEndPosition() > deletedBeginNs) {
					location.setBeginPosition((long) 1);
					location.setEndPosition(location.getEndPosition()
							- deletedBeginNs);
					if (location.getEndPosition() > newSequenceLength)

					{
						location.setEndPosition(newSequenceLength);
					}
				} else if (location.getBeginPosition() > deletedBeginNs
						&& location.getEndPosition() > deletedBeginNs) {

					location.setBeginPosition(location.getBeginPosition()
							- deletedBeginNs);
					location.setEndPosition(location.getEndPosition()
							- deletedBeginNs);

					if (location.getBeginPosition() > newSequenceLength
							&& location.getEndPosition() > newSequenceLength) {
						if (feature.getName().equals(Feature.GAP_FEATURE_NAME)) {
							gapFeatures.add(feature);
                            ValidationMessage<Origin> message = ValidationMessage.message(
                                    Severity.FIX, UTILS_2, location.getBeginPosition().toString(),
                                    location.getEndPosition().toString());
                            message.getOrigins().add(feature.getOrigin());
                            validationMessages.add(message);
							invalidFeature = true;

						}

						else {
							if(feature!=null&&!feature.getName().equals(Feature.SOURCE_FEATURE_NAME))
							{
							invalidFeatures.add(feature);
                            invalidFeature = true;
							}
						}

					}

					if (location.getBeginPosition() <= newSequenceLength
							&& location.getEndPosition() > newSequenceLength)

					{
						location.setEndPosition(newSequenceLength);

					}

				}
				if (location.getBeginPosition().equals(location.getEndPosition())) {
					
					if(removeall)
					{
						location.setEndPosition(null);
					}
					else{
					if (feature.getName().equals(Feature.GAP_FEATURE_NAME)) {
						gapFeatures.add(feature);
                        ValidationMessage<Origin> message = ValidationMessage.message(
                                Severity.FIX, UTILS_2, location.getBeginPosition().toString(),
                                location.getEndPosition().toString());
                        message.getOrigins().add(feature.getOrigin());
                        validationMessages.add(message);
						invalidFeature = true;

					} else {
						if(feature!=null&&!feature.getName().equals(Feature.SOURCE_FEATURE_NAME))
						{
						invalidFeatures.add(feature);
                        invalidFeature = true;
						}
					}
				}
				
			}
			}
			
			// Reference Location shifting
			shiftReferenceLocation(entry , newSequenceLength);
			//set the new sequencelength
		    //entry.getSequence().setLength(newSequenceLength);
			
			// Qualifier Location Shifting
			Long newBeginLocation, newEndLocation = null;
			Location newLocation = null;
			if (!invalidFeature) {
				for (Qualifier qualifier : feature.getQualifiers()) {

					// TRANSL_EXCEPT_QUALIFIER

					if (qualifier.getName().equals(
                            Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME)) {
						TranslExceptQualifier translExcepttqualifier = new TranslExceptQualifier(
								qualifier.getValue());

						/*if (shiftLocationQualifier(translExcepttqualifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									translExcepttqualifier, deletedBeginNs,
									feature));
						}*/
					} /*else if (qualifier.getName().equals(
                            Qualifier.ANTICODON_QUALIFIER_NAME)) {

						AnticodonQualifier antiCodonqualifier = new AnticodonQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(antiCodonqualifier,
								deletedBeginNs, feature) != null) {
							validationMessages
									.add(shiftLocationQualifier(
											antiCodonqualifier, deletedBeginNs,
											feature));
						}

					} */else if (qualifier.getName().equals(
                            Qualifier.RPT_UNIT_RANGE_QUALIFIER_NAME)) {

						Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(rptUnitRangequalifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									rptUnitRangequalifier, deletedBeginNs,
									feature));
						}
					}

					else if (qualifier.getName().equals(
                            Qualifier.TAG_PEPTIDE_QUALIFIER_NAME)) {

						Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(tagPeptidequalifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									tagPeptidequalifier, deletedBeginNs,
									feature));
						}
					}
				}

			}
		}

		if (gapFeatures.size() > 0) {
			for (Feature feature : gapFeatures) {

				entry.removeFeature(feature);

			}
		}
		if (invalidFeatures.size() > 0) {
			for (Feature feature : invalidFeatures) {
				ValidationMessage<Origin> message = ValidationMessage.message(
						Severity.FIX, UTILS_5, feature.getName(),
						feature.getName());
				message.getOrigins().add(feature.getOrigin());
				validationMessages.add(message);
				entry.removeFeature(feature);

			}
		}
		return validationMessages;
	}

	public static ValidationMessage shiftLocationQualifier(
			LocationQualifier qualifier, long deletedBeginNs, Feature feature) {
		Long newBeginLocation = null, newEndLocation = null;
		Location newLocation = null;
		List<Location> locationsList = null;
		locationsList = feature.getLocations().getLocations();
		long featureNewBeginLocation = 0;
		long featureNewEndLocation = 0;
		for (Location location : locationsList) {
			featureNewBeginLocation = location.getBeginPosition();
			featureNewEndLocation = location.getEndPosition();
		}
		try {
			LocationFactory factory = new LocationFactory();
			if (qualifier.getLocation().getBeginPosition() >= deletedBeginNs) {
				newBeginLocation = qualifier.getLocation().getBeginPosition()
						- deletedBeginNs;
				if (!(qualifier.getLocation().getEndPosition() == null)) {
					newEndLocation = qualifier.getLocation().getEndPosition()
							- deletedBeginNs;
					// if the new Qualifier Location is in between new
					// feature locations set the new locations to the
					// qualifier
					if (newBeginLocation >= featureNewBeginLocation
							&& newEndLocation <= featureNewEndLocation) {
						newLocation = factory.createLocalRange(
								newBeginLocation, newEndLocation);
						qualifier.setLocation(newLocation);
					}

					else {
						return ValidationMessage.message(Severity.ERROR,
								UTILS_3, qualifier.getName(), newBeginLocation,
								newEndLocation);
					}
				}

				else {
					if (newBeginLocation > featureNewBeginLocation
							&& newBeginLocation < featureNewEndLocation) {
						newBeginLocation = qualifier.getLocation()
								.getBeginPosition() - deletedBeginNs;
						newLocation = factory.createLocalBase(newBeginLocation);
						qualifier.setLocation(newLocation);
					} else {
						return ValidationMessage.message(Severity.ERROR,
								UTILS_3, qualifier.getName(), newBeginLocation,
								newEndLocation);
					}
				}
			} else {
				newBeginLocation = qualifier.getLocation().getBeginPosition()
						- deletedBeginNs;
				newEndLocation = qualifier.getLocation().getEndPosition()
						- deletedBeginNs;
				return ValidationMessage.message(Severity.ERROR, UTILS_3,
						qualifier.getName(), newBeginLocation, newEndLocation);
			}
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		return null;
	}

	// Reference Location shifting

	public static ValidationMessage shiftReferenceLocation(Entry entry,
			long newSequenceLength) {
		Collection<Reference> references = entry.getReferences();
		for (Reference reference : references) {
			for (Location rlocation : reference.getLocations().getLocations()) {
				{
					rlocation.setEndPosition(newSequenceLength);
					if (rlocation.getBeginPosition().equals(
							rlocation.getEndPosition())) {
						return ValidationMessage.message(Severity.WARNING,
								UTILS_6, rlocation.getBeginPosition(),
								rlocation.getEndPosition());
					}
				}
			}

		}
		return null;
	}

	/**
	 * Shifting the feature Locations according to the new sequence locations
	 * and remove the features which have been placed fully inside the 'n' start
	 * and end.
	 * 
	 * @param entry
	 * @param deletedBeginNs
	 *            (number of deleted 'n's at the beginning of sequence)
	 * 
	 * @return ArrayList (Validation Messages)
	 */
	public static ArrayList<ValidationMessage> shiftAndRemoveFeature(
			Entry entry, int deletedBeginNs) {
		List<Location> locationsList = null;
		ArrayList<Feature> gapFeatures = new ArrayList();
		ArrayList<ValidationMessage> validationMessages = new ArrayList();
		long featureNewBeginLocation = 0;
		long featureNewEndLocation = 0;

		if (entry == null) {
			return null;
		}
		List<Feature> features = entry.getFeatures();
		for (int i = 0; i < features.size(); i++) {

			Feature feature = features.get(i);
			locationsList = feature.getLocations().getLocations();
			boolean invalidFeature = false;
			// New Sequence String Length
			long newSequenceLength = entry.getSequence().getLength();

			for (int j = 0; j < feature.getLocations().getLocations().size(); j++) {
				// for (Location location :
				// feature.getLocations().getLocations()) {
				boolean position = false;
				Location location = feature.getLocations().getLocations()
						.get(j);
				/*
				 * check for all feature locations exists in the entry are
				 * within range of sequence Begin and End positions and shifting
				 * the locations of the feature according to the new sequence
				 * positions
				 */
				// check1

				if (location.getBeginPosition() == location.getEndPosition()) {
					position = true;
				}
				if (location.getBeginPosition() <= deletedBeginNs
						&& location.getEndPosition() <= deletedBeginNs) {

					if (position) {
						location.setBeginPosition(location.getBeginPosition()
								- deletedBeginNs);
					} else {
						location.setBeginPosition(location.getBeginPosition()
								- deletedBeginNs);
						location.setEndPosition(location.getEndPosition()
								- deletedBeginNs);
					}

					if (feature.getName().equals(Feature.GAP_FEATURE_NAME)) {
						validationMessages.add(ValidationMessage.message(
								Severity.FIX, UTILS_2, location
										.getBeginPosition().toString(),
								location.getEndPosition().toString()));
						gapFeatures.add(feature);
						invalidFeature = true;
					} else {

						// entry.removeFeature(feature);
						feature.getLocations().removeLocation(location);
						j = j - 1;

						ValidationMessage<Origin> message = ValidationMessage
								.message(Severity.FIX, UTILS_4, feature
										.getName(), location.getBeginPosition()
										.toString(), location.getEndPosition()
										.toString());
						message.getOrigins().add(feature.getOrigin());
						validationMessages.add(message);
						invalidFeature = true;
						// continue;
					}

				} else if (location.getBeginPosition() <= deletedBeginNs
						&& location.getEndPosition() > deletedBeginNs) {

					location.setBeginPosition((long) 1);
					location.setEndPosition(location.getEndPosition()
							- deletedBeginNs);
					if (location.getEndPosition() > newSequenceLength)

					{
						location.setEndPosition(newSequenceLength);
					}
				} else if (location.getBeginPosition() > deletedBeginNs
						&& location.getEndPosition() > deletedBeginNs) {

					if (position) {
						location.setBeginPosition(location.getBeginPosition()
								- deletedBeginNs);
					} else {
						location.setBeginPosition(location.getBeginPosition()
								- deletedBeginNs);
						location.setEndPosition(location.getEndPosition()
								- deletedBeginNs);

					}

					if (location.getBeginPosition() > newSequenceLength
							&& location.getEndPosition() > newSequenceLength) {
						if (feature.getName().equals(Feature.GAP_FEATURE_NAME)) {
							gapFeatures.add(feature);
							ValidationMessage<Origin> message = ValidationMessage
									.message(Severity.FIX, UTILS_2, location
											.getBeginPosition().toString(),
											location.getEndPosition()
													.toString());
							message.getOrigins().add(feature.getOrigin());
							validationMessages.add(message);
							invalidFeature = true;

						}

						else {
							feature.getLocations().removeLocation(location);
							j = j - 1;
							ValidationMessage<Origin> message = ValidationMessage
									.message(Severity.FIX, UTILS_4, feature
											.getName(), location
											.getBeginPosition().toString(),
											location.getEndPosition()
													.toString());
							message.getOrigins().add(feature.getOrigin());
							validationMessages.add(message);
							invalidFeature = true;
							// continue;
						}

					}

					if (location.getBeginPosition() <= newSequenceLength
							&& location.getEndPosition() > newSequenceLength)

					{
						location.setEndPosition(newSequenceLength);

					}

				}

			}

			if (feature.getLocations().getLocations().isEmpty()) {

				ValidationMessage<Origin> message = ValidationMessage.message(
						Severity.FIX, UTILS_5, feature.getName(),
						feature.getName());
				message.getOrigins().add(feature.getOrigin());
				validationMessages.add(message);
				removeFeatureQualifiers(feature);
				entry.removeFeature(feature);
				i = i - 1;

			}

			locationsList = feature.getLocations().getLocations();
			for (Location location : locationsList) {
				featureNewBeginLocation = location.getBeginPosition();
				featureNewEndLocation = location.getEndPosition();
			}

			// Qualifier Location Shifting
			Long newBeginLocation, newEndLocation = null;
			Location newLocation = null;
			if (!invalidFeature) {
				for (Qualifier qualifier : feature.getQualifiers()) {

					// TRANSL_EXCEPT_QUALIFIER

					/*if (qualifier.getName().equals(
							qualifier.TRANSL_EXCEPT_QUALIFIER_NAME)) {
						TranslExceptQualifier translExcepttqualifier = new TranslExceptQualifier(
								qualifier.getValue());

						if (shiftLocationQualifier(translExcepttqualifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									translExcepttqualifier, deletedBeginNs,
									feature));
						}
					}else if (qualifier.getName().equals(
							qualifier.ANTICODON_QUALIFIER_NAME)) {

						AnticodonQualifier antiCodonqualifier = new AnticodonQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(antiCodonqualifier,
								deletedBeginNs, feature) != null) {
							validationMessages
									.add(shiftLocationQualifier(
											antiCodonqualifier, deletedBeginNs,
											feature));
						}

					}*/  if (qualifier.getName().equals(
							qualifier.RPT_UNIT_RANGE_QUALIFIER_NAME)) {

						Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(rptUnitRangequalifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									rptUnitRangequalifier, deletedBeginNs,
									feature));
						}
					}

					else if (qualifier.getName().equals(
							qualifier.TAG_PEPTIDE_QUALIFIER_NAME)) {

						Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier(
								qualifier.getValue());
						if (shiftLocationQualifier(tagPeptidequalifier,
								deletedBeginNs, feature) != null) {
							validationMessages.add(shiftLocationQualifier(
									tagPeptidequalifier, deletedBeginNs,
									feature));
						}
					}
				}

			}
		}

		if (gapFeatures.size() > 0) {
			for (Feature feature : gapFeatures) {

				entry.removeFeature(feature);

			}
		}
		return validationMessages;
	}

	public static void removeFeatureQualifiers(Feature feature) {
		List<Qualifier> qualifier = new ArrayList<Qualifier>();
		List<String> qualNames = new ArrayList<String>();
		qualifier = feature.getQualifiers();
		for (Qualifier qual : qualifier) {
			qualNames.add(qual.getName());
		}

		for (String name : qualNames) {
			feature.removeSingleQualifier(name);
		}

	}
	/** Trims the string and replaces runs of whitespace with a single space.
     */ 
	public static String shrink(String string) {
		if (string == null) {
			return null;
		}
		string = string.trim();
		return SHRINK.matcher(string).replaceAll(" ");
	}   
	
	/**
	 * Split the string into values using the regular expression, removes
	 * whitespace from the beginning and end of the resultant strings and
	 * replaces runs of whitespace with a single space.
	 */
	public static Vector<String> split(String string, String regex)
	{
		Vector<String> strings = new Vector<String>();
		for (String value : string.split(new String(regex)))
		{
			value = value.trim();
			if (!value.equals(""))
			{
				strings.add(shrink(value));
			}
		}
		return strings;
	}
/*
 * returns the comment line checklist map having key,value pairs for each checklist
 */
	public static HashMap<String, HashMap<String, String>> getCommentCheckList(Entry entry)
	{
		String comment = entry.getComment().getText();
		String[] tempComment = new String[3];
		int start = comment.indexOf("##");
		String CheckListString = comment.substring(start + 2, comment.length());
		String[] commentChecklists = CheckListString.split("##");

		HashMap<String, HashMap<String, String>> ckeckListMap = new HashMap<String, HashMap<String, String>>();
		for (int i = 0; i < commentChecklists.length; i = i + 4)
		{
			HashMap<String, String> keyVal = new HashMap<String, String>();
			tempComment = Arrays.copyOfRange(commentChecklists, i, i + 3);
			String key = tempComment[0].replaceAll("-Data-START", "");
			String keyValues = tempComment[1];
			String[] keyvalue = keyValues.split("\n");
			for (int j = 1; j < keyvalue.length; j++)
			{
				keyVal.put(keyvalue[j].split("::")[0], keyvalue[j].split("::")[1]);
			}
			ckeckListMap.put(key, keyVal);
		}

		return ckeckListMap;
	}
	
	public static String getValidFeatureName(String featureName)
	{
		DataSet dataSet = dataManager.getDataSet(tsvFileManager.filePath("feature-keys.tsv", false), false);

		if (!dataSet.contains(0, featureName))
		{
			if (dataSet.findRowIgnoreCase(0, featureName) != null)
			{
				return dataSet.findRowIgnoreCase(0, featureName).getString(0);
			}
		}
		return featureName;
	}
	
	public static ValidationScope getValidaionScope(String scope)
	{
		if (scope == null)
		{
			return ValidationScope.EMBL;
		}
		return ValidationScope.get(scope);
		
	}
	
	
	public static String getComponentTypeId(Entry contigEntry)
	{
		String componentTypeId=null;
		if(Entry.WGS_DATACLASS.equals(contigEntry.getDataClass()))
		{
			componentTypeId="W";
		}
		else if(Entry.HTG_DATACLASS.equals(contigEntry.getDataClass()))
		{
			List<String> componentTypeIds= new ArrayList<String>();
				List<Text> keywords=contigEntry.getKeywords();
				for(Text keywordtext:keywords)
				{
					String keyword= keywordtext.getText();
					if(keyword.contains("PHASE1")||keyword.contains("PHASE0")||keyword.contains("PHASE2"))
					{
						componentTypeIds.add("P");
					}
					if(keyword.contains("PHASE3"))
					{
						componentTypeIds.add("F");
					}
					if(keyword.contains("DRAFT")||keyword.contains("FULLTOP"))
					{
						componentTypeIds.add("D");
					}
					if(keyword.contains("ACTIVEFIN"))
					{
						componentTypeIds.add("A");
					}
					else
					{
						componentTypeIds.add("O");
					}
				}
				
				if(componentTypeIds.size()==0)
				{
					componentTypeId="O";
				}
				if(componentTypeIds.size()==1)
				{
					componentTypeId=componentTypeIds.get(0);
				}
               else 
               { // F > A > D > P
				 if (componentTypeIds.contains("F")) 
				 {
					 componentTypeId="F";
				 } 
				 else if (componentTypeIds.contains("A")) 
				 {
					 componentTypeId="A";
				 } 
				 else if (componentTypeIds.contains("D")) 
				 {
					 componentTypeId="D";
				 } 
				 else if (componentTypeIds.contains("P")) 
				 {
					 componentTypeId="P";
				 } 
				 else 
				 {
					 componentTypeId="O";
				 }
			}
		} 
		else 
		{
			componentTypeId="O";
		}
		
		return componentTypeId;
	}

	public static boolean isMatches(String regEx, String value) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}

	public static Matcher matcher(String regEx, String value) {
		Pattern pattern = Pattern.compile(regEx);
		return pattern.matcher(value);
	}

	public static boolean isAllUpperCase(String s) {

		for(char c: s.toCharArray()) {
			if(Character.isAlphabetic(c) && !Character.isUpperCase(c)){
				return false;
			}
		}
		return true;
	}
   
}
