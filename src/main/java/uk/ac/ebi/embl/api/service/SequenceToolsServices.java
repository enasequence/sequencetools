package uk.ac.ebi.embl.api.service;

import java.util.concurrent.atomic.AtomicReference;

public class SequenceToolsServices {

    private final static AtomicReference<MasterEntryService> masterEntryService = new AtomicReference<>(new MasterEntryService());
    private final static AtomicReference<SequenceRetrievalService> sequenceRetrievalService = new AtomicReference<>();
    private final static AtomicReference<SampleRetrievalService> sampleRetrievalService = new AtomicReference<>();

    public static MasterEntryService masterEntryService() {
        return masterEntryService.get();
    }

    public static SequenceRetrievalService sequenceRetrievalService() {
        return sequenceRetrievalService.get();
    }

    public static SampleRetrievalService sampleRetrievalService() {
        return sampleRetrievalService.get();
    }

    public static void init(SequenceRetrievalService sequenceRetrievalService) {
        SequenceToolsServices.sequenceRetrievalService.compareAndSet(null, sequenceRetrievalService);
    }

    public static void init(SampleRetrievalService sampleRetrievalService) {
        SequenceToolsServices.sampleRetrievalService.compareAndSet(null, sampleRetrievalService);
    }
}
