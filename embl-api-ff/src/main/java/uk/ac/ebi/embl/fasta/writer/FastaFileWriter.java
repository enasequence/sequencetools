package uk.ac.ebi.embl.fasta.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;


public class FastaFileWriter
{
	private Entry entry;
	private Writer writer;
	private FastaHeaderFormat headerFormat;

	public enum FastaHeaderFormat
	{
		DEFAULT_HEADER_FORMAT,
		ANALYSIS_HEADER_FORMAT;
	}
	
	public FastaFileWriter(Entry entry,Writer writer)
	{
		this.entry =entry;
		this.writer =writer;
		this.headerFormat=FastaHeaderFormat.DEFAULT_HEADER_FORMAT;
	}
	public FastaFileWriter(Entry entry, Writer writer,FastaHeaderFormat headerFormat)
	{
		this.entry =entry;
		this.writer= writer;
		this.headerFormat = headerFormat;
	}

	public void write() throws IOException
	{
		String header =null;
		switch(headerFormat)
		{
		case DEFAULT_HEADER_FORMAT:
			header= String.format(">EM_%s:%s %s %s:%s",entry.getDivision(),entry.getPrimaryAccession(),entry.getSequence().getAccessionwithVersion(),entry.getDataClass(),entry.getDescription().getText());
			break;
		case ANALYSIS_HEADER_FORMAT:
			header= String.format(">%s %s",entry.getPrimaryAccession(),entry.getSubmitterAccession());
			break;
		default:
			break;
		}
		FastaSequenceWriter sequenceWriter= new FastaSequenceWriter(writer, entry);
		writer.write(header+"\n");
		sequenceWriter.write();
	}
}
