/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.service;

import java.nio.ByteBuffer;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;

/** Retrieves sequences from the cram reference registry. */
public interface SequenceRetrievalService {

  boolean isSequenceAvailable(String objectId);

  boolean isSequenceAvailable(String md5, String sha1);

  ByteBuffer getSequence(String objectId, long sequenceLength);

  ByteBuffer getSequence(String md5, String sha1, long sequenceLength);

  ByteBuffer getSequence(RemoteRange remoteRange);

  ByteBuffer getSequence(RemoteBase remoteBase);
}
