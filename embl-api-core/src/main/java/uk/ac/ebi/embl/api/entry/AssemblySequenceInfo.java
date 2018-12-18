package uk.ac.ebi.embl.api.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class AssemblySequenceInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
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
	
	public void setSequenceLength(long sequenceLength) {
		this.sequenceLength = sequenceLength;
	}
	public void setAssemblyLevel(int assemblyLevel) {
		this.assemblyLevel = assemblyLevel;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}
	
  public static void writeObject(HashMap<String,AssemblySequenceInfo> sequenceInfo,String outputDir) throws ValidationEngineException
  {
	  
	  try {
			Files.deleteIfExists(Paths.get(outputDir+File.separator+AssemblySequenceInfo.fileName));
			}catch(Exception e)
			{
				throw new ValidationEngineException("Failed to delete sequence info file: "+e.getMessage());
			}
			try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir+File.separator+AssemblySequenceInfo.fileName)))
			{
				oos.writeObject(sequenceInfo);
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Assembly sequence registration failed: "+e.getMessage());
			}
  }
  
  public static HashMap<String,AssemblySequenceInfo> getObject(String inputDir) throws ValidationEngineException
  {
	  HashMap<String,AssemblySequenceInfo> infoObject=null;
	  
			try(ObjectInputStream  oos = new ObjectInputStream (new FileInputStream(inputDir+File.separator+AssemblySequenceInfo.fileName)))
			{
				infoObject= (HashMap<String, AssemblySequenceInfo>) oos.readObject();
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Failed to read assembly sequence information: "+e.getMessage());
			}
			
			return infoObject;
  }
  
}
