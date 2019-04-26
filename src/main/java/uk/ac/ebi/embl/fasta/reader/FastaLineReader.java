package uk.ac.ebi.embl.fasta.reader;

import java.io.BufferedReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class FastaLineReader extends LineReader
{

    public 
    FastaLineReader(BufferedReader reader) 
    {
    	super(reader);
    }

    private final static int DEFAULT_TAG_WIDTH = 1;
    private final static String FASTA_TAG=">";

	@Override
    protected int getTagWidth(String line) {
		return Math.min(DEFAULT_TAG_WIDTH, line.length());
    }

	@Override    	
	protected boolean 
	isTag( String line ) 
	{
		if( line.startsWith(FASTA_TAG) )
		{
			return true;
		}

		return false;
	}
	
	@Override    			
    protected boolean 
    isSkipLine(String line) 
	{
		return line!=null&&line.trim().isEmpty();
	} 
}
