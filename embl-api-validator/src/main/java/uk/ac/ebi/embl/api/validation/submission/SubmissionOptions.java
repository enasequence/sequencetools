package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class SubmissionOptions
{
	public  Optional<SubmissionFiles> submissionFiles = Optional.empty();
	public  Optional<Context> context = Optional.empty();
	public  Optional<AssemblyInfoEntry> assemblyInfoEntry = Optional.empty();
	public  Optional<List<String>> locusTagPrefixes = Optional.empty();
	public  Optional<SourceFeature> source = Optional.empty();
	public  Optional<String> analysisId = Optional.empty();
	public  Optional<Connection> enproConnection = Optional.empty();
	public  Optional<Connection> eraproConnection = Optional.empty();
	public  Optional<String> reportDir = Optional.empty();
	public  Optional<Integer> minGapLength = Optional.empty();
	private EmblEntryValidationPlanProperty property =null;
	
	public  boolean isDevMode = false;
	public  boolean isFixMode = true;
	public  boolean isFixCds = true;
	public  boolean ignoreErrors = true;
	public  boolean isRemote = false;

	private  Optional<ValidationScope> validationScope = Optional.empty();

	public void init() throws ValidationEngineException
	{
		if(!submissionFiles.isPresent())
			throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
		if(!context.isPresent())
			throw new ValidationEngineException("SubmissionOptions:context must be provided");
		if(!assemblyInfoEntry.isPresent()&&isRemote)
			throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
		if(!source.isPresent()&&isRemote)
			throw new ValidationEngineException("SubmissionOptions:source must be provided");
		if(!reportDir.isPresent())
			throw new ValidationEngineException("SubmissionOptions:reportDir must be provided");
		if(!(new File(reportDir.get())).isDirectory())
			throw new ValidationEngineException("SubmissionOptions:invalid ReportDir");

		switch(context.get())
		{
		case sequence:
			validationScope = Optional.of(ValidationScope.EMBL_TEMPLATE);
			break;
		case transcriptome:
			validationScope=Optional.of(ValidationScope.ASSEMBLY_TRANSCRIPTOME);
			break;
		default:
			break;
		}
		if(!enproConnection.isPresent()||!eraproConnection.isPresent())
		{
			if(!isRemote)
			{
				throw new ValidationEngineException("SubmissionOptions:Database connections(ENAPRO,ERAPRO) must be given when validating submission internally");
			}
		}
	}
	
	public EmblEntryValidationPlanProperty getEntryValidationPlanProperty()
	{
		if(property!=null)
			return property;
			
		property = new EmblEntryValidationPlanProperty();
		property.isFixMode.set(isFixMode);
		property.isFixCds.set(isFixCds);
		if(locusTagPrefixes.isPresent()) property.locus_tag_prefixes.set(locusTagPrefixes.get());
		if(enproConnection.isPresent())  property.enproConnection.set(enproConnection.get());
		if(eraproConnection.isPresent())  property.eraproConnection.set(eraproConnection.get());
		if(analysisId.isPresent()) property.analysis_id.set(analysisId.get());
		if(assemblyInfoEntry.isPresent()) property.minGapLength.set(minGapLength.isPresent()?minGapLength.get():assemblyInfoEntry.get().getMinGapLength());
		property.ignore_errors.set(ignoreErrors);
		property.taxonHelper.set(new TaxonHelperImpl());
		if(validationScope.isPresent())property.validationScope.set(validationScope.get());
		property.isRemote.set(isRemote);
		return property;
	}
}
