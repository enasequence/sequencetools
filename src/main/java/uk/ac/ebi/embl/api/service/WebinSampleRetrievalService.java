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

import uk.ac.ebi.ena.webin.cli.service.SampleService;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class WebinSampleRetrievalService implements SampleRetrievalService {

  private final String webinRestUri;
  private final String biosamplesUri;
  private final String webinAuthToken;
  private final String biosamplesWebinAuthToken;

  public WebinSampleRetrievalService(
      String webinRestUri,
      String biosamplesUri,
      String webinAuthToken,
      String biosamplesWebinAuthToken) {

    this.webinRestUri = webinRestUri;
    this.biosamplesUri = biosamplesUri;
    this.webinAuthToken = webinAuthToken;
    this.biosamplesWebinAuthToken = biosamplesWebinAuthToken;
  }

  @Override
  public Sample getSample(String sampleId) {
    // Retrieve sampleObj, sampleXml and return sampleObj with attributes.
    SampleService sampleService = getSampleService();

    return sampleService.getSample(sampleId);
  }

  private SampleService getSampleService() {
    return new SampleService.Builder()
        .setWebinRestV1Uri(webinRestUri)
        .setAuthToken(webinAuthToken)
        .setBiosamplesUri(biosamplesUri)
        .setBiosamplesWebinAuthToken(webinAuthToken)
        .build();
  }
}
