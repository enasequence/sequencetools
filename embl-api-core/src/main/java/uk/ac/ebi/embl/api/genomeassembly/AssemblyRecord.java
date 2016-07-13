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

public class AssemblyRecord extends GenomeAssemblyRecord
{
	public static final String PROJECT_KEYWORD = "Project";
	public static final String ASSEMBLY_NAME_KEYWORD = "Assembly_name";
	public static final String ASSEMBLY_VERSION_KEYWORD = "Assembly_version";
	public static final String WGS_KEYWORD = "WGS";
	public static final String TITLE_KEYWORD = "Title";
	public static final String DESCRIPTION_KEYWORD = "Description";
	public static final String FINISHING_GOAL_KEYWORD = "Finishing_goal";
	public static final String FINISHING_STATUS_KEYWORD = "Finishing_status";
	public static final String COMPLETE_KEYWORD = "Complete";
	public static final String COVERAGE_KEYWORD = "Coverage";
	public static final String RELEASE_DATE_KEYWORD = "Release_date";
	public static final String[] FINISHING_SET = { "standard draft", "high-quality draft", "improved High-quality draft", "noncontiguous finished",
			"finished" };

	public AssemblyRecord()
	{
		recordType = ASSEMBLY_FILE_TYPE;

	}

}
