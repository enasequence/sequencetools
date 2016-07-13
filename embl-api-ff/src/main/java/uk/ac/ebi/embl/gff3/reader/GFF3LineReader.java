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
package uk.ac.ebi.embl.gff3.reader;

import uk.ac.ebi.embl.api.gff3.GFF3Record;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;

import java.util.HashMap;
import java.util.Map;

import static uk.ac.ebi.embl.flatfile.FlatFileUtils.parseHexString;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 15-Sep-2010
 * Time: 11:14:36
 * To change this template use File | Settings | File Templates.
 */
public class GFF3LineReader extends GFF3AbstractLineReader {

    //used by unit tests
    protected GFF3LineReader() {
    }

    public GFF3LineReader(LineReader lineReader) {
        super(lineReader);
    }

    @Override
    protected void readLine(String line) {
        String[] tokens = line.split("\t");
//        System.out.println("tokens = " + tokens.length);
        if (tokens.length != 9) {
            error("GFF.1", tokens.length);
            return;
        }

        GFF3Record record = new GFF3Record();
        String seqId = tokens[0];
        if (isDefined(seqId)) {
            record.setSequenceID(parseHexString(seqId));
        }

        String source = tokens[1];
        if (isDefined(source)) {
            record.setSource(parseHexString(source));
        }

        String type = tokens[2];
        if (isDefined(type)) {
            String parsedTypeString = parseHexString(type);
            record.setType(parsedTypeString);
        }

        String startString = tokens[3];
        if (isDefined(startString)) {
            try {
                int startInt = Integer.valueOf(startString);
                record.setStart(startInt);
            } catch (NumberFormatException e) {
                error("GFF.3", startString);
            }
        }

        String endString = tokens[4];

        if (isDefined(endString)) {
            try {
                int endInt = Integer.valueOf(endString);
                record.setEnd(endInt);
            } catch (NumberFormatException e) {
                error("GFF.4", endString);
            }
        }

        String scoreString = tokens[5];
        if (isDefined(scoreString)) {
            try {
                double scoreDouble = Double.valueOf(scoreString);
                record.setScore(scoreDouble);
            } catch (NumberFormatException e) {
                error("GFF.5", scoreString);
            }
        }

        String strandString = tokens[6];
        if (isDefined(strandString)) {
            if (strandString.equals(GFF3Record.POSITIVE_STRAND)) {
                record.setStrand(1);
            } else if (strandString.equals(GFF3Record.NEGATIVE_STRAND)) {
                record.setStrand(-1);
            } else if (strandString.equals(GFF3Record.UNKNOWN_STRAND)) {
                record.setStrand(0);
            } else {
                error("GFF.6", strandString);
            }
        }

        String phaseString = tokens[7];
        if (isDefined(phaseString)) {
            if (phaseString.equals(GFF3Record.PHASE_0)) {
                record.setPhase(0);
            } else if (phaseString.equals(GFF3Record.PHASE_1)) {
                record.setPhase(1);
            } else if (phaseString.equals(GFF3Record.PHASE_2)) {
                record.setPhase(2);
            } else {
                error("GFF.7", phaseString);
            }
        }else if(type.equals(GFF3RecordSet.CDS_TYPE)){
            error("GFF.9", phaseString);
        }

        String attributeString = tokens[8];
        if (isDefined(attributeString)) {
            Map<String, String> attributes = parseAttirbutes(attributeString);
            record.setAttributes(attributes);
        }

        record.setOrigin(getOrigin());
        this.recordSet.addRecord(record);
    }

    private Map<String, String> parseAttirbutes(String attributeString) {

        Map<String, String> results = new HashMap<String, String>();
        String[] tokens = attributeString.split(";");
        for (String keyValuePair : tokens) {
            String[] keyValuePairTokens = keyValuePair.split("=");
            if (keyValuePairTokens.length != 2) {
                error("GFF.8", keyValuePair);
                break;
            }

            String key = keyValuePairTokens[0];
            String value = keyValuePairTokens[1];

            results.put(key, value);

        }

        return results;
    }

    private boolean isDefined(String field) {
        return !field.equals(GFF3Record.UNDEFINED_FIELD);
    }
}
