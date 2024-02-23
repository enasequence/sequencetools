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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public class Publication implements HasOrigin, Serializable {

  private static final long serialVersionUID = 4822437642365396531L;

  private Origin origin;
  private String id;
  private String title;
  private String consortium;
  private final List<Person> authors;
  private final List<XRef> xRefs;

  public Publication() {
    this.authors = new ArrayList<Person>();
    this.xRefs = new ArrayList<XRef>();
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public String getId() {
    return id;
  }

  public void setId(Object id) {
    if (id != null) {
      this.id = id.toString();
    } else {
      this.id = null;
    }
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getConsortium() {
    return consortium;
  }

  public void setConsortium(String consortium) {
    this.consortium = consortium;
  }

  public List<Person> getAuthors() {
    return Collections.unmodifiableList(this.authors);
  }

  public boolean addAuthor(Person author) {
    return this.authors.add(author);
  }

  public boolean addAuthors(Collection<Person> authors) {
    if (authors == null) {
      return false;
    }
    return this.authors.addAll(authors);
  }

  public boolean removeAuthor(Person author) {
    return this.authors.remove(author);
  }

  public List<XRef> getXRefs() {
    return Collections.unmodifiableList(this.xRefs);
  }

  public boolean addXRef(XRef xRef) {
    return this.xRefs.add(xRef);
  }

  public boolean addXRefs(Collection<XRef> xRefs) {
    if (xRefs == null) {
      return false;
    }
    return this.xRefs.addAll(xRefs);
  }

  public boolean removeXRef(XRef xRef) {
    return this.xRefs.remove(xRef);
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.title);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Publication) {
      final Publication other = (Publication) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.title, other.title);
      builder.append(this.consortium, other.consortium);
      builder.append(this.authors, other.authors);
      builder.append(this.xRefs, other.xRefs);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("id", id);
    builder.append("title", title);
    builder.append("consortium", consortium);
    builder.append("authors", authors);
    builder.append("xRefs", xRefs);
    return builder.toString();
  }

  public int compareTo(Publication obj) {
    Publication other = obj;
    final CompareToBuilder builder = new CompareToBuilder();
    builder.append(this.title, other.title);
    builder.append(this.consortium, other.consortium);
    Person[] thisAuthors = this.authors.toArray(new Person[this.authors.size()]);
    Person[] otherAuthors = other.authors.toArray(new Person[other.authors.size()]);
    builder.append(thisAuthors, otherAuthors);
    XRef[] thisXRefs = this.xRefs.toArray(new XRef[this.xRefs.size()]);
    XRef[] otherXRefs = other.xRefs.toArray(new XRef[other.xRefs.size()]);
    builder.append(thisXRefs, otherXRefs);
    return builder.toComparison();
  }
}
