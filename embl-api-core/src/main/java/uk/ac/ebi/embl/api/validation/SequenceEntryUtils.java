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
package uk.ac.ebi.embl.api.validation;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SequenceEntryUtils {

	private SequenceEntryUtils() {
	}

	public static String getMoleculeType(Entry entry) {
		if (entry == null) {
			return null;
		}
		Sequence sequence = entry.getSequence();
		if (sequence == null) {
			return null;
		}
		return sequence.getMoleculeType();
	}

	private static CVTable<String, String> chromosomeLocationToOrganelleValue = null;	

	public static String getOrganelleValue(String chromosomeLocation)
	{
		if (chromosomeLocationToOrganelleValue == null)
		{
			chromosomeLocationToOrganelleValue = new CVTable<String, String>(); 
			chromosomeLocationToOrganelleValue.put("mitochondrion", "mitochondrion");
			chromosomeLocationToOrganelleValue.put("plastid:chloroplast", "chloroplast");
			chromosomeLocationToOrganelleValue.put("mitochondrion:kinetoplast", "kinetoplast");
			chromosomeLocationToOrganelleValue.put("plastid:chromoplast", "chromoplast");
			chromosomeLocationToOrganelleValue.put("plastid:cyanelle", "cyanelle");
			chromosomeLocationToOrganelleValue.put("plastid:leucoplast", "leucoplast");
			chromosomeLocationToOrganelleValue.put("plastid:proplastid", "proplastid");
			chromosomeLocationToOrganelleValue.put("plastid:apicoplast", "apicoplast");
			chromosomeLocationToOrganelleValue.put("plastid", "plastid");
			chromosomeLocationToOrganelleValue.put("nucleomorph", "nucleomorph");
			chromosomeLocationToOrganelleValue.put("hydrogenosome", "hydrogenosome");
			chromosomeLocationToOrganelleValue.put("chromatophore", "chromatophore");				
		}		
		return chromosomeLocationToOrganelleValue.getId(chromosomeLocation);
	}	

	private static CVTable<Integer, String> chromosomeLocationToOrganelleId = null;	
	
	public static Integer getOrganelleId(String chromosomeLocation)
	{
		if (chromosomeLocationToOrganelleId == null)
		{
			chromosomeLocationToOrganelleId = new CVTable<Integer, String>(); 
			chromosomeLocationToOrganelleId.put(1, "mitochondrion");
			chromosomeLocationToOrganelleId.put(2, "chloroplast");
			chromosomeLocationToOrganelleId.put(3, "kinetoplast");
			chromosomeLocationToOrganelleId.put(4, "chromoplast");
			chromosomeLocationToOrganelleId.put(5, "cyanelle");
			chromosomeLocationToOrganelleId.put(6, "leucoplast");
			chromosomeLocationToOrganelleId.put(7, "proplastid");
			chromosomeLocationToOrganelleId.put(8, "apicoplast");
			chromosomeLocationToOrganelleId.put(9, "plastid");
			chromosomeLocationToOrganelleId.put(10, "nucleomorph");
			chromosomeLocationToOrganelleId.put(11, "hydrogenosome");
			chromosomeLocationToOrganelleId.put(12, "chromatophore");				
		}		
		return chromosomeLocationToOrganelleId.getId(chromosomeLocation);
	}	
	
	public static boolean hasAnnotation(Entry entry)
	{
		for(Feature feature: entry.getFeatures())
		{
			if(!(feature instanceof SourceFeature)&& !(Feature.ASSEMBLY_GAP_FEATURE_NAME.equals(feature.getName())))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isFeatureAvailable(String featureName, Entry entry) {
		if (StringUtils.isEmpty(featureName) || entry == null) {
			return false;
		}
		for (Feature feature : entry.getFeatures()) {
			if (feature == null) {
				continue;
			}
			if (featureName.equals(feature.getName())) {
				return true;
			}
		}
		return false;
	}

	public static List<Feature> getFeatures(String featureName, Entry entry) {
		List<Feature> result = new ArrayList<Feature>();
		if (StringUtils.isEmpty(featureName) || entry == null) {
			return result;
		}
		for (Feature feature : entry.getFeatures()) {
			if (feature == null) {
				continue;
			}
			if (featureName.equals(feature.getName())) {
				result.add(feature);
			}
		}
		return result;

	}
	
	public static List<CdsFeature> getCDSFeatures(Entry entry) {
		List<CdsFeature> result = new ArrayList<CdsFeature>();
		if (entry == null) {
			return result;
		}
		for (Feature feature : entry.getFeatures()) {
			if (feature == null) {
				continue;
			}
			if (feature.getName().equals(Feature.CDS_FEATURE_NAME)) {
				result.add((CdsFeature) feature);
			}
		}
		return result;

	}

	public static Collection<Feature> getFeaturesContainingQualifier(
			String qualifierName, Entry entry) {
		Collection<Feature> result = new ArrayList<Feature>();
		if (StringUtils.isEmpty(qualifierName) || entry == null) {
			return result;
		}
		for (Feature feature : entry.getFeatures()) {
			if (feature == null) {
				continue;
			}
			if (!feature.getQualifiers(qualifierName).isEmpty()) {
				result.add(feature);
			}
		}
		return result;
	}

	public static SourceFeature getSingleSource(Entry entry) {
		Collection<Feature> sources = getFeatures(Feature.SOURCE_FEATURE_NAME,
				entry);
		if (sources == null || sources.isEmpty()) {
			return null;
		}
		return (SourceFeature) sources.iterator().next();
	}

	public static boolean isQualifierAvailable(String qualifierName,
			Feature feature) {
		if (qualifierName == null || feature == null) {
			return false;
		}

		for (Qualifier qualifier : feature.getQualifiers()) {
			if (qualifierName.equals(qualifier.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isQualifierAvailable(String qualifierName, Entry entry) {
		if (qualifierName == null || entry == null) {
			return false;
		}
		for (Feature feature : entry.getFeatures()) {
			for (Qualifier qualifier : feature.getQualifiers()) {

				if (qualifierName.equals(qualifier.getName())) {
					return true;
				}
			}

		}
		return false;
	}

	public static boolean isQualifierwithPatternAvailable(String qualifierName,
			String qualifierValuePattern, Entry entry) {
		if (qualifierName == null || entry == null
				|| qualifierValuePattern == null) {
			return false;
		}
		Pattern pattern = Pattern.compile(qualifierValuePattern);
		for (Feature feature : entry.getFeatures())
		{
			for (Qualifier qualifier : feature.getQualifiers())
			{
				if (qualifier.getName() != null && qualifier.getValue() != null)
				{
					if (qualifierName.equals(qualifier.getName()) && pattern.matcher(qualifier.getValue()).matches())
					{
						return true;
					}
				}
			}
			
		}
		return false;
	}

	public static Qualifier getQualifier(String qualifierName, Feature feature) {
		if (qualifierName == null || feature == null) {
			return null;
		}

		for (Qualifier qualifier : feature.getQualifiers()) {
			if (qualifierName.equals(qualifier.getName())) {
				return qualifier;
			}
		}
		return null;
	}
	
	public static List<Qualifier> getQualifiers(String qualifierName,
			Entry entry) {
		if (qualifierName == null || entry == null
				|| entry.getFeatures() == null) {
			return null;
		}
		List<Qualifier> qualifierList = new ArrayList<Qualifier>();
		for (Feature feature : entry.getFeatures()) {
			for (Qualifier qualifier : feature.getQualifiers()) {
				if (qualifierName.equals(qualifier.getName())) {
					qualifierList.add(qualifier);
				}
			}
		}
		return qualifierList;
	}
	
	public static String getQualifierValue(String qualifierName, Feature feature) {
        Qualifier qualifier = getQualifier(qualifierName, feature);
        if(qualifier != null){
            return qualifier.getValue();
        }
        return null;
    }

	public static boolean isAnyOfQualifiersAvailable(String[] qualifierNames,
			Feature feature) {
		if (qualifierNames == null || qualifierNames.length == 0
				|| feature == null) {
			return false;
		}
		for (Qualifier qualifier : feature.getQualifiers()) {
			if (qualifier == null) {
				continue;
			}
			if (ArrayUtils.contains(qualifierNames, qualifier.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAnyOfQualifiersAvailable(String[] qualifierNames,
			Entry entry)
	{
		if (qualifierNames == null || qualifierNames.length == 0 || entry == null)
		{
			return false;
		}
		for (Feature feature : entry.getFeatures())
		{
			if (isAnyOfQualifiersAvailable(qualifierNames, feature))
				return true;
		}
		return false;
	}

	public static Collection<String> getAllOrganismNames(Entry entry) {
		ArrayList<String> result = new ArrayList<String>();

		Collection<Feature> sources = getFeatures(Feature.SOURCE_FEATURE_NAME,
				entry);
		if (sources.isEmpty()) {
			return result;
		}

		for (Feature source : sources) {
			if (source == null) {
				continue;
			}

			Collection<Qualifier> organisms = source
					.getQualifiers(Qualifier.ORGANISM_QUALIFIER_NAME);
			if (organisms.isEmpty()) {
				continue;
			}
			for (Qualifier organism : organisms) {
				if (organism == null) {
					continue;
				}
				String organismName = organism.getValue();
				if (!StringUtils.isEmpty(organismName)) {
					result.add(organismName);
				}
			}
		}

		return result;
	}

	public static boolean isQualifierWithValueAvailable(String qualifierName,
			String qualifierValue, Feature feature) {

		if (StringUtils.isEmpty(qualifierValue)) {
			return false;
		}

		List<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
		if (qualifiers.isEmpty()) {
			return false;
		}

		for (Qualifier qualifier : qualifiers) {
			if (qualifier == null) {
				continue;
			}
			if (StringUtils.equals(qualifier.getValue(), qualifierValue)) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean isQualifierWithValueAvailable(String qualifierName,
			String qualifierValue, Entry entry) {

		if (StringUtils.isEmpty(qualifierValue)) {
			return false;
		}
		Collection<Qualifier> qualifiers = new ArrayList<Qualifier>();
		for (Feature feature : entry.getFeatures()) {
			qualifiers.addAll(feature.getQualifiers(qualifierName));
		}
		if (qualifiers.isEmpty()) {
			return false;
		}

		for (Qualifier qualifier : qualifiers) {
			if (qualifier == null) {
				continue;
			}
			if (StringUtils.equals(qualifier.getValue(), qualifierValue)) {
				return true;
			}
		}

		return false;
	}

	public static int getFeatureQualifierCount(String qualifierName,
			Feature feature) {

		if (StringUtils.isEmpty(qualifierName)) {
			return 0;
		}

		List<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
		return qualifiers.size();
	}

    /**
     * NOTE : this method looks at the extreme boundaries of both locations passed in and regards one as being within
     * the other if these extremities are contained by the other feature. If both locations are segmented, it is
     * possible for the segments to have no overlap but still be within the extreme coordinates of the other feature,
     * this method will regard one to be contained by the other, when it could be argued that it is not contained due
     * to there being no overlap. We can change this method to deal with this if it becomes relevant to do so.
     *
     * @param location1
     * @param location2
     * @return
     */
	public static boolean isLocationWithin(
			CompoundLocation<Location> location1,
			CompoundLocation<Location> location2) {
		if (location1.getLocations() == null
				|| location2.getLocations() == null) {
			return false;
		}

		Location startFirstLocation = getStartFromLocations(location1);
		Location endFirstLocation = getEndFromLocations(location1);
		Location startSecondLocation = getStartFromLocations(location2);
		Location endSecondLocation = getEndFromLocations(location2);

		if (startFirstLocation != null && endFirstLocation != null
				&& startSecondLocation != null && endSecondLocation != null) {
			if ((startFirstLocation.getBeginPosition() >= startSecondLocation.getBeginPosition())
					&& (endFirstLocation.getEndPosition() <= endSecondLocation.getEndPosition())) {

				/*
				 * tests if the start and end locations being compared are on
				 * the same strand - they need to be for comparison to count.
				 * Need to look at the global complement and the complement of
				 * the inner location as the global complement can "neutralize"
				 * the inner one, so I check that the global complement and
				 * location complement to not match to count as being
				 * complement. I have separated into 2 if statements to try to
				 * keep the code more readable.
				 */
				boolean location1IsComplement = location1.isComplement();
				boolean location2IsComplement = location2.isComplement();

				if (doComplementsMatch(location1IsComplement,
						location2IsComplement, startFirstLocation,
						startSecondLocation)) {

					if (doComplementsMatch(location1IsComplement,
							location2IsComplement, endFirstLocation,
							endSecondLocation)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean doComplementsMatch(
			boolean location1GlobalComplement,
			boolean location2GlobalComplement,
            Location location1,
			Location location2) {

		boolean location1Complement = location1.isComplement();
		if (location1GlobalComplement) {
			location1Complement = !location1Complement;
		}

		boolean location2Complement = location2.isComplement();
		if (location2GlobalComplement) {
			location2Complement = !location2Complement;
		}

		return location1Complement == location2Complement;
	}

	public static boolean doLocationsOverlap(
			CompoundLocation<Location> firstLocation,
			CompoundLocation<Location> secondLocation) {

		if (firstLocation.getLocations() == null
				|| secondLocation.getLocations() == null) {
			return false;
		}

        for (Location firstLocationSegment : firstLocation.getLocations()) {
            for (Location secondLocationSegment : secondLocation.getLocations()) {
                boolean overlap = doLocationsOverlap(
                        firstLocationSegment,
                        secondLocationSegment,
                        firstLocation.isComplement(),
                        secondLocation.isComplement());
                if(overlap){
                    return true;//return true as soon as we find one that overlaps
                }
            }
        }
        return false;
    }

	private static boolean doLocationsOverlap(
            Location firstLocation,
            Location secondLocation,
            boolean firstLocationGlobalComplement,
            boolean secondLocationGlobalComplement) {

		if (firstLocation == null || secondLocation == null) {
			return false;
		}

		/**
		 * work out which of the locations is first...
		 */
		if (firstLocation.getBeginPosition() <= secondLocation.getBeginPosition()) {
			/**
			 * does the end of the first location overlap with the beginning of
			 * the second?
			 */
            if ((firstLocation.getEndPosition() >= secondLocation.getBeginPosition())) {
                if (doComplementsMatch(
                        firstLocationGlobalComplement,
                        secondLocationGlobalComplement,
                        firstLocation,
                        secondLocation)) {
                    return true;
                }
            }
        } else {
			/**
			 * does the end of the second location overlap with the beginning of
			 * the first?
			 */
				if (secondLocation.getEndPosition() >= firstLocation.getBeginPosition()) {
					if (doComplementsMatch(
                            firstLocationGlobalComplement,
							secondLocationGlobalComplement,
                            firstLocation,
							secondLocation)) {
						return true;
				}
			}
		}

		return false;
	}

	public static Location getStartFromLocations(
			CompoundLocation<Location> locations) {
		if (locations == null) {
			return null;
		}

		List<Location> locationList = new ArrayList<Location>(
				locations.getLocations());
		Collections.sort(locationList, new LocationComparator(
				LocationComparator.START_LOCATION));

		return locationList.get(0);
	}

	public static Location getEndFromLocations(
			CompoundLocation<Location> locations) {
		if (locations == null) {
			return null;
		}

		List<Location> locationList = new ArrayList<Location>(
				locations.getLocations());
		Collections.sort(locationList, new LocationComparator(
				LocationComparator.END_LOCATION));

		return locationList.get(locationList.size() - 1);
	}

	/**
	 * Checks to see if a feature's location spans a circular boundary - assumes
	 * the genome the feature is coming from is circular.
	 * 
	 * @param location
	 * @param sequenceLength
	 * @return
	 */
	public static boolean isCircularBoundary(
			CompoundLocation<Location> location, long sequenceLength) {
		if (location.getLocations().size() == 1) {
			return false;// cant be if there is only 1 location element
		}

		boolean lastLocation = false;
		List<Location> locationList = location.getLocations();
		for (int i = 0; i < locationList.size(); i++) {
			if (i == locationList.size() - 1) {
				lastLocation = true;
			}

			Long position = location.getLocations().get(i).getEndPosition();
			if (position == sequenceLength && !lastLocation) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * deletes the qualifiers which have 'DELETED' value
	 * 
	 * @param feature
	 * 
	 */
	public static boolean deleteDeletedValueQualifiers(Feature feature, ArrayList<Qualifier> deleteQualifierList)
	{
		boolean deleted = false;
		for (Qualifier qual : deleteQualifierList)
		{
			feature.removeQualifier(qual);
			deleted = true;
		}
		return deleted;

	}
	
	/**
	 * Delete duplicated qualfiier.
	 * 
	 * @param feature
	 *            the feature
	 * @param qualifierName
	 *            the qualifier name
	 */
	public static boolean deleteDuplicatedQualfiier(Feature feature, String qualifierName)
	{
		ArrayList<Qualifier> qualifiers = (ArrayList<Qualifier>) feature.getQualifiers(qualifierName);
		Set<String> qualifierValueSet = new HashSet<String>();

		for (Qualifier qual : qualifiers)
		{
			if (qual.getValue() != null)
			{
				if (!qualifierValueSet.add(qual.getValue()))
				{
					feature.removeQualifier(qual);
					return true;
				}
			}

		}
		return false;

	}
	
	public static List<Qualifier> getSourceQualifiers(Entry entry)
	{
		List<Feature> features = getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
		if(features.size()==0)
		{
			return null;
		}
		List<Qualifier> sourceQualifiers = new ArrayList<Qualifier>();
		
		for (Feature sourceFeature : features)
		{
			sourceQualifiers.addAll(sourceFeature.getQualifiers());
		}
		return sourceQualifiers;
	}
	
	public static String generateMasterEntryDescription(SourceFeature source, AnalysisType analysisType)
	{
		
		String strainValue=source.getSingleQualifierValue(Qualifier.STRAIN_QUALIFIER_NAME);
		String scientificName= source.getScientificName();
		String isolateValue =source.getSingleQualifierValue(Qualifier.ISOLATE_QUALIFIER_NAME);
					
		if(scientificName==null||scientificName.isEmpty())
			return null;//invalid source
		
		boolean hasStrain=strainValue!=null&&!strainValue.isEmpty();
		boolean hasIsolate=isolateValue!=null&&!isolateValue.isEmpty();
		boolean includeStrain=hasStrain&&!scientificName.toLowerCase().contains(strainValue.toLowerCase());
		boolean includeIsolate=!hasStrain&&hasIsolate&&!scientificName.toLowerCase().contains(isolateValue.toLowerCase());
		
		String descriptionFormat="%s %s %s genome assembly";

		if(analysisType == AnalysisType.TRANSCRIPTOME_ASSEMBLY) {
			return Entry.TSA_DATACLASS + ": " +scientificName;
		}

		return includeStrain ?(String.format(descriptionFormat, scientificName,"strain",strainValue)):
			   includeIsolate?( String.format(descriptionFormat, scientificName,"isolate",isolateValue)): 
			   (String.format(descriptionFormat,scientificName,"","" ).replaceAll("  ", ""));
	
	}
}
