package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

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

        if (entry.getDataClass() == Entry.CON_DATACLASS) {

            boolean expandedCON = isExpandedCON();
            boolean notExpandedCON = isNotExpandedCON(); // in theory should be equal to !expandedCON

            //Expanded entry: CO line, Feature, SQ line
            if (expandedCON) {
                new COWriter(entry, wrapType).write(writer);
                writer.write(SEPARATOR_LINE);
                writeFeatures(writer);
                new EmblSequenceWriter(entry, entry.getSequence()).write(writer);
            }

            //Not expanded entry: Feature, CO line
            if (notExpandedCON) {
                writeFeatures(writer);
                new COWriter(entry, wrapType).write(writer);
            }
        }
        else {
            writeFeatures(writer);
            new EmblSequenceWriter(entry, entry.getSequence()).write(writer);
        }

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
    protected void writeReferences(Writer writer) throws IOException {
        // do nothing
    }

    private boolean isExpandedCON() {
        return entry.getSequence() != null && entry.getSequence().getSequenceByte() != null && !entry.isNonExpandedCON();
    }

    private boolean isNotExpandedCON() {
        return entry.getSequence() == null || entry.getSequence().getSequenceByte() == null || entry.isNonExpandedCON();
    }

}
