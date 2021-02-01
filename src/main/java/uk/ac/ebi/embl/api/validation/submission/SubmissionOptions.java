package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
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
	public  Optional<String> processDir = Optional.empty();
	public  Optional<File> reportFile = Optional.empty();
	private EmblEntryValidationPlanProperty property =null;
		
	public  boolean isDevMode = false;
	public  boolean isFixMode = true;
	public  boolean isFixCds = true;
	public  boolean ignoreErrors = false;
	public  boolean isWebinCLI = false;
	private String projectId;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void init() throws ValidationEngineException
	{
		if(!submissionFiles.isPresent() || submissionFiles.get().getFiles() == null || submissionFiles.get().getFiles().isEmpty())
			throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
		if(!context.isPresent())
			throw new ValidationEngineException("SubmissionOptions:context must be provided");
		if(!assemblyInfoEntry.isPresent()&& isWebinCLI)
			throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
		if(!source.isPresent()&& isWebinCLI)
		{   if(Context.sequence!=context.get())
			throw new ValidationEngineException("SubmissionOptions:source must be provided");
		}
		if(!reportDir.isPresent())
			throw new ValidationEngineException("SubmissionOptions:reportDir must be provided");
		if(!isWebinCLI || isDevMode) {
			if (!(new File(reportDir.get())).isDirectory())
				throw new ValidationEngineException("SubmissionOptions:invalid ReportDir");
        } else {
			for(SubmissionFile file: submissionFiles.get().getFiles()) {
				if(file.getReportFile() == null ) {
					throw new ValidationEngineException("SubmissionOptions:reportFile is mandatory for each file.");
				}
			}
        }
		if(!analysisId.isPresent()&&!isWebinCLI &&(context.get()==Context.genome||context.get()==Context.transcriptome))
			throw new ValidationEngineException("SubmissionOptions:analysisId must be provided for genome context");
		if(!processDir.isPresent()&&!isWebinCLI &&(context.get()==Context.genome||context.get()==Context.transcriptome))
			throw new ValidationEngineException("SubmissionOptions:processDir must be provided to write master file");

		if(!enproConnection.isPresent()||!eraproConnection.isPresent())
		{
			if(!isWebinCLI)
			{
				throw new ValidationEngineException("SubmissionOptions:Database connections(ENAPRO,ERAPRO) must be given when validating submission internally");
			}
		}
		if(!isWebinCLI)
			ignoreErrors =true;
		FileValidationCheck.setSequenceCount(0);
		FileValidationCheck.sequenceInfo.clear();
		FileValidationCheck.fastaInfo.clear();
		FileValidationCheck.flatfileInfo.clear();
		FileValidationCheck.agpInfo.clear();
		FileValidationCheck.agpEntryNames.clear();
		FileValidationCheck.chromosomeNameQualifiers.clear();
		FileValidationCheck.entryNames.clear();
		FileValidationCheck.duplicateEntryNames.clear();
		FileValidationCheck.chromosomeNames.clear();
		FileValidationCheck.unplacedEntryNames.clear();
		FileValidationCheck.unlocalisedEntryNames.clear();
		FileValidationCheck.setHasAnnotationOnlyFlatfile(false);
		GlobalDataSets.clear();
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
		if(assemblyInfoEntry.isPresent())
		{
			Integer mgl =minGapLength.isPresent()?minGapLength.get():assemblyInfoEntry.get().getMinGapLength();
			if(mgl!=null)
		     property.minGapLength.set(mgl);
		}
		if(Context.genome.equals(context))
		{
			property.sequenceNumber.set(1);
		}
		property.ignore_errors.set(ignoreErrors);
		property.taxonHelper.set(new TaxonHelperImpl());
		property.isRemote.set(isWebinCLI);
		return property;
	}
}
