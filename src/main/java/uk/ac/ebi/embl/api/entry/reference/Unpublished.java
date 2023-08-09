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
package uk.ac.ebi.embl.api.entry.reference;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Unpublished extends Publication implements Comparable<Unpublished> {

  private static final long serialVersionUID = 6789260715844060492L;

  private String journalBlock;

  protected Unpublished() {}

  protected Unpublished(String title) {
    this();
    setTitle(title);
  }

  protected Unpublished(Publication publication) {
    this();
    if (publication != null) {
      setTitle(publication.getTitle());
      setConsortium(publication.getConsortium());
      addAuthors(publication.getAuthors());
      addXRefs(publication.getXRefs());
    }
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.appendSuper(super.hashCode());
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Unpublished) {
      final Unpublished other = (Unpublished) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.appendSuper(super.equals(other));
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.appendSuper(super.toString());
    return builder.toString();
  }

  public int compareTo(Unpublished o) {
    final Unpublished other = o;
    final CompareToBuilder builder = new CompareToBuilder();
    builder.appendSuper(super.compareTo(other));
    return builder.toComparison();
  }

  public String getJournalBlock() {
    return journalBlock;
  }

  public void setJournalBlock(String journalBlock) {
    this.journalBlock = journalBlock;
  }
}
