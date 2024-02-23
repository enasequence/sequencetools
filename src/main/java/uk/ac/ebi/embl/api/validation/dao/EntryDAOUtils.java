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
package uk.ac.ebi.embl.api.validation.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public interface EntryDAOUtils {
  boolean isValueExists(String tableName, String constraintKey, String constraintValue)
      throws SQLException;

  boolean isEntryExists(String accession) throws SQLException;

  Long getSequenceLength(String accession) throws SQLException;

  ArrayList<Qualifier> getChromosomeQualifiers(
      String analysisId, String submitterAccession, SourceFeature source) throws SQLException;

  boolean isProjectValid(String project) throws SQLException;

  HashSet<String> getProjectLocutagPrefix(String project) throws SQLException;

  String isEcnumberValid(String ecNumber) throws SQLException;

  Entry getEntryInfo(String primaryAcc) throws SQLException;

  String getDbcode(String prefix) throws SQLException;

  boolean isChromosomeValid(String analysisId, String chromosomeName) throws SQLException;

  String getNewProteinId() throws SQLException;
}
