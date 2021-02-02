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
package uk.ac.ebi.embl.api.validation;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dlorenc
 *
 * A container for validation errors. It also provides additional helper
 * methods.
 */
public class ValidationPlanResult implements Serializable {

	private List<ValidationResult> results;
	private boolean hasError;
	private Origin validationMessage;
    /**
     * the name of the thing on which this plan was run - either an entry name or a file - anything really
     */
    private String targetOrigin;

	public ValidationPlanResult() {
		this.results = new ArrayList<ValidationResult>();
	}

	/**
	 * Adds a validationResult to the results - if there are any messages
	 *
	 * @param result a validation message to be added
	 */
	private void addResult(ValidationResult result) {
		if (result == null || result.count() == 0) {
			return;
		}
		this.results.add(result);
	}

	/**
	 * Appends a validation message to the result.
	 *
	 * @param result a validation message to be added
	 * @return a reference to this object
	 */
	public ValidationPlanResult append(ValidationResult result) {
		addResult(result);
		return this;
	}

	/**
	 * Appends a collection of validation message to the result.
	 *
	 * @param results a collection of validation messages to be added
	 * @return a reference to this object
	 */
	public ValidationPlanResult append(Collection<ValidationResult> results) {
		if (results == null) {
			return this;
		}
		for (ValidationResult message : results) {
			addResult(message);
		}
		return this;
	}


	/**
	 * Appends another validation result (its validation messages) to
	 * the result.
	 *
	 * @param result another validation result
	 * @return a reference to this object
	 */
	public ValidationPlanResult append(ValidationPlanResult result) {
		if (result == null) {
			return this;
		}
		return append(result.getResults());
	}

	/**
	 * Returns true if no errors have been reported.
	 *
	 * @return true if no errors have been reported
	 */
    public boolean isValid() {
        for (ValidationResult result : results) {
            for (ValidationMessage<Origin> message : result.getMessages()) {
                if (Severity.ERROR.equals(message.getSeverity())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Counts the number of messages. */
	public int count() {
		return results.size();
	}

	/**
	 * Counts validation messages by its severity.
	 *
	 * @param severity a severity of the message which should be counted
	 * @return a number of validation messages with provided severity
	 */
    public int count(Severity severity) {
        int count = 0;
        if (severity == null) {
            return count;
        }

        for (ValidationResult result : results) {
            for (ValidationMessage<Origin> message : result.getMessages()) {
                if (severity.equals(message.getSeverity())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
	 * Counts validation messages by its severity and message key.
	 *
	 * @param messageKey a message key of the messages which should be counted
	 * @param severity a severity of the messages which should be counted
	 * @return a number of validation messages with provided severity and
	 * message key
	 */
    public int count(String messageKey, Severity severity) {
        int count = 0;
        if (severity == null || messageKey == null) {
            return count;
        }

        for (ValidationResult result : results) {
            for (ValidationMessage<Origin> message : result.getMessages()) {
                if (messageKey.equals(message.getMessageKey())
                        && severity.equals(message.getSeverity())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
	 * Gets the validation messages.
	 *
	 * @return unmodifiable collection of all messages
	 */
	public List<ValidationResult> getResults() {
		return this.results;
	}

	/**
	 * Finds validation messages by the message key.
	 *
	 * @param messageKey a message key of the message
	 * @return a collection of found validation messages
	 */
    public List<ValidationMessage<Origin>> getMessages(String messageKey) {

        List<ValidationMessage<Origin>> result = new ArrayList<ValidationMessage<Origin>>();

        for (ValidationResult validationResult : results) {
            for (ValidationMessage<Origin> message : validationResult.getMessages()) {
                if (messageKey.equals(message.getMessageKey())) {
                    result.add(message);
                }
            }
        }
        return result;
    }

    public List<ValidationMessage<Origin>> getMessages() {
        List<ValidationMessage<Origin>> messages = new ArrayList<>();
        for (ValidationResult validationResult : results) {
			messages.addAll(validationResult.getMessages());
        }
        return messages;
    }

    /**
	 * Finds validation messages by the message key and severity.
	 *
	 * @param messageKey a message key of the message
	 * @param severity a severity of the message
	 * @return a collection of found validation messages
	 */
	public List<ValidationMessage<Origin>> getMessages(String messageKey, Severity severity) {

		List<ValidationMessage<Origin>> messages = new ArrayList<ValidationMessage<Origin>>();

        for (ValidationResult result : results) {
            for (ValidationMessage<Origin> message : result.getMessages()) {
                if (messageKey.equals(message.getMessageKey()) && severity.equals(message.getSeverity())) {
                    messages.add(message);
                }
            }
        }

        return messages;
	}

    public List<ValidationMessage<Origin>> getMessages(Severity severity) {

		List<ValidationMessage<Origin>> messages = new ArrayList<ValidationMessage<Origin>>();

        for (ValidationResult result : results) {
            for (ValidationMessage<Origin> message : result.getMessages()) {
                if (severity.equals(message.getSeverity())) {
                    messages.add(message);
                }
            }
        }

        return messages;
	}


    public void removeMessages(String messageId) {
        for (ValidationResult result : results) {
            result.removeMessage(messageId);
        }
    }

    /**
     * Removes all messages
     */
    public void clearMessages(){
        results.clear();
    }

    @Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("messages", results);
		return builder.toString();
	}

    public String getTargetOrigin() {
        return targetOrigin;
    }

    public void setTargetOrigin(String targetOrigin) {
        this.targetOrigin = targetOrigin;
    }

	public boolean hasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public Origin getValidationMessage() {
		return validationMessage;
	}

	public void setValidationMessage(Origin validationMessage) {
		this.validationMessage = validationMessage;
	}
}
