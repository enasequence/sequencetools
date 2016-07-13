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

import java.util.ArrayList;
import uk.ac.ebi.embl.api.validation.Origin;

public class GenomeAssemblyRecord extends GenomeAssemblyRow
{
	public static class ChromosomeDataRow extends GenomeAssemblyRow
	{
		String object_Name, chromosome_name, type, location;
		Origin origin;

		public ChromosomeDataRow()
		{
			this.object_Name = null;
			this.chromosome_name = null;
			this.type = null;
			this.location = null;
		}

		public ChromosomeDataRow(String object_Name, String chromosome_name, String type, String location)
		{
			this.object_Name = object_Name;
			this.chromosome_name = chromosome_name;
			this.type = type;
			this.location = location;

		}

		public String get_object_name()
		{
			return this.object_Name;
		}

		public String get_chromosome_name()
		{
			return this.chromosome_name;
		}

		public String get_type()
		{
			return this.type;
		}

		public String get_location()
		{
			return this.location;
		}

	}

	public static class UnlocalisedDataRow extends GenomeAssemblyRow
	{
		String object_Name, chromosome_name;
		Origin origin;

		public UnlocalisedDataRow(String object_Name, String chromosome_name)
		{
			this.object_Name = object_Name;
			this.chromosome_name = chromosome_name;
		}

		public String get_object_name()
		{
			return this.object_Name;
		}

		public String get_chromosome_name()
		{
			return this.chromosome_name;
		}

	}

	public static class Field extends GenomeAssemblyRow
	{
		private Object key, value;

		public Field()
		{
			this.key = null;
			this.value = null;
		}

		public Field(Object key, Object value)
		{
			this.key = key;
			this.value = value;
		}

		public Object getValue()
		{
			return value;
		}

		public Object getKey()
		{
			return key;
		}
	}

	public final static String ASSEMBLY_FILE_TYPE = "assembly";
	public final static String CHROMOSOME_FILE_TYPE = "chromosome";
	public final static String PLACED_FILE_TYPE = "placed";
	public final static String UNPLACED_FILE_TYPE = "unplaced";
	public final static String UNLOCALISED_FILE_TYPE = "unlocalised";
	protected String recordType;
	protected Origin origin;
	protected String pathProject;
	ArrayList<Object> genomeAssemblyFields = new ArrayList<Object>();

	public boolean isAssembly()
	{
		return recordType == ASSEMBLY_FILE_TYPE;
	}

	public boolean isChromosome()
	{
		return recordType == CHROMOSOME_FILE_TYPE;
	}

	public boolean isPlaced()
	{
		return recordType == PLACED_FILE_TYPE;
	}

	public boolean isUnplaced()
	{
		return recordType == UNPLACED_FILE_TYPE;
	}

	public boolean isUnLocalised()
	{
		return recordType == UNLOCALISED_FILE_TYPE;
	}

	public void addField(Object field)
	{
		genomeAssemblyFields.add(field);
	}

	public Object getFields()
	{
		return genomeAssemblyFields;
	}

	public void deleteField(Object obj)
	{
		genomeAssemblyFields.remove(obj);
	}
}
