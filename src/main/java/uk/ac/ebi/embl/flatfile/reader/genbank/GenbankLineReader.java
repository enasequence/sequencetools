/**
 * 
 */
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.BufferedReader;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class GenbankLineReader extends LineReader {

    public GenbankLineReader() {
    	super();
    }
		
    public GenbankLineReader(BufferedReader reader) {
    	super(reader);
    }

    public GenbankLineReader(BufferedReader reader, String fileId) {
    	super(reader, fileId);
    }

    private final static int TAG_WIDTH = 12;

	@Override
    protected int getTagWidth(String line) {
		if (line.startsWith("            ")) {
			return TAG_WIDTH;
		}
		if (!isTag(line)) {
			return 0;
		}		
		return Math.min(TAG_WIDTH, line.length());
    }
		
	private static final Pattern TAG = Pattern.compile(			
			"^\\s{0,4}[A-Z]{3,11}((\\s*)|(\\s+.*))$");
	
    @Override
    protected boolean isTag(String line) {
		if (line.startsWith(GenbankTag.TERMINATOR_TAG)) {
			return true;
		}    	
    	return TAG.matcher(line).matches();
    }
}
