# Sequencetools

Sequencetools is a Java library for reading and validating EMBL-Bank and GenBank flat files and related file formats. It is used by Webin-CLI and ENA's internal processing pipelines.

# License

Copyright 2015-2023 EMBL - European Bioinformatics Institute Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.









# Public Interface Reference

This section documents every `public interface` declared in `src/main/java`, with practical inputs, outputs, side effects, and implementation notes.

## Integration entry points

`SequenceToolsServices` is the main service registry used by the pipeline:

- `SequenceToolsServices.init(SequenceRetrievalService)`
- `SequenceToolsServices.init(SampleRetrievalService)`

Both are one-time initializers (`compareAndSet`), so the first non-null implementation wins for the process lifetime.

## What data this library actually processes

This library is primarily a transformer/validator for sequence submission files. It takes raw submission files, turns them into `Entry` objects, validates/fixes them, and writes normalized artifacts used by later loading stages.

### Input data formats

- `FASTA`: sequence records (`submitter sequence id` + nucleotide bases).
- `FLATFILE`: EMBL (or GenBank) entries with metadata, features, qualifiers, and optional sequence.
- `AGP`: assembly composition rows used to build scaffold/chromosome sequences from component sequences plus gaps.
- `CHROMOSOME_LIST`: chromosome object names + chromosome metadata/topology.
- `UNLOCALISED_LIST`: unlocalised object names linked to chromosomes.
- `TSV`: template-driven sequence rows (read as gzipped TSV in template flow).
- `SAMPLE_TSV`: polysample mapping (`Sequence_id`, `Sample_id`, `Frequency`).
- `TAX_TSV`: sequence taxonomy mapping (`Sequence_id`, `Tax_id`, optional `Scientific_name`).

### Core in-memory data the library uses

- `SubmissionOptions`: full runtime configuration (context, DB connections, file set, auth/service config).
- `SubmissionFile` / `SubmissionFiles`: typed input files with optional fixed-output/report paths.
- `Entry`: canonical sequence record model (ID line, sequence, features, qualifiers, references).
- `ValidationResult`: collected validation/fix messages.
- `AssemblySequenceInfo`: per-submitter-sequence metadata (`length`, `assemblyLevel`, `assigned accession` placeholder).

### Concrete outputs/artifacts produced by sequencetools

- Per-file validation reports: `<input-file>.report`.
- Optional validation stats file: `VALIDATION_STATS.log`.
- Generated/validated master entry: `master.dat` (genome/transcriptome contexts).
- Serialized sequence metadata maps in `processDir`: `fasta.info`, `flatfile.info`, `agp.info`, `sequence.info`.
- Intermediate reduced flatfiles in `processDir/reduced`: `contigs.reduced.tmp`, `scaffolds.reduced.tmp`, `chromosome.flatfile.tmp`.
- Genome unplaced-name artifact: `unplaced.txt` (when distribution output is enabled).
- Temporary lookup DB files used during validation (MapDB-backed, lifecycle-managed): `.contig` (AGP component placements), `.annotation` (annotation-only flatfile lookups).

### What the library does to the data

1. Parses each input record into internal objects (`Entry`, AGP rows, chromosome/unlocalised rows, template/polysample row objects).
2. Determines context and scope per record (template, transcriptome, contig, scaffold, chromosome, etc.).
3. Enriches entries with shared metadata (project/sample/reference/source qualifiers, master-entry header fields).
4. Runs fixers and validators (`EmblEntryValidationPlan` / `GenomeAssemblyValidationPlan`).
5. Emits validation messages and writes normalized artifacts for downstream loading/accessioning.

### Context-specific data flow (what goes in, what comes out)

| Context | Main input files | Main transformation | Main sequencetools outputs |
| --- | --- | --- | --- |
| `sequence` | `FLATFILE`, `TSV` | Template/flatfile records -> EMBL `Entry` validation/fix | Report files, optional fixed EMBL output, `sequence.info` (serialized sequence count integer) |
| `genome` | `FASTA`, `FLATFILE`, `AGP`, `CHROMOSOME_LIST`, `UNLOCALISED_LIST` | Build/validate contig/scaffold/chromosome `Entry` objects; AGP constructs assembled sequences from components | `master.dat`, `fasta.info`/`flatfile.info`/`agp.info`, merged `sequence.info` map, reduced tmp files, `unplaced.txt`, reports |
| `transcriptome` | `FASTA`, `FLATFILE` (+ master flow) | Validate transcript entries against transcriptome scope and master metadata | `master.dat`, reduced contig tmp file, reports, `sequence.info` (serialized sequence count integer) |
| `polysample_full` / `polysample_fasta_sample` / `polysample_tax` | FASTA + sample/tax TSV combinations | Validate cross-file mapping (sequence IDs vs sample/tax rows) and taxonomy submittability | Reports and validated sequence-count state used by downstream accessioning |

Concrete AGP example: contig sequences parsed from FASTA/flatfile are cached by submitter ID; AGP rows then assemble scaffold/chromosome sequences by slicing component ranges, applying reverse-complement for `-` orientation, and inserting `N` runs for gap rows.

## 1) Service contracts

Module summary: this is the integration boundary between sequencetools and external systems. Data coming out of these services is concrete: raw remote sequence bytes (`ByteBuffer`) and BioSample records used to enrich/validate source metadata in entries.

### `uk.ac.ebi.embl.api.service.SequenceRetrievalService`
Retrieves sequence bytes from the CRAM reference registry or equivalent backing store.

Methods and I/O:

- `boolean isSequenceAvailable(String objectId)`
Input: external object identifier.
Output: `true` if sequence can be resolved.

- `boolean isSequenceAvailable(String md5, String sha1)`
Input: checksum pair.
Output: `true` if sequence can be resolved.

- `ByteBuffer getSequence(String objectId, long sequenceLength)`
Input: object identifier and expected sequence length.
Output: sequence bytes in a `ByteBuffer`.
Errors: `ValidationEngineException` on retrieval/validation failure.

- `ByteBuffer getSequence(String md5, String sha1, long sequenceLength)`
Input: checksum pair and expected sequence length.
Output: sequence bytes in a `ByteBuffer`.
Errors: `ValidationEngineException`.

- `ByteBuffer getSequence(RemoteRange remoteRange)`
Input: remote location range (`accession`, optional version, begin/end, complement).
Output: range bytes.
Errors: `ValidationEngineException`.

- `ByteBuffer getSequence(RemoteBase remoteBase)`
Input: remote single-base location (`accession`, optional version, position, complement).
Output: base bytes.
Errors: `ValidationEngineException`.

Notes:

- There is no in-repo implementation of this interface; callers inject one.
- `SegmentFactory` expects this service to handle complement semantics for remote locations.

### `uk.ac.ebi.embl.api.service.SampleRetrievalService`
Retrieves BioSample-like metadata by ID/alias.

Methods and I/O:

- `Sample getSample(String sampleId)`
Input: sample ID, alias, or BioSample accession-like token.
Output: `uk.ac.ebi.ena.webin.cli.validator.reference.Sample` or `null` depending on implementation behavior.

Notes:

- In-repo implementation: `WebinSampleRetrievalService`, which delegates to `SampleService` (Webin/BioSamples APIs).

## 2) Validation contracts

Module summary: this module consumes parsed objects (`Entry`, `Feature`, `Sequence`, genome-list entries) and produces `ValidationResult` messages plus normalized outputs (fixed entry content, reports, and process artifacts such as `sequence.info`/reduced files).

### `uk.ac.ebi.embl.api.validation.ValidationCheck<E>`
Core validation check abstraction used by validation plans.

Methods and I/O:

- `ValidationResult check(E object)`
Input: target object of type `E`.
Output: `ValidationResult` containing zero or more messages.
Errors: `ValidationEngineException`.

- `void setEmblEntryValidationPlanProperty(EmblEntryValidationPlanProperty property)`
- `EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty()`
Input/Output: validation plan runtime configuration.

- `void setEntryDAOUtils(EntryDAOUtils daoUtils)`
- `EntryDAOUtils getEntryDAOUtils()`
Input/Output: DAO helper for ENA/ENPRO lookups.

- `void setEraproDAOUtils(EraproDAOUtils daoUtils)`
- `EraproDAOUtils getEraproDAOUtils()`
Input/Output: DAO helper for ERAPRO lookups.

Notes:

- `ValidationPlan` injects `EmblEntryValidationPlanProperty` and DAO dependencies before invoking `check(...)`.
- Most concrete checks extend `EmblEntryValidationCheck<E>`.

### `uk.ac.ebi.embl.api.validation.report.SubmissionReporter`
Writes validation results/messages to report files.

Methods and I/O:

- `void writeToFile(Path reportFile, ValidationResult validationResult, String targetOrigin)`
- `void writeToFile(Path reportFile, ValidationResult validationResult)`
- `void writeToFile(Path reportFile, ValidationMessage validationMessage)`
- `void writeToFile(Path reportFile, Severity severity, String message, Origin origin)`
- `void writeToFile(Path reportFile, Severity severity, String message)`
- `void writeToFile(Path reportFile, ConcurrentMap<String, AtomicLong> messageStats)`
Input: destination `Path` and message/result payload.
Output: none (side effect: append to file).
Errors: stats overload can throw `IOException`.

- File overloads with the same payloads:
`writeToFile(File reportFile, ...)`

Notes:

- In-repo implementation: `DefaultSubmissionReporter`.
- `DefaultSubmissionReporter` filters output by configured severities and appends UTF-8 text.

### `uk.ac.ebi.embl.api.validation.Origin`
Represents where a validation message came from (line/file/object context).

Methods and I/O:

- `String getOriginText()`
Output: human-readable origin text used in formatted messages.

Notes:

- In-repo implementations: `DefaultOrigin`, `FlatFileOrigin`.

### `uk.ac.ebi.embl.api.validation.HasOrigin`
Trait for domain objects that carry `Origin` metadata.

Methods and I/O:

- `Origin getOrigin()`
Output: current origin context.

- `void setOrigin(Origin origin)`
Input: origin context to attach.
Output: none (state mutation).

Used by `Entry`, `Feature`, `Qualifier`, locations, references, and other API model objects.

### `uk.ac.ebi.embl.api.validation.ValidationMessage.MessageFormatter`
Nested formatting interface for rendering `ValidationMessage` instances.

Methods and I/O:

- `void writeMessage(Writer writer, ValidationMessage<?> validationMessage, String targetOrigin)`
Input: writer, message, optional target origin.
Output: none (side effect: writes formatted text).
Errors: `IOException`.

- `String getFormattedMessage(ValidationMessage<?> message, String targetOrigin)`
Input: message and optional target origin.
Output: fully formatted string.

Notes:

- In-repo implementations: `TextMessageFormatter`, `TextTimeMessageFormatter`.
- Used by both `ValidationMessage` and `ValidationResult` output paths.

## 3) Data access contracts (DB-backed)

Module summary: these interfaces read submission/registry data from ENA/ERAPRO (project validity, sequence existence/length, locus tags, sample-derived source/reference metadata) so validation decisions are based on live archive state.

### `uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils`
DAO helper for ENA/ENPRO side validation/accession lookups.

Methods and I/O:

- `boolean isValueExists(String tableName, String constraintKey, String constraintValue)`
Input: table/column/value tuple.
Output: existence flag.
Errors: `SQLException`.

- `boolean isEntryExists(String accession)`
Input: accession.
Output: existence flag from `bioseq`.
Errors: `SQLException`.

- `Long getSequenceLength(String accession)`
Input: accession.
Output: sequence length (`0L` if not found in current implementation).
Errors: `SQLException`.

- `ArrayList<Qualifier> getChromosomeQualifiers(String analysisId, String submitterAccession, SourceFeature source)`
Input: assembly ID, object name, optional source feature.
Output: derived chromosome/organelle/plasmid/segment qualifiers.
Errors: `SQLException`.

- `boolean isProjectValid(String project)`
Input: project ID.
Output: validity flag.
Errors: `SQLException`.

- `HashSet<String> getProjectLocutagPrefix(String project)`
Input: project ID.
Output: locus tag prefixes for that project.
Errors: `SQLException`.

- `String isEcnumberValid(String ecNumber)`
Input: EC number.
Output: validation state string from DB or `null` if not found.
Errors: `SQLException`.

- `Entry getEntryInfo(String primaryAcc)`
Input: primary accession.
Output: minimal `Entry` info (`entry_name`, `dataclass`, keywords) or `null` if absent.
Errors: `SQLException`.

- `String getDbcode(String prefix)`
Input: accession prefix.
Output: DB code or `null`.
Errors: `SQLException`.

- `boolean isChromosomeValid(String analysisId, String chromosomeName)`
Input: assembly ID and chromosome name.
Output: existence flag.
Errors: `SQLException`.

- `String getNewProteinId()`
Input: none.
Output: generated protein ID or `null`.
Errors: `SQLException`.

Notes:

- In-repo implementation: `EntryDAOUtilsImpl`.

### `uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils`
DAO/helper contract for ERAPRO + Webin/BioSamples-backed submission metadata.

Methods and I/O:

- `Reference getSubmitterReference(String analysisId)`
Input: analysis ID.
Output: submitter reference built from submission account/contact data.
Errors: `SQLException`, `ValidationEngineException`.

- `AssemblySubmissionInfo getAssemblySubmissionInfo(String analysisId)`
Input: analysis ID.
Output: study/project/sample/account/date window data for assembly validation.
Errors: `SQLException`.

- `List<String> isSampleHasDifferentProjects(String analysisId)`
Input: analysis ID.
Output: analysis IDs sharing sample but in different study/project context.
Errors: `SQLException`.

- `Entry createMasterEntry(String analysisId, AnalysisType analysisType)`
Input: analysis ID and analysis type.
Output: assembled master `Entry` (or `null` if analysis not found).
Errors: `SQLException`, `ValidationEngineException`.

- `Reference getReference(Entry entry, String analysisId, AnalysisType analysisType)`
Input: entry + analysis context.
Output: reference from manifest XML when available (or `null`).
Side effects: may add ENA `XRef` values to provided `entry`.
Errors: `SQLException`, `ValidationEngineException`.

- `String getTemplateId(String analysisId)`
Input: analysis ID.
Output: template ID or `null`.
Errors: `SQLException`.

- `Set<String> getLocusTags(String projectId)`
Input: project ID.
Output: uppercase locus tags.
Errors: `SQLException`.

- `SourceFeature getSourceFeature(String sampleId)`
Input: sample ID.
Output: source feature built from sample + taxonomy.
Errors: `Exception`.

- `boolean isProjectValid(String text)`
Input: project identifier.
Output: validity flag.
Errors: `SQLException`.

- `boolean isIgnoreErrors(String submissionAccountId, String context, String name)`
Input: account/context/check name.
Output: ignore flag.
Errors: `SQLException`.

- `Analysis getAnalysis(String analysisId)`
Input: analysis ID.
Output: minimal `Analysis` model (`submissionAccountId`, `uniqueAlias`) or `null`.
Errors: `SQLException`.

- `String getCommentsToTranscriptomeMaster(String analysisId)`
Input: analysis ID.
Output: synthesized transcriptome comments text or `null`.
Errors: `SQLException`.

Nested DTO:

- `AssemblySubmissionInfo` carries:
`studyId`, `projectId`, `biosampleId`, `sampleId`, `submissionAccountId`, `begindate`, `enddate`.

Notes:

- In-repo implementation: `EraproDAOUtilsImpl`.
- Depends on DB connection plus Webin/BioSamples credentials for sample enrichment.

## 4) Template processing contracts

Module summary: these contracts turn tabular template input into sequence entries. One side loads template XML definitions/order, the other mutates token maps so rows can be converted into valid EMBL entries.

### `uk.ac.ebi.embl.template.LoadStrategyI`
Loads template definitions and ordering for `TemplateFactory`.

Methods and I/O:

- `void loadTemplates()`
Input: none.
Output: none (side effect: load internal state).
Errors: `TemplateException`.

- `Map<String, Integer> getOrders()`
Output: template ID to display/order index map.

- `TemplateSet getTemplates()`
Output: loaded templates by ID/version.

Notes:

- `TemplateFactory.loadTemplates()` expects `loadStrategyI` to be set before calling.
- No concrete `LoadStrategyI` implementation is present in this repository.

### `uk.ac.ebi.embl.template.TemplatePreProcessor`
Pre-processes mutable template token maps before entry generation.

Methods and I/O:

- `ValidationResult process(TemplateVariables variablesMap)`
Input: mutable token map (`TemplateVariables`).
Output: `ValidationResult` with preprocessing errors/warnings/fixes.
Side effects: may add/update tokens in `variablesMap`.

Notes:

- In-repo implementations:
`ITSTemplatePreProcessor`, `PhyloMarkerTemplatePreProcessor`, `OrganelleDETemplatePreProcessor`.
- Registered in `TemplateFactory` per template ID.

## 5) Storage contract

Module summary: this is the lookup-data loader used by validators/fixers for controlled lists and rule datasets (typically TSV resources), with caching to avoid re-reading files on each check.

### `uk.ac.ebi.embl.api.storage.DataManager`
Provides access to tabular reference datasets (`DataSet`).

Methods and I/O:

- `DataSet getDataSet(String name)`
Input: dataset name/path (typically classpath resource path).
Output: loaded `DataSet`.

Notes:

- In-repo implementation: `CachedFileDataManager` (loads TSV from classpath and caches by name).
- Current implementation returns `null` if loading fails (after catching `IOException`).

## 6) Flat-file reader contracts

Module summary: this module is the parsing layer. It turns bytes/lines from EMBL, GenBank, GFF3, and genome-list files into concrete typed objects (`Entry`, `GFF3RecordSet`, genome-list entries) that validation and load code can process.

### `uk.ac.ebi.embl.flatfile.reader.ILineReader`
Lowest-level line streaming abstraction.

Methods and I/O:

- `String readLine()`
Input: none.
Output: next line or `null` at EOF.
Errors: `IOException`.

Notes:

- In-repo implementations: `BufferedFileLineReader`, `LineReaderWrapper`.

### `uk.ac.ebi.embl.flatfile.reader.FlatFileReader<T>`
Entry/file parser abstraction used across EMBL/GenBank/GFF3/genome assembly readers.

Methods and I/O:

- `ValidationResult read()`
Input: parser state + underlying stream/file.
Output: validation messages generated during parse.
Errors: `IOException`.

- `ValidationResult skip()`
Input: parser state.
Output: validation result for skip operation.
Errors: `IOException`.

- `T getEntry()`
Output: parsed object, type depends on implementation.

- `boolean isEntry()`
Output: `true` when a valid/complete entry was read according to implementation rules.

Current implementations in this repo:

- `FlatFileEntryReader` / `EntryReader` hierarchy: `T = Entry`
- `GFF3FlatFileEntryReader`: `T = GFF3RecordSet`
- `GCSEntryReader` hierarchy (assembly/chromosome/unlocalised readers): `T = Object`

Implementation note:

- Some `GCSEntryReader` subclasses currently return `null` from `skip()` (not a full skip implementation).

## 7) Location and qualifier contracts

Module summary: these interfaces model coordinate-bearing data in features and qualifiers, including remote references to other accessions. They are the bridge between textual location syntax and structured coordinate objects.

### `uk.ac.ebi.embl.api.entry.location.LocalLocation`
Marker interface for local (same-entry sequence) location components.

Methods and I/O:

- No methods.

Used by: `LocalBase`, `LocalRange`, `LocalBetween`.

### `uk.ac.ebi.embl.api.entry.location.RemoteLocation`
Contract for remote location components that reference another accession/version.

Methods and I/O:

- `String getAccession()`
Output: remote accession.

- `Integer getVersion()`
Output: remote version, may be null.

- `void setAccession(String accession)`
Input: remote accession string.
Output: none.

- `void setVersion(int version)`
Input: remote sequence version.
Output: none.

Used by: `RemoteBase`, `RemoteRange`, `RemoteBetween`.

### `uk.ac.ebi.embl.api.entry.qualifier.LocationQualifier`
Qualifier contract for single parsed location values.

Methods and I/O:

- `boolean setLocation(Location location)`
Input: `Location` object.
Output: `true` on successful mutation, `false` in implementation-specific no-op cases.
Errors: `ValidationException` when qualifier value format is invalid.

- `Location getLocation()`
Input: current qualifier value text.
Output: parsed `Location` object.
Errors: `ValidationException` on invalid syntax.

- `String getName()`
- `String getValue()`
Output: qualifier metadata/value.

Current implementations:

- `Rpt_Unit_RangeQualifier`
- `Tag_PeptideQualifier`

Both parse a `"<begin>..<end>"` pattern.

### `uk.ac.ebi.embl.api.entry.qualifier.CompoundLocationQualifier`
Qualifier contract for compound location values embedded in qualifier text.

Methods and I/O:

- `boolean setLocations(CompoundLocation<Location> location)`
Input: compound location object.
Output: success flag.
Errors: `ValidationException`.

- `CompoundLocation<Location> getLocations()`
Input: qualifier value text.
Output: parsed compound location.
Errors: `ValidationException` when value cannot be parsed.

- `String getName()`
- `String getValue()`
Output: qualifier metadata/value.

Current implementations:

- `TranslExceptQualifier`
- `AnticodonQualifier`

Implementation note:

- In current code, `setLocations(...)` in both classes is a stub returning `false`.

## 8) Taxonomy metadata contracts

Module summary: this trait marks domain objects that carry taxonomy identity (`Taxon`) so organism/taxonomy checks and source-feature construction can run consistently across different model objects.

### `uk.ac.ebi.embl.api.taxonomy.HasTaxon`
Trait for domain objects that carry `Taxon` state.

Methods and I/O:

- `Taxon getTaxon()`
Output: taxonomy object.

- `void setTaxon(Taxon taxon)`
Input: taxonomy object.
Output: none (state mutation).

Used by `SourceFeature`, `OrganismQualifier`, and `Project`.

## 9) Runtime behavior (how interfaces collaborate)

This section explains how the public interfaces above behave as a system at runtime, based on sequencetools internals and usage in `webin-sequence-stages`.

### Validation lifecycle (`SubmissionOptions` -> `SubmissionValidator` -> `SubmissionValidationPlan`)

The main execution flow is:

1. Build `SubmissionOptions` with context and inputs.
2. Call `SubmissionValidator.validate()`.
3. `SubmissionValidator` runs `new SubmissionValidationPlan(options).execute()`.
4. Pipeline code handles `ValidationEngineException` as user/system failure.

Behavior details:

- `SubmissionOptions.init()` enforces required fields before validation starts.
- For non-CLI execution, DB connections (`enproConnection`, `eraproConnection`) are mandatory.
- For non-CLI `Context.sequence`, `serviceConfig` is mandatory.
- `SubmissionOptions.init()` creates `{processDir}/reduced` if missing.
- `SubmissionValidationPlan.execute()` runs checks in fixed order; order is intentionally stable.
- In genome context it also merges sequence metadata and writes `sequence.info` and `unplaced.txt` artifacts.
- In non-genome contexts it writes sequence count/object info instead of genome-specific artifacts.
- For non-CLI runs, if final `ValidationResult` has `ERROR`, `SubmissionValidator` throws `ValidationEngineException` with concatenated error text.

### Context drives file-type behavior (`Context`)

`Context` determines which file validators run:

- `Context.sequence`: `FLATFILE`, `TSV`
- `Context.genome`: `FASTA`, `FLATFILE`, `AGP`, `CHROMOSOME_LIST`, `UNLOCALISED_LIST`, `MASTER`, `ANNOTATION_ONLY_FLATFILE`
- `Context.transcriptome`: `FASTA`, `FLATFILE`, `MASTER`
- `Context.polysample_*`: combinations of `FASTA`, `SAMPLE_TSV`, `TAX_TSV`

Operationally, pipeline services set context before calling `SubmissionValidator`, so context selection is the main switch that changes validation behavior.

### Check execution and dependency injection (`ValidationCheck<E>`, `ValidationPlan`)

Checks are not expected to self-wire their dependencies. `ValidationPlan.execute(check, target)` injects:

- `EmblEntryValidationPlanProperty`
- `EntryDAOUtils` (lazy-created when ENA connection is present)
- `EraproDAOUtils` (lazy-created when ERA connection is present)

Then it evaluates execution annotations on each check:

- `@RemoteExclude`
- `@ExcludeScope`
- `@GroupIncludeScope`

This means check behavior depends on both runtime properties and annotations, not just on the `check(...)` method body.

### Global message filtering (`ValidationResult`)

`ValidationResult` filters messages when they are appended:

- `ValidationResult.append(...)` drops messages below static `minSeverity`.
- Default `minSeverity` is `FIX`.
- The pipeline sets `ValidationResult.setMinSeverity(Severity.ERROR)` during startup, so `FIX`, `WARNING`, and `INFO` are globally suppressed in that runtime.

This explains why the same check set can produce different visible outputs depending on caller initialization.

### Service registry semantics (`SequenceToolsServices`)

`SequenceToolsServices` is a process-global service registry:

- `init(...)` uses `AtomicReference.compareAndSet(null, service)`.
- First successful initialization wins for the JVM lifetime.
- Later `init(...)` calls do not replace an already initialized service.

Observed effects in real use:

- `SubmissionValidator` initializes `SampleRetrievalService` for `Context.sequence`.
- `TemplateEntryProcessor` reads `SequenceToolsServices.sampleRetrievalService()` to resolve sample aliases/IDs.
- `SegmentFactory` reads `SequenceToolsServices.sequenceRetrievalService()` to resolve `RemoteBase`/`RemoteRange`.

Implication for integrators: initialize required services before validation/parsing code that needs them.

### Remote-sequence behavior (`SequenceRetrievalService`, `SegmentFactory`)

`SegmentFactory` is the main consumer of `SequenceRetrievalService`.

Behavior:

- Local locations (`LocalBase`, `LocalRange`) are sliced from the current entry sequence.
- Remote locations (`RemoteBase`, `RemoteRange`) are fetched via `SequenceRetrievalService`.
- If a compound location contains remote segments and retrieval service is unavailable, segment creation returns `null`.
- Complement handling for remote locations is delegated to retrieval service implementations (`getSequence(remoteRange/base)` is expected to return orientation-correct bytes).

### Reporting behavior (`SubmissionReporter`, `DefaultSubmissionReporter`)

`DefaultSubmissionReporter` is append-only and severity-filtered:

- It writes only messages whose severity is in the configured `HashSet<Severity>`.
- It appends to existing report files (does not truncate).
- It uses UTF-8 output.
- It intentionally swallows I/O exceptions in most write paths (best-effort reporting).

In pipeline usage this is typically configured as:

- `ERROR`-only for user-facing validation reports.
- `INFO` for stage progress logging in some load services.

### Reader contract in practice (`FlatFileReader<T>`, `ILineReader`)

Callers follow this loop:

1. `read()`
2. `while (isEntry()) { getEntry(); read(); }`

This pattern is used for accession rewriting and load operations. The contract is stateful: `read()` advances parser state and `getEntry()` returns the last parsed item.

### Genome assembly list readers with pipeline data loaders

Pipeline loaders use sequencetools genome list readers directly:

- `ChromosomeListFileReader` -> yields chromosome entries for registration/persistence.
- `UnlocalisedListFileReader` -> yields unlocalised entries later mapped to assigned accessions.

These readers are part of the practical public surface for genome-context integrations.
