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
	public final Optional<SubmissionFiles> submissionFiles = Optional.empty();
	public final Optional<Context> context = Optional.empty();
	public final Optional<AssemblyInfoEntry> assemblyInfoEntry = Optional.empty();
	public final Optional<List<String>> locusTagPrefixes = Optional.empty();
	public final Optional<SourceFeature> source = Optional.empty();
	public final Optional<String> analysisId = Optional.empty();
	public final Optional<Connection> enproConnection = Optional.empty();
	public final Optional<Connection> eraproConnection = Optional.empty();
	public final Optional<String> reportDir = Optional.empty();
	
	public final boolean isDevMode = false;
	public final boolean isFixMode = true;
	public final boolean isFixCds = true;
	public final boolean ignoreErrors = true;
	public final boolean isRemote = false;

	private final Optional<ValidationScope> validationScope = Optional.empty();

	public void init() throws ValidationEngineException
	{
		if(!submissionFiles.isPresent())
			throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
		if(!context.isPresent())
			throw new ValidationEngineException("SubmissionOptions:context must be provided");
		if(!assemblyInfoEntry.isPresent())
			throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
		if(!reportDir.isPresent())
			throw new ValidationEngineException("SubmissionOptions:reportDir must be provided");
		if(!(new File(reportDir.get())).isDirectory())
			throw new ValidationEngineException("SubmissionOptions:invalid ReportDir");

		switch(context.get())
		{
		case sequence:
			validationScope.of(ValidationScope.EMBL_TEMPLATE);
			break;
		case transcriptome:
			validationScope.of(ValidationScope.ASSEMBLY_TRANSCRIPTOME);
			break;
		default:
			break;
		}
	}
	
	public EmblEntryValidationPlanProperty getEntryValidationPlanProperty()
	{
		EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
		property.isFixMode.set(isFixMode);
		property.isFixCds.set(isFixCds);
		property.locus_tag_prefixes.set(locusTagPrefixes.get());
		property.enproConnection.set(enproConnection.get());
		property.eraproConnection.set(eraproConnection.get());
		property.analysis_id.set(analysisId.get());
		property.minGapLength.set(assemblyInfoEntry.get().getMinGapLength());
		property.ignore_errors.set(ignoreErrors);
		property.taxonHelper.set(new TaxonHelperImpl());
		property.validationScope.set(validationScope.get());
		property.isRemote.set(isRemote);

		return property;
	}
}
