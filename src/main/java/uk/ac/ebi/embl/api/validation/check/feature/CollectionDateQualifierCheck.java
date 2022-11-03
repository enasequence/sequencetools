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
package uk.ac.ebi.embl.api.validation.check.feature;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Feature qualifier \"{0}\" has invalid date i.e.{1}" +
		"Feature Qualifier \"{0}\" value must not be future date i.e {1}.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class CollectionDateQualifierCheck extends FeatureValidationCheck 
{

	private final static String DATE_FORMAT_ERROR = "CollectionDateQualifierCheck_1";
	private final static String FUTURE_DATE_ERROR = "CollectionDateQualifierCheck_2";
	private final static Pattern DATE_RANGE_PATTERN = Pattern.compile("^([^/]+)/([^/]+)$");

	public final static String INSDC_DATE_FORMAT_1 = "dd-MMM-yyyy"; // dd-MMM-yyyy
	public final static String INSDC_DATE_FORMAT_2 = "MMM-yyyy"; // MMM-yyyy
	public final static String ISO_DATE_FORMAT_1 = "yyyy"; // yyyy
	public final static String ISO_DATE_FORMAT_2 = "yyyy-MM"; // yyyy-MM
	public final static String ISO_DATE_FORMAT_3 = "yyyy-MM-dd"; // yyyy-MM-dd
	public final static String ISO_DATE_FORMAT_4 = "yyyy-MM-dd'T'HH'Z'"; // yyyy-MM-ddThhZ
	public final static String ISO_DATE_FORMAT_5 = "yyyy-MM-dd'T'HH:mm'Z'"; // yyyy-MM-ddThh:mmZ
	public final static String ISO_DATE_FORMAT_6 = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // yyyy-MM-ddThh:mmZ

	private final static DateTimeFormatter INSDC_DATE_FORMATTER_1 = formatter(INSDC_DATE_FORMAT_1); // dd-MMM-yyyy
	private final static DateTimeFormatter INSDC_DATE_FORMATTER_2 = formatter(INSDC_DATE_FORMAT_2); // MMM-yyyy
	private final static DateTimeFormatter ISO_DATE_FORMATTER_1 = formatter(ISO_DATE_FORMAT_1); // yyyy
	private final static DateTimeFormatter ISO_DATE_FORMATTER_2 = formatter(ISO_DATE_FORMAT_2); // yyyy-MM
	private final static DateTimeFormatter ISO_DATE_FORMATTER_3 = formatter(ISO_DATE_FORMAT_3); // yyyy-MM-dd
	private final static DateTimeFormatter ISO_DATE_FORMATTER_4 = formatter(ISO_DATE_FORMAT_4); // yyyy-MM-ddThhZ
	private final static DateTimeFormatter ISO_DATE_FORMATTER_5 = formatter(ISO_DATE_FORMAT_5); // yyyy-MM-ddThh:mmZ
	private final static DateTimeFormatter ISO_DATE_FORMATTER_6 = formatter(ISO_DATE_FORMAT_6); // yyyy-MM-ddThh:mmZ

	private final static List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
			INSDC_DATE_FORMATTER_1,
			INSDC_DATE_FORMATTER_2,
			ISO_DATE_FORMATTER_1,
			ISO_DATE_FORMATTER_2,
			ISO_DATE_FORMATTER_3,
			ISO_DATE_FORMATTER_4,
			ISO_DATE_FORMATTER_5,
			ISO_DATE_FORMATTER_6);

	final static int DEFAULT_MONTH = 1;
	final static int DEFAULT_DAY = 1;
	final static int DEFAULT_HOUR = 0;
	final static int DEFAULT_MINUTE = 0;
	final static int DEFAULT_SECOND = 0;

	private static DateTimeFormatter formatter(String pattern) {
		return new DateTimeFormatterBuilder().appendPattern(pattern)
				.parseDefaulting(ChronoField.MONTH_OF_YEAR, DEFAULT_MONTH)
				.parseDefaulting(ChronoField.DAY_OF_MONTH, DEFAULT_DAY)
				.parseDefaulting(ChronoField.HOUR_OF_DAY, DEFAULT_HOUR)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, DEFAULT_MINUTE)
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, DEFAULT_SECOND).toFormatter();
	}

	public ValidationResult check(Feature feature) 
	{
		result = new ValidationResult();
		if(feature==null)
		{
			return result;
		}
		
		List<Qualifier> collectionDateQualifiers = feature
				.getQualifiers(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);

		if (collectionDateQualifiers.size() == 0) 
		{
			return result;
		}

		for (Qualifier collectionQualifier : collectionDateQualifiers) 
		{
			String value = collectionQualifier.getValue();
			if (isValueEmpty(value)) {
				reportError(collectionQualifier.getOrigin(),
						DATE_FORMAT_ERROR, collectionQualifier.getName(), "");
				continue;
			}

			value = StringUtils.deleteWhitespace(value);
			try {
				if (!isValidDate(value)) {
					reportError(collectionQualifier.getOrigin(), DATE_FORMAT_ERROR, collectionQualifier.getName(), value);
				} else {
                    collectionQualifier.setValue(value);
                }
			} catch (FutureDateException e) {
				reportError(collectionQualifier.getOrigin(), FUTURE_DATE_ERROR, collectionQualifier.getName(), collectionQualifier.getValue());
			}
		}
		return result;
	}

	@Override
	public boolean isValid(final String value) {
		try {
			return !isValueEmpty(value) && isValidDate(StringUtils.deleteWhitespace(value));
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isValueEmpty(final String value) {
		return value == null || value.isEmpty() || StringUtils.deleteWhitespace(value).isEmpty();
	}

	/** Returns true if the date is valid or null if the date is invalid or in the future.
	 *
	 * @param value the date string
	 * @return true if the date is valid or null if the date is invalid.
	 * @throws FutureDateException if the date is in the future
	 */
	boolean isValidDate(String value) throws FutureDateException {

		Matcher dateRangeMatcher = DATE_RANGE_PATTERN.matcher(value);

		if (dateRangeMatcher.matches()) {
			ParseDateResult fromResult = parseDate(dateRangeMatcher.group(1));
			ParseDateResult toResult = parseDate(dateRangeMatcher.group(2));

			if (fromResult == null || toResult == null) {
				return false; // invalid date format
			} else {
				if (fromResult.formatter != toResult.formatter) { // Different range date formats.
					return false;
				}
				// To date must be after from date.
				return fromResult.date.compareTo(toResult.date) <= 0;
			}
		} else {
			ParseDateResult dateResult = parseDate(value);
			if (dateResult == null) {
				return false;// invalid date format
			}
		}
		return true;
	}

	static class FutureDateException extends Exception {
    }

	static class ParseDateResult
	{
		public ParseDateResult(String value, LocalDateTime date, DateTimeFormatter formatter) {
			this.value = value;
			this.date = date;
			this.formatter = formatter;
		}
		/** The date string. */
		public final String value;

		/** The parsed date. */
		public final LocalDateTime date;

		/** The used date formatted. */
		public final DateTimeFormatter formatter;
	}

	/** Parses the date and returns null if the date could not be parsed.
	 *
	 * @param value the date string
	 * @return date or null if the date could not be parsed.
	 * @throws FutureDateException if the date is in the future
	 */
	ParseDateResult parseDate(String value) throws FutureDateException
	{
		for (DateTimeFormatter formatter : DATE_FORMATTERS) {
			try {
				LocalDateTime date = LocalDateTime.from(formatter.parse(value));
				if (LocalDateTime.now().isBefore(date)) {
					throw new FutureDateException();
				}
				return new ParseDateResult(value, date, formatter);
			} catch (DateTimeParseException ex) {
				// Ignore error
			}
		}
		return null; // invalid date format
	}
}
