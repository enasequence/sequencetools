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
package uk.ac.ebi.embl.api.validation.check.file;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.mapdb.Serializer;
import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;

@Description("")
public class AGPFileValidationCheck extends FileValidationCheck {

  public AGPFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {
    ValidationPlan validationPlan;
    ValidationResult validationResult = new ValidationResult();
    fixedFileWriter = null;
    Origin origin = null;
    ConcurrentMap annotationMap = null;
    if (sharedInfo.hasAnnotationOnlyFlatfile) {
      if (sharedInfo.annotationDB == null) {
        throw new ValidationEngineException(
            "Annotations are not parsed and stored in lookup db.",
            ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
      } else {
        annotationMap = sharedInfo.annotationDB.hashMap("map").createOrOpen();
      }
    }
    try (BufferedReader fileReader = CommonUtil.bufferedReaderFromFile(submissionFile.getFile());
        PrintWriter fixedFileWriter = getFixedFileWriter(submissionFile)) {
      clearReportFile(getReportFile(submissionFile));
      if (!validateFileFormat(
          submissionFile.getFile(),
          uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.AGP)) {
        addErrorAndReport(validationResult, submissionFile, "InvalidFileFormat", "AGP");
        return validationResult;
      }
      AGPFileReader reader = new AGPFileReader(new AGPLineReader(fileReader));
      HashMap<String, AssemblySequenceInfo> contigInfo = new HashMap<>();
      contigInfo.putAll(
          AssemblySequenceInfo.getMapObject(
              options.processDir.get(), AssemblySequenceInfo.fastafileName));
      contigInfo.putAll(
          AssemblySequenceInfo.getMapObject(
              options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
      if (contigInfo.isEmpty()) {
        addErrorAndReport(validationResult, submissionFile, "ContigInfoMissing");
        return validationResult;
      }
      ValidationResult parseResult = reader.read();
      validationResult.append(parseResult);
      getOptions()
          .getEntryValidationPlanProperty()
          .fileType
          .set(uk.ac.ebi.embl.api.validation.FileType.AGP);
      while (reader.isEntry()) {
        if (!parseResult.isValid()) {
          getReporter().writeToFile(getReportFile(submissionFile), parseResult);
          addMessageStats(parseResult.getMessages());
        }

        Entry entry = reader.getEntry();
        origin = entry.getOrigin();

        // set validation scope and collect unplacedEntries
        getOptions()
            .getEntryValidationPlanProperty()
            .validationScope
            .set(getValidationScope(entry.getSubmitterAccession()));
        Sequence.Topology chrListToplogy = getTopology(entry.getSubmitterAccession());
        if (chrListToplogy != null) {
          entry.getSequence().setTopology(chrListToplogy);
        }
        // level 2 placed entries should be removed from unplaced set
        if (!sharedInfo.unplacedEntryNames.isEmpty()) {
          for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
            if (agpRow.getComponent_type_id() != null
                && !agpRow.getComponent_type_id().equalsIgnoreCase("N")
                && agpRow.getComponent_id() != null) {
              sharedInfo.unplacedEntryNames.remove(agpRow.getComponent_id().toUpperCase());
            }
          }
        }

        if (sharedInfo.hasAnnotationOnlyFlatfile) {
          Entry annoationEntry =
              (Entry) annotationMap.get(entry.getSubmitterAccession().toUpperCase());
          if (annoationEntry != null) {
            String molType = null;
            if (annoationEntry.getSequence() != null
                && annoationEntry.getSequence().getMoleculeType() != null) {
              molType = annoationEntry.getSequence().getMoleculeType();
            }
            annoationEntry.setSequence(entry.getSequence());
            entry = annoationEntry;
            if (molType != null) {
              entry.getSequence().setMoleculeType(molType);
            }
          }
        } else {
          appendHeader(entry);
          addSubmitterSeqIdQual(entry);
        }

        getOptions().getEntryValidationPlanProperty().assemblySequenceInfo.set(contigInfo);
        getOptions()
            .getEntryValidationPlanProperty()
            .sequenceNumber
            .set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get() + 1);
        validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
        ValidationResult planResult = validationPlan.execute(entry);
        validationResult.append(planResult);

        if (null != entry.getSubmitterAccession()) {
          addEntryName(entry.getSubmitterAccession());
          int assemblyLevel =
              getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
          AssemblySequenceInfo sequenceInfo =
              new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
          sharedInfo.agpInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
          contigInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
        }

        if (!planResult.isValid()) {
          getReporter().writeToFile(getReportFile(submissionFile), planResult);
          addMessageStats(planResult.getMessages());
        } else {
          assignProteinAccessionAndWriteToFile(entry, fixedFileWriter, submissionFile, true);
        }
        parseResult = reader.read();
        validationResult.append(parseResult);
      }

    } catch (ValidationEngineException vee) {
      getReporter()
          .writeToFile(getReportFile(submissionFile), Severity.ERROR, vee.getMessage(), origin);
      throw vee;
    } catch (Exception e) {
      getReporter()
          .writeToFile(getReportFile(submissionFile), Severity.ERROR, e.getMessage(), origin);
      throw new ValidationEngineException(e.getMessage(), e);
    }
    if (validationResult.isValid()) registerAGPfileInfo();
    return validationResult;
  }

  @Override
  public ValidationResult check() {
    throw new UnsupportedOperationException();
  }

  public void createContigDB() throws ValidationEngineException {
    for (SubmissionFile submissionFile : options.submissionFiles.get().getFiles(FileType.AGP)) {
      try (BufferedReader fileReader =
          CommonUtil.bufferedReaderFromFile(submissionFile.getFile())) {
        AGPFileReader reader = new AGPFileReader(new AGPLineReader(fileReader));
        ValidationResult result = reader.read();

        while (reader.isEntry()) {
          if (result.isValid()) {
            Entry entry = reader.getEntry();
            addAgpEntryName(entry.getSubmitterAccession().toUpperCase());

            for (AgpRow agpRow : entry.getSequence().getSortedAGPRows()) {
              if (!agpRow.isGap()) {
                if (agpRow.getComponent_id() != null && sharedInfo.contigDB != null) {
                  // <componentAGPRowsMap> is to group which component placed where. If one
                  // component(let's say contig1) contig is placed in multiple scaffolds,
                  // this map will contain all the scaffolds where that component(contig1) has been
                  // placed. K<contig1> V<all scaffolds in AGPROW format>
                  ConcurrentMap<String, Object> componentAGPRowsMap =
                      sharedInfo
                          .contigDB
                          .hashMap(
                              "map", Serializer.STRING, sharedInfo.contigDB.getDefaultSerializer())
                          .createOrOpen();
                  List<AgpRow> agpRows =
                      (List<AgpRow>)
                          componentAGPRowsMap.get(agpRow.getComponent_id().toLowerCase());
                  if (agpRows == null) {
                    agpRows = new ArrayList<>();
                  }
                  agpRows.add(agpRow);
                  componentAGPRowsMap.put(agpRow.getComponent_id().toLowerCase(), agpRows);
                  sharedInfo.agpPlacedComponents.add(agpRow.getComponent_id().toUpperCase());
                }
              }
            }
          } else {
            String ex = "";
            for (ValidationMessage<Origin> msg : result.getMessages(Severity.ERROR)) {
              ex += msg + " ";
            }
            throw new ValidationEngineException(
                ex, ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
          }
          result = reader.read();
        }

        if (sharedInfo.contigDB != null) sharedInfo.contigDB.commit();
      } catch (ValidationEngineException e) {
        throw e;
      } catch (Exception e) {
        throw new ValidationEngineException(e);
      }
    }
  }

  private void registerAGPfileInfo() throws ValidationEngineException {
    AssemblySequenceInfo.writeMapObject(
        sharedInfo.agpInfo, options.processDir.get(), AssemblySequenceInfo.agpfileName);
  }
}
