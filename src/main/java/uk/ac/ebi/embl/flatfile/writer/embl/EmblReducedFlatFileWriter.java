package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

/** Reduced flat file writer.
 */
public class EmblReducedFlatFileWriter extends EntryWriter {

    public EmblReducedFlatFileWriter(Entry entry) {
        super(entry);
        wrapType = WrapType.EMBL_WRAP;
    }

    private boolean excludeSource = true;

    public boolean isExcludeSource() {
        return excludeSource;
    }

    public void setExcludeSource(boolean excludeSource) {
        this.excludeSource = excludeSource;
    }

    public final static String SEPARATOR_LINE = EmblTag.XX_TAG  + "\n";
    public final static String TERMINATOR_LINE = EmblTag.TERMINATOR_TAG  + "\n";

    public boolean write(Writer writer) throws IOException {
        if (entry == null) {
            return false;
        }
        if(new IDWriter(entry).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }

        //Not expanded entry
        if( null == entry.getSequence()
                || null == entry.getSequence().getSequenceByte()||entry.isNonExpandedCON() )
            writeFeatures( writer );

        if(new COWriter(entry, wrapType).write(writer)) {
            if (entry.getSequence() != null &&
                    entry.getSequence().getSequenceByte() != null&&!entry.isNonExpandedCON()) {
                writer.write(SEPARATOR_LINE);
            }
        }

        //Expanded entry
        if( null != entry.getSequence()
                && null != entry.getSequence().getSequenceByte()&&!entry.isNonExpandedCON() )
            writeFeatures( writer );

        // TODO; check -- these master writers are probably not needed
        if(new MasterWGSWriter(entry, wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
        if(new MasterCONWriter(entry, wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }

        if((new MasterTPAWriter(entry, wrapType)).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
        if((new MasterTLSWriter(entry, wrapType)).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }

        (new MasterTSAWriter(entry, wrapType)).write(writer);

        if(entry.getMasterScaffoldAccessions() != null && !entry.getMasterScaffoldAccessions().isEmpty()) {
            new MasterScaffoldWriter(entry, wrapType).write(writer);
        }

        if(!entry.isNonExpandedCON())
            new EmblSequenceWriter(entry, entry.getSequence()).write(writer);
        writer.write(TERMINATOR_LINE);

        writer.flush();
        return true;
    }

    @Override
    public void writeFeatures(Writer writer) throws IOException {
        if (new FTWriter(entry, isSortFeatures(), isSortQualifiers(), isExcludeSource(), wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
    }

    @Override
    protected void writeReferences(Writer writer) throws IOException {
        // do nothing
    }

}
