package uk.ac.ebi.embl.template.reader;

public class CSVLine {
    private Integer lineNumber;
    private TemplateVariables entryTokenMap;

    public CSVLine(Integer lineNumber, TemplateVariables entryTokenMap) {
        this.lineNumber = lineNumber;
        this.entryTokenMap = entryTokenMap;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public TemplateVariables getEntryTokenMap() {
        return entryTokenMap;
    }
}
