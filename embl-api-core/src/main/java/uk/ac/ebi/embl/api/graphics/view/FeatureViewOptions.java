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
package uk.ac.ebi.embl.api.graphics.view;

public enum FeatureViewOptions {
	
	/** Show gene features. Gene features are constructed from 
     * locus tags and gene names and are a contiguous overlap 
     * of all features with the same locus tag and gene names.
	 */
	SHOW_GENE,
	
	/** Group features by locus tags and gene names. 
	 */		
	GROUP_GENE,
	
	/** Show source features. 
	 */
	SHOW_SOURCE,
	
	/** Show all other features except gene and source features. 
	 */
	SHOW_FEATURE,
	
	/** Show contigs. 
	 */	
	SHOW_CONTIG,
	
	/** Show assemblies. 
	 */
	SHOW_ASSEMBLY,

	/** Show the forward and reverse translations.
	 */
	SHOW_SEQUENCE,
	
	/** Show the forward and reverse translations.
	 */
	SHOW_TRANSLATION,

	/** Show the base pair visible range rectangle on the features.
	 */
	SHOW_FOCUS

}
