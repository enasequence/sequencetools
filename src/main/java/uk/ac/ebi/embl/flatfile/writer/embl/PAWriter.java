/**
 * 
 */
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/**
 * Flat file writer for PA line for the CDS product.
 * 
 * @author simonk
 * 
 */
public class PAWriter extends FlatFileWriter {

	private String accession;

	public PAWriter(Entry entry, WrapType wrapType) {
		super(entry, wrapType);

		this.accession = entry.getPrimaryAccession() + "."
				+ entry.getSequence().getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.embl.flatfile.writer.FlatFileWriter#write(java.io.Writer)
	 */
	@Override
	public boolean write(Writer writer) throws IOException {

		writeBlock(writer, EmblPadding.PA_PADDING, accession);

		return true;
	}

}
