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
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Person implements Serializable, Comparable<Person> {

  private static final long serialVersionUID = -5269794059740307851L;

  private String id;
  private String firstName;
  private String surname;

  protected Person() {}

  protected Person(String surname) {
    this.surname = surname;
  }

  protected Person(String surname, String firstName) {
    this(surname);
    this.firstName = firstName;
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

  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstname) {
    this.firstName = firstname;
  }

  public String getSurname() {
    return this.surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.surname);
    builder.append(this.firstName);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Person) {
      final Person other = (Person) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.surname, other.surname);
      builder.append(this.firstName, other.firstName);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("id", id);
    builder.append("surname", surname);
    builder.append("firstName", firstName);
    return builder.toString();
  }

  public int compareTo(Person o) {
    final Person other = o;
    final CompareToBuilder builder = new CompareToBuilder();
    builder.append(this.surname, other.surname);
    builder.append(this.firstName, other.firstName);
    return builder.toComparison();
  }
}
