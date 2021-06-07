package uk.ac.ebi.embl.api.validation.helper;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SampleInfo;
import uk.ac.ebi.embl.api.validation.check.feature.MasterSourceQualifierValidator;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl.MASTERSOURCEQUALIFIERS;
import uk.ac.ebi.embl.api.validation.dao.model.SampleEntity;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

public class MasterSourceFeatureUtils {

	private Qualifier isolationSourceQualifier=null;
	private final  String isolation_source_regex = "^\\s*environment\\s*\\(material\\)\\s*$";
	private final  Pattern isolation_sourcePattern = Pattern.compile(isolation_source_regex);
	private boolean addUniqueName=true;
	private final Map<String,String> qualifierSynonyms = new HashMap<>();
	private final Set<String> covid19RequiredQuals = new HashSet<>();
	private final MasterSourceQualifierValidator masterSourceQualifierValidator;

	public MasterSourceFeatureUtils() {
		qualifierSynonyms.put("metagenomic source",Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);
		qualifierSynonyms.put("host scientific name",Qualifier.HOST_QUALIFIER_NAME);
		qualifierSynonyms.put("collection date",Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
		qualifierSynonyms.put("gisaid accession id",Qualifier.NOTE_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.COUNTRY_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.LAT_LON_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.HOST_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.NOTE_QUALIFIER_NAME);

		isolationSourceQualifier=null;
		masterSourceQualifierValidator = new MasterSourceQualifierValidator();
	}

	public boolean isCovidTaxId(Long taxID) {
		return taxID != null  && taxID == 2697049L;
	}
	public void addSourceQualifier(String tag, String value,SourceFeature source)
	{

		if(tag==null)
			return;
		tag = tag.toLowerCase();

		if(qualifierSynonyms.containsKey(tag)) {
			tag = qualifierSynonyms.get(tag);
		}

		if(tag.equals(Qualifier.COLLECTION_DATE_QUALIFIER_NAME) && !masterSourceQualifierValidator.isValid(Qualifier.COLLECTION_DATE_QUALIFIER_NAME, value)) {
			//we have to do similar check for other qualifier as well.
			return;
		}

		if(isolation_sourcePattern.matcher(tag).matches())
		{
			tag=Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME;
			if(value!=null&&!value.isEmpty())
			isolationSourceQualifier= (new QualifierFactory()).createQualifier(tag,value);
		}
		else
		{
		if (MASTERSOURCEQUALIFIERS.isValid(tag))
		{

			if(!MASTERSOURCEQUALIFIERS.isNoValue(tag) && MASTERSOURCEQUALIFIERS.isNullValue(value))
				return;

			if(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME.equals(tag)||Qualifier.STRAIN_QUALIFIER_NAME.equals(tag)||Qualifier.ISOLATE_QUALIFIER_NAME.equals(tag))
			{
				addUniqueName=false;
			}

			if(MASTERSOURCEQUALIFIERS.isNoValue(tag))
			{
				if(!"NO".equalsIgnoreCase(value))
					source.addQualifier(new QualifierFactory().createQualifier(tag));
			}
			else
				source.addQualifier(new QualifierFactory().createQualifier(tag, value));
		} else if(isCovidTaxId(source.getTaxId()) && covid19RequiredQuals.contains(tag)) {
			source.addQualifier(new QualifierFactory().createQualifier(tag, value));
		}
	}

	}

	public void addExtraSourceQualifiers(SourceFeature source,TaxonHelper taxonHelper,String uniqueName)
	{
		if(addUniqueName && taxonHelper.isProkaryotic(source.getScientificName()) && source.getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size()==0)
		{
			source.addQualifier( new QualifierFactory().createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,uniqueName));
		}

		if(source.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size()==0 && isolationSourceQualifier!=null)
			source.addQualifier(isolationSourceQualifier);	
	}

	public SourceFeature constructSourceFeature(SampleEntity sample, TaxonHelper taxonHelper, SampleInfo sampleInfo) {
		FeatureFactory featureFactory = new FeatureFactory();
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		sourceFeature.setTaxId(sampleInfo.getTaxId());
		sourceFeature.setScientificName(sampleInfo.getScientificName());
		sourceFeature.setMasterLocation();

		String latitude = null;
		String longitude = null;
		String country = null;
		String region = null;
		for (Map.Entry<String, String> entry : sample.getAttributes().entrySet()) {
			String tag = entry.getKey();
			String value = entry.getValue();
			if (isCovidTaxId(sourceFeature.getTaxId()) && tag != null) {
				// Master source qualifiers values created from multiple sample fields are constructed here.
				if (tag.toLowerCase().contains("latitude")) {
					latitude = value;
				} else if (tag.toLowerCase().contains("longitude")) {
					longitude = value;
				} else if (tag.trim().equalsIgnoreCase("geographic location (country and/or sea)")) {
					country = value;
				} else if (tag.trim().equalsIgnoreCase("geographic location (region and locality)")) {
					region = value;
				} else {
					addSourceQualifier(tag, value, sourceFeature);
				}
			} else {
				addSourceQualifier(tag, value, sourceFeature);
			}
		}

		if (latitude != null && longitude != null) {
			String latValue = latitude;
			String lonValue = longitude;
			try {
				double lat = Double.parseDouble(latitude);
				latValue += " " + (lat < 0 ? "S" : "N");
			} catch (NumberFormatException ex) {
				//ignore
			}
			try {
				double lon = Double.parseDouble(longitude);
				lonValue += " " + (lon < 0 ? "W" : "E");
			} catch (NumberFormatException ex) {
				//ignore
			}
			String latLonValue = latValue + " " + lonValue;
			addSourceQualifier(Qualifier.LAT_LON_QUALIFIER_NAME, latLonValue, sourceFeature);
		}
		if (country != null || region != null) {
			addSourceQualifier(Qualifier.COUNTRY_QUALIFIER_NAME, country == null ? region : region == null ? country : country + ":" + region, sourceFeature);
		}

		Taxon taxon = taxonHelper.getTaxonById(sampleInfo.getTaxId());
		if (taxon != null)
			sourceFeature.setTaxon(taxon);
		addExtraSourceQualifiers(sourceFeature, taxonHelper, sampleInfo.getUniqueName());

		return sourceFeature;
	}


}
