/**
 * 
 */
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/**
 * Flat file writer for OG lines - writes both organelles and plasmids.
 * 
 * @author simonk
 * 
 */
public class OGWriter extends FlatFileWriter {

	private SourceFeature sourceFeature;

	/**
	 * @param entry
	 * @param wrapType
	 */
	public OGWriter(Entry entry, SourceFeature sourceFeature, WrapType wrapType) {

		super(entry, wrapType);

		this.sourceFeature = sourceFeature;

		setWrapChar(WrapChar.WRAP_CHAR_SPACE);
	}

	@Override
	public boolean write(Writer writer) throws IOException {

		String organelle = sourceFeature.getSingleQualifierValue("organelle");
		String plasmid = sourceFeature.getSingleQualifierValue("plasmid");

		if (organelle != null && organelle.length() > 1) {

			// Make first letter into upper case
			organelle = organelle.substring(0, 1).toUpperCase()
					+ organelle.substring(1);

			writeBlock(writer, EmblPadding.OG_PADDING, organelle);
		}

		if (plasmid != null && plasmid.length() > 1) {

			if ( !plasmid.toUpperCase().startsWith( "PLASMID" ) &&
				 !plasmid.toUpperCase().startsWith( "VECTOR" ) )
			{
				// Prefix with Plasmid
				plasmid = "Plasmid " + plasmid;	
			}

			writeBlock(writer, EmblPadding.OG_PADDING, plasmid);
		}

		return true;
	}
}
