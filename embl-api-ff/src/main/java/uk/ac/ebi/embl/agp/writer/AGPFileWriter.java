package uk.ac.ebi.embl.agp.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;

public class AGPFileWriter
{
	private Entry entry;
	private Writer writer;

	public AGPFileWriter(Entry entry, Writer writer)
	{
		this.entry =entry;
		this.writer= writer;
	}
	
	public void write() throws IOException
	{
		AGPRowWriter agpRowWriter=null;
		for(AgpRow agpRow:entry.getAgpRows())
		{
		  agpRowWriter= new AGPRowWriter(agpRow, writer);
		  agpRowWriter.write();
		  writer.write("\n");
		}
	}
}
