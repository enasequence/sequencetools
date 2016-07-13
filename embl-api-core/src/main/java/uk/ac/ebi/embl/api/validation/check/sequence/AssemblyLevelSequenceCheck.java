package uk.ac.ebi.embl.api.validation.check.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

@Description("Invalid entry_name : \"{2}\". {0} sequence(s) already loaded for the assembly_id {1}.Entry_name \"{2}\" should match with one of the entry_names of the loaded {0} sequences with assembly_id {1}."
		+ "Entry_name is missing . {0} sequence(s) already loaded for the assembly_id {1}. Entry_name must be given to match with one of the entry_names of loaded {0} sequences for assembly_id {1}.")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
@GroupIncludeScope(group={ValidationScope.Group.ASSEMBLY})
public class AssemblyLevelSequenceCheck extends EntryValidationCheck
{
	private final static String ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_1 = "AssemblyLevelSequenceCheck_1";
	private final static String ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_2 = "AssemblyLevelSequenceCheck_2";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		EntryDAOUtils entryDAOUtils=getEntryDAOUtils();
		
		if(entryDAOUtils==null)
		{
			return result;
		}
		
		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||entry.getSecondaryAccessions()==null)
		{
			return result;
		}
		
	   if(entry.getSequence()!=null&&entry.getSequence().getSequenceByte()!=null&&entry.getSequence().getLength()!=0)
		{
		   	return result;
		}
	   
	   if(entry.getSequence().getContigs().size()!=0||entry.getAgpRows().size()!=0)//CO line exists
	   {
		   return result;
	   }
	   
	   Integer assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 : null;
	   String sequence_type= assemblyLevel==0?"contig":assemblyLevel==1?"scaffold":assemblyLevel==2?"chromosome":null;
       
	   
		try
		{
			  if(!entryDAOUtils.isAssemblyLevelExists(getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel))
			  {
				  return result;
			  }
			
			  if(entry.getSubmitterAccession()==null||entry.getSubmitterAccession().isEmpty())
			  {
	              reportError(entry.getOrigin(),ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_2);
	              
			  }
			byte[] sequence = entryDAOUtils.getSequence(entry.getSubmitterAccession(),getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel);
			if (sequence == null)
			{
              reportError(entry.getOrigin(),ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_1,sequence_type,getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession());
			}
		
		} catch (Exception e)
		{
			throw new ValidationEngineException(e);
		}

		return result;
	}

}
