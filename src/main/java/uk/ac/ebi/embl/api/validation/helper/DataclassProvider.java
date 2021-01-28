package uk.ac.ebi.embl.api.validation.helper;

import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DataclassProvider {
	
	public static String getAccessionDataclass(String primaryAccession)
	{
		if (primaryAccession == null || "XXX".equals(primaryAccession) || primaryAccession.isEmpty())
			return null;

		String dataclass = null;
		if (AccessionMatcher.isAssemblyMasterAccn(primaryAccession) || AccessionMatcher.isMasterAccession(primaryAccession)) {
			dataclass = Entry.SET_DATACLASS;
		} else if (AccessionMatcher.isTpxSeqAccession(primaryAccession)) {
			dataclass = Entry.TPX_DATACLASS;
		}
		return dataclass;
	}

	 /*
     * get the keyword dataclass from keywords in KW line(CV_DATACLASS_KEYWORD)
     */
	public static ArrayList<String> getKeywordDataclass(Entry entry, DataSet d)
	{
		Set<String> keywordDataclassSet = new HashSet<>();
		ArrayList<String> keywordDataclasses=new ArrayList<String>();
		HashMap<String, String> compressedKeywordToDataclassMap = new HashMap<String, String>();
		HashMap<String, String> compressedKeywordToKeywordMap = new HashMap<String, String>();
		HashMap<Text, Text> keywordMap = new HashMap<Text, Text>();

		for (DataRow row : d.getRows())
		{
			compressedKeywordToDataclassMap.put(row.getString(1), row.getString(0));
			if (row.getString(2) != null)
				compressedKeywordToKeywordMap.put(row.getString(1), row.getString(2));
		}
		for (Text keyword : entry.getKeywords())
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
		if (keywordDataclassSet.isEmpty())
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
			keywordDataclasses.addAll(keywordDataclassSet);
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

	public static boolean isValidDataclass(String dataClass) {

		if(dataClass == null)
			return false;
		for (DataRow row : GlobalDataSets.getDataSet(FileName.DATACLASS).getRows()) {
			String validDataclass = row.getString(0);
			if (validDataclass != null && validDataclass.trim().equalsIgnoreCase(dataClass))
				return true;
		}

		return false;
	}
}
