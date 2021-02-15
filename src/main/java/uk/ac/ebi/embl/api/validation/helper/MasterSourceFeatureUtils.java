package uk.ac.ebi.embl.api.validation.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl.MASTERSOURCEQUALIFIERS;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;

public class MasterSourceFeatureUtils {

	private Qualifier isolationSourceQualifier=null;
	private final  String isolation_source_regex = "^\\s*environment\\s*\\(material\\)\\s*$";
	private final  Pattern isolation_sourcePattern = Pattern.compile(isolation_source_regex);
	private boolean addUniqueName=true;
	private final Map<String,String> qualifierSynonyms = new HashMap<>();
	private final Set<String> covid19RequiredQuals = new HashSet<>();

	public MasterSourceFeatureUtils() {
		qualifierSynonyms.put("metagenomic source","metagenome_source");
		qualifierSynonyms.put("host scientific name","host");
		qualifierSynonyms.put("gisaid accession is","note");
		qualifierSynonyms.put("geographic location (country and/or sea)","country");
		qualifierSynonyms.put("geographic location (region and locality)","country");
		covid19RequiredQuals.add(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.COUNTRY_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.LAT_LON_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.HOST_QUALIFIER_NAME);
		covid19RequiredQuals.add(Qualifier.NOTE_QUALIFIER_NAME);

		isolationSourceQualifier=null;
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

}
