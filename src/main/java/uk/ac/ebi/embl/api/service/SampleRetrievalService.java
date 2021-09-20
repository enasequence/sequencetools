package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

/**
 * Retrieves samples from Webin.
 */
public interface SampleRetrievalService {
    Sample getSample(String sampleId);
}
