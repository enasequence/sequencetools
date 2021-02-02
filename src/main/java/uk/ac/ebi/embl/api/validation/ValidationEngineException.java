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

/**
 * @author dlorenc
 *
 * This is validation exception which provides details on validation result. 
 * It contains a validation message.
 */
public class ValidationEngineException extends Exception {

	private static final long serialVersionUID = 7675709957686224694L;
   	ReportErrorType errorType = ReportErrorType.SYSTEM_ERROR;
	
    public ReportErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ReportErrorType errorType) {
		this.errorType = errorType;
	}

	public ValidationEngineException() {
		super();
	}

	public ValidationEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationEngineException(String message) {
		super(message);
	}

	public ValidationEngineException(String message, ReportErrorType type) {
		super(message);
		setErrorType(type);
	}
	public ValidationEngineException(Throwable cause) {
		super(cause);
	}
	
		
	public enum ReportErrorType
	{
		VALIDATION_ERROR,
		SYSTEM_ERROR
	}
}



