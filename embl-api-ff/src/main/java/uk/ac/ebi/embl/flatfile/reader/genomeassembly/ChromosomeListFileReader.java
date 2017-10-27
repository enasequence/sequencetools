package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class ChromosomeListFileReader extends GCSEntryReader
{
	private final String MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERRORS = "InvalidNoOfFields";
	private final String MESSAGE_KEY_DUPLICATE_CHROMOSOME_NAME_ERROR = "ChromosomeListChromosomeNameDuplicationCheck";

	
	Pattern pattern = Pattern.compile("\\s+");
	
	private final static int MIN_NUMBER_OF_COLUMNS = 3;
	private final static int MAX_NUMBER_OF_COLUMNS = 4;
	private final static int OBJECT_NAME_COLUMN = 0;
	private final static int CHROMOSOME_NAME_COLUMN = 1;
	private final static int CHROMOSOME_TYPE_COLUMN = 2;
	private final static int CHROMOSOME_LOCATION_COLUMN = 3;
    private Set<String> chromosomeNames= new HashSet<String>();
    ChromosomeEntry chromosomeEntry =null;
	
    public ChromosomeListFileReader(File file)
    {
    	this.file=file;
    }
	@Override
	public ValidationResult read() throws FileNotFoundException, IOException
	{
		
		int lineNumber = 1;

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
				
				String[] fields = pattern.split(line);
				int numberOfColumns = fields.length;
				if (numberOfColumns < MIN_NUMBER_OF_COLUMNS || numberOfColumns > MAX_NUMBER_OF_COLUMNS)
				{
					error(lineNumber, MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERRORS);
				}
				else
				{
					chromosomeEntry = new ChromosomeEntry();
					chromosomeEntry.setObjectName(fields[OBJECT_NAME_COLUMN]);
					chromosomeEntry.setChromosomeName(fields[CHROMOSOME_NAME_COLUMN]);
					chromosomeEntry.setChromosomeType(fields[CHROMOSOME_TYPE_COLUMN]);
					if (numberOfColumns == MAX_NUMBER_OF_COLUMNS)
					{
						chromosomeEntry.setChromosomeLocation(fields[CHROMOSOME_LOCATION_COLUMN]);
					}
					chromosomeEntry.setOrigin(new FlatFileOrigin(lineNumber));
					if(!chromosomeNames.add(chromosomeEntry.getChromosomeName()))
						error(lineNumber, MESSAGE_KEY_DUPLICATE_CHROMOSOME_NAME_ERROR,chromosomeEntry.getChromosomeName());

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
		return chromosomeEntry;
	}
	@Override
	public boolean isEntry()
	{
		return validationResult.isValid();
	}
	
}
