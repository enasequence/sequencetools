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

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.genomeassembly.ChromosomeRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;

public class ChromosomeFieldandValueCheckTest
{

	private GenomeAssemblyRecord gaRecord;
	private ChromosomeFieldandValueCheck check;
	Origin origin = null;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		DataRow dataRow1 = new DataRow("macronuclear");
		DataRow dataRow2 = new DataRow("nucleomorph");
		DataRow dataRow3 = new DataRow("mitochondrion");
		DataRow dataRow4 = new DataRow("kinetoplast");
		DataSet locationSet = new DataSet();
		locationSet.addRow(dataRow1);
		locationSet.addRow(dataRow2);
		locationSet.addRow(dataRow3);
		locationSet.addRow(dataRow4);
		gaRecord = new ChromosomeRecord();
		ChromosomeDataRow row1 = new ChromosomeDataRow("chI", "I", "chromosome", null);
		gaRecord.addField(row1);
		check = new ChromosomeFieldandValueCheck(locationSet);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMandatory()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI", "I", null, null);
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("ChromosomeFieldandValueCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidObject_name()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI dfdf", "I", "chromosome", null);
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidChromosome_name()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI", "Ichr", "chromosome", null);
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidType()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI", "I", "dsgfdfg", null);
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidLocation()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI", "I", "chromosome", "fgfgh");
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_valid()
	{
		ChromosomeDataRow row2 = new ChromosomeDataRow("chI", "I", "chromosome", "nucleomorph");
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(0, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

}
