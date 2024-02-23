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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.DataclassProvider;

@Description(
    "Keywords exists in DE Line must exists in KW Line ex:\"complete genome\""
        + "ID Line Dataclass \"{0}\" and Keyword Dataclass \"{1}\" are not identical"
        + "Multiple keyword dataclasses are not allowed "
        + "Keyword \"{0}\" must not exist in the CON dataclass Entry"
        + "missing keyword \"{0}\" for dataclass \"{1}\""
        + "\"{0}\" keywords are not valid for dataclass \"{1}\"")
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    })
public class KWCheck extends EntryValidationCheck {
  private static final String DATACLASS_KEYWORD_ID = "KWCheck_1";
  private static final String MULTIPLE_DATACLASS_KEYWORD_ID = "KWCheck_2";
  private static final String CON_DATACLASS_KEYWORD_ID = "KWCheck_3";
  private static final String MISSING_DATACLASS_KEYWORD = "KWCheck_4";
  private static final String INVALID_DATACLASS_KEYWORD = "KWCheck_5";
  private static final String DE_KEYWORD = "KWCheck_6";

  public KWCheck() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    DataSet dataSet1 = GlobalDataSets.getDataSet(GlobalDataSetFile.CON_NO_KEYWORDS);
    DataSet dataSet2 = GlobalDataSets.getDataSet(GlobalDataSetFile.KEYWORD_DATACLASS);

    if (entry == null) return result;

    checkTPAandTSADescriptionAndKeyword(entry, entry.getDescription().getText());

    String idLineDataclass = entry.getDataClass();
    ArrayList<String> keywordDataclassList = DataclassProvider.getKeywordDataclass(entry, dataSet2);

    if (idLineDataclass == null && keywordDataclassList.isEmpty()) {
      return result;
    }
    if (keywordDataclassList.size() > 1) {
      StringBuilder differentDatclasses = new StringBuilder();
      for (String dataclass : keywordDataclassList) {
        differentDatclasses.append(dataclass);
      }
      reportError(entry.getOrigin(), MULTIPLE_DATACLASS_KEYWORD_ID, differentDatclasses);

    } else if (keywordDataclassList.size() == 1) {
      String keywordDataclass = keywordDataclassList.get(0);
      boolean isKeywordDataclass = keywordDataclass != "XXX";
      if (isKeywordDataclass
          && keywordDataclass != null
          && keywordDataclass.equals(Entry.TPA_DATACLASS)
          && (idLineDataclass.equals(Entry.CON_DATACLASS)
              || idLineDataclass.equals(Entry.WGS_DATACLASS)
              || idLineDataclass.equals(Entry.STD_DATACLASS)
              || idLineDataclass.equals(Entry.TSA_DATACLASS))) {
        // ok
      } else if (keywordDataclass != null
          && isKeywordDataclass
          && !keywordDataclass.equals(idLineDataclass)
          && idLineDataclass != null) {
        if (keywordDataclass.equals(Entry.WGS_DATACLASS)
            && idLineDataclass.equals(Entry.SET_DATACLASS)) {

        } else
          reportError(entry.getOrigin(), DATACLASS_KEYWORD_ID, idLineDataclass, keywordDataclass);
      }
    }

    boolean wgs = false;

    boolean tpa1 = false;

    boolean tpa2 = false;

    boolean tpa3 = false;

    boolean tpa4 = false;

    boolean tpa5 = false;

    boolean tpx1 = false;

    boolean tpx2 = false;
    List<Text> keywords = entry.getKeywords();

    for (Text key : keywords) {
      String keyword = key.getText();

      if (keyword.equals("WGS")) {
        wgs = true;
      } else if (keyword.equals("TPA")) {
        tpa1 = true;
      } else if (keyword.equals("Third Party Annotation") || keyword.equals("Third Party Data")) {
        tpa2 = true;
      } else if (keyword.equals("TPA:experimental")) {
        tpa3 = true;
      } else if (keyword.equals("TPA:inferential")) {
        tpa4 = true;
      } else if (keyword.equals("TPA:reassembly")) {
        tpa5 = true;
      } else if (keyword.equals("TPA Extra")) {
        tpx1 = true;
      } else if (keyword.equals("Third Party Annotation Extra")) {
        tpx2 = true;
      }
    }
    if (idLineDataclass != null && idLineDataclass.equals(Entry.WGS_DATACLASS)) {
      if (!wgs) {
        reportError(
            entry.getOrigin(), MISSING_DATACLASS_KEYWORD, Entry.WGS_DATACLASS, idLineDataclass);
      }
    } else {
      if (wgs) {
        reportError(
            entry.getOrigin(), INVALID_DATACLASS_KEYWORD, Entry.WGS_DATACLASS, idLineDataclass);
      }
    }
    if ((tpa1 || tpa2 || tpa3 || tpa4 || tpa5)
        && idLineDataclass != null
        && (idLineDataclass.equals(Entry.WGS_DATACLASS)
            || idLineDataclass.equals(Entry.CON_DATACLASS)
            || idLineDataclass.equals(Entry.TSA_DATACLASS)
            || // EMD-5315
            idLineDataclass.equals(Entry.STD_DATACLASS))) {
      if (!tpa1) {
        reportError(
            entry.getOrigin(), MISSING_DATACLASS_KEYWORD, Entry.TPA_DATACLASS, idLineDataclass);
      }

      if (!tpa2) {
        reportError(
            entry.getOrigin(),
            MISSING_DATACLASS_KEYWORD,
            "Third Party Annotation",
            idLineDataclass);
      }

    } else {
      if (tpa1 || tpa2 || tpa3 || tpa4 || tpa5) {
        reportError(
            entry.getOrigin(), INVALID_DATACLASS_KEYWORD, Entry.TPA_DATACLASS, idLineDataclass);
      }
    }

    if (idLineDataclass != null && idLineDataclass.equals(Entry.TPX_DATACLASS)) {
      if (!tpx1) {
        reportError(entry.getOrigin(), MISSING_DATACLASS_KEYWORD, "TPA Extra", idLineDataclass);
      }

      if (!tpx2) {
        reportError(
            entry.getOrigin(),
            MISSING_DATACLASS_KEYWORD,
            "Third Party Annotation Extra",
            idLineDataclass);
      }
    } else {
      if (tpx1 || tpx2) {
        reportError(
            entry.getOrigin(), INVALID_DATACLASS_KEYWORD, Entry.TPX_DATACLASS, idLineDataclass);
      }
    }

    if (idLineDataclass != null && idLineDataclass.equals(Entry.CON_DATACLASS)) {
      for (DataRow row : dataSet1.getRows()) {
        if (hasKeyword(entry, row.getString(0)))
          reportError(entry.getOrigin(), CON_DATACLASS_KEYWORD_ID, row.getString(0));
      }
    }

    return result;
  }

  /** this method is used by EntryProjectIdCheck */
  public boolean hasDEKeyword(Entry entry, String keyword) {

    if (entry.getDescription() == null) {
      return false;
    }
    String descriptionText = entry.getDescription().getText();
    if (descriptionText != null) {
      descriptionText = trimRight(descriptionText, '.');
      String[] descriptionKeywords = descriptionText.split(" ");
      if (descriptionKeywords.length < 2) {
        return false;
      }
      String deKeyword =
          descriptionKeywords[(descriptionKeywords.length) - 2]
              + " "
              + descriptionKeywords[(descriptionKeywords.length) - 1];
      deKeyword = trimLeft(deKeyword, ' ');
      return deKeyword.equals(keyword);
    }

    return false;
  }

  public boolean hasKeyword(Entry entry, String keyword) {
    List<Text> keywords = entry.getKeywords();
    if (keywords.isEmpty()) {
      return false;
    }
    for (Text textKeyword : keywords) {
      String stringKeyword = textKeyword.getText();
      if (stringKeyword.equalsIgnoreCase(keyword)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes all whitespace characters and instances of the given character from the end of the
   * string.
   */
  public static String trimRight(String string, char c) {
    for (int i = string.length(); i > 0; --i) {
      if (string.charAt(i - 1) != c
          && string.charAt(i - 1) != ' '
          && string.charAt(i - 1) != '\t'
          && string.charAt(i - 1) != '\n'
          && string.charAt(i - 1) != '\r') {
        return string.substring(0, i);
      }
    }
    return "";
  }

  /**
   * Removes all whitespace characters and instances of the given character from the beginning of
   * the string.
   */
  public static String trimLeft(String string, char c) {
    for (int i = 0; i < string.length(); ++i) {
      if (string.charAt(i) != c
          && string.charAt(i) != ' '
          && string.charAt(i) != '\t'
          && string.charAt(i) != '\n'
          && string.charAt(i) != '\r') {
        return string.substring(i);
      }
    }
    return string;
  }

  /*
   * check is there any TPA keywords exists in entry KW line
   */

  private boolean hasTPAKeywords(Entry entry) {
    List<Text> keywords = entry.getKeywords();
    for (Text keyword : keywords) {
      String entryKeyword = keyword.getText();
      if (entryKeyword.equals("TPA")
          || entryKeyword.equals("Third Party Annotation")
          || entryKeyword.equals("TPA:experimental")
          || entryKeyword.equals("TPA:inferential")
          || entryKeyword.equals("TPA:reassembly")) {
        return true;
      }
    }
    return false;
  }

  private boolean hasTSAKeywords(Entry entry) {
    List<Text> keywords = entry.getKeywords();
    for (Text keyword : keywords) {
      String entryKeyword = keyword.getText();
      if (entryKeyword.equals("TSA") || entryKeyword.equals("Transcriptome Shotgun Assembly")) {
        return true;
      }
    }
    return false;
  }

  /*
   * Check the DE line keywords
   */
  public void checkTPAandTSADescriptionAndKeyword(Entry entry, String description) {
    String dataclass = entry.getDataClass();

    if (description == null || dataclass == null || description.length() < 6) return;

    if (Entry.CON_DATACLASS.equals(dataclass)
        || Entry.STD_DATACLASS.equals(dataclass)
        || Entry.TSA_DATACLASS.equals(dataclass)) {

      boolean hasTPADescription = description.startsWith("TPA: ");
      boolean hasTSADescription = description.startsWith("TSA: ");

      boolean hasTPAKeywords = hasTPAKeywords(entry);
      boolean hasTSAKeywords = hasTSAKeywords(entry);

      if (hasTPAKeywords && !hasTPADescription) {
        reportError(entry.getOrigin(), DE_KEYWORD, "", "TPA:");
      } else if (!hasTPAKeywords && hasTPADescription) {
        reportError(entry.getOrigin(), DE_KEYWORD, "not", "TPA:");
      }

      if (hasTSAKeywords && !hasTSADescription) {
        reportError(entry.getOrigin(), DE_KEYWORD, "", "TSA:");
      } else if (!hasTSAKeywords && hasTSADescription) {
        reportError(entry.getOrigin(), DE_KEYWORD, "not", "TSA:");
      }
    }
  }
}
