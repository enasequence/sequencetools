package uk.ac.ebi.embl.api.service;

public class SequenceRetrievalServiceHolder {

    private SequenceRetrievalServiceHolder() {
    }

    /**
     * Register sequence retrieval service by setting the static variable.
     */
    public static SequenceRetrievalService service;
}
