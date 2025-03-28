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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

public class AssemblyInfoSubmissionIdCheckTest {
  //	private AssemblyInfoEntry assemblyEntry;
  //	private AssemblyInfoValidationheck check;
  //	private EmblEntryValidationPlanProperty planProperty;
  //
  //	@Before
  //	public void setUp() throws SQLException
  //	{
  //		check = new AssemblyInfoValidationheck();
  //		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
  //        EntryDAOUtils entryDAOUtils = createMock(EntryDAOUtils.class);
  //		planProperty=TestHelper.testEmblEntryValidationPlanProperty();
  //		planProperty.analysis_id.set("ERZ0001");
  //		check = new AssemblyInfoValidationheck();
  //		check.setEmblEntryValidationPlanProperty(planProperty);
  //	}
  //
  //	@Test
  //	public void testCheck_NoEntry() throws ValidationEngineException
  //	{
  //		assertTrue(check.check(null).isValid());
  //	}
  //
  //	@Test
  //	public void testCheck_NoMandatory() throws ValidationEngineException
  //	{
  //		ValidationResult result = check.check(assemblyEntry);
  //		assertEquals(1, result.count("AssemblyFieldandValueCheck-2", Severity.ERROR));
  //	}
  //
  //	@Test
  //	public void testCheck_InvalidField() throws ValidationEngineException
  //	{
  //		Field field2 = new Field("assbly_name", "cb4");
  //		gaRecord.addField(field2);
  //		ValidationResult result = check.check(gaRecord);
  //		assertEquals(1, result.count("AssemblyFieldandValueCheck-1", Severity.ERROR));
  //	}
  //
  //	@Test
  //	public void testCheck_InvalidFinishing_goal() throws ValidationEngineException
  //	{
  //		Field field2 = new Field("finishing_goal", "fgfhh");
  //		gaRecord.addField(field2);
  //		ValidationResult result = check.check(gaRecord);
  //		assertEquals(0, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
  //	}
  //
  //	@Test
  //	@Ignore
  //	public void testCheck_validRecord() throws SQLException, ValidationEngineException
  //	{
  //		Field field2 = new Field("project", "PRJEA84437");
  //		gaRecord.addField(field2);
  //		assertTrue(check.check(gaRecord).isValid());
  //
  //	}
  //
  //	@Test
  //	public void testCheck_InvalidReleaseDate() throws ValidationEngineException
  //	{
  //		Field field2 = new Field("release_date", "12-MON-34");
  //		gaRecord.addField(field2);
  //		ValidationResult result = check.check(gaRecord);
  //		assertEquals(1, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
  //	}
  //
  //	@Test
  //	public void testCheck_InvalidVersion() throws ValidationEngineException
  //	{
  //		Field field2 = new Field("assembly_version", "er");
  //		gaRecord.addField(field2);
  //		ValidationResult result = check.check(gaRecord);
  //		assertEquals(0, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
  //	}

}
