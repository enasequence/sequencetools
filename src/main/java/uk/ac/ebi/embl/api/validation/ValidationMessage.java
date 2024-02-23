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
package uk.ac.ebi.embl.api.validation;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ValidationMessage<T extends Origin> implements Serializable {

  private static final long serialVersionUID = -2932989221653951201L;

  public static final MessageFormatter TEXT_MESSAGE_FORMATTER_PRECEDING_LINE_END =
      new TextMessageFormatter("\n", "");
  public static final boolean writeCuratorMessage = true;
  public static final MessageFormatter TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END =
      new TextMessageFormatter("", "\n");
  public static final MessageFormatter TEXT_TIME_MESSAGE_FORMATTER_TRAILING_LINE_END =
      new TextTimeMessageFormatter("", "\n");
  private static MessageFormatter defaultMessageFormatter =
      ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END;

  /** Message severity. */
  private Severity severity;

  /** Message key (from message bundle) */
  private final String messageKey;

  /** Message parameters */
  private final Object[] params;

  /** Validation origin - where validation problem occurred */
  private final List<T> origins;

  /** The message to which the messageKey resolves */
  private String message;

  /** additional information to help resolve the error/warning/info */
  private String curatorMessage;

  /**
   * A full-text 'report' if needed - will be rendered on a separate page as should contain a lot of
   * text
   */
  private String reportMessage;

  /** An exception associated with the validation message. */
  private Throwable throwable;

  private ValidationMessage.MessageFormatter messageFormatter = getDefaultMessageFormatter();

  /** Static string denoting that there is no message key for this message */
  public static final String NO_KEY = "NO_KEY";

  public static void setDefaultMessageFormatter(MessageFormatter defaultMessageFormatter) {
    ValidationMessage.defaultMessageFormatter = defaultMessageFormatter;
  }

  public static MessageFormatter getDefaultMessageFormatter() {
    return ValidationMessage.defaultMessageFormatter;
  }

  public ValidationMessage(Severity severity, String messageKeyParam, Object... params) {
    this.severity = severity;
    this.params = params;
    this.origins = new ArrayList<T>();
    this.messageKey = messageKeyParam;
    if (!messageKeyParam.equals(NO_KEY)) {
      this.message = ValidationMessageManager.getString(messageKeyParam, params);
    }
  }

  public Object[] getParams() {
    return params;
  }

  /**
   * Returns the message string.
   *
   * @return message string
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getMessageKey() {
    return messageKey;
  }

  /**
   * Adds an origin to the validation message.
   *
   * @param origin an origin to be added to the validation message
   */
  protected void addOrigin(T origin) {
    if (origin != null) {
      this.origins.add(origin);
    }
  }

  /**
   * Adds a collection of origins to the validation message.
   *
   * @param origins the origins to be added to the validation message
   */
  protected void addOrigins(Collection<T> origins) {
    if (this.origins != null) {
      this.origins.addAll(origins);
    }
  }

  /**
   * Appends an origin to the validation message.
   *
   * @param origin an origin to be added to the validation message
   * @return a reference to this object
   */
  public ValidationMessage<T> append(T origin) {
    addOrigin(origin);
    return this;
  }

  /**
   * Appends origins to the validation message.
   *
   * @param origins origins to be added to the validation message
   * @return a reference to this object
   */
  public ValidationMessage<T> append(Collection<T> origins) {
    addOrigins(origins);
    return this;
  }

  /**
   * Gets an list of all origins.
   *
   * @return an unmodifiable list of all origins
   */
  public List<T> getOrigins() {
    return this.origins;
  }

  public boolean isHasCuratorMessage() {
    return curatorMessage != null;
  }

  public String getCuratorMessage() {
    return curatorMessage;
  }

  public void setCuratorMessage(String curatorMessage) {
    this.curatorMessage = curatorMessage;
  }

  public void appendCuratorMessage(String curatorMessage) {
    if (this.curatorMessage != null) {
      this.curatorMessage = this.curatorMessage + " " + curatorMessage;
    } else {
      this.curatorMessage = curatorMessage;
    }
  }

  public boolean isHasReportMessage() {
    return reportMessage != null;
  }

  public String getReportMessage() {
    return reportMessage;
  }

  public void setReportMessage(String reportMessage) {
    this.reportMessage = reportMessage;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public MessageFormatter getMessageFormatter() {
    return messageFormatter;
  }

  public void setMessageFormatter(MessageFormatter messageFormatter) {
    this.messageFormatter = messageFormatter;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("severity", severity);
    builder.append("messageKey", messageKey);
    builder.append("params", params);
    builder.append("origins", origins);
    builder.append("message", getMessage());
    return builder.toString();
  }

  /**
   * Creates a ValidationMessage - severity ERROR
   *
   * @param messageKey message key
   * @param params message parameters
   * @return a validation message - severity ERROR
   */
  public static ValidationMessage<Origin> error(String messageKey, Object... params) {
    return ValidationMessage.message(Severity.ERROR, messageKey, params);
  }

  /**
   * Creates a ValidationMessage - severity WARNING
   *
   * @param messageKey message key
   * @param params message parameters
   * @return a validation message - severity WARNING
   */
  public static ValidationMessage<Origin> warning(String messageKey, Object... params) {
    return ValidationMessage.message(Severity.WARNING, messageKey, params);
  }

  /**
   * Creates a ValidationMessage - severity INFO
   *
   * @param messageKey message key
   * @param params message parameters
   * @return a validation message - severity INFO
   */
  public static ValidationMessage<Origin> info(String messageKey, Object... params) {
    return ValidationMessage.message(Severity.INFO, messageKey, params);
  }

  /**
   * Creates a ValidationMessage with provided severity
   *
   * @param severity message severity
   * @param messageKey message key
   * @param params message parameters
   * @return a validation message - severity INFO
   * @return created validation message
   */
  public static ValidationMessage<Origin> message(
      Severity severity, String messageKey, Object... params) {
    return new ValidationMessage<>(severity, messageKey, params);
  }

  public interface MessageFormatter {
    void writeMessage(Writer writer, ValidationMessage<?> validationMessage, String targetOrigin)
        throws IOException;

    String getFormattedMessage(ValidationMessage<?> message, String targetOrigin);
  }

  public static class TextMessageFormatter implements MessageFormatter {
    private final String LINE_BEGIN;
    private final String LINE_END;
    private final String ORIGIN_BEGIN = "[";
    private final String ORIGIN_END = "]";
    private final String ORIGIN_SEPARATOR = ", ";

    public TextMessageFormatter(String lineBegin, String lineEnd) {
      this.LINE_BEGIN = lineBegin;
      this.LINE_END = lineEnd;
    }

    @Override
    public void writeMessage(Writer writer, ValidationMessage<?> message, String targetOrigin)
        throws IOException {
      writer.write(getFormattedMessage(message, targetOrigin));
    }

    protected String getMessagePrefix() {
      return "";
    }

    @Override
    public String getFormattedMessage(ValidationMessage<?> message, String targetOrigin) {
      List<Origin> allOrigins =
          new ArrayList<>(message.getOrigins().size() + (targetOrigin != null ? 1 : 0));
      if (targetOrigin != null) {
        allOrigins.add(new DefaultOrigin(targetOrigin));
      }
      allOrigins.addAll(message.getOrigins());

      String origin =
          allOrigins.stream()
              .map((e) -> e.getOriginText())
              .collect(Collectors.joining(ORIGIN_SEPARATOR, ORIGIN_BEGIN, ORIGIN_END));
      if ((ORIGIN_BEGIN + ORIGIN_END).equals(origin)) {
        origin = "";
      } else {
        origin = " " + origin;
      }

      return String.format(
          "%s%s%s: %s%s%s",
          LINE_BEGIN,
          getMessagePrefix(),
          message.getSeverity(),
          message.getMessage(),
          origin,
          LINE_END);
    }
  }

  public static class TextTimeMessageFormatter extends TextMessageFormatter {
    public TextTimeMessageFormatter(String lineBegin, String lineEnd) {
      super(lineBegin, lineEnd);
    }

    @Override
    protected String getMessagePrefix() {
      return String.format("%TFT%1$tH:%1$tM:%1$tS ", System.currentTimeMillis());
    }
  }

  /** Writes the message in text format. */
  public void writeMessage(Writer writer) throws IOException {
    writeMessage(writer, messageFormatter, null);
  }

  /** Writes the message with an additional target origin. */
  public void writeMessage(Writer writer, String targetOrigin) throws IOException {
    writeMessage(writer, messageFormatter, targetOrigin);
  }

  public void writeMessage(Writer writer, MessageFormatter formatter, String targetOrigin)
      throws IOException {
    formatter.writeMessage(writer, this, targetOrigin);
  }
}
