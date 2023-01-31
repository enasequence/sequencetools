package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.genomeassembly.UnlocalisedEntry;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.ChromosomeNameFix;
import uk.ac.ebi.embl.common.CommonUtil;

public class UnlocalisedListFileReader extends GCSEntryReader
{
	public static final String MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERROR = "InvalidNoOfFields";
	Pattern pattern = Pattern.compile("\\s+");
	private static final int NUMBER_OF_COLUMNS = 2;
	private static final int OBJECT_NAME_COLUMN = 0;
	private static final int CHROMOSOME_NAME_COLUMN = 1;
	List<UnlocalisedEntry> unlocalisedEntries = new ArrayList<UnlocalisedEntry>();
	
	public UnlocalisedListFileReader(File file)
	{
		this.file=file;
	}

	@Override
	public ValidationResult read() throws FileNotFoundException, IOException
	{
		int lineNumber = 1;

		try(BufferedReader reader= CommonUtil.bufferedReaderFromFile(file))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] fields = pattern.split(line.trim());
				int noOfColumns = fields.length;
				if (noOfColumns != NUMBER_OF_COLUMNS)
				{
					error(lineNumber, MESSAGE_KEY_INVALID_NO_OF_FIELDS_ERROR);
				}
				else
				{
				   UnlocalisedEntry unlocalisedEntry = new UnlocalisedEntry();
					unlocalisedEntry.setObjectName(fields[OBJECT_NAME_COLUMN]);
					String chromosomeName = fields[CHROMOSOME_NAME_COLUMN];
					String fixedChromosomeName = ChromosomeNameFix.fix(chromosomeName);
					unlocalisedEntry.setChromosomeName(fixedChromosomeName);

					if(!chromosomeName.equals(fixedChromosomeName))
						fix(lineNumber, "ChromosomeListNameFix",chromosomeName, fixedChromosomeName);

					unlocalisedEntry.setOrigin(new FlatFileOrigin(lineNumber));
					unlocalisedEntries.add(unlocalisedEntry);
				}
				lineNumber++;
			}
		}
		return validationResult;
	}
	@Override
	public ValidationResult skip() throws IOException
	{
		return null;
	}
	@Override
	public Object getEntry()
	{
		throw new UnsupportedOperationException();
	}
	
    public List<UnlocalisedEntry> getentries()
    {
    	return unlocalisedEntries;
    }
	@Override
	public boolean isEntry()
	{
		return validationResult.isValid();
	}

}
