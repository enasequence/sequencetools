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

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.genomeassembly.AssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("")
public class AssemblyFieldandValueCheck extends GenomeAssemblyValidationCheck
{
   
	@CheckDataSet("genome-assembly-keywords.tsv")
	private DataSet keywordsSet;
	private final static String INVALID_KEY_ID = "AssemblyFieldandValueCheck-1";
	private final static String MISSING_MANDATORY_ID = "AssemblyFieldandValueCheck-2";
	private final static String INVALID_VALUE_ID = "AssemblyFieldandValueCheck-3";
	private final static String CONNECTION_REQUIRED_ID = "AssemblyFieldandValueCheck-4";
	private final static String NO_KEY_VALUE_ID = "AssemblyFieldandValueCheck-5";
	private Set<String> keywordSet = new TreeSet<String>();
	private boolean isProject = false, isassemblyName = false;
	private String project_acc, assembly_name, version, finishing_goal, finishing_status, release_date;
	private static final Pattern PATTERN = Pattern.compile("^\\s*((PRJ[E,D,N][A-Z]){1}([0-9]\\d*))\\s*$", Pattern.CASE_INSENSITIVE);
    final private static String projectConstraintKey="project_id";
    public AssemblyFieldandValueCheck()
	{

	}
	AssemblyFieldandValueCheck(DataSet dataSet ) {
		
		this.keywordsSet = dataSet;
	}

	public ValidationResult check(GenomeAssemblyRecord assemblyRecord) throws ValidationEngineException
	{
		try
		{
		result = new ValidationResult();

		if (assemblyRecord == null)
		{
			return result;
		}
		@SuppressWarnings("unchecked")
		ArrayList<Field> fields = (ArrayList<Field>) assemblyRecord.getFields();

		for (DataRow dataRow : keywordsSet.getRows())
		{
			String keyword = Utils.parseTSVString(dataRow.getString(0));
			keywordSet.add(keyword);
		}
		for (Field field : fields)
		{
			String key = (String) field.getKey();
			String value = (String) field.getValue();
			if(!field.isValid())
			{
				reportError(field.getOrigin(),NO_KEY_VALUE_ID);
			}
			if (!keywordSet.contains(key.toLowerCase()))
			{
				reportError(field.getOrigin(), INVALID_KEY_ID, key);
			}
			if (key.equalsIgnoreCase(AssemblyRecord.PROJECT_KEYWORD))
			{
				isProject = true;
				project_acc = value;
				if (project_acc != null)
				{
				if(!PATTERN.matcher(project_acc).matches())
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.PROJECT_KEYWORD, project_acc);
				}
				else if(getEntryDAOUtils()!=null&&getEntryDAOUtils().isValueExists("mv_project",projectConstraintKey,project_acc ))
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.PROJECT_KEYWORD, project_acc);
				}
				}
			} else if (key.equalsIgnoreCase(AssemblyRecord.ASSEMBLY_NAME_KEYWORD))
			{
				isassemblyName = true;
				assembly_name = value;
				if (assembly_name != null && assembly_name.split(" ").length > 1)
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.ASSEMBLY_NAME_KEYWORD, assembly_name);
				}
			} /*else if (key.equalsIgnoreCase(AssemblyRecord.ASSEMBLY_VERSION_KEYWORD))
			{
				version = value;
				if (version != null && !StringUtils.isNumeric(version))
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.ASSEMBLY_VERSION_KEYWORD, version);
				}
			} else if (key.equalsIgnoreCase(AssemblyRecord.FINISHING_GOAL_KEYWORD))
			{
				finishing_goal = value;
				if (finishing_goal != null && ArrayUtils.indexOf(AssemblyRecord.FINISHING_SET, finishing_goal) == -1)
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.FINISHING_GOAL_KEYWORD, finishing_goal);
				}
			} else if (key.equalsIgnoreCase(AssemblyRecord.FINISHING_STATUS_KEYWORD))
			{
				finishing_status = value;
				if (finishing_status != null && ArrayUtils.indexOf(AssemblyRecord.FINISHING_SET, finishing_status) == -1)
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.FINISHING_STATUS_KEYWORD, finishing_status);
				}
			}*/ else if (key.equalsIgnoreCase(AssemblyRecord.RELEASE_DATE_KEYWORD))
			{
				release_date = value;
				if (release_date != null && !isValidDate(release_date))
				{
					reportError(field.getOrigin(), INVALID_VALUE_ID, AssemblyRecord.RELEASE_DATE_KEYWORD, release_date);
				}
			}

		}
		if (!isProject)
		{
			reportError(assemblyRecord.getOrigin(), MISSING_MANDATORY_ID, AssemblyRecord.PROJECT_KEYWORD);
		}
		if (!isassemblyName)
		{
			reportError(assemblyRecord.getOrigin(), MISSING_MANDATORY_ID, AssemblyRecord.ASSEMBLY_NAME_KEYWORD);
		}
		}catch(Exception e)
		{
			throw new ValidationEngineException(e);
		}
		return result;
	}

	private boolean isValidDate(String dateValue)
	{
		SimpleDateFormat parser = new SimpleDateFormat("dd-MMM-yyyy");
		try
		{
			parser.parse(dateValue);
		} catch (ParseException e)
		{
			return false;
		}
		return true;
	}
}
