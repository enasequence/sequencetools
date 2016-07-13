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
package uk.ac.ebi.embl.api.genomeassembly;

import uk.ac.ebi.embl.api.validation.Origin;

public abstract class GenomeAssemblyRow
{
	private Origin origin;
	private boolean valid=true;

	public void setOrigin(Origin origin)
	{
		this.origin = origin;
	}

	public Origin getOrigin()
	{
		return origin;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public boolean isValid()
	{
		return valid;
	}
}
