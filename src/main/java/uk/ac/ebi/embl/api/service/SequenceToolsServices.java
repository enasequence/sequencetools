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

import java.util.concurrent.atomic.AtomicReference;

public class SequenceToolsServices {

  private static final AtomicReference<MasterEntryService> masterEntryService =
      new AtomicReference<>(new MasterEntryService());
  private static final AtomicReference<SequenceRetrievalService> sequenceRetrievalService =
      new AtomicReference<>();
  private static final AtomicReference<SampleRetrievalService> sampleRetrievalService =
      new AtomicReference<>();

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
