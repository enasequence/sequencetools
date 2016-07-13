/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.embl.api.genomeassembly.AssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyFieldandValueCheckTest
{

	private GenomeAssemblyRecord gaRecord;
	private AssemblyFieldandValueCheck check;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		DataRow dataRow1 = new DataRow("assembly_name");
		DataRow dataRow2 = new DataRow("project");
		DataRow dataRow3 = new DataRow("assembly_version");
		DataRow dataRow4 = new DataRow("finishing_goal");
		DataRow dataRow5 = new DataRow("release_date");

		DataSet keySet = new DataSet();
		keySet.addRow(dataRow1);
		keySet.addRow(dataRow2);
		keySet.addRow(dataRow3);
		keySet.addRow(dataRow4);
		keySet.addRow(dataRow5);
		gaRecord = new AssemblyRecord();
		Field field1 = new Field("assembly_name", "cb4");
		gaRecord.addField(field1);
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		check = new AssemblyFieldandValueCheck(keySet);
		check.setEmblEntryValidationPlanProperty(property);

	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMandatory() throws ValidationEngineException
	{
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("AssemblyFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_InvalidField() throws ValidationEngineException
	{
		Field field2 = new Field("assbly_name", "cb4");
		gaRecord.addField(field2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("AssemblyFieldandValueCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_InvalidFinishing_goal() throws ValidationEngineException
	{
		Field field2 = new Field("finishing_goal", "fgfhh");
		gaRecord.addField(field2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(0, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
	}

	@Test
	@Ignore
	public void testCheck_validRecord() throws SQLException, ValidationEngineException
	{
		Field field2 = new Field("project", "PRJEA84437");
		gaRecord.addField(field2);
		assertTrue(check.check(gaRecord).isValid());

	}

	@Test
	public void testCheck_InvalidReleaseDate() throws ValidationEngineException
	{
		Field field2 = new Field("release_date", "12-MON-34");
		gaRecord.addField(field2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
	}

	@Test
	public void testCheck_InvalidVersion() throws ValidationEngineException
	{
		Field field2 = new Field("assembly_version", "er");
		gaRecord.addField(field2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(0, result.count("AssemblyFieldandValueCheck-3", Severity.ERROR));
	}

}
