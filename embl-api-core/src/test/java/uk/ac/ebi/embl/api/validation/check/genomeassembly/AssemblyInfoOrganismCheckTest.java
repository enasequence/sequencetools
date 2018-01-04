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

import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyInfoOrganismCheckTest
{
	private AssemblyInfoEntry assemblyEntry;
	private AssemblyInfoOrganismCheck check;
	private EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() throws SQLException
	{
		check = new AssemblyInfoOrganismCheck();
		planProperty=new EmblEntryValidationPlanProperty();
		TaxonHelper taxonHelper = new TaxonHelperImpl();
		planProperty.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(planProperty);
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoOrganism() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry(); 
		ValidationResult result = check.check(assemblyEntry);
		assertEquals(1, result.count("AssemblyInfoOrganismMissingCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_invalidOrganism() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry();
		assemblyEntry.setOrganism("asfsdfg");
		ValidationResult result = check.check(assemblyEntry);
		assertEquals(1, result.count("AssemblyInfoInvalidOrganismCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_validOrganism() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry();
		assemblyEntry.setOrganism("Bacteria");
		ValidationResult result = check.check(assemblyEntry);
		assertEquals(0, result.count("AssemblyInfoInvalidOrganismCheck", Severity.ERROR));
	}
	
}
