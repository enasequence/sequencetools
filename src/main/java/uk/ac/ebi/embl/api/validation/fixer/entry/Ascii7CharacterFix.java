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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Ascii7CharacterConverter;

/**
 * Removes diacritics and replaces non-printable ASCII7 characters with ? from:
 *
 * <ul>
 *   <li>comment
 *   <li>description
 *   <li>reference fields
 *   <li>feature qualifiers
 * </ul>
 */
public class Ascii7CharacterFix extends EntryValidationCheck {
  private static final String FIX_ID = "Ascii7CharacterFix_1";

  private final Ascii7CharacterConverter converter = new Ascii7CharacterConverter();

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) return result;

    if (entry.getComment() != null) {
      fix(
          entry.getComment().getText(),
          entry.getComment().getOrigin(),
          fixedText -> entry.getComment().setText(fixedText));
    }
    if (entry.getDescription() != null) {
      fix(
          entry.getDescription().getText(),
          entry.getDescription().getOrigin(),
          fixedText -> entry.getDescription().setText(fixedText));
    }

    for (Reference reference : entry.getReferences()) {
      fix(reference.getPublication(), reference.getOrigin());
      Publication publication = reference.getPublication();
      if (publication != null && publication instanceof Patent) {
        Patent patent = (Patent) publication;
        for (int i = 0; i < patent.getApplicants().size(); i++) {
          final int fixedIndex = i;
          fix(
              patent.getApplicants().get(i),
              patent.getOrigin(),
              fixedVal -> patent.getApplicants().set(fixedIndex, fixedVal));
        }
      }
    }
    for (Feature feature : entry.getFeatures()) {
      for (Qualifier qualifier : feature.getQualifiers()) {
        String qualifierValue = qualifier.getValue();
        if (qualifierValue != null) {
          fix(qualifierValue, qualifier.getOrigin(), fixedVal -> qualifier.setValue(fixedVal));
        }
      }
    }
    return result;
  }

  private void fix(String str, Origin origin, Consumer<String> replaceStr) {
    if (Ascii7CharacterConverter.doConvert(str)) {
      String fixedStr = converter.convert(str);
      reportMessage(Severity.FIX, origin, FIX_ID, str, fixedStr);
      replaceStr.accept(fixedStr);
    }
  }

  private static final Set<Class<?>> fixClasses =
      new HashSet<>(
          Arrays.asList(
              Publication.class,
              Article.class,
              Book.class,
              ElectronicReference.class,
              Patent.class,
              Submission.class,
              Thesis.class,
              Unpublished.class,
              Person.class));

  /** Fix all the String members of Publication and related objects using java reflection. */
  private void fix(Object obj, Origin origin) {
    if (obj == null) {
      return;
    }

    Class<?> objType = obj.getClass();
    while (objType != null) {
      if (!fixClasses.contains(objType)) {
        // Stop if class is not in the list of classes to fix.
        break;
      }

      // Iterate over fields
      for (Field field : objType.getDeclaredFields()) {
        // Allow changing private fields.
        field.setAccessible(true);

        try {
          Class fieldType = field.getType();
          Object fieldValue = field.get(obj);
          if (fieldValue == null) {
            continue;
          }

          if (fieldType == String.class) {
            // Fix String if required.
            String value = (String) fieldValue;
            if (value != null && Ascii7CharacterConverter.doConvert(value)) {
              // Fix String.
              String fixedValue = converter.convert(value);
              field.set(obj, fixedValue);
              reportMessage(Severity.FIX, origin, FIX_ID, value, fixedValue);
            }
          } else if (fixClasses.contains(fieldType)) {
            // Fix Strings in object if required.
            fix(field, origin);
          } else if (Collection.class.isAssignableFrom(fieldType)) {
            Collection<?> collection = (Collection<?>) fieldValue;
            if (collection != null) {
              for (Object element : collection) {
                Class elementType = element.getClass();
                if (fixClasses.contains(elementType)) {
                  // Fix Strings in object if required.
                  fix(element, origin);
                }
              }
            }
            // Fixing of Strings in Collection is not supported.
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      // Fix Strings in parent class if required.
      objType = objType.getSuperclass();
    }
  }
}
