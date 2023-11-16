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
package uk.ac.ebi.embl.api.validation.file;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.UnlocalisedListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class UnlocalisedListFileValidationCheckTest extends SubmissionValidationTest {

  @Before
  public void init() throws SQLException {
    options = new SubmissionOptions();
    options.context = Optional.of(Context.genome);
    options.isWebinCLI = true;
  }

  @Test
  public void testvalidUnlocalisedList() throws ValidationEngineException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionTestFile("unlocalised_list.txt", SubmissionFile.FileType.UNLOCALISED_LIST);
    UnlocalisedListFileValidationCheck check =
        new UnlocalisedListFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testInvalidUnlocalisedList() throws ValidationEngineException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionTestFile(
            "invalid_unlocalised_list.txt", SubmissionFile.FileType.UNLOCALISED_LIST);
    UnlocalisedListFileValidationCheck check =
        new UnlocalisedListFileValidationCheck(options, sharedInfo);
    assertFalse(check.check(file).isValid());
    assertNotNull(check.getMessageStats().get("InvalidNoOfFields"));
  }
}
