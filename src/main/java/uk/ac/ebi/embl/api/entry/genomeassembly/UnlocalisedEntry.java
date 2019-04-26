package uk.ac.ebi.embl.api.entry.genomeassembly;

public class UnlocalisedEntry extends GCSEntry
{
	private String chromosomeName;
	private String objectName;
	private String acc;
	
	public String getAcc()
	{
		return acc;
	}
	
	public void setAcc(String acc)
	{
		this.acc=acc;
	}
	
	public String getObjectName()
	{
		return objectName;
	}
	
	public void setObjectName(String objectName)
	{
		this.objectName=objectName;
	}
	
	public String getChromosomeName()
	{
		return chromosomeName;
	}
	
	public void setChromosomeName(String chromosomeName)
	{
		this.chromosomeName = chromosomeName;
	}
	
}
