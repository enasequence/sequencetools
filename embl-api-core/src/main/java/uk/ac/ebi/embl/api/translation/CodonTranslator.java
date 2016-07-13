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
package uk.ac.ebi.embl.api.translation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.embl.api.validation.ValidationException;


/** Translates a codon to an amino acid. The bases are encoded using
 * lower case single letter JCBN abbreviations and the amino acids 
 * are encoded using upper case single letter JCBN abbreviations.
 */
public class CodonTranslator {

    private TranslationTable translationTable;       
    public void setTranslationTable(Integer translationTable) throws ValidationException {
        TranslationTableFactory factory = new TranslationTableFactory();
        this.translationTable = factory.createTranslationTable(translationTable);
        if (this.translationTable == null) {
        	ValidationException.throwError("CodonTranslator-2");
        }        
    }

    private static final Map<Character, List<Character>> ambiguousBaseMap =
        new HashMap<Character, List<Character>>();

    private static final Map<Character, Character> ambiguousAminoAcidMap =
          new HashMap<Character, Character>();

    static {
        addAmbiguousBase('a', 'a');
        addAmbiguousBase('t', 't');
        addAmbiguousBase('c', 'c');
        addAmbiguousBase('g', 'g');
        addAmbiguousBase('r', 'g');
        addAmbiguousBase('r', 'a');
        addAmbiguousBase('y', 't');
        addAmbiguousBase('y', 'c');
        addAmbiguousBase('m', 'a');
        addAmbiguousBase('m', 'c');
        addAmbiguousBase('k', 'g');
        addAmbiguousBase('k', 't');
        addAmbiguousBase('s', 'g');
        addAmbiguousBase('s', 'c'); 
        addAmbiguousBase('w', 'a');
        addAmbiguousBase('w', 't');        
        addAmbiguousBase('h', 'a');
        addAmbiguousBase('h', 'c');
        addAmbiguousBase('h', 't');
        addAmbiguousBase('b', 'g');
        addAmbiguousBase('b', 't');
        addAmbiguousBase('b', 'c');
        addAmbiguousBase('v', 'g');
        addAmbiguousBase('v', 'c');
        addAmbiguousBase('v', 'a');
        addAmbiguousBase('d', 'g');
        addAmbiguousBase('d', 'a');
        addAmbiguousBase('d', 't');
        addAmbiguousBase('n', 'g');              
        addAmbiguousBase('n', 'a');              
        addAmbiguousBase('n', 't');              
        addAmbiguousBase('n', 'c');
        addAmbiguousAminoAcid('B', 'N');
        addAmbiguousAminoAcid('B', 'D');
        addAmbiguousAminoAcid('B', 'B');
        addAmbiguousAminoAcid('Z', 'Q');
        addAmbiguousAminoAcid('Z', 'E');
        addAmbiguousAminoAcid('Z', 'Z');
        addAmbiguousAminoAcid('J', 'I');
        addAmbiguousAminoAcid('J', 'L');
        addAmbiguousAminoAcid('J', 'J');        
    }

    private static void addAmbiguousBase(
        Character ambiguousBase, Character unAmbiguousBase) {
        List<Character> unAmbiguousBases = ambiguousBaseMap.get(ambiguousBase);
        if (unAmbiguousBases == null) {
            unAmbiguousBases = new LinkedList<Character>();
            ambiguousBaseMap.put(ambiguousBase, unAmbiguousBases);
        }
        unAmbiguousBases.add(unAmbiguousBase);
    }

    private static void addAmbiguousAminoAcid(
        Character ambiguousAminoAcid, Character unAmbiguousAminoAcid) {
        ambiguousAminoAcidMap.put(unAmbiguousAminoAcid, ambiguousAminoAcid);
    }

    private Map<String, Character> codonExceptionMap =
        new HashMap<String, Character>();
    public void addCodonException(String codon, Character aminoAcid) {
        codonExceptionMap.put(codon, aminoAcid);
    }
        
    public void translateStartCodon(Codon codon) throws ValidationException {
        translateCodon(codon, translationTable.getStartCodonMap());
    }
    
    public void translateOtherCodon(Codon codon) throws ValidationException {
        translateCodon(codon, translationTable.getOtherCodonMap());
    }

    private void translateCodon(Codon codon,
        Map<String, Character> codonMap) throws ValidationException {
        List<UnAmbiguousCodon> unAmbiguousCodons = getUnAmbiguousCodons(codon);
        Character aminoAcid = null;
        for ( UnAmbiguousCodon unAmbiguousCodon : unAmbiguousCodons ) {
            Character newAminoAcid = null;
            newAminoAcid = codonExceptionMap.get(unAmbiguousCodon.getCodon());
            unAmbiguousCodon.setCodonException(newAminoAcid != null);
            if (!unAmbiguousCodon.isCodonException()) {
                newAminoAcid = codonMap.get(unAmbiguousCodon.getCodon());
            }
            if (newAminoAcid == null) {
            	ValidationException.throwError("CodonTranslator-1", codon);
            }
            unAmbiguousCodon.setAminoAcid(newAminoAcid);
            if (aminoAcid == null) {
                aminoAcid = newAminoAcid;
            }
            else if (aminoAcid != newAminoAcid) {
                Character ambiguousAminoAcid =
                    ambiguousAminoAcidMap.get(aminoAcid);
                Character ambiguousNewAminoAcid =
                    ambiguousAminoAcidMap.get(newAminoAcid);
                if (ambiguousAminoAcid != null &&
                    ambiguousNewAminoAcid != null &&
                    ambiguousAminoAcid.equals(ambiguousNewAminoAcid)) {
                    aminoAcid = ambiguousAminoAcid;
                }
                else {
                    aminoAcid = 'X';
                }
            }
        }
        if (aminoAcid == null) {
        	ValidationException.throwError("CodonTranslator-1", codon);
        }
        codon.setAminoAcid(aminoAcid);
        codon.setUnAmbiguousCodons(unAmbiguousCodons);
    }

    public boolean isDegenerateStopCodon(Codon codon) throws ValidationException {
        return isDegenerateCodon(codon,
            translationTable.getOtherCodonMap(), '*');
    }

    public boolean isDegenerateStartCodon(Codon codon) throws ValidationException {
        return isDegenerateCodon(codon,
            translationTable.getStartCodonMap(), 'M');
    }

    private boolean isDegenerateCodon(Codon codon,
		Map<String, Character> codonMap, Character aminoAcid) throws ValidationException {
		List<UnAmbiguousCodon> unAmbiguousCodons = getUnAmbiguousCodons(codon);
		for ( UnAmbiguousCodon unAmbiguousCodon : unAmbiguousCodons ) {
		    Character newAminoAcid = null;
		    if (codonExceptionMap != null ) {
		        newAminoAcid = codonExceptionMap.get(
		          unAmbiguousCodon.getCodon());
		    }
		    if (newAminoAcid == null) {
		       newAminoAcid = codonMap.get(unAmbiguousCodon.getCodon());
		    }
		    if (newAminoAcid == null) {
            	ValidationException.throwError("CodonTranslator-1", codon);
	        }
			if (newAminoAcid.equals(aminoAcid)) {
			    return true;
			}
		}
		return false;
    }

    public boolean isAmbiguousCodon(Codon codon) {
		return getUnAmbiguousCodons(codon).size() > 1;
    }

    public boolean isAlternativeStartCodon(Codon codon) throws ValidationException {
    	return (isDegenerateStartCodon(codon) &&    			
    			!isAmbiguousCodon(codon) &&
    			!codon.getAminoAcid().equals('M'));		
    }
        
    private List<Character> getUnAmbiguousBases(Character base) {
        return ambiguousBaseMap.get(base);
    }

    private List<UnAmbiguousCodon> getUnAmbiguousCodons(Codon codon) {
	    List<UnAmbiguousCodon> unAmbiguousCodons = new LinkedList<UnAmbiguousCodon>();
	    //System.out.println("codon0:" + codon.getCodon().charAt(0));
	    //System.out.println("codon1:" + codon.getCodon().charAt(1));
	    //System.out.println("codon2:" + codon.getCodon().charAt(2));
	    List<Character> unAmbiguousBases1 =
	        getUnAmbiguousBases(codon.getCodon().charAt(0));
	    //for (Character t : unAmbiguousBases1) {
	    	//System.out.println("unAmbiguousBases1:" + t);	
	    //}
	    List<Character> unAmbiguousBases2 =
	        getUnAmbiguousBases(codon.getCodon().charAt(1));
	    //for (Character t : unAmbiguousBases2) {
	    	//System.out.println("unAmbiguousBases2:" + t);	
	    //}
	    List<Character> unAmbiguousBases3 =
	        getUnAmbiguousBases(codon.getCodon().charAt(2));
	    //for (Character t : unAmbiguousBases3) {
	    	//System.out.println("unAmbiguousBases3:" + t);	
	    //}
	    char[] bases = new char[3];
	     for ( char unAmbiguousBase1 : unAmbiguousBases1 ) {
	        bases[0] = unAmbiguousBase1;
	        for ( char unAmbiguousBase2 : unAmbiguousBases2 ) {
	            bases[1] = unAmbiguousBase2;
	            for ( char unAmbiguousBase3 : unAmbiguousBases3 ) {
	                bases[2] = unAmbiguousBase3;
	                UnAmbiguousCodon unAmbiguousCodon = new UnAmbiguousCodon();
	                unAmbiguousCodon.setCodon(new String(bases));
	                unAmbiguousCodons.add(unAmbiguousCodon);
	            }
	        }
	    }
	    return unAmbiguousCodons;
	}

    public TranslationTable getTranslationTable() {
        return translationTable;
    }
}
