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
package uk.ac.ebi.embl.api.entry.qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AminoAcidFactory {

    /** Creates an amino acid from a JCBN letter.
     * @param letter The JCBN amino acid letter.
     * @return The amino acid.
     */
    public AminoAcid createAminoAcid(Character letter) {
        return letterToAminoAcid.get(letter);
    }

    /** Creates an amino acid from a JCBN abbreviation.
     * @param abbreviation The JCBN amino acid abbreviation.
     * @return The amino aid.
     */
    public AminoAcid createAminoAcid(String abbreviation) {
        return abbreviationToAminoAcid.get(abbreviation);
    }

  /*if the anticodon contains abbreviation(amino acid) with different casing 
  e.g.:FT                   /anticodon="(pos:3828893..3828895,aa:SeC)*/
    
    public String getAbbreviation(String abbr) 
    {
    	for(String abbreviation:abbrevations)
    	{
    		if(abbreviation.equalsIgnoreCase(abbr))
    		{
    			return abbreviation;
    		}
    	}
    	return null;
    	
    }
    private static final Map<Character, AminoAcid> letterToAminoAcid = 
        new HashMap<Character, AminoAcid>();

    private static final Map<String, AminoAcid> abbreviationToAminoAcid = 
        new HashMap<String, AminoAcid>(); 
    private static final ArrayList<String> abbrevations= new ArrayList<String>();
        
    private static void addAminoAcid(String name, Character letter,
        String abbreviation) {
        AminoAcid aminoAcid = new AminoAcid(name, letter, abbreviation);
        letterToAminoAcid.put(aminoAcid.getLetter(), aminoAcid);
        abbreviationToAminoAcid.put(aminoAcid.getAbbreviation(), aminoAcid);
        abbrevations.add(abbreviation);
    }
    
    static {
        addAminoAcid("3-Hydroxyproline",null,"3Hyp");
        addAminoAcid("4-Aminobutyric acid (piperidinic acid)",null,"4Abu");
        addAminoAcid("4-Hydroxyproline",null,"4Hyp");
        addAminoAcid("2-Aminoadipic acid",null,"Aad");
        addAminoAcid("2-Aminobutyric acid",null,"Abu");
        addAminoAcid("6-Aminocaproic acid",null,"Acp");
        addAminoAcid("2-Aminoheptanoic acid",null,"Ahe");
        addAminoAcid("allo-Hydroxylysine",null,"aHyl");
        addAminoAcid("2-Aminoisobutyric acid",null,"Aib");
        addAminoAcid("allo-Isoleucine",null,"aIle");
        addAminoAcid("2-Aminopimelic acid",null,"Apm");
        addAminoAcid("3-Aminoadipic acid",null,"bAad");
        addAminoAcid("3-Aminoisobutyric acid",null,"bAib");
        addAminoAcid("beta-Alanine (beta-Aminoproprionic acid)",null,"bAla");
        addAminoAcid("2,4-Diaminobutyric acid",null,"Dbu");
        addAminoAcid("Desmosine",null,"Des");
        addAminoAcid("2,2-Diaminopimelic acid",null,"Dpm");
        addAminoAcid("2,3-Diaminoproprionic acid",null,"Dpr");
        addAminoAcid("N-Ethylasparagine",null,"EtAsn");
        addAminoAcid("N-Ethylglycine",null,"EtGly");
        addAminoAcid("Hydroxylysine",null,"Hyl");
        addAminoAcid("Isodesmosine",null,"Ide");
        addAminoAcid("N-Methylglycine (sarcosine)",null,"MeGly");
        addAminoAcid("N-Methylisoleucine",null,"MeIle");
        addAminoAcid("6-N-Methyllysine",null,"MeLys");
        addAminoAcid("N-Methylvaline",null,"MeVal");
        addAminoAcid("Norleucine",null,"Nle");
        addAminoAcid("Norvaline",null,"Nva");
        addAminoAcid("Ornithine",null,"Orn");
        addAminoAcid("Selenocysteine",'U',"Sec");
        addAminoAcid("Alanine",'A',"Ala");
        addAminoAcid("Arginine",'R',"Arg");
        addAminoAcid("Asparagine",'N',"Asn");
        addAminoAcid("Aspartic acid (Aspartate)",'D',"Asp");
        addAminoAcid("Asparagine or Aspartate",'B',"Asx");
        addAminoAcid("Cysteine",'C',"Cys");
        addAminoAcid("Glutamine",'Q',"Gln");
        addAminoAcid("Glutamic acid (Glutamate)",'E',"Glu");
        addAminoAcid("Glutamine or GLutamate",'Z',"Glx");
        addAminoAcid("Glycine",'G',"Gly");
        addAminoAcid("Histidine",'H',"His");
        addAminoAcid("Isoleucine",'I',"Ile");
        addAminoAcid("Leucine",'L',"Leu");
        addAminoAcid("Lysine",'K',"Lys");
        addAminoAcid("Methionine",'M',"Met");
        addAminoAcid("Other",'X',"OTHER");
        addAminoAcid("Phenylalanine",'F',"Phe");
        addAminoAcid("Proline",'P',"Pro");
        addAminoAcid("Pyrrolysine",'O',"Pyl");
        addAminoAcid("Serine",'S',"Ser");
        addAminoAcid("termination codon",'*',"TERM");
        addAminoAcid("Threonine",'T',"Thr");
        addAminoAcid("Tryptophan",'W',"Trp");
        addAminoAcid("Tyrosine",'Y',"Tyr");
        addAminoAcid("Valine",'V',"Val");
        addAminoAcid("Any amino acid",'X',"Xaa");
        addAminoAcid("Isoleucine or Leucine",'J',"Xle");
    }
}
