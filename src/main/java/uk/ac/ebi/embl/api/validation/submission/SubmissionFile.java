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
package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;

public class SubmissionFile {

  public enum FileType {
    ASSEMBLY_INFO,
    FASTA,
    FLATFILE,
    AGP,
    TSV,
    SAMPLE_TSV,
    TAX_TSV,
    CHROMOSOME_LIST,
    UNLOCALISED_LIST,
    MASTER,
    ANNOTATION_ONLY_FLATFILE,
    GFF3
  }

  private final FileType fileType;
  private final File file;
  private final File fixedFile;
  private final File reportFile;

  public SubmissionFile(FileType fileType, File file) {
    this.fileType = fileType;
    this.file = file;
    this.fixedFile = null;
    this.reportFile = null;
  }

  public SubmissionFile(FileType fileType, File file, File fixedFile) {
    this.fileType = fileType;
    this.file = file;
    this.fixedFile = fixedFile;
    this.reportFile = null;
  }

  public SubmissionFile(FileType fileType, File file, File fixedFile, File reportFile) {
    this.fileType = fileType;
    this.file = file;
    this.fixedFile = fixedFile;
    this.reportFile = reportFile;
  }

  public FileType getFileType() {
    return fileType;
  }

  public File getFile() {
    return file;
  }

  public File getFixedFile() {
    return fixedFile;
  }

  public boolean createFixedFile() {
    return fixedFile != null;
  }

  public File getReportFile() {
    return reportFile;
  }

  public boolean createReportFile() {
    return reportFile != null;
  }
}
