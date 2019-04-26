package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AssemblyInfoReader extends GCSEntryReader
{
	private final String MESSAGE_KEY_INVALID_FORMAT_ERROR = "invalidlineFormat";
	private final String MESSAGE_KEY_INVALID_VALUE_ERROR = "invalidfieldValue";
	private final String MESSAGE_KEY_INVALID_FIELD_ERROR = "invalidfieldName";
	private static final String EMPTY_FILE_ERROR = "EmptyFileCheck";


	AssemblyInfoEntry assemblyInfoEntry = null;
	public AssemblyInfoReader(File file)
	{
		super();
		this.file=file;
	}
	@Override
	public ValidationResult read() throws NumberFormatException, IOException
	{
		assemblyInfoEntry= new AssemblyInfoEntry();
		int lineNumber = 1;
		if(file!=null&&file.length()==0)
		{
			error(1, EMPTY_FILE_ERROR);
			return validationResult;
		}
		try(BufferedReader reader = getBufferedReader(file))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty()) // Skip empty lines
				{
					continue;
				}
				
				String[] fields = line.trim().split("\\s+",2);
				int numberOfColumns = fields.length;
				if(numberOfColumns==1||numberOfColumns==2)
				{
					String field= StringUtils.deleteWhitespace(fields[0].toUpperCase());
					field= field.replaceAll("_","").replaceAll("-","").replaceAll("\\.","");
					switch(field)
					{
						case "NAME":
					case "ASSEMBLYNAME":
						assemblyInfoEntry.setName(numberOfColumns==1?null:fields[1]);
						break;
					case "COVERAGE":
						assemblyInfoEntry.setCoverage(numberOfColumns==1?null:fields[1]);
						break;
					case "PROGRAM":
						assemblyInfoEntry.setProgram(numberOfColumns==1?null:fields[1]);
						break;
					case "PLATFORM":
						assemblyInfoEntry.setPlatform(numberOfColumns==1?null:fields[1]);
						break;
					case "MINGAPLENGTH":
						String minGapLength=numberOfColumns==1?null:fields[1];
						if(isInteger(minGapLength))
						  assemblyInfoEntry.setMinGapLength(new Integer(minGapLength));
						else
						  error(lineNumber,MESSAGE_KEY_INVALID_VALUE_ERROR,fields[0],minGapLength);
						break;
					case "MOLECULETYPE":
						String mol_type=numberOfColumns==1?null:fields[1];
						if(isValidMoltype(mol_type))
							assemblyInfoEntry.setMoleculeType(mol_type);
						else
						    error(lineNumber,MESSAGE_KEY_INVALID_VALUE_ERROR,fields[0],mol_type);
						break;
					case "SAMPLE":
						 assemblyInfoEntry.setSampleId(numberOfColumns==1?null:fields[1]);
						 break;
					case "STUDY":
					  	 assemblyInfoEntry.setStudyId(numberOfColumns==1?null:fields[1]);
					  	 break;
					case "TPA":
						String tpa=numberOfColumns==1?null:fields[1];
						if(isValidTPA(tpa))
							assemblyInfoEntry.setTpa("yes".equalsIgnoreCase(tpa)||"true".equalsIgnoreCase(tpa)?true:false);
						else
						    error(lineNumber,MESSAGE_KEY_INVALID_VALUE_ERROR,fields[0],tpa);
						break;
					case "ASSEMBLY_TYPE" :
						String assemblyType = fields[1];
						assemblyInfoEntry.setAssemblyType(assemblyType);
						break;
					default :
						error(lineNumber,MESSAGE_KEY_INVALID_FIELD_ERROR,line);
						break;
					}
					
				}
				else
				{
					error(lineNumber,MESSAGE_KEY_INVALID_FORMAT_ERROR,line);
				}
				lineNumber++;

			}
		}
		return validationResult;
	}

	public static boolean isInteger(String s) {
		if(s==null)
			return false;
	    try {
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}

	public static boolean isValidMoltype(String molType) {
		
		if (molType == null) {
			return false;
		}

		molType = StringUtils.deleteWhitespace(molType).toUpperCase();
		if (!molType.equals("GENOMICDNA") && !molType.equals("GENOMICRNA") && !molType.equals("VIRALCRNA"))
			return false;

		return true;

	}
	
public static boolean isValidTPA(String tpa) {
		
		if (tpa == null) 
		{
			return false;
		}

		tpa = StringUtils.deleteWhitespace(tpa).toUpperCase();
		if ("true".equalsIgnoreCase(tpa)||"false".equalsIgnoreCase(tpa)||"yes".equalsIgnoreCase(tpa)||"no".equalsIgnoreCase(tpa))
			return true;;

		return false;

	}

	@Override
	public ValidationResult skip() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getEntry()
	{
		return assemblyInfoEntry;
	}

	@Override
	public boolean isEntry()
	{
		return validationResult.isValid();
	}

}
