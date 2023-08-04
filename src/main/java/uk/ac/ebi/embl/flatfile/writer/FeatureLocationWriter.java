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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.*;

/** Flat file writer for the feature names and locations. */
public class FeatureLocationWriter extends FlatFileWriter {

  protected Feature feature;

  public FeatureLocationWriter(
      Entry entry,
      Feature feature,
      WrapType wrapType,
      String featureHeader,
      String qualifierHeader) {
    super(entry, wrapType);
    this.feature = feature;
    this.featureHeader = featureHeader;
    this.qualifierHeader = qualifierHeader;
    wrapChar = WrapChar.WRAP_CHAR_COMMA;
  }

  private String featureHeader;
  private String qualifierHeader;

  public boolean write(Writer writer) throws IOException {
    if (feature == null) {
      return false;
    }
    String block = "";
    if (feature.getLocations() != null) {
      CompoundLocation<Location> location = feature.getLocations();
      block = renderCompoundLocation(location);
    }
    writeBlock(
        writer,
        featureHeader + feature.getName() + getFeaturePadding(feature.getName()),
        qualifierHeader,
        block);
    return true;
  }

  public static String getFeaturePadding(String name) {
    int headerLength = 16 - name.length();
    String headerString = "                ";
    return headerString.substring(0, headerLength);
  }

  public static String renderCompoundLocation(CompoundLocation<Location> compoundLocation) {

    StringBuilder block = new StringBuilder();
    List<Location> locations = compoundLocation.getLocations();

    if (locations.size() > 0) {
      if (compoundLocation.isComplement()) {
        block.append("complement(");
      }

      if (locations.size() > 1) {

        if (compoundLocation instanceof Join<?>) {
          block.append("join(");
        } else if (compoundLocation instanceof Order<?>) {
          block.append("order(");
        }
      }

      boolean leftPartial = compoundLocation.isLeftPartial();
      boolean rightPartial = compoundLocation.isRightPartial();

      if (locations.size() == 1) {
        renderLocation(block, locations.get(0), leftPartial, rightPartial);
      } else { // need to be a bit clever regarding wheter to render left and right partial
        boolean firstLocation = true;
        Iterator<Location> iterator = locations.iterator();
        while (iterator.hasNext()) {
          Location location = iterator.next();
          if (firstLocation) {
            renderLocation(block, location, leftPartial, false);
          } else if (!iterator.hasNext()) { // last location
            block.append(",");
            renderLocation(block, location, false, rightPartial);
          } else {
            block.append(",");
            renderLocation(block, location, false, false);
          }
          firstLocation = false;
        }
        block.append(")");
      }

      if (compoundLocation.isComplement()) {
        block.append(")");
      }
    }
    return block.toString();
  }

  public static void renderLocation(
      StringBuilder block, Location location, boolean leftPartial, boolean rightPartial) {
    boolean isComplement = location.isComplement();
    if (isComplement) {
      // Complement location.
      block.append("complement(");
    }

    if (location instanceof RemoteLocation && ((RemoteLocation) location).getAccession() != null) {
      // Remote location.
      block.append(((RemoteLocation) location).getAccession());
      if (((RemoteLocation) location).getVersion() != null) {
        block.append(".");
        block.append(((RemoteLocation) location).getVersion());
      }
      block.append(":");
    }

    if (location instanceof Base && location.getBeginPosition() != null) {
      renderBase(block, location.getBeginPosition(), isComplement, leftPartial, rightPartial);
    } else if (location instanceof Range
        && location.getBeginPosition() != null
        && location.getEndPosition() == null) {
      renderBase(block, location.getBeginPosition(), isComplement, leftPartial, rightPartial);
    } else if (location instanceof Range
        && location.getBeginPosition() != null
        && location.getEndPosition() != null
        && location.getBeginPosition().equals(location.getEndPosition())) {
      renderBase(block, location.getBeginPosition(), isComplement, leftPartial, rightPartial);
    } else if (location instanceof Range
        && location.getBeginPosition() != null
        && location.getEndPosition() != null) {
      renderRange(
          block,
          location.getBeginPosition(),
          location.getEndPosition(),
          isComplement,
          leftPartial,
          rightPartial);
    } else if (location instanceof Between) {
      renderBetween(
          block,
          location.getBeginPosition(),
          location.getEndPosition(),
          isComplement,
          leftPartial,
          rightPartial);
    } else if (location instanceof Gap) {
      renderGap(block, (Gap) location);
    }

    if (isComplement) {
      block.append(")");
    }
  }

  private static void renderBase(
      StringBuilder block,
      Long position,
      boolean isComplement,
      boolean leftPartial,
      boolean rightPartial) {
    // Partiality.
    if (isComplement) {
      if (rightPartial) {
        block.append("<");
      } else if (leftPartial) {
        block.append(">");
      }
    } else {
      if (leftPartial) {
        block.append("<");
      } else if (rightPartial) {
        block.append(">");
      }
    }
    block.append(position.toString());
  }

  private static void renderRange(
      StringBuilder block,
      Long beginPosition,
      Long endPosition,
      boolean isComplement,
      boolean leftPartial,
      boolean rightPartial) {
    renderRangeOrBetween(
        block, beginPosition, endPosition, isComplement, leftPartial, rightPartial, "..");
  }

  private static void renderBetween(
      StringBuilder block,
      Long beginPosition,
      Long endPosition,
      boolean isComplement,
      boolean leftPartial,
      boolean rightPartial) {
    renderRangeOrBetween(
        block, beginPosition, endPosition, isComplement, leftPartial, rightPartial, "^");
  }

  private static void renderGap(StringBuilder block, Gap gap) {
    if (gap.isUnknownLength()) {
      block.append("gap(unk");
      block.append(gap.getLength());
      block.append(")");
    } else {
      block.append("gap(");
      block.append(gap.getLength());
      block.append(")");
    }
  }

  private static void renderRangeOrBetween(
      StringBuilder block,
      Long beginPosition,
      Long endPosition,
      boolean isComplement,
      boolean leftPartial,
      boolean rightPartial,
      String separator) {
    // Partiality.
    if (isComplement) {
      if (rightPartial) {
        block.append("<");
      }
    } else {
      if (leftPartial) {
        block.append("<");
      }
    }
    block.append(beginPosition.toString());
    block.append(separator);
    // Partiality.
    if (isComplement) {
      if (leftPartial) {
        block.append(">");
      }
    } else {
      if (rightPartial) {
        block.append(">");
      }
    }
    block.append(endPosition.toString());
  }
}
