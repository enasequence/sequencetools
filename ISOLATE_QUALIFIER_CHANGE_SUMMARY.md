# Isolate Qualifier Change Summary

These are code-path call stacks, not exception stack traces.

## Conclusion

`/isolate` can change during processing in this project.

This does **not** happen via a dedicated isolate-to-isolate remapping table.
It happens because the source feature is rebuilt/replaced from other metadata, or because fixers add or rewrite source qualifiers.

## Scenarios

### 1. Master source replacement during non-`sequence` entry processing

Effect:
- A submitted entry-level `/isolate` can disappear or be replaced by the master source qualifiers.

Main callers that reach this path:
- `FlatfileFileValidationCheck.check(...)`
- `FastaFileValidationCheck.check(...)`
- `AGPFileValidationCheck.check(...)`
- `AnnotationOnlyFlatfileValidationCheck.check(...)`
- `TSVFileValidationCheck.check(...)`

Call stack:
- `SubmissionValidator.validate()`
- `SubmissionValidationPlan.execute()`
- `SubmissionValidationPlan.createMaster()`
- `MasterEntryValidationCheck.check()`
- `MasterEntryService.createMasterEntry(...)`
- `MasterEntryService.getMasterEntryFromWebinCli(...)`
- `SubmissionValidationPlan.validateFlatfile()` or another file validator above
- `...FileValidationCheck.check(...)`
- `FileValidationCheck.appendHeader(...)`
- `FileValidationCheck.addSourceQualifiers(...)`
- `entry.getPrimarySourceFeature().removeAllQualifiers()`
- copy qualifiers from `sharedInfo.masterEntry.getPrimarySourceFeature()`

Key code:
- `src/main/java/uk/ac/ebi/embl/api/validation/check/file/FileValidationCheck.java:335`
- `src/main/java/uk/ac/ebi/embl/api/validation/check/file/FileValidationCheck.java:538`
- `src/main/java/uk/ac/ebi/embl/api/service/MasterEntryService.java:122`

### 2. Auto-added isolate from sample name during source construction

Effect:
- `/isolate=<sample name>` can be added for prokaryotic samples when the source has no `environmental_sample`, `strain`, or `isolate`.

Call stack:
- `SubmissionValidator.validate(Manifest)`
- `SubmissionValidator.mapManifestToSubmissionOptions(...)`
- `SourceFeatureUtils.constructSourceFeature(...)`
- `SourceFeatureUtils.addQualifiers(...)`
- `SourceFeatureUtils.addExtraSourceQualifiers(...)`

Key code:
- `src/main/java/uk/ac/ebi/embl/api/validation/submission/SubmissionValidator.java:147`
- `src/main/java/uk/ac/ebi/embl/api/validation/helper/SourceFeatureUtils.java:106`
- `src/main/java/uk/ac/ebi/embl/api/validation/helper/SourceFeatureUtils.java:183`

### 3. Template entry source updated from resolved sample

Effect:
- If a template row resolves to a BioSample, the existing source feature can be updated from sample metadata, including isolate-related qualifiers.

Call stack:
- `TemplateEntryProcessor.updateSourceFeatureUsingOrganismFieldValue(...)`
- `TemplateEntryProcessor.updateSourceFeature(...)`
- `SourceFeatureUtils.updateSourceFeature(...)`
- `SourceFeatureUtils.addQualifiers(...)`
- `SourceFeatureUtils.addExtraSourceQualifiers(...)`

Key code:
- `src/main/java/uk/ac/ebi/embl/template/TemplateEntryProcessor.java:505`
- `src/main/java/uk/ac/ebi/embl/template/TemplateEntryProcessor.java:568`
- `src/main/java/uk/ac/ebi/embl/api/validation/helper/SourceFeatureUtils.java:147`

### 4. Source fixer: remove `strain`, add `/isolate="unknown"`

Effect:
- If `environmental_sample` exists and `strain` exists, `strain` is removed.
- If no isolate exists at that point, `/isolate="unknown"` is added.

Call stack:
- `...FileValidationCheck.check(...)`
- `EmblEntryValidationPlan.execute(...)`
- `ValidationUnit.SEQUENCE_ENTRY_FIXES` or `ValidationUnit.SOURCE_FEATURE_FIXES`
- `SourceQualifierMissingFix.check(...)`

Key code:
- `src/main/java/uk/ac/ebi/embl/api/validation/plan/EmblEntryValidationPlan.java:37`
- `src/main/java/uk/ac/ebi/embl/api/validation/plan/ValidationUnit.java:55`
- `src/main/java/uk/ac/ebi/embl/api/validation/plan/ValidationUnit.java:164`
- `src/main/java/uk/ac/ebi/embl/api/validation/fixer/sourcefeature/SourceQualifierMissingFix.java:180`

### 5. Generic qualifier value rewrite also applies to `isolate`

Effect:
- Any qualifier value containing `"` is rewritten to `'`.
- If an isolate value contains double quotes, its value changes here.

Call stack:
- `...FileValidationCheck.check(...)`
- `EmblEntryValidationPlan.execute(...)`
- `ValidationUnit.SEQUENCE_ENTRY_FIXES` or `ValidationUnit.SOURCE_FEATURE_FIXES`
- `QualifierValueFix.check(...)`

Key code:
- `src/main/java/uk/ac/ebi/embl/api/validation/fixer/feature/QualifierValueFix.java:42`

## Non-scenario

I did **not** find an isolate-specific remapping table entry in:
- `src/main/resources/uk/ac/ebi/embl/api/validation/data/qualifier-value-to-fix-value.tsv`

So there is no evidence here of a dedicated rule like:
- submitted isolate value A -> canonical isolate value B

## Tests Added / Relevant

Added now:
- `SourceFeatureUtilsTest.testConstructSourceFeatureAddsIsolateFromSampleNameForProkaryote`
- `SourceFeatureUtilsTest.testConstructSourceFeatureDoesNotAddIsolateWhenStrainExists`
- `FlatfileFileValidationCheckTest.testAppendHeaderReplacesSubmittedIsolateWithMasterSource`

Already present:
- `SourceQualifierMissingFixTest.testCheckSourceWithStrainAndEnvSample`
- `SourceQualifierMissingFixTest.testCheckSourceWithStrainAndEnvSampleAndIsolate`
