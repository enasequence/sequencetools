package uk.ac.ebi.embl.api.entry.genomeassembly;

public class ChromosomeEntry extends GCSEntry
{
	
	private String chromosomeName;
	private String chromosomeType;
	private String chromosomeLocation;
	private String objectName;
	
	public String getObjectName()
	{
		return objectName;
	}
	
	public void setObjectName(String objectName)
	{
		this.objectName = objectName;
	}
	
	public String getChromosomeName()
	{
		return chromosomeName;
	}
	
	public void setChromosomeName(String chromosomeName)
	{
		this.chromosomeName = chromosomeName;
	}
	
	public String getChromosomeType()
	{
		return chromosomeType;
	}
	
	public void setChromosomeType(String chromosomeType)
	{
		this.chromosomeType = chromosomeType;
	}
	
	public String getChromosomeLocation()
	{
		return chromosomeLocation;
	}
	
	public void setChromosomeLocation(String chromosomeLocation)
	{
		this.chromosomeLocation = chromosomeLocation;
	}
	
	public boolean equals(ChromosomeEntry entry)
	{
		return (entry.getObjectName()!=null&&this.getObjectName()!=null&&this.getObjectName().equals(entry.getObjectName()))
				&&(entry.getChromosomeName()!=null&&this.getChromosomeName()!=null&&this.getChromosomeName().equals(entry.getChromosomeName()))
				&&(entry.getChromosomeLocation()!=null&&this.getChromosomeLocation()!=null&&this.getChromosomeLocation().equals(entry.getChromosomeLocation()))
				&&(entry.getChromosomeType()!=null&&this.getChromosomeType()!=null&&this.getChromosomeType().equals(entry.getChromosomeType()));
	}
}
