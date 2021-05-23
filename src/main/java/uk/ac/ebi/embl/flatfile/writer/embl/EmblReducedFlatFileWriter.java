package uk.ac.ebi.embl.flatfile.writer.embl;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;

/** Reduced flat file writer.
 */
public class EmblReducedFlatFileWriter extends EntryWriter {

    public static final String REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE = "Failed to write reduced flat file. Missing sequence for entry";
    public static final String REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS = "Failed to write reduced flat file. Missing contigs for CON entry";
    public static final String REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS = "Failed to write reduced flat file. Unexpected contigs for non-CON entry";

    public final static String SEPARATOR_LINE = EmblTag.XX_TAG  + "\n";
    public final static String TERMINATOR_LINE = EmblTag.TERMINATOR_TAG  + "\n";


    public EmblReducedFlatFileWriter(Entry entry) {
        super(entry);
        wrapType = WrapType.EMBL_WRAP;
    }

    public boolean write(Writer writer) throws IOException {
        if (entry == null) {
            return false;
        }

        SourceFeature source = entry.getPrimarySourceFeature();
        entry.getFeatures().removeIf(feature -> feature instanceof SourceFeature);
        source.removeAllQualifiers();
        source.addQualifier(new QualifierFactory().createQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME, entry.getSubmitterAccession()));
        entry.addFeature(source);

        if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
            throw new IOException(REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE + ": " + entry.getPrimaryAccession());
        }

        if (entry.getDataClass().equals(Entry.CON_DATACLASS) ) {
            if(!entry.hasContigs()) {
                throw new IOException(REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS + ": " + entry.getPrimaryAccession());
            }
        } else if (entry.hasContigs()) {
            throw new IOException(REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS + ": " + entry.getPrimaryAccession());
        }

        if(new IDWriter(entry).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }

        if (new FTWriter(entry, isSortFeatures(), isSortQualifiers(), true, wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
        // Always write sequence features.
        writeFeatures(writer);

        // Write CO lines for CONs.
        if (entry.hasContigs()) {
            new COWriter(entry, wrapType).write(writer);
            writer.write(SEPARATOR_LINE);
        }

        // Always write sequence.
        new EmblSequenceWriter(entry, entry.getSequence()).write(writer);

        writer.write(TERMINATOR_LINE);

        writer.flush();
        return true;
    }

    @Override
    public void writeFeatures(Writer writer) throws IOException {
        if (new FTWriter(entry, isSortFeatures(), isSortQualifiers(), true, wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
    }

    @Override
    protected void writeReferences(Writer writer) {
        // do nothing
    }

}
