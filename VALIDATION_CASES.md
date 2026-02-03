# Sequencetools Validation Check Cases

This document provides comprehensive examples of valid and invalid cases for all validation checks in the sequencetools library. Each check is documented with its error message IDs and concrete examples.

---

## Table of Contents

- [Entry-Level Checks](#entry-level-checks)
- [Feature-Level Checks](#feature-level-checks)
- [Sequence-Level Checks](#sequence-level-checks)
- [Source Feature Checks](#source-feature-checks)
- [Genome Assembly Checks](#genome-assembly-checks)
- [File-Level Checks](#file-level-checks)
- [Error Message ID Reference](#error-message-id-reference)

---

## Entry-Level Checks

### DataclassCheck

Validates that the entry dataclass is valid and consistent with other entry properties.

**Error IDs:** `DataclassCheck1`, `DataclassCheck2`, `DataclassCheck3`, `DataclassCheck4`

| Case | Dataclass | Condition | Result | Error |
|------|-----------|-----------|--------|-------|
| Valid standard | STD | Standard entry | PASS | - |
| Valid WGS | WGS | WGS entry with valid accession | PASS | - |
| Valid SET | SET | SET with contig accessions | PASS | - |
| Invalid dataclass | "qsd" | Unknown dataclass value | FAIL | `DataclassCheck1` |
| SET without contigs | SET | Missing contig accessions | FAIL | `DataclassCheck2` |
| WGS with CON keyword | WGS | Has CON keyword | FAIL | `DataclassCheck3` |
| WGS format mismatch | WGS | Non-WGS accession format | FAIL | `DataclassCheck4` |

---

### MoleculeTypeAndDataclassCheck

Cross-validates molecule type with dataclass.

**Error IDs:** `MoleculeTypeAndDataclassCheck-1` (ERROR), `MoleculeTypeAndDataclassCheck-2` (WARNING)

| Case | Dataclass | Molecule Type | Result | Error |
|------|-----------|---------------|--------|-------|
| Valid EST | EST | mRNA | PASS | - |
| Valid EST | EST | rRNA | PASS | - |
| Valid STD | STD | genomic DNA | PASS | - |
| Invalid EST | EST | genomic DNA | FAIL | `MoleculeTypeAndDataclassCheck-1` |
| Warning GSS | GSS | other DNA | WARN | `MoleculeTypeAndDataclassCheck-2` |

---

### KWCheck (Keywords)

Validates keywords are appropriate for the dataclass.

**Error IDs:** `KWCheck_1` through `KWCheck_6`

| Case | Dataclass | Keywords | Result | Error |
|------|-----------|----------|--------|-------|
| Valid WGS | WGS | WGS | PASS | - |
| Valid CON | CON | (none) | PASS | - |
| Valid TSA | TSA | TSA | PASS | - |
| STD with WGS keyword | STD | WGS | FAIL | `KWCheck_1` |
| Multiple mismatched | WGS | EST, WGS | FAIL | `KWCheck_2` |
| CON with HTG | CON | HTG | FAIL | `KWCheck_3` |
| WGS without keywords | WGS | (none) | FAIL | `KWCheck_4` |
| WGS with invalid | WGS | TPX | FAIL | `KWCheck_4` |
| TPA in description | STD | (no TPA) | FAIL | `KWCheck_6` |

---

### ReferenceCheck

Validates that entries have required references.

**Error ID:** `ReferenceCheck_1`

| Case | References | Result | Error |
|------|------------|--------|-------|
| Valid | Submission + Unpublished | PASS | - |
| Valid | Submission only | PASS | - |
| Missing | No references | FAIL | `ReferenceCheck_1` |
| Invalid | Unpublished only (no submission) | FAIL | `ReferenceCheck_1` |

---

### PrimaryAccessionCheck

Validates primary accession format and consistency.

**Error IDs:** `PrimaryAccessionCheck1`, `PrimaryAccessionCheck2`

| Case | Accession | Dataclass | Result | Error |
|------|-----------|-----------|--------|-------|
| Valid WGS | ABCD01000001 | WGS | PASS | - |
| Valid STD | AB123456 | STD | PASS | - |
| Invalid prefix | XXXX01000001 | WGS | FAIL | `PrimaryAccessionCheck2` |
| WGS acc with CON | ABCD01000001 | CON | FAIL | `PrimaryAccessionCheck1` |

---

### SubmitterAccessionCheck

Validates submitter accession presence and length.

**Error IDs:** `SubmitterAccessionCheck_1`, `SubmitterAccessionCheck_2`

| Case | Submitter Accession | Context | Result | Error |
|------|---------------------|---------|--------|-------|
| Valid | "contig_001" (10 chars) | Assembly | PASS | - |
| Valid | 50 character name | Assembly | PASS | - |
| Missing | (none) | Assembly scope | FAIL | `SubmitterAccessionCheck_1` |
| Too long | 51+ characters | Webin-CLI | FAIL | `SubmitterAccessionCheck_2` |

---

### LocustagExistsCheck

Validates locus_tag presence in WGS entries.

**Error ID:** `LocustagExistsCheck_1`

| Case | Dataclass | Features | Has locus_tag | Result | Error |
|------|-----------|----------|---------------|--------|-------|
| Valid WGS | WGS | CDS, gene | Yes | PASS | - |
| Valid WGS | WGS | source, assembly_gap only | No | PASS | - |
| Valid CON | CON | CDS | No | PASS | - |
| Valid virus | WGS | CDS (Siadenovirus) | No | PASS | - |
| Invalid WGS | WGS | CDS, gene | No | FAIL | `LocustagExistsCheck_1` |

---

### HoldDateCheck

Validates hold date is in the future.

| Case | Hold Date | Result | Error |
|------|-----------|--------|-------|
| Valid | 2030-01-01 | PASS | - |
| Invalid | 1970-01-01 | FAIL | HoldDateCheck |

---

### DuplicateFeatureCheck

Detects duplicate features in entries.

**Error IDs:** `DuplicateFeatureCheck.DUPLICATE_FEATURE_LOCATIONS`, `DuplicateFeatureCheck.DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID`

| Case | Features | Result | Error |
|------|----------|--------|-------|
| Valid | CDS at 1..100, CDS at 200..300 | PASS | - |
| Valid | CDS at 1..100, gene at 1..100 | PASS | - |
| Valid | CDS with different codon_start | PASS | - |
| Duplicate | CDS at 1..100, CDS at 1..100 | FAIL | `DUPLICATE_FEATURE_LOCATIONS` |
| Duplicate source | Two sources same organism/location | FAIL | `DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID` |

---

### MoltypeExistsCheck

Validates molecule type is present.

**Error ID:** `MoltypeExistsCheck`

| Case | Source Feature | Sequence mol_type | Result | Error |
|------|----------------|-------------------|--------|-------|
| Valid | Has mol_type qualifier | - | PASS | - |
| Valid | - | Has moleculeType | PASS | - |
| Invalid | No mol_type | No moleculeType | FAIL | `MoltypeExistsCheck` |

---

### Assembly_gapFeatureCheck

Validates assembly_gap features and their qualifiers.

**Error IDs:** Multiple (see table)

| Case | Feature | Qualifiers | Result | Error |
|------|---------|------------|--------|-------|
| Valid | assembly_gap | gap_type="within scaffold", linkage_evidence="paired-ends" | PASS | - |
| Valid | assembly_gap | gap_type="between scaffolds", estimated_length | PASS | - |
| Invalid qualifier | assembly_gap | note (not allowed) | FAIL | `INVALID_QUALIFIER_MESSAGE` |
| Invalid gap_type | assembly_gap | gap_type="spiA" | FAIL | QualifierCheck-4 |
| Wrong feature | gene | gap_type qualifier | FAIL | `NO_ASSEMBLY_GAP_MESSAGE` |
| Gap present | gap + assembly_gap | - | FAIL | `GAP_FEATURE_MESSAGE` |
| Linkage not allowed | assembly_gap | gap_type="between scaffolds" + linkage_evidence | FAIL | `LINKAGE_EVIDENCE_NOTALLOWED_MESSAGE` |
| Linkage missing | assembly_gap | gap_type="within scaffold" (no linkage) | FAIL | `LINKAGE_EVIDENCE_MISSING_MESSAGE` |
| Complex location | assembly_gap | Non-simple location | FAIL | `INVALID_LOCATION_MESSAGE` |

**TSA-specific:**

| Case | Dataclass | Condition | Result | Error |
|------|-----------|-----------|--------|-------|
| Invalid TSA | TSA | Invalid estimated_length | FAIL | `ESTIMATED_LENGTH__MESSAGE_TSA` |
| Invalid TSA | TSA | Invalid gap_type | FAIL | `GAP_TYPE_MESSAGE_TSA` |
| Invalid TSA | TSA | Invalid linkage_evidence | FAIL | `LINKAGE_EVIDENCE_INVALID_MESSAGE_TSA` |

---

### WGSGapCheck

Validates that WGS entries don't contain gap features.

**Error ID:** `WGSGapCheck` (WARNING)

| Case | Dataclass | Gap Features | Result | Error |
|------|-----------|--------------|--------|-------|
| Valid WGS | WGS | None | PASS | - |
| Valid STD | STD | Has gap | PASS | - |
| Invalid WGS | WGS | Has gap | WARN | `WGSGapCheck` |
| Invalid WGS | WGS | Multiple gaps | WARN | `WGSGapCheck` (aggregated) |

---

### GapFeatureBasesCheck

Validates gap features span only 'n' bases.

**Error ID:** `GapFeatureBasesCheck.MESSAGE_ID`

| Case | Gap Location | Sequence at Location | Result | Error |
|------|--------------|---------------------|--------|-------|
| Valid | 10..20 | "nnnnnnnnnnn" | PASS | - |
| Invalid | 10..20 | "nnnnAnnnnnn" | FAIL | `GapFeatureBasesCheck.MESSAGE_ID` |

---

### GapFeatureLocationsCheck

Validates gap features don't overlap.

**Error ID:** `GapFeatureLocationsCheck.MESSAGE_ID`

| Case | Gap Locations | Result | Error |
|------|---------------|--------|-------|
| Valid | 10..20, 30..40 | PASS | - |
| Invalid | 10..20, 15..25 (overlap) | FAIL | `GapFeatureLocationsCheck.MESSAGE_ID` |

---

### AGPValidationCheck

Validates AGP file rows.

**Error IDs:** `AGPValidationCheck-3` through `AGPValidationCheck-13`

| Case | AGP Row | Issue | Result | Error |
|------|---------|-------|--------|-------|
| Valid component | All fields valid | - | PASS | - |
| Valid gap | gap_type, linkage valid | - | PASS | - |
| Empty row | - | Missing data | FAIL | `AGPValidationCheck-13` |
| Invalid component_type | Invalid type_id | Wrong type | FAIL | `AGPValidationCheck-3` |
| Invalid gap_type | Invalid gap type | Bad value | FAIL | `AGPValidationCheck-4` |
| Invalid orientation | Wrong orientation | Bad value | FAIL | `AGPValidationCheck-5` |
| Invalid object range | object_beg > object_end | Reversed | FAIL | `AGPValidationCheck-7` |
| Invalid gap length | Gap length mismatch | Calculation error | FAIL | `AGPValidationCheck-8` |
| Invalid linkage | Bad linkage_evidence | Bad value | FAIL | `AGPValidationCheck-8` |

---

### AssemblyTopologyCheck

Validates assembly topology (contigs/scaffolds must be linear).

**Error ID:** `assemblyTopologyCheck_1`

| Case | Entry Type | Topology | Result | Error |
|------|------------|----------|--------|-------|
| Valid contig | Contig | LINEAR | PASS | - |
| Valid scaffold | Scaffold | LINEAR | PASS | - |
| Invalid contig | Contig | CIRCULAR | FAIL | `assemblyTopologyCheck_1` |
| Invalid scaffold | Scaffold | CIRCULAR | FAIL | `assemblyTopologyCheck_1` |

---

## Feature-Level Checks

### QualifierCheck

Validates qualifier keys, values, and patterns.

**Error IDs:** `QualifierCheck-1` through `QualifierCheck-9`

| Case | Qualifier | Value | Result | Error |
|------|-----------|-------|--------|-------|
| Valid collection_date | collection_date | "21-Oct-1952" | PASS | - |
| Valid collection_date | collection_date | "Oct-1952" | PASS | - |
| Valid collection_date | collection_date | "1952" | PASS | - |
| Valid lat_lon | lat_lon | "6.13 N 6.13 E" | PASS | - |
| Valid protein_id | protein_id | "CBI84061.1" | PASS | - |
| Valid EC_number | EC_number | "3.6.1.-" | PASS | - |
| Invalid qualifier key | "david" | - | FAIL | `QualifierCheck-1` |
| Artemis qualifier | "color" | - | FAIL | `QualifierCheck-1` |
| Missing mandatory value | collection_date | (empty) | FAIL | `QualifierCheck-2` |
| Regex mismatch | collection_date | "david" | FAIL | `QualifierCheck-3` |
| Invalid month | collection_date | "21-Bod-1952" | FAIL | `QualifierCheck-4` |
| Case mismatch | collection_date | "21-OCT-1952" | FAIL | `QualifierCheck-4` |
| Invalid latitude | lat_lon | "453534.54656 N 6.13 E" | FAIL | `QualifierCheck-7` |
| Invalid longitude | lat_lon | "6.13 N 453534.54656 E" | FAIL | `QualifierCheck-8` |
| protein_id version 0 | protein_id | "CBI84061.0" | FAIL | `QualifierCheck-9` |

---

### FeatureKeyCheck

Validates feature keys and required qualifiers.

**Error IDs:** `FeatureKeyCheck-1` through `FeatureKeyCheck-7`

| Case | Feature | Qualifiers | Result | Error |
|------|---------|------------|--------|-------|
| Valid CDS | CDS | product, translation | PASS | - |
| Valid operon | operon | operon="lac" | PASS | - |
| Invalid key | "meaty_pipe" | - | FAIL | `FeatureKeyCheck-1` |
| Missing mandatory | operon | (no operon qualifier) | FAIL | `FeatureKeyCheck-2` |
| Unpermitted qualifier | source | translation | FAIL | `FeatureKeyCheck-7` |

---

### FeatureLocationCheck

Validates feature locations.

**Error IDs:** `FeatureLocationCheck-1`, `FeatureLocationCheck-3`, `FeatureLocationCheck-5`

| Case | Location | Scope | Result | Error |
|------|----------|-------|--------|-------|
| Valid | 100..200 | EMBL | PASS | - |
| Valid remote | M12561:100..200 | NCBI | PASS | - |
| Missing | (none) | - | FAIL | `FeatureLocationCheck-1` |
| Invalid order | 120..108 (start > end) | - | FAIL | `FeatureLocationCheck-3` |
| Remote in EMBL | M12561:100..200 | EMBL | FAIL | `FeatureLocationCheck-5` |

---

### FeatureLengthCheck

Validates minimum feature lengths.

**Error ID:** `FeatureLengthCheck-1`

| Case | Feature | Length | Min Required | Result | Severity |
|------|---------|--------|--------------|--------|----------|
| Valid intron | intron | 18 nt | 10 nt | PASS | - |
| Valid exon | exon | 20 nt | 15 nt | PASS | - |
| Invalid intron | intron | 9 nt | 10 nt | FAIL | ERROR |
| Invalid exon | exon | 14 nt | 15 nt | FAIL | WARNING |

---

### FeatureQualifiersRequiredCheck

Validates that features have at least one required qualifier.

**Error ID:** `FeatureQualifiersRequiredCheck`

| Case | Feature | Qualifiers | Result | Error |
|------|---------|------------|--------|-------|
| Valid misc_feature | misc_feature | note="..." | PASS | - |
| Invalid misc_feature | misc_feature | (none) | FAIL | `FeatureQualifiersRequiredCheck` |

---

### EC_numberFormatCheck

Validates EC number format.

**Error ID:** `EC_numberFormatCheck`

| Case | EC_number Value | Result | Error |
|------|-----------------|--------|-------|
| Valid | "3.6.1.-" | PASS | - |
| Valid | "3.6.1.n" | PASS | - |
| Valid | "3.6.1.5" | PASS | - |
| Valid | "3.-.-.-" | PASS | - |
| Valid | "3.6.1.n234" | PASS | - |
| Invalid | "3.6.1.i" | FAIL | `EC_numberFormatCheck` |
| Invalid | "-.6.9.9" | FAIL | `EC_numberFormatCheck` |

---

### EC_numberCheck

Validates EC number presence with product.

**Error IDs:** `EC_numberCheck_1` (WARNING), `EC_numberCheck_2` (ERROR)

| Case | Feature | EC_number | Product | Result | Error |
|------|---------|-----------|---------|--------|-------|
| Valid | CDS | "3.6.1.5" | "ATP synthase" | PASS | - |
| Warning | CDS | (none) | "ATP synthase" | WARN | `EC_numberCheck_1` |
| Invalid | CDS | (none) | (none) | FAIL | `EC_numberCheck_2` |

---

### DeprecatedQualifiersCheck

Validates qualifiers are not deprecated.

**Error IDs:** `DeprecatedQualifiersCheck`, `DeprecatedQualifiersCheck-2`, `DeprecatedQualifiersCheck-3`

| Case | Qualifier | Status | Result | Error |
|------|-----------|--------|--------|-------|
| Valid | gene | Current | PASS | - |
| Deprecated | partial | Deprecated | FAIL | `DeprecatedQualifiersCheck` |
| Has replacement | "david" | Has replacement | FAIL | `DeprecatedQualifiersCheck-2` |
| Has fix | specific_host | Has fix | WARN | `DeprecatedQualifiersCheck-3` |

---

### ExclusiveQualifiersCheck

Validates mutually exclusive qualifiers.

**Error IDs:** `ExclusiveQualifiersCheck1` (ERROR), `ExclusiveQualifiersCheck2` (WARNING)

| Case | Qualifier 1 | Qualifier 2 | Result | Error |
|------|-------------|-------------|--------|-------|
| Valid | proviral | (none) | PASS | - |
| Valid | (none) | virion | PASS | - |
| Invalid | proviral | virion | FAIL | `ExclusiveQualifiersCheck1` |
| Warning | pseudo | product | WARN | `ExclusiveQualifiersCheck2` |

---

### ExclusiveQualifiersWithSameValuesCheck

Validates qualifiers don't have same values.

**Error ID:** `ExclusiveQualifiersWithSameValueCheck`

| Case | Qualifier 1 | Qualifier 2 | Values | Result | Error |
|------|-------------|-------------|--------|--------|-------|
| Valid | clone="A" | sub_clone="B" | Different | PASS | - |
| Invalid | clone="A" | sub_clone="A" | Same | FAIL | `ExclusiveQualifiersWithSameValueCheck` |

---

### CdsFeatureTranslationCheck

Validates CDS translation.

**Error ID:** `Translator-19` and others

| Case | CDS Location | Sequence | Expected Translation | Result | Error |
|------|--------------|----------|---------------------|--------|-------|
| Valid | 1..12 | "atggagtggtaa" | "MEW" | PASS | - |
| Invalid | 12..1 (reversed) | - | - | FAIL | `Translator-19` |

---

### TranslExceptQualifierCheck

Validates transl_except qualifier format.

**Error IDs:** `TranslExceptQualifierCheck_1` through `TranslExceptQualifierCheck_6`

| Case | transl_except Value | Result | Error |
|------|---------------------|--------|-------|
| Valid | "(pos:100..102,aa:Sec)" | PASS | - |
| Invalid format | "invalid" | FAIL | Various |

---

### AntiCodonQualifierCheck

Validates anticodon qualifier format.

**Error IDs:** `AntiCodonQualifierCheck_1` through `AntiCodonQualifierCheck_6`

| Case | anticodon Value | Result | Error |
|------|-----------------|--------|-------|
| Valid | "(pos:100..102,aa:Met,seq:cat)" | PASS | - |
| Invalid format | "invalid" | FAIL | Various |

---

### PCRPrimersQualifierCheck

Validates PCR_primers qualifier format.

**Error IDs:** `PCRPrimersQualifierCheck_1` through `PCRPrimersQualifierCheck_4`

| Case | PCR_primers Value | Result | Error |
|------|-------------------|--------|-------|
| Valid | "fwd_name: F1, fwd_seq: acgt" | PASS | - |
| Invalid format | "invalid" | FAIL | Various |

---

## Sequence-Level Checks

### SequenceBasesCheck

Validates sequence contains only valid bases.

**Error IDs:** `SequenceBasesCheck`, `SequenceBasesCheck-2`

| Case | Sequence | Topology | Result | Error |
|------|----------|----------|--------|-------|
| Valid | "acgtacgt" | LINEAR | PASS | - |
| Valid | "acgtnacgt" | LINEAR | PASS | - |
| Valid | "nacgtacgtn" | CIRCULAR | PASS | - |
| Invalid char | "acgtxcgt" | LINEAR | FAIL | `SequenceBasesCheck` |
| Terminal N | "acgtacgtn" | LINEAR | FAIL | `SequenceBasesCheck-2` |
| Leading N | "nacgtacgt" | LINEAR | FAIL | `SequenceBasesCheck-2` |

---

### SequenceLengthCheck

Validates sequence length constraints.

**Error IDs:** `SequenceLengthCheck`, `SequenceLengthCheck3`, `SequenceLengthCheck4`, `SequenceLengthCheck5`

| Case | Dataclass | Length | Feature | Result | Error |
|------|-----------|--------|---------|--------|-------|
| Valid STD | STD | 500 bp | - | PASS | - |
| Valid EST | EST | 150 bp | - | PASS | - |
| Valid EST exception | EST | 50 bp | ncRNA | PASS | - |
| Valid EST exception | EST | 50 bp | satellite | PASS | - |
| Valid GSS | GSS | 500 bp | - | PASS | - |
| Valid TSA | TSA | 100 bp | - | PASS | - |
| Valid lncRNA | - | 250 bp | lncRNA | PASS | - |
| Invalid EST | EST | 22 bp | - | FAIL | `SequenceLengthCheck` |
| Invalid GSS short | GSS | 5 bp | - | FAIL | `SequenceLengthCheck3` |
| Invalid GSS long | GSS | 1000 bp | - | FAIL | `SequenceLengthCheck3` |
| Invalid TSA | TSA | 30 bp | - | FAIL | `SequenceLengthCheck4` |
| Warning lncRNA | - | 150 bp | lncRNA | WARN | `SequenceLengthCheck5` |

---

## Source Feature Checks

### MoleculeTypeAndOrganismCheck

Validates molecule type is compatible with organism.

**Error ID:** `MoleculeTypeAndOrganismCheck`

| Case | Molecule Type | Organism | Result | Error |
|------|---------------|----------|--------|-------|
| Valid | genomic RNA | Deltavirus | PASS | - |
| Valid | genomic RNA | Retroviridae | PASS | - |
| Invalid | genomic RNA | Homo sapiens | FAIL | `MoleculeTypeAndOrganismCheck` |

**Message:** "Organism must belong to one of 'Deltavirus, Retro-transcribing viruses, ssRNA viruses, dsRNA viruses' when molecule type is 'genomic RNA'"

---

### SequenceCoverageCheck

Validates source features cover entire sequence.

**Error IDs:** `SequenceCoverageCheck-1` through `SequenceCoverageCheck-9`

| Case | Source Locations | Sequence Length | Result | Error |
|------|------------------|-----------------|--------|-------|
| Valid single | 1..180 | 180 | PASS | - |
| Valid multiple | 1..90, 91..180 | 180 | PASS | - |
| Missing start | 10..180 | 180 | FAIL | `SequenceCoverageCheck-1` |
| Missing end | 1..170 | 180 | FAIL | `SequenceCoverageCheck-3` |
| Gap in coverage | 1..80, 100..180 | 180 | FAIL | `SequenceCoverageCheck-5` |
| Transgenic only | 1..180 (transgenic) | 180 | FAIL | `SequenceCoverageCheck-4` |
| Overlapping | 1..100, 90..180 | 180 | FAIL | `SequenceCoverageCheck-9` |
| Transgenic + focus | Same source | - | FAIL | `SequenceCoverageCheck-8` |

---

## Genome Assembly Checks

### AssemblyInfoNameCheck

Validates assembly name format and length.

**Error IDs:** `AssemblyInfoMissingNameCheck`, `AssemblyInfoInvalidAssemblyName`, `AssemblyInfoInvalidAssemblyNameLength`

| Case | Assembly Name | Result | Error |
|------|---------------|--------|-------|
| Valid | "MyAssembly_v1" | PASS | - |
| Valid | "fdgfghhjhgj" | PASS | - |
| Missing | (none) | FAIL | `AssemblyInfoMissingNameCheck` |
| Invalid chars | "dfdfg878*dhfgh" | FAIL | `AssemblyInfoInvalidAssemblyName` |
| Too long | 100+ characters | FAIL | `AssemblyInfoInvalidAssemblyNameLength` |

---

### ChromosomeListFileValidationCheck

Validates chromosome list file format.

**Error ID:** `InvalidNoOfFields`

| Case | File Content | Result | Error |
|------|--------------|--------|-------|
| Valid | Correct number of fields per line | PASS | - |
| Invalid | Wrong number of fields | FAIL | `InvalidNoOfFields` |

---

## File-Level Checks

### FastaFileValidationCheck

Validates FASTA file format and content.

**Error ID:** `SQ.1`

| Case | FASTA Content | Result | Error |
|------|---------------|--------|-------|
| Valid genome | Valid sequences | PASS | - |
| Valid transcriptome | Valid sequences | PASS | - |
| Invalid sequence | Invalid characters | FAIL | `SQ.1` |
| Missing unlocalised | Expected sequences missing | FAIL | ValidationEngineException |

---

### FlatfileFileValidationCheck

Validates EMBL/GenBank flatfile format.

| Case | File Content | Result | Error |
|------|--------------|--------|-------|
| Valid EMBL | Correct format | PASS | - |
| Valid GenBank | Correct format | PASS | - |
| Invalid format | Malformed file | FAIL | Various |

---

### AGPFileValidationCheck

Validates AGP file with associated sequence files.

| Case | Files | Result | Error |
|------|-------|--------|-------|
| Valid | Flatfile + AGP | PASS | - |
| Valid | FASTA + AGP | PASS | - |

---

## Error Message ID Reference

### Entry-Level Error IDs

| Error ID | Check | Description |
|----------|-------|-------------|
| `DataclassCheck1` | DataclassCheck | Invalid dataclass value |
| `DataclassCheck2` | DataclassCheck | SET without contig accessions |
| `DataclassCheck3` | DataclassCheck | WGS with CON keyword |
| `DataclassCheck4` | DataclassCheck | WGS format mismatch |
| `KWCheck_1` - `KWCheck_6` | KWCheck | Keyword/dataclass mismatch |
| `ReferenceCheck_1` | ReferenceCheck | Missing required reference |
| `PrimaryAccessionCheck1` | PrimaryAccessionCheck | Accession/dataclass mismatch |
| `PrimaryAccessionCheck2` | PrimaryAccessionCheck | Invalid accession prefix |
| `SubmitterAccessionCheck_1` | SubmitterAccessionCheck | Missing submitter accession |
| `SubmitterAccessionCheck_2` | SubmitterAccessionCheck | Submitter accession too long |
| `LocustagExistsCheck_1` | LocustagExistsCheck | Missing locus_tag in WGS |
| `MoltypeExistsCheck` | MoltypeExistsCheck | Missing molecule type |
| `MoleculeTypeAndDataclassCheck-1` | MoleculeTypeAndDataclassCheck | Incompatible mol_type/dataclass |
| `assemblyTopologyCheck_1` | AssemblyTopologyCheck | Non-linear contig/scaffold |
| `WGSGapCheck` | WGSGapCheck | Gap feature in WGS entry |
| `AGPValidationCheck-*` | AGPValidationCheck | Various AGP errors |

### Feature-Level Error IDs

| Error ID | Check | Description |
|----------|-------|-------------|
| `QualifierCheck-1` | QualifierCheck | Invalid qualifier key |
| `QualifierCheck-2` | QualifierCheck | Missing mandatory value |
| `QualifierCheck-3` | QualifierCheck | Value doesn't match pattern |
| `QualifierCheck-4` | QualifierCheck | Invalid regex group value |
| `QualifierCheck-7` | QualifierCheck | Invalid latitude |
| `QualifierCheck-8` | QualifierCheck | Invalid longitude |
| `QualifierCheck-9` | QualifierCheck | Invalid protein_id version |
| `FeatureKeyCheck-1` | FeatureKeyCheck | Invalid feature key |
| `FeatureKeyCheck-2` | FeatureKeyCheck | Missing mandatory qualifier |
| `FeatureKeyCheck-7` | FeatureKeyCheck | Unpermitted qualifier |
| `FeatureLocationCheck-1` | FeatureLocationCheck | Missing location |
| `FeatureLocationCheck-3` | FeatureLocationCheck | Invalid location order |
| `FeatureLocationCheck-5` | FeatureLocationCheck | Remote location in EMBL |
| `FeatureLengthCheck-1` | FeatureLengthCheck | Feature too short |
| `EC_numberFormatCheck` | EC_numberFormatCheck | Invalid EC number format |
| `EC_numberCheck_1` | EC_numberCheck | Missing EC_number (warning) |
| `EC_numberCheck_2` | EC_numberCheck | Missing EC_number (error) |
| `DeprecatedQualifiersCheck` | DeprecatedQualifiersCheck | Deprecated qualifier |
| `ExclusiveQualifiersCheck1` | ExclusiveQualifiersCheck | Mutually exclusive qualifiers |

### Sequence-Level Error IDs

| Error ID | Check | Description |
|----------|-------|-------------|
| `SequenceBasesCheck` | SequenceBasesCheck | Invalid base character |
| `SequenceBasesCheck-2` | SequenceBasesCheck | Terminal N in linear sequence |
| `SequenceLengthCheck` | SequenceLengthCheck | Sequence too short (EST) |
| `SequenceLengthCheck3` | SequenceLengthCheck | GSS length out of range |
| `SequenceLengthCheck4` | SequenceLengthCheck | TSA too short |
| `SequenceLengthCheck5` | SequenceLengthCheck | lncRNA too short (warning) |

### Source Feature Error IDs

| Error ID | Check | Description |
|----------|-------|-------------|
| `MoleculeTypeAndOrganismCheck` | MoleculeTypeAndOrganismCheck | Incompatible mol_type/organism |
| `SequenceCoverageCheck-1` | SequenceCoverageCheck | Missing start coverage |
| `SequenceCoverageCheck-3` | SequenceCoverageCheck | Missing end coverage |
| `SequenceCoverageCheck-5` | SequenceCoverageCheck | Gap in coverage |
| `SequenceCoverageCheck-9` | SequenceCoverageCheck | Overlapping sources |

### Assembly Error IDs

| Error ID | Check | Description |
|----------|-------|-------------|
| `AssemblyInfoMissingNameCheck` | AssemblyInfoNameCheck | Missing assembly name |
| `AssemblyInfoInvalidAssemblyName` | AssemblyInfoNameCheck | Invalid characters in name |
| `AssemblyInfoInvalidAssemblyNameLength` | AssemblyInfoNameCheck | Name too long |
| `InvalidNoOfFields` | ChromosomeListFileValidationCheck | Wrong field count |
