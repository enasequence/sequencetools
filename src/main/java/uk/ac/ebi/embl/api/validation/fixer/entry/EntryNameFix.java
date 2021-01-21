package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.apache.commons.lang.StringUtils;


public class EntryNameFix {

    public static String getFixedEntryName(String entryName) {

        if (entryName != null) {
            entryName = entryName.trim();

            entryName = entryName.replaceAll("[\\\\/|]","_");

            int i = 0;
            while ( entryName.length()>i && entryName.charAt(i) == '_') {
                ++i;
            }
            if (i > 0) {
                entryName = entryName.substring(i);
            }

            if (entryName.endsWith(";")) {
                entryName = StringUtils.removeEnd(entryName, ";");
            }

        }
        return entryName;
    }
}
