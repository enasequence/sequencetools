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
package uk.ac.ebi.embl.api.entry.qualifier;

public class QualifierFactory {

  public Qualifier createQualifier(String name, String value) {
    if (Qualifier.CODON_QUALIFIER_NAME.equals(name)) {
      return createCodonQualifier(value);
    } else if (Qualifier.CODON_START_QUALIFIER_NAME.equals(name)) {
      return createCodonStartQualifier(value);
    } else if (Qualifier.PROTEIN_ID_QUALIFIER_NAME.equals(name)) {
      return createProteinIdQualifier(value);
    } else if (Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME.equals(name)) {
      return createTranslExceptQualifier(value);
    } else if (Qualifier.TRANSL_TABLE_QUALIFIER_NAME.equals(name)) {
      return createTranslTableQualifier(value);
    } else if (Qualifier.ORGANISM_QUALIFIER_NAME.equals(name)) {
      return createOrganismQualifier(value);
    } else {
      return new Qualifier(name, value);
    }
  }

  public Qualifier createQualifier(String name) {
    return createQualifier(name, null);
  }

  public CodonQualifier createCodonQualifier(String value) {
    return new CodonQualifier(value);
  }

  public CodonStartQualifier createCodonStartQualifier(String value) {
    return new CodonStartQualifier(value);
  }

  public ProteinIdQualifier createProteinIdQualifier(String value) {
    return new ProteinIdQualifier(value);
  }

  public TranslExceptQualifier createTranslExceptQualifier(String value) {
    return new TranslExceptQualifier(value);
  }

  public TranslTableQualifier createTranslTableQualifier(String value) {
    return new TranslTableQualifier(value);
  }

  public OrganismQualifier createOrganismQualifier(String value) {
    return new OrganismQualifier(value);
  }
}
