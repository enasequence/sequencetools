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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.DataclassProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Description("dataclass has been fixed to \"{0}\"" +
        "Dataclass Keyword \"{0}\" has been added to the entry")
@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class DataclassFix extends EntryValidationCheck {

    private final static String DATACLASS_FIX_ID = "DataclassFix_1";
    private final static String KEYWORD_FIX_ID = "DataclassFix_2";

    public enum DataclassKeywords {
        WGS(Arrays.asList(new Text(Entry.WGS_DATACLASS))),
        EST(Arrays.asList(new Text(Entry.EST_DATACLASS))),
        GSS(Arrays.asList(new Text(Entry.GSS_DATACLASS))),
        HTC(Arrays.asList(new Text(Entry.HTC_DATACLASS))),
        STS(Arrays.asList(new Text(Entry.STS_DATACLASS))),
        TSA(Arrays.asList(new Text(Entry.TSA_DATACLASS))),
        TLS(Arrays.asList(new Text(Entry.TLS_DATACLASS)));

        List<Text> keyword;

        DataclassKeywords(List<Text> keyword) {
            this.keyword = keyword;
        }

        static public List<Text> getKeywords(String dataclass) {
            if (dataclass == null)
                return null;
            try {
                return valueOf(dataclass.toUpperCase()).keyword;
            } catch (IllegalArgumentException x) {
                return null;
            }
        }
    }

    public DataclassFix() {
    }

    public ValidationResult check(Entry entry) {
        if (entry == null) {
            return result;
        }

        if (entry.getDataClass() != null && Entry.CON_DATACLASS.equals(entry.getDataClass())) {
            return result;
        }

        DataSet dataSet = GlobalDataSets.getDataSet(FileName.KEYWORD_DATACLASS);
        result = new ValidationResult();

        String dataclass = entry.getDataClass();
        String accession = entry.getPrimaryAccession();
        List<Text> keywords = entry.getKeywords();

        if ((dataclass == null || "XXX".equals(dataclass)) && keywords.size() == 0) {
            return result;
        }

        //Do not set dataclass from KW if there are multiple dataclass exists in KW
        String dataClassFromKw = null;
        for (DataRow row : dataSet.getRows()) {
            if (keywords.stream().anyMatch(kw -> kw.getText().equalsIgnoreCase(row.getString(1))) ||
                    keywords.stream().anyMatch(kw -> kw.getText().equalsIgnoreCase(row.getString(2)))) {
                if (dataClassFromKw == null)
                    dataClassFromKw = row.getString(0);
                else if (!dataClassFromKw.equals(row.getString(0))) {
                    dataClassFromKw = null;
                    break;
                }
            }
        }
        if (dataClassFromKw != null) {
            entry.setDataClass(dataClassFromKw);
            reportMessage(Severity.FIX, entry.getOrigin(), KEYWORD_FIX_ID, dataClassFromKw);
            return result;
        }

        if (dataclass == null || "XXX".equals(dataclass)) {
            String accessionDataclass = DataclassProvider.getAccessionDataclass(accession);

            if (accessionDataclass != null) {
                entry.setDataClass(accessionDataclass);
                reportMessage(Severity.FIX, entry.getOrigin(), DATACLASS_FIX_ID, entry.getDataClass());
            } else {
                ArrayList<String> keywordDataclassList = DataclassProvider.getKeywordDataclass(entry, dataSet);
                if (keywordDataclassList != null && keywordDataclassList.size() == 1 && !keywordDataclassList.contains("XXX")) {
                    entry.setDataClass(keywordDataclassList.get(0));
                    reportMessage(Severity.FIX, entry.getOrigin(), DATACLASS_FIX_ID, entry.getDataClass());
                }
            }

        } else {
            List<Text> dataclassKeywords = DataclassKeywords.getKeywords(dataclass);

            if (dataclassKeywords == null) {
                return result;
            }

            for (Text dataclassKeyword : dataclassKeywords) {
                if (dataclassKeyword != null && !keywords.contains(dataclassKeyword)) {
                    entry.addKeyword(dataclassKeyword);
                    reportMessage(Severity.FIX, entry.getOrigin(), KEYWORD_FIX_ID, dataclassKeyword.getText());
                }
            }

        }

        return result;
    }

}
