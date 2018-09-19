package uk.ac.ebi.embl.api.validation.submission;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlanProperty;

public class SubmissionOptions
{
	public final SubmissionProperty<SubmissionFiles> submissionFiles = new SubmissionProperty<SubmissionFiles>(null);
	public final SubmissionProperty<String> context = new SubmissionProperty<String>(null);
	public final SubmissionProperty<AssemblyInfoEntry> assemblyInfoEntry = new SubmissionProperty<AssemblyInfoEntry>(null);
	public final SubmissionProperty<List<String>> locusTagPrefixes = new SubmissionProperty<List<String>>(null);
	public final SubmissionProperty<SourceFeature> source = new SubmissionProperty<SourceFeature>(null);
	public final SubmissionProperty<String> analysisId = new SubmissionProperty<String>(null);
	public final SubmissionProperty<Connection> enproConnection = new SubmissionProperty<Connection>(null);
	public final SubmissionProperty<Connection> eraproConnection = new SubmissionProperty<Connection>(null);
	public final SubmissionProperty<Boolean> isDevMode = new SubmissionProperty<Boolean>(false);
	public final SubmissionProperty<Boolean> isFixMode = new SubmissionProperty<Boolean>(false);
	public final SubmissionProperty<HashMap<String, Long>> contigEntryNames= new SubmissionProperty<HashMap<String, Long>>(new HashMap<String,Long>());
	public final SubmissionProperty<Boolean> isFixCds = new SubmissionProperty<Boolean>(false);
	public final SubmissionProperty<Boolean> ignore_errors = new SubmissionProperty<Boolean>(false);
	public final SubmissionProperty<ValidationScope> validationScope = new SubmissionProperty<ValidationScope>(ValidationScope.EMBL);

	public void init() throws ValidationEngineException
	{
		if(submissionFiles==null)
			throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
		if(context ==null)
			throw new ValidationEngineException("SubmissionOptions:context must be provided");
		if(assemblyInfoEntry==null)
			throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
		if(Context.getContext(context.get())==null)
            throw new ValidationEngineException("SubmissionOptions:Invalid context-"+context.get());
		switch(Context.getContext(context.get()))
		{
		case sequence :
			validationScope.set(ValidationScope.EMBL_TEMPLATE);
			break;
		case transcriptome:
			validationScope.set(ValidationScope.ASSEMBLY_TRANSCRIPTOME);
			break;
		default:
			break;
			
		}
		
	}
	
	EmblEntryValidationPlanProperty getEntryValidationPlanProperty()
	{
		EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
		property.isFixMode.set(isFixMode.get());
		property.isFixCds.set(isFixCds.get());
		property.locus_tag_prefixes.set(locusTagPrefixes.get());
		property.enproConnection.set(enproConnection.get());
		property.eraproConnection.set(eraproConnection.get());
		property.analysis_id.set(analysisId.get());
		property.minGapLength.set(assemblyInfoEntry.get().getMinGapLength());
		property.ignore_errors.set(ignore_errors.get());
		property.taxonHelper.set(new TaxonHelperImpl());
		property.validationScope.set(validationScope.get());
		return property;
	}
}
