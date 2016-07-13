package uk.ac.ebi.embl.api.validation.cvtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class cv_fqual_value_fix_table
{
	ArrayList<cv_fqual_value_fix_record> cv_fqual_value_fix_records = new ArrayList<cv_fqual_value_fix_record>();
	Set<String> uniqueQualifierNames = new HashSet<String>();

	public void add(cv_fqual_value_fix_record record)
	{
		cv_fqual_value_fix_records.add(record);
	}

	public Set<String> getUniqueNames()
	{
		if (uniqueQualifierNames.size()!=0)
			return uniqueQualifierNames;

		for (cv_fqual_value_fix_record cv_fqual_value_fix_record : cv_fqual_value_fix_records)
		{
			uniqueQualifierNames.add(cv_fqual_value_fix_record.getFqualName());
		}

		return uniqueQualifierNames;
	}
	
	public HashMap<String, String> getQualifierValueMap(String qualifierName)
	{
		HashMap<String ,String> qualifierValueMap=new HashMap<String,String>();
		
		for(cv_fqual_value_fix_record cv_fqual_value_fix_record:cv_fqual_value_fix_records)
		{
			if(qualifierName.equals(cv_fqual_value_fix_record.getFqualName()))
			{
				qualifierValueMap.put(cv_fqual_value_fix_record.getRegex(), cv_fqual_value_fix_record.getValue());
			}
		}
		return qualifierValueMap;
		
	}
	
	public cv_fqual_value_fix_record create_cv_fqual_value_fix_record()

	{
		return new cv_fqual_value_fix_record();
	}
	public class cv_fqual_value_fix_record
	{
		private String fqualName;
		private String regex;
		private String value;

		public String getFqualName()
		{
			return fqualName;
		}

		public void setFqualName(String fqualName)
		{
			this.fqualName = fqualName;
		}

		public String getRegex()
		{
			return regex;
		}

		public void setRegex(String regex)
		{
			this.regex = regex;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

	}

}
