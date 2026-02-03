# Sequencetools

Sequencetools is a Java library for reading, writing, and validating EMBL-Bank and GenBank flat files and related file formats. It is used by Webin-CLI and ENA's internal processing pipelines (webin-sequence-stages).

---

## Table of Contents

- [Key Concepts](#key-concepts)
- [Core Data Model](#core-data-model)
- [File Readers](#file-readers)
- [File Writers](#file-writers)
- [Validation Framework](#validation-framework)
- [Genome Assembly Classes](#genome-assembly-classes)
- [Usage Examples](#usage-examples)
- [License](#license)

---

## Key Concepts

This section explains the fundamental concepts and terminology used throughout the ENA sequence processing ecosystem.

### File Formats

#### Flat File (EMBL/GenBank Format)

A flat file is a text-based format for representing sequence entries with their annotations. It contains structured blocks identified by two-letter tags.

**EMBL Format Example:**
```
ID   CONTIG001; SV 1; linear; genomic DNA; STD; PRO; 1000 BP.
XX
AC   AB123456;
XX
DE   Homo sapiens example gene
XX
KW   example; gene.
XX
OS   Homo sapiens (human)
OC   Eukaryota; Metazoa; Chordata; Mammalia; Primates; Hominidae; Homo.
XX
FH   Key             Location/Qualifiers
FH
FT   source          1..1000
FT                   /organism="Homo sapiens"
FT                   /mol_type="genomic DNA"
FT   CDS             100..900
FT                   /gene="EXAMPLE"
FT                   /product="example protein"
XX
SQ   Sequence 1000 BP; 250 A; 250 C; 250 G; 250 T; 0 other;
     atgcatgcat gcatgcatgc atgcatgcat gcatgcatgc atgcatgcat gcatgcatgc       60
     ...
//
```

**Key Sections:**
- `ID` - Entry identifier and metadata
- `AC` - Accession number(s)
- `DE` - Description
- `KW` - Keywords
- `OS/OC` - Organism and classification
- `FT` - Feature table (annotations)
- `SQ` - Sequence data
- `//` - Entry terminator

---

#### Reduced Flat File

A minimal version of the flat file format used for archival purposes. Contains only essential information:
- ID line
- Source feature with `submitter_seqid` qualifier only
- CO lines (contig references for master entries)
- Sequence data

**Example:**
```
ID   CONTIG001; SV 1; linear; genomic DNA; WGS; PRO; 1000 BP.
FT   source          1..1000
FT                   /submitter_seqid="contig_001"
SQ   Sequence 1000 BP; 250 A; 250 C; 250 G; 250 T; 0 other;
     atgcatgcat gcatgcatgc ...
//
```

**Purpose:** Reduces storage requirements while preserving essential data for sequence retrieval and identification.

---

#### FASTA Format

A simple text format for nucleotide or protein sequences.

**Example:**
```
>contig_001 Example contig sequence
ATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGC
ATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGC
>contig_002 Another contig
GCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTA
```

**Structure:**
- Header line starting with `>` followed by sequence ID and optional description
- Sequence data on subsequent lines (typically 60-80 characters per line)

---

#### AGP File (Assembly Golden Path)

A tab-delimited file that describes how component sequences (contigs) are assembled into larger structures (scaffolds/chromosomes).

**Example:**
```
# ORGANISM: Homo sapiens
# ASSEMBLY NAME: Example_v1
scaffold_1	1	1000	1	W	contig_001	1	1000	+
scaffold_1	1001	1100	2	N	100	scaffold	yes	paired-ends
scaffold_1	1101	2100	3	W	contig_002	1	1000	+
```

**Columns:**
1. Object name (scaffold/chromosome)
2. Object start position
3. Object end position
4. Part number
5. Component type (W=WGS contig, N=gap)
6. Component ID or gap length
   7-9. Component coordinates and orientation (for sequences) or gap attributes (for gaps)

---

#### Chromosome List File

A TSV file defining chromosome metadata for genome assemblies.

**Example:**
```
object_name	chromosome_name	chromosome_type	chromosome_location
chr1_seq	1	chromosome	nuclear
chrM_seq	MT	chromosome	mitochondrion
plasmid1	pXYZ	plasmid	nuclear
```

**Columns:**
- `object_name` - Sequence identifier matching FASTA/flatfile entries
- `chromosome_name` - Chromosome designation (1, 2, X, MT, etc.)
- `chromosome_type` - chromosome, plasmid, segment, etc.
- `chromosome_location` - nuclear, mitochondrion, chloroplast, etc.

---

#### Unlocalised List File

A TSV file listing sequences that belong to a chromosome but whose exact location is unknown.

**Example:**
```
object_name	chromosome_name
unlocal_001	1
unlocal_002	1
unlocal_003	X
```

---

### Sequence Types

#### Contig

A **contig** (contiguous sequence) is a continuous stretch of DNA sequence without gaps. It represents the direct output of sequence assembly from overlapping reads.

**Characteristics:**
- No internal gaps (no N runs from assembly)
- Smallest unit of assembled sequence
- Typically has WGS dataclass
- Linear topology only

---

#### Scaffold

A **scaffold** is an ordered and oriented set of contigs connected by gaps of estimated size. Scaffolds represent a higher level of assembly than individual contigs.

**Structure:**
```
[Contig A]---[gap 500bp]---[Contig B]---[gap 200bp]---[Contig C]
```

**Characteristics:**
- Contains multiple contigs joined by gaps
- Gap sizes are estimated (e.g., from paired-end read data)
- Represented in CON dataclass entries
- Uses `assembly_gap` features for gaps
- References component contigs via CO lines or AGP

---

#### Chromosome

A **chromosome** represents the highest level of genome assembly - a complete or near-complete chromosome sequence.

**Characteristics:**
- May be complete (no gaps) or contain gaps
- Can be circular (bacterial chromosomes, plasmids) or linear
- Includes unlocalised sequences assigned to it
- Has chromosome-specific metadata (name, type, location)

---

#### Master Entry

A **master entry** is a special entry that doesn't contain sequence data itself but references other sequences. It serves as a summary record for a set of related sequences (e.g., all contigs in a WGS project).

**Types:**
- **WGS Master** - References all contigs/scaffolds in a WGS project
- **TSA Master** - References all transcripts in a TSA project
- **TLS Master** - References targeted locus sequences

**Example WGS Master:**
```
ID   XXX; SV 1; linear; genomic DNA; SET; PRO; 0 BP.
AC   ABCD00000000;
DE   Organism name, whole genome shotgun sequencing project
KW   WGS.
CO   join(ABCD01000001.1:1..1000,ABCD01000002.1:1..2000,...)
//
```

**Characteristics:**
- Sequence length is 0
- Contains CO lines listing all component accessions
- `contigAccessions` and `scaffoldAccessions` fields store accession ranges
- Dataclass is typically SET

---

### Submission Types

#### Assembly

A standard genome assembly submission containing contigs and/or scaffolds. Used for isolate genomes.

**Components:**
- FASTA or flat file with sequences
- Optional AGP file for scaffold structure
- Optional chromosome list for chromosome-level assemblies

---

#### Genome

A chromosome-level genome assembly with complete chromosome organization.

**Additional Components:**
- Chromosome list file (required)
- Unlocalised list file (optional)
- AGP file for scaffold/chromosome structure

---

#### Template

A submission using predefined templates for specific sequence types (16S rRNA, ITS, COI barcodes, etc.). Uses TSV format with standardized columns.

**Example Template Types:**
- ERT000002 - 16S rRNA
- ERT000020 - ITS region
- ERT000029 - COI barcode

---

#### Transcriptome (TSA)

Transcriptome Shotgun Assembly - a collection of assembled transcript sequences from RNA-seq data.

**Characteristics:**
- Uses TSA dataclass
- Requires TSA keyword
- Has TSA master entry
- Accessions in format: GAAA01000001

---

### Assembly Levels

| Level | Value | Description |
|-------|-------|-------------|
| Contig | 0 | Individual contiguous sequences |
| Scaffold | 1 | Ordered contigs with gaps |
| Chromosome | 2 | Complete/near-complete chromosomes |

---

### Accession Formats

#### Standard (STD) Accessions

Format: `[A-Z]{1,2}[0-9]{5,6}`

**Examples:** `AB123456`, `A12345`

**Used for:** Chromosomes, individual sequences, template submissions

---

#### WGS Accessions

**Old format (4-letter prefix):** `[A-Z]{4}[0-9]{2}[0-9]{6,8}`
- Example: `ABCD01000001`
- Prefix: `ABCD`, Version: `01`, Number: `000001`

**New format (6-letter prefix):** `[A-Z]{6}[0-9]{2}[0-9]{7,9}`
- Example: `ABCDEF01000001`

**Used for:** Contigs and scaffolds in genome assemblies

---

#### TSA Accessions

Same format as WGS but with TSA-specific prefixes.
- Example: `GAAA01000001`

**Used for:** Transcriptome sequences

---

#### ERZ Accessions

Format: `ERZ[0-9]+`
- Example: `ERZ123456`

**Used for:** Analysis records, metagenome sequences

---

### Data Classes

| Class | Full Name | Description |
|-------|-----------|-------------|
| STD | Standard | Standard annotated sequence |
| CON | Constructed | Assembled from other sequences (scaffolds, chromosomes) |
| WGS | Whole Genome Shotgun | Genome assembly contigs |
| TSA | Transcriptome Shotgun Assembly | Assembled transcripts |
| EST | Expressed Sequence Tag | Single-pass cDNA sequences |
| GSS | Genome Survey Sequence | Random genome survey reads |
| TPA | Third Party Annotation | Re-annotation of existing sequences |
| SET | Set | Master entry for a collection |

---

### Features and Qualifiers

#### Source Feature

Every entry must have at least one `source` feature describing the biological source of the sequence.

**Required Qualifiers:**
- `/organism` - Scientific name
- `/mol_type` - Molecule type (genomic DNA, mRNA, etc.)

**Common Qualifiers:**
- `/strain`, `/isolate`, `/clone` - Sample identifiers
- `/country` - Geographic origin
- `/collection_date` - When sample was collected
- `/submitter_seqid` - Submitter's sequence identifier

---

#### Assembly Gap Feature

Represents gaps in assembled sequences where the sequence is unknown.

**Required Qualifiers:**
- `/gap_type` - Type of gap (within scaffold, between scaffolds, etc.)
- `/estimated_length` - Estimated gap size

**Conditional Qualifiers:**
- `/linkage_evidence` - Evidence for gap linkage (required for "within scaffold" gaps)

**Example:**
```
FT   assembly_gap    1001..1100
FT                   /gap_type="within scaffold"
FT                   /linkage_evidence="paired-ends"
FT                   /estimated_length=100
```

---

### Topology

| Value | Description |
|-------|-------------|
| LINEAR | Sequence has distinct ends (most sequences) |
| CIRCULAR | Sequence forms a closed loop (bacterial chromosomes, plasmids, mitochondria) |

**Note:** Contigs and scaffolds must always be LINEAR. Only complete chromosomes/plasmids can be CIRCULAR.

---

### Molecule Types

| Type | Description |
|------|-------------|
| genomic DNA | Nuclear or organellar genomic DNA |
| genomic RNA | RNA genomes (some viruses) |
| mRNA | Messenger RNA |
| tRNA | Transfer RNA |
| rRNA | Ribosomal RNA |
| other RNA | Other RNA types |
| other DNA | Other DNA types |
| transcribed RNA | Generic transcribed RNA |
| viral cRNA | Viral complementary RNA |
| unassigned DNA | Unclassified DNA |
| unassigned RNA | Unclassified RNA |

---

## Core Data Model

### Entry

The `Entry` class is the central domain object representing a sequence entry with all its metadata and biological content.

**Package:** `uk.ac.ebi.embl.api.entry`

**Key Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `primaryAccession` | String | Primary accession number (e.g., AB123456) |
| `submitterAccession` | String | Submitter-provided identifier |
| `version` | Integer | Entry version number |
| `dataClass` | String | Data class (EST, WGS, STD, CON, TPA, TSA, etc.) |
| `division` | String | Taxonomic division |
| `description` | Text | Entry description (DE line) |
| `sequence` | Sequence | Nucleotide/protein sequence data |
| `features` | List&lt;Feature&gt; | Biological annotations (CDS, gene, source, etc.) |
| `references` | List&lt;Reference&gt; | Bibliographic citations |
| `keywords` | List&lt;Text&gt; | Entry keywords |
| `xRefs` | List&lt;XRef&gt; | Cross-references (BioSample, etc.) |

**Key Methods:**

```java
// Accession management
entry.getPrimaryAccession();
entry.setPrimaryAccession("AB123456");
entry.getSubmitterAccession();
entry.setSubmitterAccession("contig_001");

// Sequence access
entry.getSequence();
entry.setSequence(sequence);

// Feature management
entry.getFeatures();
entry.addFeature(feature);
entry.removeFeature(feature);
entry.getPrimarySourceFeature();  // Get main source feature

// Reference management
entry.getReferences();
entry.addReference(reference);

// Status
entry.isMaster();  // Check if entry references other sequences
```

**Data Classes:**

| Class | Description |
|-------|-------------|
| STD | Standard sequence |
| CON | Constructed/assembled sequence |
| WGS | Whole Genome Shotgun |
| TSA | Transcriptome Shotgun Assembly |
| EST | Expressed Sequence Tag |
| TPA | Third Party Annotation |

---

### Sequence

The `Sequence` class contains the actual nucleotide/protein data and related metadata.

**Package:** `uk.ac.ebi.embl.api.entry.sequence`

**Key Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `sequence` | ByteBuffer | Actual sequence bytes |
| `length` | long | Total sequence length |
| `moleculeType` | String | Type (genomic DNA, mRNA, etc.) |
| `topology` | Topology | LINEAR or CIRCULAR |
| `contigs` | List&lt;Location&gt; | Contig locations for CON entries |

**Key Methods:**

```java
sequence.getLength();
sequence.getSequenceByte(position);
sequence.getContigs();               // For master/CON entries
sequence.setVersion(1);
```

---

### Feature

The `Feature` class represents biological annotations within entries.

**Package:** `uk.ac.ebi.embl.api.entry.feature`

**Common Feature Types:**
- `source` - Source organism and sample info
- `CDS` - Coding sequence
- `gene` - Gene annotation
- `mRNA`, `tRNA`, `rRNA`, `ncRNA` - RNA types
- `exon`, `intron` - Gene structure
- `assembly_gap` - Assembly gaps

**Key Methods:**

```java
feature.getName();                    // Feature type (CDS, gene, etc.)
feature.getLocations();               // Feature location on sequence
feature.getQualifiers();              // List of qualifiers
feature.getSingleQualifierValue("gene");  // Get specific qualifier value
```

---

### Qualifier

The `Qualifier` class represents feature qualifiers (key-value annotations).

**Package:** `uk.ac.ebi.embl.api.entry.qualifier`

**Common Qualifiers:**
- `gene` - Gene symbol
- `product` - Product name
- `locus_tag` - Locus identifier
- `translation` - Protein translation
- `submitter_seqid` - Submitter sequence ID
- `organism` - Organism name
- `mol_type` - Molecule type

**Usage:**

```java
// Get qualifier value from source feature
String seqId = entry.getPrimarySourceFeature()
    .getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
```

---

### Location

Location classes represent positions on sequences.

**Package:** `uk.ac.ebi.embl.api.entry.location`

**Types:**
- `Range` - Start..end position
- `Base` - Single position
- `Between` - Gap between positions
- `RemoteLocation` / `RemoteRange` - Locations on other sequences
- `Join` - join(loc1, loc2, ...) compound
- `Order` - order(loc1, loc2, ...) compound

**Usage with Remote Locations:**

```java
// Assign accessions to contigs in scaffold entries
for (Location contig : entry.getSequence().getContigs()) {
    if (contig instanceof RemoteLocation) {
        String contigName = ((RemoteLocation) contig).getAccession();
        ((RemoteLocation) contig).setAccession(newAccession);
    }
}
```

---

## File Readers

### EmblEntryReader

Reads EMBL-format flat files containing sequence entries.

**Package:** `uk.ac.ebi.embl.flatfile.reader.embl`

**Formats:**

| Format | Description |
|--------|-------------|
| `EMBL_FORMAT` | Full EMBL flat file |
| `REDUCED_FILE_FORMAT` | Minimal format (ID, CO, SQ, FH only) |
| `MASTER_FORMAT` | Master entry format |
| `CDS_FORMAT` | CDS-focused format |

**Usage:**

```java
BufferedReader fileReader = Files.newBufferedReader(path);
FlatFileEntryReader reader = new EmblEntryReader(
    fileReader,
    EmblEntryReader.Format.EMBL_FORMAT,
    filePath
);

ValidationResult result = reader.read();
while (reader.isEntry()) {
    if (result.isValid()) {
        Entry entry = reader.getEntry();
        // Process entry
    }
    result = reader.read();
}
```

**How It Works:**

1. Creates Entry and Sequence objects
2. Reads tag-based blocks (ID, AC, DE, FT, SQ, etc.)
3. Dispatches to appropriate BlockReader for each tag
4. Validates block occurrences
5. Returns ValidationResult with entry and any parse messages

---

### FastaFileReader

Reads FASTA format files.

**Package:** `uk.ac.ebi.embl.fasta.reader`

**Usage:**

```java
FastaFileReader reader = new FastaFileReader(new FastaLineReader(fileReader));
reader.read();
while (reader.isEntry()) {
    Entry entry = reader.getEntry();
    // Process entry
    reader.read();
}
```

---

### ChromosomeListFileReader

Reads chromosome list TSV files containing ChromosomeEntry objects.

**Package:** `uk.ac.ebi.embl.flatfile.reader.genomeassembly`

**Usage:**

```java
ChromosomeListFileReader reader = new ChromosomeListFileReader(file);
reader.read();
for (ChromosomeEntry chromosomeEntry : reader.getentries()) {
    // Process chromosome
}
```

---

### UnlocalisedListFileReader

Reads unlocalised sequence list files.

**Package:** `uk.ac.ebi.embl.flatfile.reader.genomeassembly`

**Usage:**

```java
UnlocalisedListFileReader reader = new UnlocalisedListFileReader(file);
reader.read();
for (UnlocalisedEntry entry : reader.getentries()) {
    // Process unlocalised entry
}
```

---

## File Writers

### EmblEntryWriter

Writes Entry objects to EMBL flat file format (80-character wrapped lines).

**Package:** `uk.ac.ebi.embl.flatfile.writer.embl`

**Output Structure:**
```
ID   entry_name; ...
XX
AC   accession;
XX
DE   description
XX
KW   keywords
XX
OS   organism
OC   classification
XX
RN   [1]
...references...
XX
FH   Key             Location/Qualifiers
FT   source          1..1000
FT                   /organism="..."
FT   CDS             100..500
...more features...
XX
SQ   Sequence 1000 BP; 250 A; 250 C; 250 G; 250 T; 0 other;
     acgtacgt...
//
```

**Usage:**

```java
Entry entry = ...;
Writer writer = new PrintWriter(outputPath);
new EmblEntryWriter(entry).write(writer);
```

---

### EmblReducedFlatFileWriter

Writes Entry objects to reduced EMBL format (minimal metadata for archival).

**Package:** `uk.ac.ebi.embl.flatfile.writer.embl`

**Output Structure:**
```
ID   entry_name; ...
FT   source          1..1000
FT                   /submitter_seqid="contig_001"
SQ   Sequence 1000 BP; ...
     acgtacgt...
//
```

**Characteristics:**
- Removes all non-source features
- Source feature limited to submitter_seqid qualifier only
- Used for reduced flatfiles in archival

**Usage:**

```java
new EmblReducedFlatFileWriter(entry).write(writer);
```

---

### FastaFileWriter

Writes Entry objects to FASTA format.

**Package:** `uk.ac.ebi.embl.fasta.writer`

**Header Formats:**

| Format | Example |
|--------|---------|
| DEFAULT | `>EM_DIV:ACC SEQ:ACC CLASS:DESC` |
| ANALYSIS | `>ACC SUBMITTER_ACC` |
| ENA | `>ENA\|ACC\|SEQACC DESC` |
| POLYSAMPLE | Custom polysample format |

**Usage:**

```java
new FastaFileWriter(
    entry,
    writer,
    FastaFileWriter.FastaHeaderFormat.POLYSAMPLE_HEADER_FORMAT
).write();
```

---

## Validation Framework

### Architecture Overview

```
SubmissionValidator (Entry Point)
        │
        ▼
SubmissionValidationPlan (Orchestration)
        │
        ├── File-Level Validation
        │   └── FileValidationCheck implementations
        │
        └── Entry-Level Validation
            │
            ▼
        EmblEntryValidationPlan
            │
            ├── EntryValidationCheck (70+ checks)
            ├── FeatureValidationCheck (60+ checks)
            └── SequenceValidationCheck
```

---

### SubmissionValidator

Main entry point for validation that orchestrates the complete validation process.

**Package:** `uk.ac.ebi.embl.api.validation.submission`

**Usage:**

```java
SubmissionOptions options = new SubmissionOptions();
options.context = Optional.of(Context.genome);
options.analysisId = Optional.of("ERZ123456");
options.submissionFiles = Optional.of(submissionFiles);
options.processDir = Optional.of(processDirectory);
options.reportDir = Optional.of(reportDirectory);
options.isFixMode = true;

SubmissionValidator validator = new SubmissionValidator(options);
try {
    validator.validate();
} catch (ValidationEngineException ex) {
    if (ex.getErrorType() == ErrorType.VALIDATION_ERROR) {
        // User data error - can be fixed
    } else {
        // System error
    }
}
```

---

### SubmissionOptions

Configuration container for validation runs.

**Package:** `uk.ac.ebi.embl.api.validation.submission`

**Key Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `context` | Context | Validation context (genome, transcriptome, sequence, etc.) |
| `analysisId` | String | Analysis identifier |
| `submissionFiles` | SubmissionFiles | Files to validate |
| `processDir` | File | Working directory |
| `reportDir` | File | Report output directory |
| `isFixMode` | boolean | Enable auto-fix of errors |
| `ignoreErrors` | boolean | Continue despite errors |
| `assemblyInfoEntry` | AssemblyInfoEntry | Assembly metadata |

**Context Types:**

| Context | Description |
|---------|-------------|
| `genome` | Genome assembly validation |
| `transcriptome` | TSA validation |
| `sequence` | Standard sequence/template validation |
| `polysample_fasta_sample` | Polysample with FASTA + sample TSV |
| `polysample_full` | Polysample with FASTA + sample + taxonomy |
| `polysample_tax` | Polysample taxonomy only |

---

### ValidationResult

Container for validation messages returned from checks.

**Package:** `uk.ac.ebi.embl.api.validation`

**Key Methods:**

```java
ValidationResult result = reader.read();

result.isValid();                           // true if no ERROR messages
result.count(Severity.ERROR);               // Count errors
result.count(Severity.WARNING);             // Count warnings
result.getMessages(Severity.ERROR);         // Get error messages
result.getMessages("CheckId", Severity.ERROR);  // Get specific check errors
result.append(otherResult);                 // Combine results
result.writeMessages(writer);               // Write formatted output
```

---

### ValidationCheck

Base interface for all validation checks.

**Package:** `uk.ac.ebi.embl.api.validation`

**Check Hierarchy:**

```
ValidationCheck<E>
├── EmblEntryValidationCheck<E>
│   ├── EntryValidationCheck        (validates Entry objects)
│   ├── FeatureValidationCheck      (validates Feature objects)
│   ├── SequenceValidationCheck     (validates Sequence objects)
│   └── GenomeAssemblyValidationCheck<GCSEntry>
└── FileValidationCheck             (validates entire files)
```

**Check Execution Flow:**

1. Check instantiated with validation properties
2. `init()` called to setup resources
3. `check(object)` invoked with entity to validate
4. Check inspects object, calls `reportError()` / `reportWarning()` as needed
5. Returns `ValidationResult` with collected messages

---

### Key Entry-Level Checks

| Check | Description |
|-------|-------------|
| DataclassCheck | Validates dataclass against allowed values |
| MoleculeTypeAndDataclassCheck | Cross-validates molecule type with dataclass |
| AssemblyTopologyCheck | Ensures contigs/scaffolds are linear |
| Assembly_gapFeatureCheck | Validates assembly_gap features |
| WGSGapCheck | Validates WGS-specific gap features |
| ReferenceCheck | Validates literature references |

---

### Key Feature-Level Checks

| Check | Description |
|-------|-------------|
| CdsFeatureTranslationCheck | Validates CDS translation |
| FeatureLocationCheck | Validates location format and ranges |
| FeatureQualifiersRequiredCheck | Enforces mandatory qualifiers |
| QualifierCheck | Validates qualifier values |
| EC_numberFormatCheck | Validates EC number format |
| DeprecatedQualifiersCheck | Warns about deprecated qualifiers |

---

### Severity Levels

| Severity | Description |
|----------|-------------|
| `ERROR` | Validation failure, submission rejected |
| `WARNING` | Advisory, submission proceeds |
| `INFO` | Informational message |
| `FIX` | Auto-correction applied |

---

### ValidationEngineException

Exception thrown during validation with error type classification.

**Package:** `uk.ac.ebi.embl.api.validation`

**Error Types:**

| Type | Description |
|------|-------------|
| `VALIDATION_ERROR` | User data error - can be fixed by user |
| `SYSTEM_ERROR` | System/infrastructure error |

**Handling:**

```java
try {
    validator.validate();
} catch (ValidationEngineException ex) {
    switch (ex.getErrorType()) {
        case VALIDATION_ERROR:
            // Report to user for correction
            break;
        case SYSTEM_ERROR:
            // Log and alert operations
            break;
    }
}
```

---

## Genome Assembly Classes

### AssemblyInfoEntry

Container for assembly metadata.

**Package:** `uk.ac.ebi.embl.api.entry.genomeassembly`

**Key Fields:**
- Assembly name
- Assembly type
- Project ID
- Coverage
- Program/Platform

**Usage:**

```java
AssemblyInfoEntry info = eraService.getAssemblyInfo(analysisId);
options.assemblyInfoEntry = Optional.of(info);
```

---

### AssemblyType

Enumeration of assembly classification types.

**Package:** `uk.ac.ebi.embl.api.entry.genomeassembly`

| Type | Description |
|------|-------------|
| CLONE_OR_ISOLATE | Standard isolate assembly |
| PRIMARY_METAGENOME | Environmental metagenomic assembly |
| BINNED_METAGENOME | Binned metagenomic assembly (MAG) |
| CLINICAL_ISOLATE_ASSEMBLY | Clinical isolate |
| COVID_19_OUTBREAK | COVID-19 specific |

---

### ChromosomeEntry

Represents a chromosome with accession, name, and type information.

**Package:** `uk.ac.ebi.embl.api.entry.genomeassembly`

**Key Fields:**
- Object name
- Chromosome name
- Chromosome type
- Chromosome location
- Assigned accession

---

### UnlocalisedEntry

Represents unlocalised sequences (not assigned to specific chromosomes).

**Package:** `uk.ac.ebi.embl.api.entry.genomeassembly`

**Key Methods:**

```java
entry.getObjectName();    // Get sequence name
entry.setAcc(accession);  // Set assigned accession
```

---

## Usage Examples

### Reading and Processing Entries

```java
// Read EMBL flat file
BufferedReader fileReader = Files.newBufferedReader(inputPath);
FlatFileEntryReader reader = new EmblEntryReader(
    fileReader,
    EmblEntryReader.Format.EMBL_FORMAT,
    inputPath.toString()
);

ValidationResult result = reader.read();
while (reader.isEntry()) {
    if (result.isValid()) {
        Entry entry = reader.getEntry();
        
        // Get submitter sequence ID
        String seqId = entry.getPrimarySourceFeature()
            .getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
        
        // Assign accession
        entry.setPrimaryAccession("AB123456");
        entry.getSequence().setVersion(1);
        
        // Write to output
        new EmblEntryWriter(entry).write(outputWriter);
    } else {
        // Handle validation errors
        for (ValidationMessage msg : result.getMessages(Severity.ERROR)) {
            System.err.println(msg);
        }
    }
    result = reader.read();
}
```

### Running Validation

```java
// Configure validation
SubmissionOptions options = new SubmissionOptions();
options.context = Optional.of(Context.genome);
options.analysisId = Optional.of("ERZ123456");
options.submissionFiles = Optional.of(submissionFiles);
options.processDir = Optional.of(new File("/path/to/process"));
options.reportDir = Optional.of(new File("/path/to/reports"));
options.isFixMode = true;

// Run validation
try {
    SubmissionValidator validator = new SubmissionValidator(options);
    validator.validate();
    // Validation passed
} catch (ValidationEngineException ex) {
    if (ex.getErrorType() == ErrorType.VALIDATION_ERROR) {
        // User can fix this error
        System.err.println("Validation failed: " + ex.getMessage());
    } else {
        // System error - needs investigation
        throw ex;
    }
}
```

### Assigning Accessions to Scaffold Contigs

```java
// Read scaffold entry
Entry entry = reader.getEntry();

// Assign accessions to contigs within scaffold
for (Location contig : entry.getSequence().getContigs()) {
    if (contig instanceof Gap) {
        continue;  // Skip gaps
    }
    if (contig instanceof RemoteLocation) {
        String contigName = ((RemoteLocation) contig).getAccession();
        String newAccession = accessionMap.get(contigName.toUpperCase());
        ((RemoteLocation) contig).setAccession(newAccession);
    }
}

// Write updated entry
new EmblReducedFlatFileWriter(entry).write(outputWriter);
```

### Writing Reduced Flat Files

```java
// Read full entry
FlatFileEntryReader reader = new EmblEntryReader(
    fileReader,
    EmblEntryReader.Format.REDUCED_FILE_FORMAT,
    filePath
);

reader.read();
while (reader.isEntry()) {
    Entry entry = reader.getEntry();
    
    // Set submitter accession from qualifier
    String entryName = entry.getPrimarySourceFeature()
        .getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
    entry.setSubmitterAccession(entryName);
    
    // Set primary accession
    entry.setPrimaryAccession(accessionMap.get(entryName));
    entry.getSequence().setVersion(1);
    
    // Write reduced format
    new EmblReducedFlatFileWriter(entry).write(outputWriter);
    
    reader.read();
}
```

---

## Supporting Classes

### SubmissionFiles

Container for multiple submission files with filtering capabilities.

**Package:** `uk.ac.ebi.embl.api.validation.submission`

```java
submissionFiles.getFiles();                          // All files
submissionFiles.getFiles(FileType.FASTA);            // Filter by type
submissionFiles.getFiles(FileType.FLATFILE);
submissionFiles.getFiles(FileType.CHROMOSOME_LIST);
submissionFiles.getFiles(FileType.UNLOCALISED_LIST);
submissionFiles.getFiles(FileType.AGP);
```

### AssemblySequenceInfo

Stores sequence metadata and serializes to files.

**Package:** `uk.ac.ebi.embl.api.entry`

```java
// Write sequence info to file
Map<String, AssemblySequenceInfo> sequenceInfo = ...;
AssemblySequenceInfo.writeMapObject(
    sequenceInfo,
    processDir,
    AssemblySequenceInfo.sequencefileName
);

// Read sequence info from file
Map<String, AssemblySequenceInfo> loaded = 
    (Map<String, AssemblySequenceInfo>) AssemblySequenceInfo.getObject(
        processDir,
        AssemblySequenceInfo.sequencefileName
    );
```

### AccessionMatcher

Parses and matches accession number patterns.

**Package:** `uk.ac.ebi.embl.api`

```java
AccessionMatcher.Accession acc = 
    AccessionMatcher.getSplittedAccession("AAAA01000001");

String prefix = acc.prefix;   // "AAAA"
String version = acc.version; // "01"
String number = acc.number;   // "000001"

// Check accession type
if (AccessionMatcher.isWgsSeqAccession(accession)) {
    // WGS accession
}
```

---

## License

Copyright 2015-2023 EMBL - European Bioinformatics Institute

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
