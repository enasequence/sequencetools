/*
 * Copyright 2020-2022 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.ena.util;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.ena.exception.AccessionValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SequenceAccessionUtil {

    private static final String DEFAULT_RANGES_SEPARATOR = ",";

    private static final String DEFAULT_ACCESSIONS_SEPARATOR = "-";

    /**
     * Uses {@value #DEFAULT_RANGES_SEPARATOR} and {@value #DEFAULT_ACCESSIONS_SEPARATOR} as default ranges and accessions separators.
     *
     * @param rangesStr
     * @return List of accession ranges found in the given string.
     */
    public static List<AccessionRange> getAccessionRanges(String rangesStr) {
        return getAccessionRanges(rangesStr, DEFAULT_RANGES_SEPARATOR, DEFAULT_ACCESSIONS_SEPARATOR);
    }

    /**
     * Uses {@value #DEFAULT_RANGES_SEPARATOR} and {@value #DEFAULT_ACCESSIONS_SEPARATOR} as default ranges and accessions separators.
     *
     * @param rangesStr
     * @return List of all accessions found in the given string.
     */
    public static List<String> getAccessions(String rangesStr) {
        return getAccessions(rangesStr, DEFAULT_RANGES_SEPARATOR, DEFAULT_ACCESSIONS_SEPARATOR);
    }

    /**
     * Uses {@value #DEFAULT_RANGES_SEPARATOR} and {@value #DEFAULT_ACCESSIONS_SEPARATOR} as default ranges and accessions separators.
     *
     * @param rangesStr
     * @return Sum of all accession counts from all ranges in the given string.
     */
    public static int count(String rangesStr) {
        return count(rangesStr, DEFAULT_RANGES_SEPARATOR, DEFAULT_ACCESSIONS_SEPARATOR);
    }

    /**
     * Get list of accession ranges found in the string using the given separators.
     *
     * @param rangesStr
     * @param rangesSeparator
     * @param accessionsSeparator
     * @return
     */
    public static List<AccessionRange> getAccessionRanges(String rangesStr, String rangesSeparator, String accessionsSeparator) {
        List<AccessionRange> ranges = new ArrayList<>();
        if (rangesStr == null) {
            return ranges;
        }
        for (String range : rangesStr.split(rangesSeparator)) {
            range = range.trim();
            if (StringUtils.isNotBlank(range)) {
                String[] accessions = range.split(accessionsSeparator);
                if (accessions.length == 1) {
                    ranges.add(new AccessionRange(accessions[0], accessions[0]));
                } else if (accessions.length == 2) {
                    ranges.add(new AccessionRange(accessions[0], accessions[1]));
                } else {
                    throw new AccessionValidationException("Malformed range : " + range, rangesStr);
                }
            }
        }
        return ranges;
    }

    /**
     * Get list of all accessions found in the string using the given separators.
     *
     * @param rangesStr
     * @param rangesSeparator
     * @param accessionsSeparator
     * @return
     */
    public static List<String> getAccessions(String rangesStr, String rangesSeparator, String accessionsSeparator) {
        if (rangesStr == null) {
            return Collections.emptyList();
        }

        List<AccessionRange> ranges;
        try {
            ranges = getAccessionRanges(rangesStr, rangesSeparator, accessionsSeparator);
        } catch (Exception ex) {
            throw new AccessionValidationException("Unable to get extract ranges.", rangesStr);
        }

        Set<String> accessions = new TreeSet<>();
        for (AccessionRange range : ranges) {
            if (range.getFrom().equals(range.getTo())) {
                accessions.add(range.getFrom());
            } else {
                AccessionMatcher.Accession from = AccessionMatcher.getSplittedAccession(range.getFrom());
                AccessionMatcher.Accession to = AccessionMatcher.getSplittedAccession(range.getTo());
                if (from == null || to == null) {
                    throw new AccessionValidationException("Unable to extract accessions from range : " + range, rangesStr);
                }
                for (int i = Integer.parseInt(from.number); i <= Integer.parseInt(to.number); i++) {
                    String prefix = from.prefix + (from.version == null ? "" : from.version);
                    for (int j = 0; j < from.number.length() - String.valueOf(i).length(); j++) {
                        prefix += "0";
                    }
                    accessions.add(prefix + i);
                }
            }
        }

        return new ArrayList<>(accessions);
    }

    /**
     * Sum of all accession counts from all ranges in the string using given separators.
     *
     * @param rangesStr
     * @param rangesSeparator
     * @param accessionsSeparator
     * @return
     */
    public static int count(String rangesStr, String rangesSeparator, String accessionsSeparator) {
        if (rangesStr == null) {
            return 0;
        }
        int cnt = 0;

        for (String range : rangesStr.split(rangesSeparator)) {
            range = range.trim();
            if (StringUtils.isNotBlank(range)) {
                String accessions[] = range.split(accessionsSeparator);
                if (accessions.length == 1) {
                    cnt++;
                } else if (accessions.length == 2) {
                    AccessionMatcher.Accession start = AccessionMatcher.getSplittedAccession(accessions[0]);
                    AccessionMatcher.Accession end = AccessionMatcher.getSplittedAccession(accessions[1]);

                    cnt += 1 + Integer.parseInt(end.number) - Integer.parseInt(start.number);

                } else {
                    throw new AccessionValidationException("Malformed range : " + range, rangesStr);
                }
            }
        }

        return cnt;
    }
}
