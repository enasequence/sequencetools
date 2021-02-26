package uk.ac.ebi.embl.api.entry.genomeassembly;

public enum AssemblyType {

	CLONEORISOLATE("CLONE OR ISOLATE", "clone or isolate"),
	PRIMARYMETAGENOME("PRIMARY METAGENOME","primary metagenome"),
	BINNEDMETAGENOME("BINNED METAGENOME", "binned metagenome"),
	CLINICALISOLATEASSEMBLY("CLINICAL ISOLATE ASSEMBLY", "clinical isolate assembly"),
	COVID_19_OUTBREAK("COVID-19 OUTBREAK", "COVID-19 outbreak"),
	METAGENOME_ASSEMBLEDGENOME("METAGENOME-ASSEMBLED GENOME (MAG)", "Metagenome-Assembled Genome (MAG)"),
	ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME("ENVIRONMENTAL SINGLE-CELL AMPLIFIED GENOME (SAG)", "Environmental Single-Cell Amplified Genome (SAG)");
	String value;
	String fixedValue;

	private AssemblyType(String value) 
	{
		this.value =value;
	}

	AssemblyType(String value, String fixedValue) {
		this.value = value;
		this.fixedValue = fixedValue;
	}

	public String getValue()
	{
		return value;
	}

	public String getFixedValue()
	{
		return fixedValue;
	}
}
