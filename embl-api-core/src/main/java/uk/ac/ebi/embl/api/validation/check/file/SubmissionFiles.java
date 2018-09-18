/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.ac.ebi.embl.api.validation.check.file;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toCollection;

public class SubmissionFiles {

    private List<SubmissionFile> files = new ArrayList<>();

    public void addFile(SubmissionFile file) {
        files.add(file);
    }

    public List<SubmissionFile> getFiles() {
        return files;
    }

    public List<SubmissionFile> getFiles(SubmissionFile.FileType fileType) {
        return files
            .stream()
            .filter(f -> fileType.equals(f.getFileType()))
                .collect(toCollection(ArrayList::new));
    }

    public void clear() {
        files.clear();
    }
}