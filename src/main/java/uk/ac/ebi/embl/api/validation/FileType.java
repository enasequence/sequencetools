package uk.ac.ebi.embl.api.validation;

public enum FileType
{
	EMBL, ASSEMBLYINFO,CHROMOSOMELIST,UNLOCALISEDLIST,GFF3,FASTA,AGP,GENBANK,MASTER;
	
	static public FileType get(String fileType)
	{
		if (fileType == null)
			return EMBL;
		try
		{
			return valueOf(fileType.toUpperCase());
		}
		catch (IllegalArgumentException x)
		{
			throw new IllegalArgumentException("invalid file type: "+ fileType);
		}
	}
}
