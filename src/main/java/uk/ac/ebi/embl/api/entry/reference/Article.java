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
package uk.ac.ebi.embl.api.entry.reference;

import java.util.Date;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Article extends Publication implements Comparable<Article> {

  private static final long serialVersionUID = 1783711866778315255L;

  private String firstPage;
  private String lastPage;
  private String volume;
  private String issue;
  private String journal;
  private Date year;

  protected Article() {}

  protected Article(Publication publication) {
    this();
    if (publication != null) {
      setTitle(publication.getTitle());
      setConsortium(publication.getConsortium());
      addAuthors(publication.getAuthors());
      addXRefs(publication.getXRefs());
    }
  }

  protected Article(String title, String journal) {
    this();
    setTitle(title);
    this.journal = journal;
  }

  public String getFirstPage() {
    return firstPage;
  }

  public void setFirstPage(String firstPage) {
    this.firstPage = firstPage;
  }

  public String getLastPage() {
    return lastPage;
  }

  public void setLastPage(String lastPage) {
    this.lastPage = lastPage;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
  }

  public Date getYear() {
    return year;
  }

  public void setYear(Date year) {
    this.year = year;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.appendSuper(super.hashCode());
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Article) {
      final Article other = (Article) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.appendSuper(super.equals(other));
      builder.append(this.firstPage, other.firstPage);
      builder.append(this.lastPage, other.lastPage);
      builder.append(this.volume, other.volume);
      builder.append(this.issue, other.issue);
      builder.append(this.journal, other.journal);
      builder.append(this.year, other.year);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.appendSuper(super.toString());
    builder.append("firstPage", firstPage);
    builder.append("lastPage", lastPage);
    builder.append("volume", volume);
    builder.append("issue", issue);
    builder.append("journal", journal);
    builder.append("year", year);
    return builder.toString();
  }

  public int compareTo(Article o) {
    final Article other = o;
    final CompareToBuilder builder = new CompareToBuilder();
    builder.appendSuper(super.compareTo(other));
    builder.append(this.firstPage, other.firstPage);
    builder.append(this.lastPage, other.lastPage);
    builder.append(this.volume, other.volume);
    builder.append(this.issue, other.issue);
    builder.append(this.journal, other.journal);
    builder.append(this.year, other.year);
    return builder.toComparison();
  }
}
