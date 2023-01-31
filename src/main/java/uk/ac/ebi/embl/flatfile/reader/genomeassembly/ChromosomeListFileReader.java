package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.ChromosomeNameFix;
import uk.ac.ebi.embl.api.validation.fixer.entry.SubmitterAccessionFix;
import uk.ac.ebi.embl.common.CommonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

public class ChromosomeListFileReader extends GCSEntryReader
{
	private final String MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERRORS = "InvalidNoOfFields";
	private final String MESSAGE_KEY_DUPLICATE_CHROMOSOME_NAME_ERROR = "ChromosomeListChromosomeNameDuplicationCheck";
	private final String MESSAGE_KEY_DUPLICATE_OBJECT_NAME_ERROR = "ChromosomeListObjectNameDuplicationCheck";
	private static final String INVALID_FILE_FORMAT_ERROR = "FileFormatCheck";
	private static final String EMPTY_FILE_ERROR = "EmptyFileCheck";

	Pattern pattern = Pattern.compile("\\s+");
	
	private final static int MIN_NUMBER_OF_COLUMNS = 3;
	private final static int MAX_NUMBER_OF_COLUMNS = 4;
	private final static int OBJECT_NAME_COLUMN = 0;
	private final static int CHROMOSOME_NAME_COLUMN = 1;
	private final static int CHROMOSOME_TYPE_COLUMN = 2;
	private final static int CHROMOSOME_LOCATION_COLUMN = 3;
    private Set<String> chromosomeNames= new HashSet<String>();
    private Set<String> objectNames= new HashSet<String>();
    List<ChromosomeEntry> chromosomeEntries =new ArrayList<ChromosomeEntry>();

    public ChromosomeListFileReader(File file)
    {
    	this.file=file;
    }
    

	@Override
	public ValidationResult read() throws FileNotFoundException, IOException
	{
		
		if(file!=null&&file.length()==0)
		{
			error(1, EMPTY_FILE_ERROR);
			return validationResult;
		}
		
		if(!validateFileFormat(file))
			return validationResult;
		int lineNumber = 1;

		try(BufferedReader reader = CommonUtil.bufferedReaderFromFile(file))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				
			   if (line.isEmpty()) // Skip empty lines
				{
					continue;
				}
				String[] fields = pattern.split(line);
				int numberOfColumns = fields.length;
				if (numberOfColumns < MIN_NUMBER_OF_COLUMNS || numberOfColumns > MAX_NUMBER_OF_COLUMNS)
				{
					error(lineNumber, MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERRORS);
				}
				else
				{
					ChromosomeEntry chromosomeEntry = new ChromosomeEntry();
					String submitterAccession = SubmitterAccessionFix.fix(fields[OBJECT_NAME_COLUMN]);
					chromosomeEntry.setObjectName(submitterAccession);

					String chromosomeName = fields[CHROMOSOME_NAME_COLUMN];
					String fixedChromosomeName = ChromosomeNameFix.fix(chromosomeName);
					chromosomeEntry.setChromosomeName(fixedChromosomeName);

					if(!chromosomeName.equals(fixedChromosomeName))
						fix(lineNumber, "ChromosomeListNameFix", chromosomeName, fixedChromosomeName);

					String[] topologyAndChrType = fields[CHROMOSOME_TYPE_COLUMN].split("-");
					if (topologyAndChrType.length == 2) {
						chromosomeEntry.setTopology(SequenceEntryUtils.getTopology(topologyAndChrType[0].trim()));
						chromosomeEntry.setChromosomeType(topologyAndChrType[1]);
					} else {
						  chromosomeEntry.setChromosomeType(topologyAndChrType[0]);
					}
					if (numberOfColumns == MAX_NUMBER_OF_COLUMNS)
					{
						chromosomeEntry.setChromosomeLocation(fields[CHROMOSOME_LOCATION_COLUMN].toLowerCase());
					}
					chromosomeEntry.setOrigin(new FlatFileOrigin(lineNumber));
					if(!chromosomeNames.add(chromosomeEntry.getChromosomeName().toLowerCase()))
						error(lineNumber, MESSAGE_KEY_DUPLICATE_CHROMOSOME_NAME_ERROR,chromosomeEntry.getChromosomeName());
					if(chromosomeEntry.getObjectName()!=null)
					{
						chromosomeEntry.setObjectName(StringUtils.removeEnd(chromosomeEntry.getObjectName().trim(),";"));
						if(!objectNames.add(chromosomeEntry.getObjectName()))
							error(lineNumber, MESSAGE_KEY_DUPLICATE_OBJECT_NAME_ERROR,chromosomeEntry.getObjectName());
					}
					chromosomeEntries.add(chromosomeEntry);
				}
				lineNumber++;

			}
		}
		return validationResult;
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
		throw new UnsupportedOperationException();
	}
	
    public List<ChromosomeEntry> getentries()
    {
    	return chromosomeEntries;
    }
    
    public boolean isSingleChromosome()
    {
    	return chromosomeEntries.size()==1;
    }
	@Override
	public boolean isEntry()
	{
		return validationResult.isValid();
	}
	

	public boolean validateFileFormat(File file) throws IOException 
	{  
        int emptylines =0;
		String line=null;

		try(BufferedReader  fileReader=CommonUtil.bufferedReaderFromFile(file))
		{
			while(line==null||line.isEmpty())
			{
				line=fileReader.readLine();
				emptylines++;
				if(emptylines>30)
				{
					error(1, INVALID_FILE_FORMAT_ERROR);
					return false;
				}
			}
			

			if(line.split("\\s+").length<MIN_NUMBER_OF_COLUMNS||line.split("\\s+").length>MAX_NUMBER_OF_COLUMNS)
			{
				error(1, INVALID_FILE_FORMAT_ERROR);
			}
		}
		return true;
	}
	
}
