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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyInfoTypeCheckTest
{
	private AssemblyInfoEntry assemblyEntry;
	private AssemblyInfoTypeCheck check;

	@Before
	public void setUp() throws SQLException
	{
		check = new AssemblyInfoTypeCheck();
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoAssemblyType() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry(); 
		assertTrue(check.check(assemblyEntry).isValid());
	}
	
	@Test
	public void testCheck_invalidAssemblyType() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry(); 
		assemblyEntry.setAssemblyType("sdffgdfg");
		ValidationResult result = check.check(assemblyEntry);
		assertEquals(1, result.count("AssemblyinfoAssemblyTypeCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_validAssemblytype() throws ValidationEngineException
	{
		assemblyEntry= new AssemblyInfoEntry();
		assemblyEntry.setAssemblyType("clone or isolate");
		ValidationResult result = check.check(assemblyEntry);
		assertEquals(0, result.count("AssemblyinfoAssemblyTypeCheck", Severity.ERROR));
	}
}
