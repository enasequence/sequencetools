package uk.ac.ebi.embl.api.entry;

import java.io.Serializable;

public class AssemblySequenceInfo implements Serializable
{
	public static final String fileName= "sequence.info"; 
	long sequenceLength;
	int assemblyLevel;
	String accession;
	public AssemblySequenceInfo(long sequenceLength,int assemblyLevel,String accession) {
      this.sequenceLength =sequenceLength;
      this.assemblyLevel=assemblyLevel;
      this.accession=accession;
	}
	public long getSequenceLength() {
		return sequenceLength;
	}
	public int getAssemblyLevel() {
		return assemblyLevel;
	}
	public String getAccession() {
		return accession;
	}
	
	
}
