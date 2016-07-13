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
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.UnlocalisedDataRow;
import uk.ac.ebi.embl.api.genomeassembly.UnlocalisedRecord;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;

public class UnlocalisedFieldandValueCheckTest
{

	private GenomeAssemblyRecord gaRecord;
	private UnlocalisedFieldandValueCheck check;
	Origin origin = null;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		gaRecord = new UnlocalisedRecord();
		UnlocalisedDataRow row1 = new UnlocalisedDataRow("cb25.NA_084", "I");
		gaRecord.addField(row1);
		check = new UnlocalisedFieldandValueCheck();
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMandatory()
	{
		UnlocalisedDataRow row2 = new UnlocalisedDataRow("chI", null);
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("UnlocalisedFieldandValueCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidObject_name()
	{
		UnlocalisedDataRow row2 = new UnlocalisedDataRow("chI dfdf", "I");
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("UnlocalisedFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidChromosome_name()
	{
		UnlocalisedDataRow row2 = new UnlocalisedDataRow("chI", "Ichr");
		gaRecord.addField(row2);
		ValidationResult result = check.check(gaRecord);
		assertEquals(1, result.count("UnlocalisedFieldandValueCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_valid()
	{
		ValidationResult result = check.check(gaRecord);
		assertEquals(0, result.count("ChromosomeFieldandValueCheck-2", Severity.ERROR));
	}

}
