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
package uk.ac.ebi.embl.api.validation.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.dbutils.DbUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class EntryDAOUtilsImpl implements EntryDAOUtils {
  private final Connection connection;
  private static EntryDAOUtilsImpl entryDAOUtils;

  public static EntryDAOUtilsImpl getEntryDAOUtilsImpl(Connection connection) {
    if (null == entryDAOUtils) {
      entryDAOUtils = new EntryDAOUtilsImpl(connection);
    }
    return entryDAOUtils;
  }

  private EntryDAOUtilsImpl(Connection connection) {
    this.connection = connection;
  }

  @Override
  public ArrayList<Qualifier> getChromosomeQualifiers(
      String analysisId, String submitterAccession, SourceFeature source) throws SQLException {
    String sql =
        "select chromosome_name, chromosome_location, chromosome_type "
            + "from gcs_chromosome where assembly_id = ? and upper(object_name) = upper(?)";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean virus = false;
    if (source != null) {
      TaxonomyClient taxonomyClient = new TaxonomyClient();
      String scientificName = source.getScientificName();
      virus = taxonomyClient.isChildOf(scientificName, "Viruses");
    }
    ArrayList<Qualifier> qualifiers = new ArrayList<Qualifier>();

    QualifierFactory qualifierFactory = new QualifierFactory();

    try {
      stmt = connection.prepareStatement(sql);
      stmt.setString(1, analysisId);
      stmt.setString(2, submitterAccession); // entry name
      rs = stmt.executeQuery();

      if (!rs.next()) {
        return qualifiers;
      }
      String chromosomeType = rs.getString(3);
      String chromosomeLocation = rs.getString(2);
      String chromosomeName = rs.getString(1);

      if (chromosomeLocation != null
          && !chromosomeLocation.isEmpty()
          && !virus
          && !chromosomeLocation.equalsIgnoreCase("Phage")) {
        String organelleValue = SequenceEntryUtils.getOrganelleValue(chromosomeLocation);
        if (organelleValue != null) {
          qualifiers.add(
              qualifierFactory.createQualifier(
                  Qualifier.ORGANELLE_QUALIFIER_NAME,
                  SequenceEntryUtils.getOrganelleValue(chromosomeLocation)));
        }
      } else if (chromosomeName != null && !chromosomeName.isEmpty()) {
        if (Qualifier.PLASMID_QUALIFIER_NAME.equals(chromosomeType)) {
          qualifiers.add(
              qualifierFactory.createQualifier(Qualifier.PLASMID_QUALIFIER_NAME, chromosomeName));
        } else if (Qualifier.CHROMOSOME_QUALIFIER_NAME.equals(chromosomeType)) {
          qualifiers.add(
              qualifierFactory.createQualifier(
                  Qualifier.CHROMOSOME_QUALIFIER_NAME, chromosomeName));
        } else if ("segmented".equals(chromosomeType) || "multipartite".equals(chromosomeType)) {
          qualifiers.add(
              qualifierFactory.createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, chromosomeName));

        } else if ("monopartite".equals(chromosomeType)) {
          qualifiers.add(
              qualifierFactory.createQualifier(Qualifier.NOTE_QUALIFIER_NAME, chromosomeType));
        }
      }

    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(stmt);
    }
    return qualifiers;
  }

  @Override
  public boolean isValueExists(String tableName, String constraintKey, String constraintValue)
      throws SQLException {
    String sqlSearchStringTemp = "select 1 from %s where %s ='%s'";
    String sql = String.format(sqlSearchStringTemp, tableName, constraintKey, constraintValue);
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement(sql);
      if (ps.executeQuery(sql).next()) return true;
      return false;

    } finally {
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public boolean isEntryExists(String accession) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement("select 1 from bioseq where sequence_acc=? or seq_accid=?");
      ps.setString(1, accession);
      ps.setString(2, accession);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }

      return false;
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public Long getSequenceLength(String accession) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps =
          connection.prepareStatement(
              "select seqlen from bioseq where sequence_acc=? or seq_accid=?");
      ps.setString(1, accession);
      ps.setString(2, accession);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getLong(1);
      }

      return 0L;
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public boolean isProjectValid(String project) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps =
          connection.prepareStatement(
              "select 1 from mv_project where project_id=? or ncbi_project_id=?");
      ps.setString(1, project);
      ps.setString(2, project);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
      return false;
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public HashSet<String> getProjectLocutagPrefix(String project) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    HashSet<String> locusTagPrefixes = new HashSet<String>();
    try {
      ps =
          connection.prepareStatement(
              "select upper(locus_tag) from mv_project where project_id=? or ncbi_project_id=?");
      ps.setString(1, project);
      ps.setString(2, project);
      rs = ps.executeQuery();
      while (rs.next()) {
        locusTagPrefixes.add(rs.getString(1));
      }
      return locusTagPrefixes;
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public String isEcnumberValid(String ecNumber) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement("select valid from cv_ec_numbers where ec_number=?");
      ps.setString(1, ecNumber);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString(1);
      } else return null;
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }
  }

  @Override
  public Entry getEntryInfo(String primaryAcc) throws SQLException {
    Entry entry = (new EntryFactory()).createEntry();
    ResultSet rs = null;
    PreparedStatement ps = null;
    boolean isValid = false;
    try {
      ps =
          connection.prepareStatement(
              "select entry_name,dataclass,keyword from dbentry "
                  + "left outer join keywords on(dbentry.dbentryid=keywords.dbentryid ) where dbentry.primaryacc#=?");
      ps.setString(1, primaryAcc);
      rs = ps.executeQuery();
      while (rs.next()) {
        isValid = true;
        entry.setSubmitterAccession(rs.getString("entry_name"));
        entry.setDataClass(rs.getString("dataclass"));
        entry.addKeyword(new Text(rs.getString("keyword")));
      }
      if (!isValid) {
        return null;
      }
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }

    return entry;
  }

  @Override
  public String getDbcode(String prefix) throws SQLException {
    if (prefix == null) return null;
    String sql = "select dbcode from cv_database_prefix where prefix= ?";
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement(sql);
      ps.setString(1, prefix);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString(1);
      }
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(ps);
    }

    return null;
  }

  @Override
  public boolean isChromosomeValid(String analysisId, String chromosomeName) throws SQLException {
    String sql = "select 1 from gcs_chromosome where assembly_id = ? and chromosome_name = ?";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, analysisId);
      ps.setString(2, chromosomeName);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        return false;
      }
      return true;
    }
  }

  @Override
  public String getNewProteinId() throws SQLException {
    try (PreparedStatement pstsmt =
            connection.prepareStatement("select prefix_pkg.get_new_protein_id from dual");
        ResultSet rs = pstsmt.executeQuery()) {
      if (rs.next()) {
        return rs.getString(1);
      } else {
        return null;
      }
    }
  }
}
