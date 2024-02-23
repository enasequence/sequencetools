/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.submission;

public class ServiceConfig {

  private String eraServiceUrl;
  private String eraServiceUser;
  private String eraServicePassword;

  public String getEraServiceUrl() {
    return eraServiceUrl;
  }

  public void setEraServiceUrl(String eraServiceUrl) {
    this.eraServiceUrl = eraServiceUrl;
  }

  public String getEraServiceUser() {
    return eraServiceUser;
  }

  public void setEraServiceUser(String eraServiceUser) {
    this.eraServiceUser = eraServiceUser;
  }

  public String getEraServicePassword() {
    return eraServicePassword;
  }

  public void setEraServicePassword(String eraServicePassword) {
    this.eraServicePassword = eraServicePassword;
  }
}
