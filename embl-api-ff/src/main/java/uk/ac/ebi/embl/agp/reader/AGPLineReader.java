package uk.ac.ebi.embl.agp.reader;

import java.io.BufferedReader;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class AGPLineReader extends LineReader
{
    public 
    AGPLineReader(BufferedReader reader) 
    {
    	super(reader);
    }

	private final static String SCREGEX = "\\s";


	@Override
    protected int getTagWidth(String line) {
		return getTag(line).length();
    }

	@Override    	
	protected boolean 
	isTag( String line ) 
	{
		return true;
	}
	
	@Override
	protected String getTag(String line)
	{
		if (line == null)
			return null;
		String[] fields = line.trim().split(SCREGEX);
		if (fields.length == 0)
		{
			return null;
		}
		return trimObjectName(fields[0]);
	}
	
	@Override
	public boolean joinLine() {
		if (!isCurrentLine()) {
			return false;
		}
		if (!isNextLine()) {
			return false;
		}
		// compare current and next tag
		return getCurrentTag().equals(getNextTag());
	}
	
	@Override    			
    protected boolean 
    isSkipLine(String line) 
	{
		return line!=null&&line.trim().isEmpty();
	} 
	
	
	private String trimObjectName(String object_name)
	{
		return StringUtils.removeEnd(object_name.replaceAll("\\s", ""),";");
	}
	
	
}
