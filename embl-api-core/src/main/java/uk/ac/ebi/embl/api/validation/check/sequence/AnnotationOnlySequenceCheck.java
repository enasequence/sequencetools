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

@Description("")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
public class AnnotationOnlySequenceCheck extends EntryValidationCheck
{
	private final static String ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_1 = "AnnotationOnlySequenceCheck_1";
	private final static String ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_2 = "AnnotationOnlySequenceCheck_2";
	private final static String ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_3 = "AnnotationOnlySequenceCheck_3";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
	try{	
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
		
	   if(entry.getSequence()!=null&&entry.getSequence().getSequenceByte()!=null&&entry.getSequence().getLength()!=0)
		{
		   	return result;
		}
	   
	   if(entry.getSequence()!=null&&(entry.getSequence().getContigs().size()!=0||entry.getSequence().getAgpRows().size()!=0))//CO line exists
	   {
		   return result;
	   }
	   String primaryAcc= null;
	   byte[] sequence=null;
	   
	   if(entry.getPrimaryAccession()!=null)
	   {
		   sequence=entryDAOUtils.getSequence(primaryAcc);
	   }
	   else
	   {
	   if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||entry.getSecondaryAccessions()==null)
		{
			return result;
		}
	   Integer assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 : null;
	   String sequence_type= assemblyLevel==0?"contig":assemblyLevel==1?"scaffold":assemblyLevel==2?"chromosome":null;
       if(!entryDAOUtils.isAssemblyLevelExists(getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel))
		  {
			  return result;
		  }
	   if(entry.getSubmitterAccession()==null||entry.getSubmitterAccession().isEmpty())
		  {
	         reportError(entry.getOrigin(),ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_2);
	              
		  }
	      primaryAcc = entryDAOUtils.getPrimaryAcc(getEmblEntryValidationPlanProperty().analysis_id.get(), entry.getSubmitterAccession(), assemblyLevel);
		if(primaryAcc==null)
		{
            reportError(entry.getOrigin(),ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_1,sequence_type,getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession());
            return result;
		}

		sequence = entryDAOUtils.getSequence(primaryAcc);		
		}
	   
		if (sequence == null)
		 {
            reportError(entry.getOrigin(),ASSEMBLY_LEVEL_SEQUENCE_CHECK_MESSAGE_3,primaryAcc,entry.getSubmitterAccession());
		 }
		
	}catch(Exception e)
	{
		throw new ValidationEngineException(e.getMessage());
	}

		return result;
	}

}
