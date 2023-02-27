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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class FlatFileMatcher {
	
	public FlatFileMatcher(FlatFileLineReader reader, Pattern pattern) {
		this.reader = reader;		
		this.pattern = pattern;
	}
	
	private FlatFileLineReader reader;
	private String locationString;
	private Pattern pattern;
	private Matcher matcher;
	
	public boolean match(String string) {	
		matcher = pattern.matcher(string);
		return matcher.matches();
	}	

	public boolean isValue(int group) {
		return matcher.group(group) != null;
	}
		
	public boolean isValueXXX(int group) {
		if (!isValue(group)) {
			return false;
		}
		return !matcher.group(group).equalsIgnoreCase("XXX");
	}

	public String getString(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;		
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		return value;
	}

	public String getString(int group, Matcher matcher) {
		String value = matcher.group(group);
		if (value == null) {
			return null;
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}

		if((value.lastIndexOf(".") == value.length()-1)
				|| (value.lastIndexOf(",") == value.length()-1) ){
			value = value.substring(0, value.length()-1);
		}

		return value;
	}

	public String getUpperString(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;		
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		return value.toUpperCase();
	}

	public String getLowerString(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;		
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		return value.toLowerCase();
	}
		
	public Integer getInteger(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;		
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		Integer number = null;
		try {
			number = Integer.parseInt(matcher.group(group));
		}
		catch (NumberFormatException ex) {
			error("FF.3");		
		}
		return number;			
	}
	
	public Long getLong(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		Long number = null;
		try {
			number = Long.parseLong(matcher.group(group));
		}
		catch (NumberFormatException ex) {
			error("FF.3");
		}
		return number;
	}

    /** Returns the day given a string in format dd-MMM-yyyy.
     */		
	public Date getDay(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;
		}
		Date date = FlatFileDateUtils.getDay(value);
		if (date == null) {
			error("FF.2", value);
		}
		return date;
	}

    /** Returns the year given a string in format yyyy.
     */			
	public Date getYear(int group) {
		String value = matcher.group(group);
		if (value == null) {
			return null;
		}
		Date date = FlatFileDateUtils.getYear(value);
		if (date == null) {
			error("FF.4", value);
		}
		return date;
	}

    public Date getYear(int group, Matcher matcher) {
        String value = matcher.group(group);
        if (value == null) {
            return null;
        }
        Date date = FlatFileDateUtils.getYear(value);
        if (date == null) {
            error("FF.4", value);
        }
        return date;
    }
	
    protected void error(String messageKey, Object... params) {
    	reader.error(messageKey, params);
    }

    protected void warning(String messageKey, Object... params) {
    	reader.warning(messageKey, params);
    }
        
	protected FlatFileLineReader getReader() {
		return reader;
	}    
}
