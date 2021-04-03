package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Reduced flat file writer.
 */
public class EmblReducedFlatFileWriter extends EntryWriter {

    public static final String REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE = "Failed to write reduced flat file. Missing sequence for entry";
    public static final String REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS = "Failed to write reduced flat file. Missing contigs for CON entry";
    public static final String REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS = "Failed to write reduced flat file. Unexpected contigs for non-CON entry";

    public EmblReducedFlatFileWriter(Entry entry) {
        super(entry);
        wrapType = WrapType.EMBL_WRAP;
    }

    private final boolean excludeSource = true;

    public final static String SEPARATOR_LINE = EmblTag.XX_TAG  + "\n";
    public final static String TERMINATOR_LINE = EmblTag.TERMINATOR_TAG  + "\n";

    public boolean write(Writer writer) throws IOException {
        if (entry == null) {
            return false;
        }

        if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
            throw new IOException(REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE + ": " + entry.getPrimaryAccession());
        }

        if (entry.getDataClass() == Entry.CON_DATACLASS && !entry.isContigs()) {
            throw new IOException(REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS + ": " + entry.getPrimaryAccession());
        }

        if (entry.getDataClass() != Entry.CON_DATACLASS && entry.isContigs()) {
            throw new IOException(REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS + ": " + entry.getPrimaryAccession());
        }

        if(new IDWriter(entry).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }

        // Always write sequence features.
        writeFeatures(writer);

        // Write CO lines for CONs.
        if (entry.isContigs()) {
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
        if (new FTWriter(entry, isSortFeatures(), isSortQualifiers(), excludeSource, wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
    }

    @Override
    protected void writeReferences(Writer writer) {
        // do nothing
    }

}
