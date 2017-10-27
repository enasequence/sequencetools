package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.genomeassembly.UnlocalisedEntry;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class UnlocalisedListFileReader extends GCSEntryReader
{
	public static final String MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERROR = "InvalidNoOfFields";
	Pattern pattern = Pattern.compile("\\s+");
	private static final int NUMBER_OF_COLUMNS = 2;
	private static final int OBJECT_NAME_COLUMN = 0;
	private static final int CHROMOSOME_NAME_COLUMN = 1;
	
	@Override
	public void read(File file) throws IOException
	{
		int lineNumber = 1;
		
		try(BufferedReader reader= getBufferedReader(file))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				UnlocalisedEntry unlocalisedEntry = null;
				String[] fields = pattern.split(line.trim());
				int noOfColumns = fields.length;
				if (noOfColumns != NUMBER_OF_COLUMNS)
				{
					error(lineNumber, MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERROR);
				}
				else
				{
					unlocalisedEntry = new UnlocalisedEntry();
					unlocalisedEntry.setObjectName(fields[OBJECT_NAME_COLUMN]);
					unlocalisedEntry.setChromosomeName(fields[CHROMOSOME_NAME_COLUMN]);
					unlocalisedEntry.setOrigin(new FlatFileOrigin(lineNumber));
				}
			}
			lineNumber++;
		}
		
	}

}
