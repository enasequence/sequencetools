package uk.ac.ebi.embl.api;

import uk.ac.ebi.embl.api.entry.Entry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessionMatcher {

    private static final Pattern ANY_SEQ_ACC_VERSION_PATTERN = Pattern.compile("(^([A-Z]{1,2})[0-9]{5,6}(\\.)(\\d+)$|^([A-Z]{2})[0-9]{8}(\\.)(\\d+)$|^([A-Z]{4})[0-9]{2}S?[0-9]{6,8}(\\.)(\\d+)$|^([A-Z]{6})[0-9]{2}S?[0-9]{7,9}(\\.)(\\d+)$)");
    private static final Pattern ANY_SEQ_ACC_PATTERN = Pattern.compile("(^([A-Z]{1,2})[0-9]{5,6}$|^([A-Z]{2})[0-9]{8}$|^([A-Z]{4})[0-9]{2}S?[0-9]{6,8}$|^([A-Z]{6})[0-9]{2}S?[0-9]{7,9}$)");

    //All non WGS
    private static final Pattern STANDARD_SEQ_ACCESSION_PATTERN = Pattern.compile("(^([A-Z]{1,2})[0-9]{5,6}$|^([A-Z]{2})[0-9]{8}$)");

    //All WGS
    private static final Pattern WGS_SEQ_ACCESSION_PATTERN_OLD = Pattern.compile("^([A-Z]{4})[0-9]{2}S?[0-9]{6,8}$");
    private static final Pattern WGS_SEQ_ACCESSION_PATTERN_NEW = Pattern.compile("^([A-Z]{6})[0-9]{2}S?[0-9]{7,9}$");
    private static final Pattern WGS_MASTER_SEQ_ACCESSION_PATTERN_OLD = Pattern.compile("^([A-Z]{4})[0-9]{2}[0]{6,8}$");
    private static final Pattern WGS_MASTER_SEQ_ACCESSION_PATTERN_NEW = Pattern.compile("^([A-Z]{6})[0-9]{2}[0]{7,9}$");

    private static final Pattern PROTEIN_ID_PATTERN = Pattern.compile("^\\s*([A-Z]{3}\\d{5}(\\d{2})?)(\\.)(\\d+)\\s*$");
    private static final Pattern ASSEMBLY_MASTER_SEQ_ACCESSION_PATTERN = Pattern.compile("^(ERZ|GCA_)[0-9]+$");
    private static final Pattern TPX_SEQ_ACCESSION_PATTERN = Pattern.compile("^TPX_[0-9]{6}$");

    public static Matcher getAccMatcherWithVersion(String input) {
        return input == null ? null : ANY_SEQ_ACC_VERSION_PATTERN.matcher(input);
    }

    public static Matcher getAccMatcher(String input) {
        return input == null ? null : ANY_SEQ_ACC_PATTERN.matcher(input);
    }

    public static Matcher getAssemblyMasterPrimaryAccMatcher(String input) {
        return input == null ? null : ASSEMBLY_MASTER_SEQ_ACCESSION_PATTERN.matcher(input);
    }

    public static boolean isAssemblyMasterAccn(String accn) {
        Matcher m = getAssemblyMasterPrimaryAccMatcher(accn);
        return m != null && m.matches();
    }

    public static Matcher getSeqPrimaryAccMatcher(String input) {
        return input == null? null: STANDARD_SEQ_ACCESSION_PATTERN.matcher(input);
    }

    public static boolean isSeqAccession(String input) {
        Matcher m = getSeqPrimaryAccMatcher(input);
        return m != null && m.matches();
    }

    public static Matcher getOldWgsAccMatcher(String input) {
        return input == null ? null : WGS_SEQ_ACCESSION_PATTERN_OLD.matcher(input);
    }

    public static Matcher getNewWgsAccMatcher(String input) {
        return input == null ? null : WGS_SEQ_ACCESSION_PATTERN_NEW.matcher(input);
    }

    public static Matcher getOldWgsMasterAccMatcher(String input) {
        return input == null ? null : WGS_MASTER_SEQ_ACCESSION_PATTERN_OLD.matcher(input);
    }

    public static Matcher getNewWgsMasterAccMatcher(String input) {
        return input == null ? null : WGS_MASTER_SEQ_ACCESSION_PATTERN_NEW.matcher(input);
    }

    public static boolean isWgsSeqAccession(String accText) {
        return isAnyMatch(getOldWgsAccMatcher(accText),  getNewWgsAccMatcher(accText));
    }

    public static boolean isWgsMasterAccession(String accText) {
        boolean rtn =  isAnyMatch( getOldWgsMasterAccMatcher(accText), getNewWgsMasterAccMatcher(accText));
        return rtn;
    }

    public static Matcher getTpxPrimaryAccMatcher(String input) {
        return input == null ? null : TPX_SEQ_ACCESSION_PATTERN.matcher(input);
    }

    public static boolean isTpxSeqAccession(String input) {
        Matcher m =  getTpxPrimaryAccMatcher(input);
        return m != null && m.matches();
    }

    public static Matcher getProteinIdMatcher(String proteinIdStr) {
        return proteinIdStr == null ? null : PROTEIN_ID_PATTERN.matcher(proteinIdStr);
    }

    public static String getProteinAccession(String proteinIdStr) {
        return getFromProteinId(proteinIdStr,1);
    }

    public static String getProteinVersion(String proteinIdStr) {
        return getFromProteinId(proteinIdStr,4);
    }

    private static String getFromProteinId(String proteinIdStr, int group) {
        Matcher m = getProteinIdMatcher(proteinIdStr);
        if(m != null && m.matches()) {
            return m.group(group);
        }
        return null;
    }

    public static boolean isPrimaryAcc(String accession) {
        return isAnyMatch(getAccMatcher(accession) , getAccMatcherWithVersion(accession));
    }

    public static String getAccessionPrefix(String primaryAccession, String dataClass) {
        String accnPrefix;

        if (primaryAccession == null || dataClass == null)
            return null;

        if (Entry.SET_DATACLASS.equals(dataClass)) {

            accnPrefix = getPrefix(getAssemblyMasterPrimaryAccMatcher(primaryAccession), 1);
            if (accnPrefix == null) {
                accnPrefix = getPrefix(getOldWgsMasterAccMatcher(primaryAccession), 1);
                if (accnPrefix == null) {
                    accnPrefix = getPrefix(getNewWgsMasterAccMatcher(primaryAccession), 1);
                }
            }
        } else if ( Entry.TPX_DATACLASS.equals(dataClass)) {
            accnPrefix = getPrefix(getTpxPrimaryAccMatcher(primaryAccession), 1);
        } else if ( Entry.WGS_DATACLASS.equals(dataClass)) {
            accnPrefix = getPrefix(getOldWgsAccMatcher(primaryAccession), 1);
            if(accnPrefix == null) {
                accnPrefix = getPrefix(getNewWgsAccMatcher(primaryAccession), 1);
            }
        } else {
            accnPrefix = getPrefix(getSeqPrimaryAccMatcher(primaryAccession), getPrimaryAccnPrefixGroup(primaryAccession));
        }

        return accnPrefix;
    }

    public static int getPrimaryAccnPrefixGroup(String primaryAccession) {
        return primaryAccession.length() > 8 ? 3 : 2;
    }

    private static boolean isAnyMatch(Matcher oldM, Matcher newM) {
        return (oldM != null && oldM.matches() ) || (newM != null && newM.matches() );
    }

    private static String getPrefix(Matcher matcher, int group) {
        return matcher == null ? null : matcher.matches() ? matcher.group(group) : null;
    }
}
