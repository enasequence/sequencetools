# Sequencetools Fixers Reference

This document covers fixer classes under:

- `src/main/java/uk/ac/ebi/embl/api/validation/fixer`

It explains what each fixer does, how it does it, and whether it is currently wired into validation plans.

## 1) How fixers execute

1. `SubmissionValidationPlan` builds an `EmblEntryValidationPlan` (or `GenomeAssemblyValidationPlan` for genome-assembly info/list objects).
2. In `EmblEntryValidationPlan`, **fixers run before checks** when `SubmissionOptions.isFixMode == true`.
3. Fixers mutate in-memory model objects (`Entry`, `Feature`, `Sequence`, `AssemblyInfoEntry`) in place.
4. Fixers emit `Severity.FIX` messages via `reportMessage(...)` (and sometimes warnings/errors) into `ValidationResult`.
5. The mutated object then continues through later fixers and checks in the same plan execution.

Implementation detail:

- Source-update mode (`planProperty.isSourceUpdate == true`) uses `ValidationUnit.SOURCE_FEATURE_FIXES`.
- Normal sequence-entry mode uses `ValidationUnit.SEQUENCE_ENTRY_FIXES`.
- Assembly-info objects use `GenomeAssemblyValidationUnit.ASSEMBLYINFO_FIXES`.

## 2) Active vs non-active fixers

Legend:

- `Active`: referenced by `ValidationUnit` or `GenomeAssemblyValidationUnit`.
- `Utility`: not a `ValidationCheck` fixer in plan lists, but used by parsers/processors directly.
- `Inactive`: fixer class exists but is not currently wired into plan execution.

Current non-active classes:

- `ChromosomeNameFix` (`Utility`)
- `SubmitterAccessionFix` (`Utility`)
- `DeleteEntryOnErrorFix` (`Inactive`)
- `NonContoAGPFix` (`Inactive`)
- `QualifierWithinQualifierFix` (`Inactive`, commented out in `ValidationUnit`)

## 3) Entry fixers (`fixer.entry`)

| Class | Status | Mutates | What it does and how |
| --- | --- | --- | --- |
| `AccessionFix` | Active | `Entry.secondaryAccessions` | Removes secondary accessions that match master-accession format (`AccessionMatcher.isMasterAccession`). |
| `AgptoConFix` | Active | `Entry.features/contigs/dataclass` | For AGP file context only, re-validates AGP (`AGPValidationCheck`), converts AGP rows into CON-style features/contigs via `EntryUtils.convertAGPtofeatureNContigs`, sets dataclass to `CON`. |
| `AnticodonQualifierFix` | Active | `anticodon` qualifier values | Re-parses `anticodon` qualifiers, derives sequence from location (`SegmentFactory`), normalizes amino-acid token, injects missing sequence into qualifier value text. |
| `Ascii7CharacterFix` | Active | Entry text fields, refs, qualifier values | Converts diacritics/non-ASCII7 chars to ASCII-safe values across comment/description/reference text/qualifier values using `Ascii7CharacterConverter`. |
| `AssemblyLevelEntryNameFix` | Active | `Entry.submitterAccession` | In assembly scopes, when submitter accession is missing, auto-assigns `contigN` / `scaffoldN` / `chromosomeN` based on validation scope and sequence number. |
| `AssemblyTopologyFix` | Active | `Entry.sequence.topology` | Forces topology to `linear` for assembly contig/scaffold/transcriptome scopes if not already linear. |
| `CDS_RNA_LocusFix` | Active | CDS/tRNA/rRNA qualifiers | For CDS/tRNA/rRNA features missing `locus_tag`/`gene`/`gene_synonym`, copies these from containing gene feature when location inclusion rules allow it. |
| `ChromosomeNameFix` | Utility | chromosome name strings | Normalizes chromosome name tokens: strips spaces, replaces punctuation with `_`, removes words like `chromosome/chr/linkage group/plasmid`, maps mitochondria names to `MT`. |
| `DataclassFix` | Active | `Entry.dataClass`, `Entry.keywords` | Resolves/repairs dataclass from KW dataset, accession-derived rules, or keywords; also injects dataclass keywords when dataclass exists but KW is missing. |
| `DeleteEntryOnErrorFix` | Inactive | none (reporting only) | Only reports a FIX if `entry.isDelete()` is already true; does not mark delete itself. |
| `DivisionFix` | Active | `Entry.division` | Sets division using source qualifiers/taxonomy: transgenic -> `TGN`, environmental sample -> `ENV`, else taxonomy lookup by taxid/scientific name; falls back to `XXX`; caches division lookups. |
| `GaptoAssemblyGapFeatureFix` | Active | Feature names/qualifiers | In assembly scopes, renames `gap` features to `assembly_gap` and adds `/gap_type=\"unknown\"`. |
| `GeneAssociatedwithFeatureFix` | Active | `Entry.features` | Removes gene features that are exact-location duplicates of associated CDS/tRNA/rRNA features. |
| `GeneAssociationFix` | Active | feature `gene` qualifier | Builds stable 1:1 mapping from `locus_tag` to `gene`, then adds missing `/gene` qualifiers; also strips single quotes from `pseudogene` values. |
| `GeneSynonymFix` | Active | feature `gene_synonym` qualifiers | Creates a stable synonym set per identifier (`locus_tag` or `gene`), adds missing synonyms and removes extra ones; avoids unreliable identifiers with conflicting mappings. |
| `HoldDateFix` | Active | `Entry.holdDate` | Removes hold date when it is in the past. |
| `JournalFix` | Active | none (reporting only) | Detects non-standard unpublished journal text and reports fix message; implementation currently reports but does not mutate `journalBlock`. |
| `LocusTagAssociationFix` | Active | feature `locus_tag` qualifier | Builds stable 1:1 mapping from `gene` to `locus_tag`, then adds missing `/locus_tag` qualifiers. |
| `MoleculeTypeAndQualifierFix` | Active | source qualifiers on features | Uses dataset `SOURCE_QUALIFIERS_MOLTYPE_VALUES`; removes qualifiers not allowed for current molecule type. |
| `NonContoAGPFix` | Inactive | `Entry.sequence.agpRows` | For non-CON entries with sequence + assembly_gap features, synthesizes AGP rows (component and gap rows) from gap feature coordinates and qualifiers. |
| `ProteinIdRemovalFix` | Active | `/protein_id` qualifiers | Removes all protein_id qualifiers from features in non-NCBI/non-master scopes (protein IDs must be assigned by archive). |
| `QualifierRemovalFix` | Active | `/citation`, `/compare` qualifiers | Removes `citation` and `compare` qualifiers from all features except `old_sequence`. |
| `ReferencePositionFix` | Active | reference RP ranges | Clamps reference location ranges (`RP`) to valid sequence boundaries (`1..sequenceLength`). |
| `SubmitterAccessionFix` | Utility | submitter accession strings | Sanitizes submitter accessions (remove whitespace/quotes, replace separators with `_`, coalesce underscores), and synchronizes entry value with `/submitter_seqid` when present. Used by readers/template code. |
| `TPA_dataclass_Fix` | Active | `Entry.description` | Rewrites TPA description prefixes like `TPA_inf:`/`TPA_exp:`/`TPA_reasm:` to canonical `TPA:`. |

## 4) Feature fixers (`fixer.feature`)

| Class | Status | Mutates | What it does and how |
| --- | --- | --- | --- |
| `CollectionDateQualifierFix` | Active | `/collection_date` values | Normalizes date forms (`D-MON-YYYY` -> `DD-MON-YYYY`; in NCBI scope also `DD-MON-YY` -> `DD-MON-20YY`). |
| `EC_numberValueFix` | Active | `/EC_number` qualifiers | Removes EC qualifiers with invalid placeholder values (`deleted`, `-.-.-.-`, etc.). |
| `EC_numberfromProductValueFix` | Active | `product` and `EC_number` qualifiers | Extracts EC numbers embedded in product text, removes EC fragments from product value, adds `/EC_number`; for hypothetical/unknown products removes EC qualifiers. |
| `ExclusiveQualifierTransformToNoteQualifierFix` | Active | qualifiers and `/note` | For mutually exclusive qualifier pairs from dataset, removes disallowed qualifier and appends its value into `/note` (or creates `/note`). |
| `ExperimentQualifierFix` | Active | `/experiment` qualifiers | If multiple experiment qualifiers exist, removes legacy placeholder value `experimental evidence, no additional details recorded`. |
| `FeatureLocationFix` | Active | feature locations | Fixes reversed ranges by swapping begin/end and setting complement when needed; sorts locations afterward; reports error in contradictory complement case. |
| `FeatureQualifierDuplicateValueFix` | Active | `old_locus_tag` qualifiers | Removes duplicate `old_locus_tag` values; also removes `old_locus_tag` when same value exists in `locus_tag`. |
| `FeatureQualifierRenameFix` | Active | qualifier names/values | Renames qualifiers using `FEATURE_QUALIFIER_RENAME` dataset; special case: old `label` values are moved to `note`-style value prefix (`label:<value>`). |
| `FeatureRenameFix` | Active | feature key names (+ note) | Renames feature keys using `FEATURE_RENAME` dataset; special rule for `repeat_region` requires `/mobile_element`; renaming `conflict` adds `/note=\"conflict\"`. |
| `Isolation_sourceQualifierFix` | Active | source qualifiers | If `isolation_source` value looks like valid country, moves to `/country`; if it matches lat/lon regex, moves to `/lat_lon`. |
| `Lat_lonValueFix` | Active | `/lat_lon` values | Normalizes numeric formatting and separator style; removes malformed lat/lon qualifiers. |
| `Linkage_evidenceFix` | Active | `/linkage_evidence` qualifiers | Normalizes underscores to spaces, handles contamination and incompatible gap-type cases, may remove linkage evidence when gap type disallows it. |
| `LocusTagValueFix` | Active | `/locus_tag` values | Uppercases locus tag qualifier values. |
| `ObsoleteFeatureFix` | Active | feature key and qualifiers | Converts obsolete keys: `scRNA/snRNA/snoRNA` -> `ncRNA` + `/ncRNA_class`, `repeat_unit` -> `repeat_region`. |
| `ObsoleteFeaturetoNewFeatureFix` | Active | feature key and qualifiers | Uses `OBSOLETE_FEATURE_TO_FEATURE` dataset to transform obsolete feature key into new key and inject required qualifier/value pair. |
| `QualifierValueFix` | Active | qualifier values | Performs value cleanup/rules: quote replacement, altitude normalization, and dataset-driven value replacements from `QUALIFIER_VALUE_TO_FIX_VALUE`. |
| `QualifierWithinQualifierFix` | Inactive | qualifiers list/values | Extracts embedded `/qualifier=...` fragments from qualifier values into real qualifiers, removes embedded text from original qualifier value. Currently commented out in plan lists. |
| `Transl_exceptLocationFix` | Active | `transl_except` values | Removes complement flags from transl_except location expressions and rewrites normalized qualifier value string. |

## 5) Sequence fixers (`fixer.sequence`)

| Class | Status | Mutates | What it does and how |
| --- | --- | --- | --- |
| `ContigstosequenceFix` | Active | `Entry.sequence.sequenceByte` | Builds full sequence from CO-line contig components (`RemoteRange`, `RemoteBase`, `Gap`) using `SegmentFactory`; marks entry `nonExpandedCON=true`. |
| `Mol_typeFix` | Active | `Entry.sequence.moleculeType` | For TSA entries containing assembly_gap features, changes molecule type from `mRNA` to `transcribed RNA`. |
| `SequenceBasesFix` | Active | sequence bytes, ID length, feature locations | Trims leading/trailing `n` bases on eligible linear sequences; updates ID line length; shifts feature locations via `Utils.shiftLocation`; reports error if sequence becomes empty. |
| `SequenceToGapFeatureBasesFix` | Active | feature list (`gap`/`assembly_gap`) | Scans long N-runs and adds missing gap features with coordinates and estimated length; in assembly scopes creates `assembly_gap` + `gap_type=unknown`; sets `estimated_length=unknown` when 90%+ created gaps are length 100. |

## 6) Source-feature fixers (`fixer.sourcefeature`)

| Class | Status | Mutates | What it does and how |
| --- | --- | --- | --- |
| `GeoLocationQualifierFix` | Active | `/country`, `/note` | Validates country values against controlled list; strips trailing punctuation; if invalid, removes country qualifier and appends value to note. |
| `HostQualifierFix` | Active | `/host` value | If host is not valid organism name, looks up taxon by common name and replaces host value with scientific name. |
| `SourceQualifierFix` | Active | source qualifier names/values | Salmonella-specific cleanup (`serotype` -> `serovar`, text normalization), and `metagenome_source` cleanup/removal when invalid or redundant. |
| `SourceQualifierMissingFix` | Active | source qualifiers (`environmental_sample`, `isolation_source`, `isolate`, `strain`, `metagenome_source`) | Adds missing qualifiers based on metagenome/uncultured/lineage logic, normalizes metagenome_source value, and rewrites/removes strain-related qualifiers in environmental sample context. |
| `StrainQualifierValueFix` | Active | `/strain` value | Normalizes strain formatting for type-strain conventions based on whether organism name is formal taxonomy. |
| `TaxonomicDivisionNotQualifierFix` | Active | source qualifiers | Removes qualifiers forbidden for the taxonomic division according to `TAXONOMIC_DIVISION_NO_QUALIFIER` dataset. |

## 7) Genome-assembly fixer (`fixer.genomeassembly`)

| Class | Status | Mutates | What it does and how |
| --- | --- | --- | --- |
| `AssemblyTypeFix` | Active | `AssemblyInfoEntry.assemblyType` | Canonicalizes assembly type to the enum fixed value (`AssemblyType.getFixedValue()`), used in `GenomeAssemblyValidationUnit.ASSEMBLYINFO_FIXES`. |

## 8) Practical notes for maintainers

1. Fixers are order-sensitive. Some depend on outputs of earlier fixers (for example association fixes and qualifier normalization).
2. Several fixers are intentionally scope-gated with `@ExcludeScope`/`@GroupIncludeScope`; behavior differs significantly across EMBL/assembly/NCBI modes.
3. A few classes in `fixer` are not in active plan lists (`DeleteEntryOnErrorFix`, `NonContoAGPFix`, `QualifierWithinQualifierFix`) but remain useful reference implementations.
4. Utility sanitizers (`SubmitterAccessionFix`, `ChromosomeNameFix`) run in reader code paths, so they affect parsed input even when not listed in plan fixers.
