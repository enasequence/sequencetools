package uk.ac.ebi.embl.api.validation.template;

import java.util.Iterator;

public class StringBuilderLineIterator implements Iterator<String> {
    private static final String newline = "\n";

    private StringBuilder builder;
    private boolean hasNext;
    private int currentLocation;
    private int extractToIndex;
    private int previousLocation;

    public StringBuilderLineIterator(StringBuilder builder) {
        this.builder = builder;
        currentLocation = 0;
        setNextNewLine();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String next() {
        if (hasNext()) {

            String nextLine = builder.substring(currentLocation, extractToIndex);
            previousLocation = currentLocation;
            currentLocation = extractToIndex;
            setNextNewLine();
            return nextLine.equals(newline) ? "" : nextLine;
        } else {
            throw new IllegalArgumentException("No new line to be read");
        }
    }

    private void setNextNewLine() {

        if (builder.length() == 0 || currentLocation >= builder.length()) {
            hasNext = false;
            return;
        }

        int searchFrom = currentLocation;
//        if(currentLocation == 0){
//            searchFrom = 0;//increment location if not the first element - as will be set to the last new line otherwise
//        }

        extractToIndex = builder.indexOf(newline, searchFrom);
        if (extractToIndex == -1) {//no more new lines
            extractToIndex = builder.length();
        } else {
            extractToIndex++;
        }

        hasNext = true;
    }

    @Override
    public void remove() {
        int lengthDeleted = currentLocation - previousLocation;
        builder.delete(previousLocation, currentLocation);
        currentLocation = currentLocation - lengthDeleted;
        setNextNewLine();
    }

}
