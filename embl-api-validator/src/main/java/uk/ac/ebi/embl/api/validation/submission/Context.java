package uk.ac.ebi.embl.api.validation.submission;

import java.util.Arrays;
import java.util.List;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public enum Context {
	sequence(FileType.FLATFILE,FileType.TSV),
	transcriptome(FileType.FASTA,FileType.FLATFILE,FileType.MASTER),
	genome(FileType.FASTA,FileType.FLATFILE,FileType.AGP,FileType.CHROMOSOME_LIST,FileType.UNLOCALISED_LIST,FileType.MASTER);

	List<FileType> fileTypes;
	private Context(FileType...fileTypes) {
		this.fileTypes =Arrays.asList(fileTypes);
	}

	public List<FileType> getFileTypes()
	{
		return fileTypes;
	}
	public Context getContext(String context)
	{
		try {
			return Context.valueOf(context);
		}catch(Exception e)
		{
			return null;
		}
	}
}
