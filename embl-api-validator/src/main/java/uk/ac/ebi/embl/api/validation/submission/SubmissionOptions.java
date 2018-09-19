package uk.ac.ebi.embl.api.validation.submission;

import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class SubmissionOptions
{
	SubmissionProperty<SubmissionFiles> submissionFiles = new SubmissionProperty<SubmissionFiles>(null);
	SubmissionProperty<String> context = new SubmissionProperty<String>(null);
	SubmissionProperty<AssemblyInfoEntry> assemblyInfoEntry = new SubmissionProperty<AssemblyInfoEntry>(null);
	SubmissionProperty<List<String>> locusTagPrefixes = new SubmissionProperty<List<String>>(null);
	SubmissionProperty<SourceFeature> source = new SubmissionProperty<SourceFeature>(null);
	
	public void validate() throws ValidationEngineException
	{
		if(submissionFiles==null)
			throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
		if(context ==null)
			throw new ValidationEngineException("SubmissionOptions:context must be provided");
		if(assemblyInfoEntry==null)
			throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
	}
	
}
