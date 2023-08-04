/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.template;

import java.util.Iterator;

public class StringBuilderLineIterator implements Iterator<String> {
  private static final String newline = "\n";

  private final StringBuilder builder;
  private boolean hasNext;
  private int currentLocation;
  private int extractToIndex;
  private int previousLocation;

  public StringBuilderLineIterator(StringBuilder builder) {
    this.builder = builder;
    currentLocation = 0;
    setNextNewLine();
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public String next() {
    if (hasNext()) {

      String nextLine = builder.substring(currentLocation, extractToIndex);
      previousLocation = currentLocation;
      currentLocation = extractToIndex;
      setNextNewLine();
      return nextLine.equals(newline) ? "" : nextLine;
    } else {
      throw new IllegalArgumentException("No new line to be read");
    }
  }

  private void setNextNewLine() {

    if (builder.length() == 0 || currentLocation >= builder.length()) {
      hasNext = false;
      return;
    }

    int searchFrom = currentLocation;
    //        if(currentLocation == 0){
    //            searchFrom = 0;//increment location if not the first element - as will be set to
    // the last new line otherwise
    //        }

    extractToIndex = builder.indexOf(newline, searchFrom);
    if (extractToIndex == -1) { // no more new lines
      extractToIndex = builder.length();
    } else {
      extractToIndex++;
    }

    hasNext = true;
  }

  @Override
  public void remove() {
    int lengthDeleted = currentLocation - previousLocation;
    builder.delete(previousLocation, currentLocation);
    currentLocation = currentLocation - lengthDeleted;
    setNextNewLine();
  }
}
