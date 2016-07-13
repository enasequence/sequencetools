package uk.ac.ebi.embl.api.validation.plan;

import java.sql.Connection;

import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;

public class ValidationPlanFactory
{
	public static ValidationPlan getValidationPlan(FileType fileType, ValidationScope scope, Connection con,int min_gap_length,boolean assembly)
	{
		return getValidationPlan(fileType, scope, false, false, con, min_gap_length,assembly);		
	}

	public static ValidationPlan getValidationPlan(FileType fileType, ValidationScope scope, boolean fixMode, boolean devMode, Connection con,
			int min_gap_length,boolean assembly)
	{//ValidationScope validationScope, boolean devMode, boolean fix,Connection con,int min_gap_length,boolean assembly
		ValidationPlan validationPlan = null;
		EmblEntryValidationPlanProperty emblEntryValidationProperty = new EmblEntryValidationPlanProperty();
		emblEntryValidationProperty.validationScope.set(scope);
		emblEntryValidationProperty.isDevMode.set(devMode);
		emblEntryValidationProperty.isFixMode.set(fixMode);
		emblEntryValidationProperty.enproConnection.set(con);
		emblEntryValidationProperty.minGapLength.set(min_gap_length);
		emblEntryValidationProperty.isAssembly.set(assembly);
		emblEntryValidationProperty.fileType.set(fileType);
		
		switch (fileType)
		{
			case EMBL:
			case GENBANK:
		{

			validationPlan = new EmblEntryValidationPlan(emblEntryValidationProperty);
			validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
			validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
			break;
		}
			case GFF3:
			{
				validationPlan = new GFF3ValidationPlan(emblEntryValidationProperty);
				validationPlan.addMessageBundle(ValidationMessageManager.GFF3_VALIDATION_BUNDLE);
				break;
			}
			case GENOMEASSEMBLY:
			{
				validationPlan = new GenomeAssemblyValidationPlan(emblEntryValidationProperty);
				validationPlan.addMessageBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
				validationPlan.addMessageBundle(ValidationMessageManager.GENOMEASSEMBLY_FIXER_BUNDLE);
				break;
			}
		default:
			break;
				
		}
		return validationPlan;
	}
}
