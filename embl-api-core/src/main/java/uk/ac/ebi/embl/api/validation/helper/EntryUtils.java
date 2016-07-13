package uk.ac.ebi.embl.api.validation.helper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class EntryUtils
{
	public enum Topology
	{
		LINEAR("L"),
		CIRCULAR("C");
		
		String topology;
		
		private Topology(String topology)
		{
			this.topology = topology;
		}
		
		public String getTopology()
		{
			return topology;
		}
	}
	
	public static boolean isProject(String id)
	{
		return id.startsWith("PRJ");
	}
	
	public static boolean isAnalysis_id(String id)
	{
		return id.startsWith("ERZ");
	}
	
	public static String getIdType(String id)
	{
		return isProject(id) ? "study_id" : "analysis_id";
	}
	
	public static boolean hasLetter(String subject)
	{
		boolean found = false;
		Pattern p = Pattern.compile(".*\\w+.*");
		Matcher m = p.matcher(subject);
		if (m.find())
		{
			found = true;
		}
		return found;
	}
	
	public static String getObjectNameFromDescription(String desc)
	{
		String objectName = null;
		if (desc.indexOf("\\t+") != -1)
		{
			System.err.println("ERROR: The description line is a tab delimited line. It must be a white space delimited.");
			return objectName;
		}
		String[] words = desc.trim().split("\\s+");
		if (!hasLetter(words[words.length - 1]))
		{
			
			objectName = words[words.length - 2] + " " + words[words.length - 1];
		}
		else
		{
			
			objectName = words[words.length - 1];
		}
		return objectName;
		
	}
	
	public static boolean isPrimaryAcc(String name)
	{
		Pattern accVersionPattern = Pattern.compile("[A-Z]{1,4}[0-9]{5,8}(\\.)(\\d)+");
		Pattern accPattern = Pattern.compile("[A-Z]{1,4}[0-9]{5,8}");
		if (accPattern.matcher(name).matches() || accVersionPattern.matcher(name).matches())
			return true;
		return false;
	}
	
	public static boolean isValidEntry_name(String entry_name)
	{
		if (entry_name.split(" ").length > 1)
		{
			return false;
		}
		return true;
		
	}
	
	public static String concat(String delimiter,
								String... params)
	{
		StringBuffer concatString = new StringBuffer();
		int i = 0;
		if (params.length != 0)
		{
			
			for (String param : params)
			{
				i++;
				if (param != null)
				{
					concatString.append(param);
					if (i != (params.length))
						concatString.append(delimiter);
				}
			}
			
		}
		return concatString.toString();
	}
	
	public static String convertNonAsciiStringtoAsciiString(String non_asciiString) throws UnsupportedEncodingException
	{
		 if(StringUtils.isAsciiPrintable(non_asciiString)||non_asciiString==null||non_asciiString.isEmpty())
			 return non_asciiString;
		 String encodedString = Normalizer.normalize(new String(non_asciiString.getBytes(), Charset.forName("UTF-8")), Normalizer.Form.NFKD);
		 String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
	     String asciiString = new String(encodedString.replaceAll(regex, "").getBytes("ascii"), "ascii");
	     return asciiString;
	}
	
	 /** Returns the day given a string in format dd-MMM-yyyy.
     */	
	public static Date getDay(String string) {
		if (string == null) {
			return null;
		}
		Date date = null;
		try {
			date = (new SimpleDateFormat("dd-MMM-yyyy").parse(string));
		}
		catch (ParseException ex) {
			return null;
		}
		return date;
	}
	
	public static String getAccessionPrefix(String primaryAccession)
	{
		if(primaryAccession==null)
			return null;
		Matcher assemblyMasterMatcher=DataclassProvider.ASSEMBLYMASTER_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		Matcher sequenceAccessionMatcher=DataclassProvider.SEQUENCE_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		Matcher tpxAccessionMatcher=DataclassProvider.TPX_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		Matcher wgsAccessionMatcher=DataclassProvider.WGS_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		Matcher wgsMasterAccessionMatcher=DataclassProvider.WGSMASTER_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		
			if(assemblyMasterMatcher.matches())
				return assemblyMasterMatcher.group(1);
			if(sequenceAccessionMatcher.matches())
				return sequenceAccessionMatcher.group(1);
			if(tpxAccessionMatcher.matches())
				return tpxAccessionMatcher.group(1);
			if(wgsAccessionMatcher.matches())
				return wgsAccessionMatcher.group(1);
			if(wgsMasterAccessionMatcher.matches())
				return wgsMasterAccessionMatcher.group(1);
			
			return null; 
		
		
	}
}
