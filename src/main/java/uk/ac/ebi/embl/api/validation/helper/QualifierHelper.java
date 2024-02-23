/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.helper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierHelper {

  private static final String NO_VALUE_ID = "QualifierCheck-2";
  private static final String NO_VALUE_ID_2 = "QualifierCheck-5";
  private static final String REGEX_FAIL_ID = "QualifierCheck-3";
  private static final String REGEX_GROUP_FAIL_ID = "QualifierCheck-4";
  private static final String LAT_LON_MESSAGE_ID1 = "QualifierCheck-7";
  private static final String LAT_LON_MESSAGE_ID2 = "QualifierCheck-8";
  private static final String PROTEIN_ID_VERSION_MESSAGE_ID = "QualifierCheck-9";
  /**
   * if a list of permitted values exceeds this limit, they will be displayed in a separate page,
   * rather than a tooltip
   */
  private static final int MAX_VALID_VALUES_SIZE = 20;
  /*
   * for /lat_lon qualifier value: ex: 30.13 N 6.13 E
   * latitude value i.e. 30.13 must not be greater than 90.00
   * longitude value i.e. 6.13 must not be grater than 180.00
   */
  private static final double MAX_LATITUDE_VALUE = 90.00;
  private static final double MAX_LONGITUDE_VALUE = 180.00;

  private QualifierHelper() {}

  public static Optional<ValidationMessage<Origin>> checkNoValue(
      Qualifier qualifier, boolean noValue, String featureName) {
    Optional<ValidationMessage<Origin>> message = Optional.empty();
    String value = qualifier.getValue();
    if (!noValue && (value == null || value.equals(""))) {
      message =
          Optional.of(
              EntryValidations.createMessage(
                  qualifier.getOrigin(),
                  Severity.ERROR,
                  NO_VALUE_ID,
                  qualifier.getName(),
                  featureName));
    } else if (noValue && value != null && !value.isEmpty()) {
      message =
          Optional.of(
              EntryValidations.createMessage(
                  qualifier.getOrigin(),
                  Severity.ERROR,
                  NO_VALUE_ID_2,
                  qualifier.getName(),
                  featureName));
    }
    return message;
  }

  public static ValidationResult checkRegEx(QualifierInfo qualifierInfo, Qualifier qualifier) {
    ValidationResult result = new ValidationResult();
    if (null != qualifier.getValue() && null != qualifierInfo.getRegex()) {
      Matcher matcher = Utils.matcher(qualifierInfo.getRegex(), qualifier.getValue());
      if (!matcher.matches()) {
        if (!isAlternateRegexMatch(qualifier)) {
          ValidationMessage<Origin> message =
              EntryValidations.createMessage(
                  qualifier.getOrigin(),
                  Severity.ERROR,
                  REGEX_FAIL_ID,
                  qualifier.getName(),
                  qualifier.getValue(),
                  qualifierInfo.getRegex());
          if (qualifierInfo.getCuratorComments() != null) {
            message.setCuratorMessage(qualifierInfo.getCuratorComments());
          }
          result.append(message);
        }
      } else if (!qualifierInfo
          .getRegexGroupInfos()
          .isEmpty()) { // there are specific group requirements
        for (QualifierHelper.RegexGroupInfo groupInfo : qualifierInfo.getRegexGroupInfos()) {
          int groupNumber = groupInfo.getGroupNumber();
          String group = matcher.group(groupNumber);
          List<String> validValues = groupInfo.getValues();

          if ((group == null && !groupInfo.canBeNonMatch())
              || (group != null
                  && !valuesContains(validValues, group, groupInfo.isCaseInseneitive()))) {
            if (!"inference".equals(qualifier.getName())
                && !qualifier
                    .getValue()
                    .equalsIgnoreCase(
                        "non-experimental evidence, no additional details recorded")) {
              ValidationMessage<Origin> message =
                  EntryValidations.createMessage(
                      qualifier.getOrigin(),
                      Severity.ERROR,
                      REGEX_GROUP_FAIL_ID,
                      qualifier.getName(),
                      group);

              if (validValues.size() > MAX_VALID_VALUES_SIZE) {
                message.setReportMessage(
                    Utils.paramArrayToCuratorReportString(validValues.toArray()));
              } else {
                String permittedValues = Utils.paramArrayToCuratorTipString(validValues.toArray());
                message.setCuratorMessage("Permitted values are : " + permittedValues);
              }
              result.append(message);
            }
          }
        }
      }
    }
    return result;
  }

  public static ValidationResult checkLatLonRange(
      QualifierInfo qualifierInfo, Qualifier qualifier) {
    ValidationResult result = new ValidationResult();

    String regex = qualifierInfo.getRegex();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(qualifier.getValue());
    if (matcher.find()) {
      Double lat = getLatLonVal(matcher.group(1));
      Double lon = getLatLonVal(matcher.group(4));
      String direcSN = matcher.group(3); // if not required delete these 2 assignments
      String direcWE = matcher.group(6);

      if (lat < 0) { // need to do fixing for the direction direcSN
        lat = -lat;
      }
      if (lon < 0) { // need to do fixing for the direction direcWE
        lon = -lon;
      }
      if (lat > MAX_LATITUDE_VALUE) {
        result.append(
            EntryValidations.createMessage(
                qualifier.getOrigin(),
                Severity.ERROR,
                LAT_LON_MESSAGE_ID1,
                qualifier.getName(),
                lat,
                MAX_LATITUDE_VALUE));
      }
      if (lon > MAX_LONGITUDE_VALUE) {
        result.append(
            EntryValidations.createMessage(
                qualifier.getOrigin(),
                Severity.ERROR,
                LAT_LON_MESSAGE_ID2,
                qualifier.getName(),
                lat,
                MAX_LONGITUDE_VALUE));
      }
    }

    return result;
  }

  private static Double getLatLonVal(String s) {
    Double lon = null;
    if (s != null) {
      lon = Double.valueOf(s);
      lon = Math.floor(lon * 10000 + 0.5) / 10000;
    }
    return lon;
  }

  public static ValidationResult checkProteinIdVersion(
      QualifierInfo qualifierInfo, Qualifier qualifier) {
    ValidationResult result = new ValidationResult();
    Integer version = 0;
    String regex = qualifierInfo.getRegex();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(qualifier.getValue());
    if (matcher.find()) {
      version = Integer.valueOf(matcher.group(3));
      if (version < 1) {
        result.append(
            EntryValidations.createMessage(
                qualifier.getOrigin(),
                Severity.ERROR,
                PROTEIN_ID_VERSION_MESSAGE_ID,
                qualifier.getName(),
                qualifier.getName()));
      }
    }
    return result;
  }

  /**
   * Sets the regex group ids for a particular qualifier type that are permitted to not match. For
   * example, the "collection_date" qualifier has a regular expression where group '3' can be the
   * month, which should match a CV ("21-Oct-1952") but could also be just the year ("1952"), in
   * which case group 3 will not match (i.e. exist at all) but this does not represent a validation
   * failure. This map keeps track of the qualifier names that tolerate a does-not-exist for a
   * particular group number. Maintaining by hand for now.
   */
  public static List<RegexGroupInfo> setNullGroupTolerance(QualifierInfo qualifierInfo) {

    return qualifierInfo.getRegexGroupInfos().stream()
        .map(
            rgInfo -> {
              if (rgInfo.getGroupNumber() == 3) {
                rgInfo.setNonMatch(true);
              }
              return rgInfo;
            })
        .collect(Collectors.toList());
  }

  private static boolean isAlternateRegexMatch(Qualifier qualifier) {
    if (qualifier != null && qualifier.getName().equals(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME)) {
      Matcher matcher = TranslExceptQualifier.PATTERN.matcher(qualifier.getValue());
      return matcher.matches();
    }

    return false;
  }

  private static boolean valuesContains(
      List<String> validValues, String toMatch, boolean caseInsensitive) {
    for (String value : validValues) {
      if (caseInsensitive) {
        if (value.equalsIgnoreCase(toMatch)) {
          return true;
        }
      } else {
        if (value.equals(toMatch)) {
          return true;
        }
      }
    }
    return false;
  }

  public static class QualifierInfo {
    private final String name;
    private final String regex;
    private final String curatorComments;
    private final boolean noValue;
    private final boolean newField;
    private List<RegexGroupInfo> regexGroupInfos = new ArrayList<>();

    private QualifierInfo(
        String name, String regex, boolean noValue, boolean newField, String curatorComments) {
      this.name = name;
      this.regex = regex;
      this.noValue = noValue;
      this.newField = newField;
      this.curatorComments = curatorComments;
    }

    public String getName() {
      return name;
    }

    public String getRegex() {
      return regex;
    }

    public boolean isNoValue() {
      return noValue;
    }

    public boolean isNewField() {
      return newField;
    }

    public void addRegexGroupInfo(RegexGroupInfo info) {
      regexGroupInfos.add(info);
    }

    public void addRegexGroupInfos(List<RegexGroupInfo> regexGroupInfos) {
      this.regexGroupInfos = regexGroupInfos;
    }

    public List<RegexGroupInfo> getRegexGroupInfos() {
      return regexGroupInfos;
    }

    public String getCuratorComments() {
      return curatorComments;
    }
  }

  public static class RegexGroupInfo {
    private final int groupNumber;
    private final List<String> values;
    private boolean nonMatch = false; // default
    private boolean caseInseneitive = false; // default

    RegexGroupInfo(String groupId) {
      this.groupNumber = Integer.valueOf(groupId);
      this.values = new ArrayList<String>();
    }

    public int getGroupNumber() {
      return groupNumber;
    }

    public List<String> getValues() {
      return values;
    }

    public void addValues(List<String> strings) {
      values.addAll(strings);
    }

    public void setNonMatch(boolean nonMatch) {
      this.nonMatch = nonMatch;
    }

    /**
     * Is permissible for this group to not match in the regex and still be valid.
     *
     * @return true if non-match permitted
     */
    public boolean canBeNonMatch() {
      return nonMatch;
    }

    /**
     * whether the regexp cares about case
     *
     * @return
     */
    public boolean isCaseInseneitive() {
      return caseInseneitive;
    }

    public void setCaseInseneitive(boolean caseInseneitive) {
      this.caseInseneitive = caseInseneitive;
    }
  }

  public static Set<String> getArtemisQualifierSet() {

    Set<String> artemisQualifiersSet = new TreeSet<>();
    List<DataRow> dataRows = GlobalDataSets.getRows(GlobalDataSetFile.ARTEMIS_QUALIFIERS);
    if (null == dataRows) {
      throw new IllegalArgumentException("");
    } else {
      for (DataRow dataRow : dataRows) {
        String artemisQualifier = Utils.parseTSVString(dataRow.getString(0));
        artemisQualifiersSet.add(artemisQualifier);
      }
    }
    return artemisQualifiersSet;
  }

  public static Map<String, QualifierInfo> getQualifierMap() {
    HashMap<String, QualifierInfo> qualifierMap = new HashMap<>();
    List<DataRow> dataRows = GlobalDataSets.getRows(GlobalDataSetFile.FEATURE_QUALIFIER_VALUES);
    if (null == dataRows) {
      throw new IllegalArgumentException("");
    } else {
      for (DataRow dataRow : dataRows) {
        String qualifierName = Utils.parseTSVString(dataRow.getString(0));
        String noValue = Utils.parseTSVString(dataRow.getString(1));
        String newField = Utils.parseTSVString(dataRow.getString(2));
        String regex = Utils.parseTSVString(dataRow.getString(4));
        String comments = Utils.parseTSVString(dataRow.getString(6));

        QualifierInfo qualifierInfo =
            new QualifierInfo(
                qualifierName, regex, noValue.equals("Y"), newField.equals("Y"), comments);
        if (!qualifierName.equals("EC_number")) { // EMD-2496
          List<DataRow> regexpRows = GlobalDataSets.getRows(GlobalDataSetFile.FEATURE_REGEX_GROUPS);
          if (null == regexpRows) {
            throw new IllegalArgumentException("");
          } else {
            for (DataRow regexpRow :
                regexpRows) { // look at all the qualifier values associatesd with a regexp group
              if (regexpRow.getString(0).equals(qualifierName)) {
                String regexGroupId = Utils.parseTSVString(regexpRow.getString(1));
                boolean caseInsensitive =
                    Utils.parseTSVString(regexpRow.getString(2)).equals("TRUE");
                String[] regexpGroupValues = regexpRow.getStringArray(3);

                RegexGroupInfo groupInfo = new RegexGroupInfo(regexGroupId);
                groupInfo.addValues(Arrays.asList(regexpGroupValues));
                groupInfo.setCaseInseneitive(caseInsensitive);
                qualifierInfo.addRegexGroupInfo(groupInfo);
              }
            }
          }
        }
        qualifierMap.put(qualifierName, qualifierInfo);
      }
    }
    return qualifierMap;
  }
}
