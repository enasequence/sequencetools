package uk.ac.ebi.embl.api.validation;

public enum GlobalDataSetFile {
  FEATURE_QUALIFIER_VALUES("feature-qualifier-values.tsv"),
  FEATURE_REGEX_GROUPS("feature-regex-groups.tsv"),
  ARTEMIS_QUALIFIERS("artemis-qualifiers.tsv"),
  DATACLASS("dataclass.tsv"),
  KEYWORD_DATACLASS("keyword_dataclass.tsv"),
  MOLTYPE_FEATURE("moltype-feature.tsv"),
  FEATURE_SOURCE_QUALIFIER("feature-source-qualifier.tsv"),
  CON_NO_KEYWORDS("con-no-keywords.tsv"),
  ORG_PERMITTED_QUALIFIER("organism-permitted-qualifier.tsv"),
  QUALIFIER_REQUIRED_QUALIFIER_IN_ENTRY("qualifier-required-qualifier-in-entry.tsv"),
  QUALIFIER_PATTERN_FEATURE("qualifier-pattern-feature.tsv"),
  QUALIFIER_VALUE_NOT_QUALIFIER_ENTRY("qualifier-value-not-qualifier-entry.tsv"),
  QUALIFIER_VAL_REQUIRED_QUALIFIER_ENTRY("qualifier-value-required-qualifier-entry-value.tsv"),
  DEPRECATED_QUALIFIERS("deprecated-qualifiers.tsv"),
  EXCLUSIVE_QUALIFIERS("exclusive-qualifiers.tsv"),
  EXCLUSIVE_QUALIFIERS_SAME_VALUE("exclusive-qualifiers-same-value.tsv"),
  FEATURE_KEYS("feature-keys.tsv"),
  FEATURE_KEY_QUALIFIERS("feature-key-qualifiers.tsv"),
  FEATURE_REQUIRE_QUALIFIERS("feature-require-qualifiers.tsv"),
  NCRNA_QUALIFIER_VAL_QUALIFIER_PATTERN("ncrna-qualifier-value-qualifier-pattern.tsv"),
  NUCLEOTIDE_CODE("nucleotide-code.tsv"),
  QUALIFIER_REQUIRED_QUALIFIER_IN_FEATURE("qualifier-required-qualifier-in-feature.tsv"),
  QUALIFIER_PATTERN_QUALIFIER("qualifier-pattern-qualifier.tsv"),
  QUALIFIER_VALUE_NOT_QUALIFIER("qualifier-value-not-qualifier.tsv"),
  QUALIFIER_VALUE_NOT_QUALIFIER_PATTERN("qualifier-value-not-qualifier-pattern.tsv"),
  QUALIFIER_VALUE_REQ_QUALIFIER_STARTSWITH_VALUE(
      "qualifier-value-required-qualifier-startswith-value.tsv"),
  MOLTYPE_ORGANISM("moltype-organism.tsv"),
  MOLTYPE_SOURCE_QUALIFIERS("moltype-source-qualifier.tsv"),
  SOURCE_QUALIFIERS_MOLTYPE_VALUES("source_qualifier_moltype_value.tsv"),
  ORGANISM_REQUIRED_QUALIFIER("organism-required-qualifier.tsv"),
  SOURCE_QUALIFIER_PATTERN_FEATURE("source-qualifier-pattern-feature.tsv"),
  TAXONOMIC_DIVISION_NO_QUALIFIER("taxonomic_division-no-qualifier.tsv"),
  TAXONOMIC_DIVISION_QUALIFIER("taxonomic_division-qualifier.tsv"),
  EXCLUSIVE_QUALIFIERS_TO_REMOVE("exclusive-qualifiers-to-remove.tsv"),
  FEATURE_QUALIFIER_RENAME("feature-qualifier-rename.tsv"),
  // A list of feature keys with old and new values
  FEATURE_RENAME("feature-rename.tsv"),
  OBSOLETE_FEATURE_TO_FEATURE("obsoletefeature-to-feature.tsv"),
  QUALIFIER_VALUE_TO_FIX_VALUE("qualifier-value-to-fix-value.tsv");

  private final String fileName;

  GlobalDataSetFile(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
