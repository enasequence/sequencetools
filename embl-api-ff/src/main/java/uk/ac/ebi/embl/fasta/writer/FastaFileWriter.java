package uk.ac.ebi.embl.fasta.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;

public class FastaFileWriter
{
	private Entry entry;
	private Writer writer;

	public FastaFileWriter(Entry entry, Writer writer)
	{
		this.entry =entry;
		this.writer= writer;
	}
	
	public void write() throws IOException
	{
		  String header= String.format(">EM_%s:%s %s %s:%s",entry.getDivision(),entry.getPrimaryAccession(),entry.getSequence().getAccessionwithVersion(),entry.getDataClass(),entry.getDescription().getText());
		  FastaSequenceWriter sequenceWriter= new FastaSequenceWriter(writer, entry);
		  writer.write(header+"\n");
		  sequenceWriter.write();
	}
}
