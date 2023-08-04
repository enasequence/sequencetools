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
package uk.ac.ebi.embl.api.validation.submission;

import java.util.Arrays;
import java.util.List;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public enum Context {
  sequence(FileType.FLATFILE, FileType.TSV),
  transcriptome(FileType.FASTA, FileType.FLATFILE, FileType.MASTER),
  genome(
      FileType.FASTA,
      FileType.FLATFILE,
      FileType.AGP,
      FileType.CHROMOSOME_LIST,
      FileType.UNLOCALISED_LIST,
      FileType.MASTER,
      FileType.ANNOTATION_ONLY_FLATFILE);

  List<FileType> fileTypes;

  private Context(FileType... fileTypes) {
    this.fileTypes = Arrays.asList(fileTypes);
  }

  public List<FileType> getFileTypes() {
    return fileTypes;
  }

  public Context getContext(String context) {
    try {
      return Context.valueOf(context);
    } catch (Exception e) {
      return null;
    }
  }
}
