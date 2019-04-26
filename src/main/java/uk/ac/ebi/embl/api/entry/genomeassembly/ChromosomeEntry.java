package uk.ac.ebi.embl.api.entry.genomeassembly;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;

public class ChromosomeEntry extends GCSEntry
{
	
	private String chromosomeName;
	private String chromosomeType;
	private String chromosomeLocation;
	private String objectName;
	private String accession;
	private List<Qualifier> chromosomeQualifeirs = new ArrayList<Qualifier>();
	
	public String getAccession() 
	{
		return accession;
	}

	public void setAccession(String accession) 
	{
		this.accession = accession;
	}
	
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
	
	public List<Qualifier> getQualifiers(boolean virus)
	{
		if (chromosomeLocation != null && !chromosomeLocation.isEmpty()&& !virus&&!chromosomeLocation.equalsIgnoreCase("Phage"))
		{
			String organelleValue =  SequenceEntryUtils.getOrganelleValue(chromosomeLocation);
			if (organelleValue != null)
			{									
				chromosomeQualifeirs.add(new QualifierFactory().createQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, SequenceEntryUtils.getOrganelleValue(chromosomeLocation)));
			}
		}	
		else if (chromosomeName != null && !chromosomeName.isEmpty())
		{
			if (Qualifier.PLASMID_QUALIFIER_NAME.equals(chromosomeType))
			{
				chromosomeQualifeirs.add(new QualifierFactory().createQualifier(Qualifier.PLASMID_QUALIFIER_NAME, chromosomeName));
			}
			else if (Qualifier.CHROMOSOME_QUALIFIER_NAME.equals(chromosomeType))
			{
				chromosomeQualifeirs.add(new QualifierFactory().createQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, chromosomeName));
			}
			else if("segmented".equals(chromosomeType)||"multipartite".equals(chromosomeType))
			{
				chromosomeQualifeirs.add(new QualifierFactory().createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, chromosomeName));

			}

		}
		if("monopartite".equals(chromosomeType))
		{
			chromosomeQualifeirs.add(new QualifierFactory().createQualifier(Qualifier.NOTE_QUALIFIER_NAME, chromosomeType));
		}
		return chromosomeQualifeirs;
	}
}
