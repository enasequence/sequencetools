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
package uk.ac.ebi.embl.api.translation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class SimpleTranslatorTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testValidTranslation1() throws ValidationException {
    String sequence = "nacgtaaaacccggttaaccggtcacaagtgcatcgatcgnn";
    SimpleTranslator translator = new SimpleTranslator();
    translator.setTranslationTable(1);
    ExtendedResult<TranslationResult> extendedResult = translator.translate(sequence.getBytes());
    TranslationResult translationResult = extendedResult.getExtension();
    assertEquals("XVKPG*PVTSASIX", translationResult.getTranslation());
    assertEquals("XVKPG*PVTSASIX", translationResult.getConceptualTranslation());
  }

  @Test
  public void testValidTranslation2() throws ValidationException {
    String sequence = "cgtaaaacccggttaaccggtcacaagtgcatcgatcgn";
    SimpleTranslator translator = new SimpleTranslator();
    translator.setTranslationTable(1);
    ExtendedResult<TranslationResult> extendedResult = translator.translate(sequence.getBytes());
    TranslationResult translationResult = extendedResult.getExtension();
    assertEquals("RKTRLTGHKCIDR", translationResult.getTranslation());
    assertEquals("RKTRLTGHKCIDR", translationResult.getConceptualTranslation());
  }
}
