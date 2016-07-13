/*
 * # Copyright 2012-2013 EMBL-EBI, Hinxton outstation
*
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
*
# http://www.apache.org/licenses/LICENSE-2.0
*
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.genomeassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fix.genomeassembly.DeleteInvalidRowFix;

public class DeleteInvalidRowFixTest
{
	private GenomeAssemblyRecord gaRecord;
	private DeleteInvalidRowFix check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		check = new DeleteInvalidRowFix();

	}

	@Test
	public void testCheck_NoGenomeAssemblyRecord()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_inValidAssemblyRow()
	{
		gaRecord = new GenomeAssemblyRecord();
		gaRecord.addField(new Field("PROJECT", "PRJEB100"));
		gaRecord.addField(new Field("ASSEMBLY_NAME", "CB4"));
		Field invalidField = new Field();
		invalidField.setValid(false);
		gaRecord.addField(invalidField);
		ValidationResult validationResult = check.check(gaRecord);
		assertEquals(1, validationResult.count("deleteInvalidRowFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_validAssemblyRow()
	{
		gaRecord = new GenomeAssemblyRecord();
		gaRecord.addField(new Field("PROJECT", "PRJEB100"));
		gaRecord.addField(new Field("ASSEMBLY_NAME", "CB4"));
		ValidationResult validationResult = check.check(gaRecord);
		assertEquals(0, validationResult.count("deleteInvalidRowFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_inValidChromosomeRow()
	{
		gaRecord = new GenomeAssemblyRecord();
		gaRecord.addField(new ChromosomeDataRow("I", "chrI", "chromosome", null));
		gaRecord.addField(new ChromosomeDataRow("X", "chrX", "chromosome", null));
		ChromosomeDataRow invalidRow = new ChromosomeDataRow();
		invalidRow.setValid(false);
		gaRecord.addField(invalidRow);
		ValidationResult validationResult = check.check(gaRecord);
		assertEquals(1, validationResult.count("deleteInvalidRowFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_validChromosomeRow()
	{
		gaRecord = new GenomeAssemblyRecord();
		gaRecord.addField(new ChromosomeDataRow("I", "chrI", "chromosome", null));
		gaRecord.addField(new ChromosomeDataRow("X", "chrX", "chromosome", null));
		ValidationResult validationResult = check.check(gaRecord);
		assertEquals(0, validationResult.count("deleteInvalidRowFix_1", Severity.FIX));
	}
}
