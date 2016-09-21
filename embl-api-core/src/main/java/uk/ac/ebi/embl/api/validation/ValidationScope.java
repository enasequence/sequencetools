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
package uk.ac.ebi.embl.api.validation;
public enum ValidationScope {
	EMBL(Group.SEQUENCE),
	NCBI(Group.SEQUENCE),
	EMBL_TEMPLATE(Group.SEQUENCE),
	EPO_PEPTIDE(Group.SEQUENCE),
	EPO(Group.SEQUENCE),
	INSDC(Group.SEQUENCE),
	EGA(Group.SEQUENCE),
	ARRAYEXPRESS(Group.SEQUENCE),
	//Assembly GROUP
	ASSEMBLY_MASTER(Group.ASSEMBLY),
	ASSEMBLY_CONTIG(Group.ASSEMBLY),
	ASSEMBLY_SCAFFOLD(Group.ASSEMBLY),
	ASSEMBLY_CHROMOSOME(Group.ASSEMBLY);
	
	
	private Group group;
	
	ValidationScope(Group group)
	{
		this.group=group;
	}
	
	public boolean isInGroup(Group group)
	{
		return this.group==group;
	}
	
	public Group group()
	{
		return this.group;
	}
	
	static public ValidationScope get(String scope) {
	    if (scope== null) return ValidationScope.EMBL;
	    try { 
	         return valueOf(scope.toUpperCase()); 
	    } catch (IllegalArgumentException x) { 
	        return null;
	    }
	  }
	public int getAssemblyLevel()
	{
		switch(this)
		{
		case ASSEMBLY_CHROMOSOME:
			return 2;
		case ASSEMBLY_CONTIG:
			return 0;
		case ASSEMBLY_SCAFFOLD:
			return 1;
		default:
			return -1;
				
	}
			
	}
	public enum Group
	{
		ASSEMBLY,
		SEQUENCE
	}
}
