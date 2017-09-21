package uk.ac.ebi.embl.fasta.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;

public class FastaSequenceWriter
{
	Writer writer;
	Entry entry;
	public FastaSequenceWriter(Writer writer,Entry entry)
	{
		this.writer = writer;
		this.entry = entry;
	}
	
	public void write() throws IOException
	{
		byte[] sequence=entry.getSequence().getSequenceByte();
		StringBuffer line= new StringBuffer();
		for(byte sequenceByte: sequence)
		{
			line.append((char)sequenceByte);
			if((line.length())%60==0)
			{
				writer.write(line.toString().toUpperCase()+"\n");
				line =new StringBuffer();
			}
		}
		writer.write(line.toString().toUpperCase()+"\n");
	}
}
