package uk.ac.ebi.embl.api.entry.genomeassembly;

public enum AssemblyType {

	CLONEORISOLATE("CLONE OR ISOLATE"),
	PRIMARYMETAGENOME("PRIMARY METAGENOME"),
	BINNEDMETAGENOME("BINNED METAGENOME"),
	METAGENOME_ASSEMBLEDGENOME("METAGENOME-ASSEMBLED GENOME (MAG)"),
	ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME("ENVIRONMENTAL SINGLE-CELL AMPLIFIED GENOME (SAG)");
	String value;

	private AssemblyType(String value) 
	{
		this.value =value;
	}

	public String getValue()
	{
		return value;
	}

}
