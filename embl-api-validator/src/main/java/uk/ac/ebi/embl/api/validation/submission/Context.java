package uk.ac.ebi.embl.api.validation.submission;

public enum Context {
	sequence,
    transcriptome,
    genome;
	
	public static Context getContext(String context)
	{
		try {
			return Context.valueOf(context);
		}catch(Exception e)
		{
			return null;
		}
	}
}
