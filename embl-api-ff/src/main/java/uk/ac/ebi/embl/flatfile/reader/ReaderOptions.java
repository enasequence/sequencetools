package uk.ac.ebi.embl.flatfile.reader;

public class ReaderOptions {

    private boolean ignoreSequence;
    private boolean parseSourceOnly;

    public boolean isIgnoreSequence() {
        return ignoreSequence;
    }

    public void setIgnoreSequence(boolean ignoreSequence) {
        this.ignoreSequence = ignoreSequence;
    }

    public boolean isParseSourceOnly() {
        return parseSourceOnly;
    }

    public void setParseSourceOnly(boolean parseSourceOnly) {
        this.parseSourceOnly = parseSourceOnly;
    }
}
