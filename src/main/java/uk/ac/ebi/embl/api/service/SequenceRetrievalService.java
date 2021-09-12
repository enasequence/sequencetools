package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Retrieves sequences from the cram reference registry.
 */
public interface SequenceRetrievalService {

    boolean isSequenceAvailable(String objectId);

    boolean isSequenceAvailable(String md5, String sha1);

    ByteBuffer getSequence(String objectId, long sequenceLength) throws ValidationEngineException;

    ByteBuffer getSequence(String md5, String sha1, long sequenceLength) throws ValidationEngineException;

    ByteBuffer getSequence(RemoteRange remoteRange) throws ValidationEngineException;

    ByteBuffer getSequence(RemoteBase remoteBase) throws ValidationEngineException;

    ByteBuffer getSequence(List<Location> locations, int sequenceLength) throws ValidationEngineException;
}
