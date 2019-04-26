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

import java.util.Stack;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("check qualifier values for unbalanced parentheses")
public class UnbalancedParenthesesCheck extends FeatureValidationCheck {


    private final static String UNBALANCED_PARENTHESES_ID_1 = "UnbalancedParenthesesCheck_1";
    private final static String UNBALANCED_PARENTHESES_ID_2 = "UnbalancedParenthesesCheck_2";


    public UnbalancedParenthesesCheck() {

    }


    public ValidationResult check(Feature feature) {
        Stack<Integer> stringStack1 = new Stack<Integer>();
        Stack<Integer> stringStack2 = new Stack<Integer>();
        Stack<Integer> stringStack3 = new Stack<Integer>();

        int i = 0;
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        for (Qualifier qualifier : feature.getQualifiers()) {
            String qualifierValue = qualifier.getValue();
            String qualifiername = qualifier.getName();

            if(qualifierValue == null){
                continue;//bail out quietly if there is no value
            }
            
//            System.out.println(qualifierValue);
            while (i < qualifierValue.length()) {
                Integer k;
				if (qualifierValue.charAt(i) == '(') {
                    stringStack1.push(i);
                } else if (qualifierValue.charAt(i) == ')') {

                    if (stringStack1.empty()) { //stack is empty.

                                reportWarning(qualifier.getOrigin(), UNBALANCED_PARENTHESES_ID_1, qualifiername,
                                        qualifierValue, qualifierValue.charAt(i));

					} else { // stack is not empty remove the existing left
								// parentheses for balancing
						k = stringStack1.pop();

					}

                } else if (qualifierValue.charAt(i) == '{') {
                    stringStack2.push(i);
                } else if (qualifierValue.charAt(i) == '}') {

                    if (stringStack2.empty()) { //stack is empty.

                                reportWarning(qualifier.getOrigin(), UNBALANCED_PARENTHESES_ID_1, qualifiername,
                                        qualifierValue, qualifierValue.charAt(i));

					} else { // stack is not empty remove the existing left
								// parentheses for balancing
						k = stringStack2.pop();

					}

                } else if (qualifierValue.charAt(i) == '[') {
                    stringStack3.push(i);
                } else if (qualifierValue.charAt(i) == ']') {

                    if (stringStack3.empty()) { //stack is empty.

                                reportWarning(qualifier.getOrigin(), UNBALANCED_PARENTHESES_ID_1, qualifiername,
                                        qualifierValue, qualifierValue.charAt(i));

					} else { // stack is not empty remove the existing left
								// parentheses for balancing
						k = stringStack3.pop();

					}
                } else {
                    //*******Do nothing if it's not not a parenthesis.*******
                }

                i++; //Loop count

            } //***Second While Loop Brace***
            i = 0;


            //*****check for unmatched left parenthesis still on stack*****

			if (stringStack1.empty() == false || stringStack2.empty() == false
					|| stringStack3.empty() == false) {

				reportWarning(qualifier.getOrigin(),
						UNBALANCED_PARENTHESES_ID_2, qualifiername,
						qualifierValue, qualifierValue.charAt(i));
			}
        }
        return result;
    }
}
        			 




    
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        			
        		
