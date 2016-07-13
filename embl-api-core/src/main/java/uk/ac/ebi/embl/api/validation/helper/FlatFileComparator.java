package uk.ac.ebi.embl.api.validation.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class FlatFileComparator
{
	private Logger log = Logger.getRootLogger();			
	
	private final FlatFileComparatorOptions options;
	private int lineContext = 5;	
	private final boolean ignoreEmptyLines = false;

	public FlatFileComparator( FlatFileComparatorOptions options )
	{
		this.options = options;
	}
	
	public int 
	getLineContext()
	{
		return lineContext;
	}

	public void 
	setLineContext(int lineContext)
	{
		this.lineContext = lineContext;
	}

	private boolean 
	ignoreLine( String line )
	{
		if ( line == null)
			return false;		
		return options.isIgnoreLine( line );
	}

	private BufferedReader 
	openFile(String fileName) throws FlatFileComparatorException
	{
		InputStream expectedStream = getClass().getResourceAsStream( fileName );
				
		if ( null == expectedStream )
		{
			try
			{
				expectedStream = new FileInputStream( fileName );
			}
			catch ( FileNotFoundException e)
			{
				throw new FlatFileComparatorException( "File not found: " + fileName );
			}
		}
		
		return new BufferedReader( new InputStreamReader( expectedStream ) );
	}
	
	class Line
	{		
		Line(String lineText, int lineNumber) 
		{
			this.lineText = lineText;
			this.lineNumber = lineNumber;
		}
		
		String lineText;
		int lineNumber;
	}
	
	public boolean compare(
	    String expectedFileName,
	    String actualFileName ) throws FlatFileComparatorException
	{
		BufferedReader expectedFileReader = openFile( expectedFileName );
		BufferedReader actualFileReader = openFile( actualFileName );
    	    
		log.info( String.format( "Comparing flat file [%s] against [%s]", expectedFileName, actualFileName ) );
		
	    LinkedList<Line> expectedLineHistory = new LinkedList<Line>();
	    LinkedList<Line> actualLineHistory = new LinkedList<Line>();
	    LinkedList<Line> expectedLineFuture = new LinkedList<Line>();
	    LinkedList<Line> actualLineFuture = new LinkedList<Line>();
	    	    
	    try
	    {
	    	Line prevExpectedLine = null;
	    	Line prevActualLine = null;
	    	
		    while ( true )
		    {	    			    	
		    	Line expectedLine = readLine( expectedFileReader, prevExpectedLine );	
		    	Line actualLine = readLine( actualFileReader, prevActualLine );
		   
		    	prevExpectedLine = expectedLine;
		    	prevActualLine = actualLine;		    	
		    	
		    	if ( null != expectedLine )
		    		expectedLineHistory.add( expectedLine );
			    if ( null != actualLine )
			   		actualLineHistory.add( actualLine );
	
			    while ( expectedLineHistory.size() > lineContext) 
			    	 expectedLineHistory.remove();	
			    while ( actualLineHistory.size() >  lineContext) 
			    	actualLineHistory.remove();

		    	if ( null == expectedLine && null == actualLine )
		    	{
		    	    return true;
		    	}			    
		    	else
		    	if ( null == expectedLine && null != actualLine )
		    	{
		    		System.err.println( String.format( "Expected flat file: %s terminates before the actual flat file: [%s]", expectedFileName, actualFileName ) );
		    		report( expectedFileName, 
		    				actualFileName, 
		    				expectedLineHistory, 
		    				actualLineHistory,
		    				expectedLineFuture,
		    				actualLineFuture );
		    		return false;
		    	}
		    	else
		    	if ( null != expectedLine && null == actualLine )
		    	{
		    		System.err.println( String.format( "Expected flat file: %s terminates after the actual flat file: [%s]", expectedFileName, actualFileName ) );
		    		report( expectedFileName, 
		    				actualFileName, 
		    				expectedLineHistory, 
		    				actualLineHistory,
		    				expectedLineFuture,
		    				actualLineFuture );
		    				
		    		return false;
		    	}	    	
		    	else
		        if ( !compareLine( expectedLine.lineText, actualLine.lineText ) )
		        {
		        	for ( int i = 0 ; i < lineContext ; ++ i)
		        	{
		        		Line line = readLine( expectedFileReader, prevExpectedLine );
		        		if ( null == line )
		        			break;
	        			expectedLineFuture.add( line );
	        			prevExpectedLine = line;
		        	}
		        	for ( int i = 0 ; i < lineContext ; ++ i)
		        	{
		        		Line line = readLine( actualFileReader, prevActualLine );
		        		if ( null == line )
		        			break;
	        			actualLineFuture.add( line );
	        			prevActualLine = line;
		        	}		        	
		        	
		    		report( expectedFileName, 
		    				actualFileName, 
		    				expectedLineHistory, 
		    				actualLineHistory,
		    				expectedLineFuture,
		    				actualLineFuture);
		    		return false;
		        }
		    }
	    }
	    finally
	    {
	    	try
	    	{
		    	expectedFileReader.close();
		    	actualFileReader.close();	    		
	    	}
	    	catch ( IOException e )
	    	{	    		
	    	}
	    }
	}
	
	public Line 
	readLine( BufferedReader reader, Line prevLine ) throws FlatFileComparatorException
	{
		String lineText = null;		
		int lineNumber = (null == prevLine) ? 0 : prevLine.lineNumber;
		try 
		{
			lineText = reader.readLine();
			if ( null != lineText )
				lineText.replace("\r","").replace("\n","");
			++lineNumber;
			
			while ( ( ignoreEmptyLines && null != lineText && lineText.trim().isEmpty() ) || ignoreLine( lineText ) )
			{
				lineText = reader.readLine();
				if ( null != lineText )
					lineText.replace("\r","").replace("\n","");				
				++lineNumber;
			}
		} 
		catch (IOException e) 
		{
			throw new FlatFileComparatorException( e );
		}	
		if ( null == lineText )
			return null;		
		return new Line( lineText, lineNumber );
	}

	private boolean 
	compareLine ( String expectedLine, String actualLine )
	{
		return expectedLine.equals( actualLine );
	}	
	
	private void
	report( String expectedFileName, 
			String actualFileName, 
			LinkedList<Line> expectedLineHistory, 
			LinkedList<Line> actualLineHistory,
			LinkedList<Line> expectedLineFuture,
			LinkedList<Line> actualLineFuture
			)
	{
		System.err.println( String.format( "Difference between the expected file: [%s] and the actual file: [%s]", expectedFileName, actualFileName ) );
		System.err.println( "---------------------------------------------------------------------" );
		System.err.println( "| Expected                                                          |" );
		System.err.println( "---------------------------------------------------------------------" );
		int i = expectedLineHistory.size();
		for ( Line line : expectedLineHistory )
		{
			if (  --i <= 0 )
			{
				System.err.println( "! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! " );
				// System.err.print( String.format("%-7s", line.lineNumber ) );
				System.err.println( line.lineText );
				System.err.println( "! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! " );
			}
			else
			{
			// System.err.print( String.format("%-7s", line.lineNumber ) );			
				System.err.println( line.lineText );
			}
		}
		for ( Line line : expectedLineFuture )
		{
			// System.err.print( String.format("%-7s", line.lineNumber ) );
			System.err.println( line.lineText );
		}
		System.err.println( "---------------------------------------------------------------------" );
		System.err.println( "| Actual                                                            |" );
		System.err.println( "---------------------------------------------------------------------" );
		i = actualLineHistory.size();
		for ( Line line : actualLineHistory )
		{
			if (  --i <= 0 )
			{
				System.err.println( "! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! " );
				// System.err.print( String.format("%-7s", line.lineNumber ) );
				System.err.println( line.lineText );
				System.err.println( "! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! " );
			}
			else
			{
			// System.err.print( String.format("%-7s", line.lineNumber ) );			
				System.err.println( line.lineText );
			}
		}
		for ( Line line : actualLineFuture )
		{
			//System.err.print( String.format("%-7s", line.lineNumber ) );
			System.err.println( line.lineText );
		}		
	}
}
