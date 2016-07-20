package uk.ac.ebi.embl.api.validation.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

public class DataclassProvider {

	public static final Pattern ASSEMBLYMASTER_PRIMARY_ACCESSION_PATTERN = Pattern.compile("^(ERZ|GCA_)[0-9]+$");
	public static final Pattern WGSMASTER_PRIMARY_ACCESSION_PATTERN = Pattern.compile("^([A-Z]{4})[0-9]{2}S?[0]{6,10}$");
	public static final Pattern SEQUENCE_PRIMARY_ACCESSION_PATTERN = Pattern.compile("^([A-Z]{1})[0-9]{5,6}$");
	public static final Pattern TPX_PRIMARY_ACCESSION_PATTERN = Pattern.compile("^TPX_[0-9]{6}$");
	public static final Pattern WGS_PRIMARY_ACCESSION_PATTERN = Pattern.compile("^([A-Z]{4})[0-9]{2}S?[0-9]{6,10}$");
	private static EntryDAOUtils entryDaoUtils;
	private static HashMap<String,String> prefixDataclass = new HashMap<String, String>();
	
	public DataclassProvider(EntryDAOUtils entryDaoUtils)
	{
		DataclassProvider.entryDaoUtils=entryDaoUtils;
	}
	
	
	public static String getAccessionDataclass(String primaryAccession) throws SQLException
	{
		  if(primaryAccession==null||"XXX".equals(primaryAccession)||primaryAccession.isEmpty())
			return null;
		   Matcher wgsAcessionMatcher = WGS_PRIMARY_ACCESSION_PATTERN.matcher( primaryAccession );
		   Matcher assemblyMasterAccessionMatcher = ASSEMBLYMASTER_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		   Matcher wgsMasterAccessionMatcher = WGSMASTER_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		   Matcher sequenceAccessionMatcher = SEQUENCE_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		   Matcher tpxAccessionMatcher =TPX_PRIMARY_ACCESSION_PATTERN.matcher(primaryAccession);
		   String dataclass= null;
		   if(wgsAcessionMatcher.matches())
		   {
			 dataclass = Entry.WGS_DATACLASS;
		   }
		   else if(assemblyMasterAccessionMatcher.matches()||wgsMasterAccessionMatcher.matches())
		   {
			   dataclass = Entry.SET_DATACLASS;
		   }
		   else if(tpxAccessionMatcher.matches())
		   {
			   dataclass = Entry.TPX_DATACLASS;
		   }
		   else if(sequenceAccessionMatcher.matches())
		   {
			    String prefix=sequenceAccessionMatcher.group(1);
			    if(prefixDataclass.get(prefix)!=null)
			    {
			    	dataclass= prefixDataclass.get(prefix);
			    }
			    else if(entryDaoUtils!=null)
			    {
			    	dataclass=entryDaoUtils.getAccessionDataclass(prefix);
			    }
		   }
	 return dataclass;
	}
	 	
	 /*
     * get the keyword dataclass from keywords in KW line(CV_DATACLASS_KEYWORD)
     */
	public static ArrayList<String> getKeywordDataclass(Entry entry, DataSet d)
	{
		Set<String> keywordDataclassSet = new HashSet<String>();
		ArrayList<String> keywordDataclasses=new ArrayList<String>();

		HashMap<String, String> compressedKeywordToDataclassMap = new HashMap<String, String>();
		HashMap<String, String> compressedKeywordToKeywordMap = new HashMap<String, String>();
		HashMap<Text, Text> keywordMap = new HashMap<Text, Text>();
		List<Text> keywords = entry.getKeywords();
		for (DataRow row : d.getRows())
		{
			compressedKeywordToDataclassMap.put(row.getString(1), row.getString(0));
			if (row.getString(2) != null)
				compressedKeywordToKeywordMap.put(row.getString(1), row.getString(2));
		}
		for (Text keyword : keywords)
		{
			String compressedKeyword = getcompressedKeyword(keyword);
			if (compressedKeywordToDataclassMap.containsKey(compressedKeyword) && compressedKeywordToKeywordMap.containsKey(compressedKeyword))
			{
				keywordMap.put(keyword, new Text(compressedKeywordToKeywordMap.get(compressedKeyword)));
				keywordDataclassSet.add(compressedKeywordToDataclassMap.get(compressedKeyword));
			}
		}
		for (Text key : keywordMap.keySet())
		{
			entry.removeKeyword(key);
			entry.addKeyword(keywordMap.get(key));
		}
		if (keywordDataclassSet.size() == 0)
		{
			keywordDataclasses.add("XXX");
			return keywordDataclasses;
		}
		if (keywordDataclassSet.size() == 1)
		{
			keywordDataclasses.add(keywordDataclassSet.toArray()[0].toString());
			return keywordDataclasses;
		}

		if (keywordDataclassSet.size() == 2 && keywordDataclassSet.contains(Entry.TPA_DATACLASS))
		{
			if (keywordDataclassSet.contains(Entry.WGS_DATACLASS))
			{
				 keywordDataclasses.add(Entry.WGS_DATACLASS);
				 return keywordDataclasses;
			}
			if (keywordDataclassSet.contains(Entry.CON_DATACLASS))
			{
				 keywordDataclasses.add(Entry.CON_DATACLASS);
				 return keywordDataclasses;
			}
			if (keywordDataclassSet.contains(Entry.TSA_DATACLASS))
			{
				keywordDataclasses.add(Entry.TSA_DATACLASS);// EMD-5315
				return keywordDataclasses;
			}
		}
		else
		{
		for (String keywordDataclass:keywordDataclassSet)
		{
			keywordDataclasses.add(keywordDataclass);
		}
		return keywordDataclasses;
		}
		return null;

	}
	
	/*
     * FIX:remove if any special characters exists in keywords
     */
	public static String getcompressedKeyword(Text keyword)
	{
		String compressedKeyword = "";
		String key = keyword.getText();
		String remove = " ()+,/:[]<>\"*";
		for (int i = 0; i < key.length(); i++)
		{
			if (remove.indexOf(key.charAt(i)) == -1)
			{
				compressedKeyword = compressedKeyword + key.charAt(i);
			}
		}

		return compressedKeyword.toUpperCase();
	}
	
}
