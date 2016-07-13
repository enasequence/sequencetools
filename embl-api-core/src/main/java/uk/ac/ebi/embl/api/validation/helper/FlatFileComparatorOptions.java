package uk.ac.ebi.embl.api.validation.helper;

import java.util.ArrayList;

public class FlatFileComparatorOptions 
{
	private boolean ignoreSTLines = true;
	private boolean ignoreXXLines = true;
	private boolean ignoreDTLines = true;
	private boolean ignoreDRLines = true;
	private boolean ignoreDbXrefLines = true;
	
	private ArrayList<String> ignoreLine = new ArrayList<String> ();

	public boolean 
	isIgnoreLine( String line )
	{
		if ( ignoreSTLines && line.startsWith( "ST *" ) ) return true;
		if ( ignoreXXLines && line.startsWith( "XX" ) ) return true;
		if ( ignoreDTLines && line.startsWith( "DT" ) ) return true;
		if ( ignoreDRLines && line.startsWith( "DR" ) ) return true;
		if ( ignoreDbXrefLines && line.startsWith( "FT                   /db_xref" ) ) return true;
		
		for ( String prefix : ignoreLine )
		{
			if ( line.startsWith( prefix ) )
				return true;
		}
		return false;		
	}
	
	public void 
	setIgnoreLine( String linePrefix )
	{
		ignoreLine.add( linePrefix );
	}

	public boolean 
	isIgnoreSTLines() 
	{
		return ignoreSTLines;
	}

	public void 
	setIgnoreSTLines(boolean ignoreSTLines) 
	{
		this.ignoreSTLines = ignoreSTLines;
	}

	public 	boolean
	isIgnoreXXLines() 
	{
		return ignoreXXLines;
	}

	public void 
	setIgnoreXXLines(boolean ignoreXXLines) 
	{
		this.ignoreXXLines = ignoreXXLines;
	}

	public boolean 
	isIgnoreDTLines() 
	{
		return ignoreDTLines;
	}

	public void 
	setIgnoreDTLines(boolean ignoreDTLines) 
	{
		this.ignoreDTLines = ignoreDTLines;
	}

	public boolean 
	isIgnoreDRLines() 
	{
		return ignoreDRLines;
	}

	public void 
	setIgnoreDRLines(boolean ignoreDRLines) 
	{
		this.ignoreDRLines = ignoreDRLines;
	}

	public boolean 
	isIgnoreDbXrefLines() 
	{
		return ignoreDbXrefLines;
	}

	public void 
	setIgnoreDbXrefLines(boolean ignoreDbXrefLines) 
	{
		this.ignoreDbXrefLines = ignoreDbXrefLines;
	}
}
