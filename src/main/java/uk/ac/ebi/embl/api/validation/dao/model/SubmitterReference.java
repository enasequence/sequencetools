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
package uk.ac.ebi.embl.api.validation.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubmitterReference {

  private String submissionAccountId;
  private Date firstCreated;
  private SubmissionAccount submissionAccount;
  private List<SubmissionContact> submissionContacts = new ArrayList<>();

  public SubmitterReference(
      List<SubmissionContact> submissionContacts, SubmissionAccount submissionAccount) {
    this.submissionContacts = submissionContacts;
    this.submissionAccount = submissionAccount;
  }

  public SubmitterReference() {}

  public String getSubmissionAccountId() {
    return submissionAccountId;
  }

  public void setSubmissionAccountId(String submissionAccountId) {
    this.submissionAccountId = submissionAccountId;
  }

  public Date getFirstCreated() {
    return firstCreated;
  }

  public void setFirstCreated(Date firstCreated) {
    this.firstCreated = firstCreated;
  }

  public SubmissionAccount getSubmissionAccount() {
    return submissionAccount;
  }

  public void setSubmissionAccount(SubmissionAccount submissionAccount) {
    this.submissionAccount = submissionAccount;
  }

  public List<SubmissionContact> getSubmissionContacts() {
    return submissionContacts;
  }

  public void setSubmissionContacts(List<SubmissionContact> submissionContacts) {
    this.submissionContacts = submissionContacts;
  }
}
