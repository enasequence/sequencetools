package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.sql.SQLException;
import java.util.ArrayList;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
@ExcludeScope(validationScope = { ValidationScope.ASSEMBLY_MASTER })
public class AssemblySourceQualiferFix extends EntryValidationCheck
{
	private final static String QUALIFIER_FIX_ID_1 = "AssemblySourceQualiferFix_1";
	private final static String QUALIFIER_FIX_ID_2= "AssemblySourceQualiferFix_2";

	
	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		
		if(getEntryDAOUtils()==null)
		{
			return result;
		}
				
		result = new ValidationResult();
		
		if (entry == null)
		{
			return result;
		}
		
		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null)
		{
			return result;
		}
		
		if(entry.getPrimarySourceFeature() == null)
		{
			FeatureFactory featureFactory=new FeatureFactory();
			Order<Location>featureLocation = new Order<Location>();
			LocationFactory locationFactory=new LocationFactory();
			if(entry.getSequence()!=null)
			featureLocation.addLocation(locationFactory.createLocalRange(1l, entry.getSequence().getLength()));
			SourceFeature sourceFeature=featureFactory.createSourceFeature();
			sourceFeature.setLocations(featureLocation);
			entry.addFeature(sourceFeature);
		}
		try
		{
			Entry masterEntry = getEntryDAOUtils().getMasterEntry(getEmblEntryValidationPlanProperty().analysis_id.get());
			
			SourceFeature source=null;
			if(masterEntry!=null&&masterEntry.getPrimarySourceFeature()!=null)
			{
				source=masterEntry.getPrimarySourceFeature();
			}
			// Add chromosome, plasmid and organelle qualifiers.
			// TODO: add segment qualifer for viruses.
			if (getEmblEntryValidationPlanProperty().validationScope.get().equals(ValidationScope.ASSEMBLY_CHROMOSOME))
			{
				entry.getPrimarySourceFeature().removeAllQualifiers(); // TODO: except /note and maybe some other qualifiers
				
				ArrayList<Qualifier> chromosomeQualifiers = getEntryDAOUtils().getChromosomeQualifiers(getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(),source);

				for (Qualifier chromosomeQualifier : chromosomeQualifiers)
				{
					addSingleSourceQualifierIfDoesNotExist(entry, chromosomeQualifier,QUALIFIER_FIX_ID_1);
				}
			}
			
			if(source!=null)
			{
				for (Qualifier sourceQualifier : source.getQualifiers())
				{
					addSingleSourceQualifierIfDoesNotExist(entry, sourceQualifier,QUALIFIER_FIX_ID_2);				
				}
			}

			
		}
		catch (SQLException e)
		{
			throw new ValidationEngineException(e);
		}
		
		return result;
	}
		
	private void addSingleSourceQualifierIfDoesNotExist(Entry entry, Qualifier qualifier,String message_id)
	{
		if (entry.getPrimarySourceFeature().getSingleQualifier(qualifier.getName()) == null)
		{
			//if(Qualifier.ORGANISM_QUALIFIER_NAME.equals(qualifier.getName()))
			//{
			//	entry.getPrimarySourceFeature().setScientificName(qualifier.getValue());
			//}
			entry.getPrimarySourceFeature().addQualifier(qualifier);
			if(Qualifier.MOL_TYPE_QUALIFIER_NAME.equals(qualifier.getName())&&entry.getSequence()!=null)
			{
				entry.getSequence().setMoleculeType(qualifier.getValue());
			}
			reportMessage(Severity.FIX, entry.getPrimarySourceFeature().getOrigin(), message_id, qualifier.getName());					
		}			
	}	
}
