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
package uk.ac.ebi.embl.api.gff3;

import uk.ac.ebi.embl.api.validation.Origin;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 15-Sep-2010
 * Time: 11:03:09
 * To change this template use File | Settings | File Templates.
 */
public class GFF3Record {

    private String sequenceID;
    private String source;
    private String type;
    private int start;
    private int end;
    private double score;
    private int strand;
    private int phase;
    private Map<String, String> attributes;

    private Origin origin;

    public static final String POSITIVE_STRAND = "+";
    public static final String NEGATIVE_STRAND = "-";
    public static final String UNKNOWN_STRAND = "?";

    public static final String PHASE_0 = "0";
    public static final String PHASE_1 = "1";
    public static final String PHASE_2 = "2";

    public static final String UNDEFINED_FIELD = ".";

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String,String> attributes) {
        this.attributes = attributes;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Origin getOrigin() {
        return origin;
    }
}
