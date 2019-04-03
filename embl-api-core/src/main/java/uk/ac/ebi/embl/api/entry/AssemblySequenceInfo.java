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
import java.util.List;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class AssemblySequenceInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String sequencefileName= "sequence.info";
	public static final String fastafileName= "fasta.info";
	public static final String flatfilefileName= "flatfile.info";
	public static final String agpfileName= "agp.info";
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
	
  public static void writeMapObject(HashMap<String,AssemblySequenceInfo> sequenceInfo,String outputDir,String fileName) throws ValidationEngineException
  {
	  
	  try {
			Files.deleteIfExists(Paths.get(outputDir+File.separator+fileName));
			}catch(Exception e)
			{
				throw new ValidationEngineException("Failed to delete sequence info file: "+e.getMessage(), e);
			}
			try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir+File.separator+fileName)))
			{
				oos.writeObject(sequenceInfo);
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Assembly sequence registration failed: "+e.getMessage(), e);
			}
  }
  
  public static HashMap<String,AssemblySequenceInfo> getMapObject(String inputDir,String fileName) throws ValidationEngineException
  {
	  HashMap<String,AssemblySequenceInfo> infoObject=new HashMap<String,AssemblySequenceInfo>();
	  
	  if(!Files.exists(Paths.get(inputDir+File.separator+fileName)))
		  return infoObject;
		  
			try(ObjectInputStream  oos = new ObjectInputStream (new FileInputStream(inputDir+File.separator+fileName)))
			{
				infoObject= (HashMap<String, AssemblySequenceInfo>) oos.readObject();
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Failed to read assembly sequence information: "+e.getMessage(), e);
			}
			
			return infoObject;
  }
  
  public static void writeListObject(List<String> entryNames,String outputDir,String fileName) throws ValidationEngineException
  {
	  
	  try {
			Files.deleteIfExists(Paths.get(outputDir+File.separator+fileName));
			}catch(Exception e)
			{
				throw new ValidationEngineException("Failed to delete file: "+fileName+"\n"+e.getMessage(), e);
			}
			try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir+File.separator+fileName)))
			{
				oos.writeObject(entryNames);
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Assembly names registration failed: "+e.getMessage(), e);
			}
  }

  public static List<String> getListObject(String inputDir,String fileName) throws ValidationEngineException
  {
	     List<String> infoObject=null;
	  
			try(ObjectInputStream  oos = new ObjectInputStream (new FileInputStream(inputDir+File.separator+fileName)))
			{
				infoObject= (List<String>) oos.readObject();
		               
			}catch(Exception e)
			{
	        throw new ValidationEngineException("Failed to read assembly names information: "+fileName+"\n"+e.getMessage(), e);
			}
			
			return infoObject;
  }


	public static void writeObject(Object o,String outputDir,String fileName) throws ValidationEngineException
	{

		try {
			Files.deleteIfExists(Paths.get(outputDir+File.separator+fileName));
		}catch(Exception e)
		{
			throw new ValidationEngineException("Failed to delete file: "+fileName+"\n"+e.getMessage(), e);
		}
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputDir+File.separator+fileName)))
		{
			oos.writeObject(o);

		}catch(Exception e)
		{
			throw new ValidationEngineException("Assembly names registration failed: "+e.getMessage());
		}
	}

	public static Object getObject(String inputDir,String fileName) throws ValidationEngineException
	{
		Object infoObject=null;

		try(ObjectInputStream  oos = new ObjectInputStream (new FileInputStream(inputDir+File.separator+fileName)))
		{
			infoObject=  oos.readObject();

		}catch(Exception e)
		{
			throw new ValidationEngineException("Failed to read assembly names information: "+fileName+"\n"+e.getMessage(), e);
		}

		return infoObject;
	}
}
