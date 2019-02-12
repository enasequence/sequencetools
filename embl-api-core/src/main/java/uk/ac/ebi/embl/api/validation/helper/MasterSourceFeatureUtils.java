package uk.ac.ebi.embl.api.validation.helper;

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
	public MasterSourceFeatureUtils() {
		isolationSourceQualifier=null;
		addUniqueName =true;
	}

	public void addSourceQualifier(String tag, String value,SourceFeature source)
	{

		if(tag==null)
			return;
		tag=tag.toLowerCase();
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
		}
	}

	}

	public void addExtraSourceQualifiers(SourceFeature source,TaxonHelper taxonHelper,String uniqueName)
	{
		if(addUniqueName&&taxonHelper.isProkaryotic(source.getScientificName())&&source.getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size()==0)
		{
			source.addQualifier( new QualifierFactory().createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,uniqueName));
		}

		if(source.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size()==0 && isolationSourceQualifier!=null)
			source.addQualifier(isolationSourceQualifier);	
	}

}
