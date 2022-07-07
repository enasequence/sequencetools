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
import java.util.Map;

public class TranslationTableDescriptor {

	public final static Map<Integer, TranslationTableDescriptor> TABLES = 
		new HashMap<Integer, TranslationTableDescriptor>();
	
	static {
		TABLES.put(1, new TranslationTableDescriptor(
			1, 
			"The Standard Code",
			"FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"---M---------------M---------------M----------------------------")
		); 
		
		TABLES.put(2, new TranslationTableDescriptor(
			2, 
			"The Vertebrate Mitochondrial Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSS**VVVVAAAADDEEGGGG",
			"--------------------------------MMMM---------------M------------")
		);
		
		TABLES.put(3, new TranslationTableDescriptor(
			3, 
			"The Yeast Mitochondrial Code",
			"FFLLSSSSYY**CCWWTTTTPPPPHHQQRRRRIIMMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"----------------------------------MM----------------------------")
		);
		
		TABLES.put(4, new TranslationTableDescriptor(
			4,
			"The Mold, Protozoan, and Coelenterate Mitochondrial Code and " +
			"the Mycoplasma/Spiroplasma Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"--MM---------------M------------MMMM---------------M------------")
		);

		TABLES.put(5, new TranslationTableDescriptor(
			5, 
			"The Invertebrate Mitochondrial Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSSSSVVVVAAAADDEEGGGG",
			"---M----------------------------MMMM---------------M------------")
		);
		
		TABLES.put(6, new TranslationTableDescriptor(
			6,
			"The Ciliate, Dasycladacean and Hexamita Nuclear Code",
			"FFLLSSSSYYQQCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"-----------------------------------M----------------------------")
		);

		TABLES.put(9, new TranslationTableDescriptor(
			9,
			"The Echinoderm and Flatworm Mitochondrial Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNNKSSSSVVVVAAAADDEEGGGG",
			"-----------------------------------M---------------M------------")
		);

		TABLES.put(10, new TranslationTableDescriptor(
			10, 
			"The Euplotid Nuclear Code",
			"FFLLSSSSYY**CCCWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"-----------------------------------M----------------------------")
		);

		TABLES.put(11, new TranslationTableDescriptor(
			11, 
			"The Bacterial and Plant Plastid Code",
			"FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"---M---------------M------------MMMM---------------M------------")
		);

		TABLES.put(12, new TranslationTableDescriptor(
			12, 
			"The Alternative Yeast Nuclear Code",
			"FFLLSSSSYY**CC*WLLLSPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"-------------------M---------------M----------------------------")
		);

		TABLES.put(13, new TranslationTableDescriptor(
			13, 
			"The Ascidian Mitochondrial Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSSGGVVVVAAAADDEEGGGG",
			"---M------------------------------MM---------------M------------")
		);

		TABLES.put(14, new TranslationTableDescriptor(
			14, 
			"The Alternative Flatworm Mitochondrial Code",
			"FFLLSSSSYYY*CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNNKSSSSVVVVAAAADDEEGGGG",
			"-----------------------------------M----------------------------")
		);
		
		TABLES.put(15, new TranslationTableDescriptor(
				15, 
				"Blepharisma Macronuclear",
				"FFLLSSSSYY*QCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"----------*---*--------------------M----------------------------")
			);
		
		TABLES.put(16, new TranslationTableDescriptor(
			16, 
			"Chlorophycean Mitochondrial Code",
			"FFLLSSSSYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"-----------------------------------M----------------------------")
		);

		TABLES.put(21, new TranslationTableDescriptor(
			21, 
			"Trematode Mitochondrial Code",
			"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNNKSSSSVVVVAAAADDEEGGGG",
			"-----------------------------------M---------------M------------")
		);

		TABLES.put(22, new TranslationTableDescriptor(
			22, 
			"Scenedesmus obliquus mitochondrial Code",
			"FFLLSS*SYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"-----------------------------------M----------------------------")
		);

		TABLES.put(23, new TranslationTableDescriptor(
			23, 
			"Thraustochytrium Mitochondrial Code",
			"FF*LSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
			"--------------------------------M--M---------------M------------")
		);
		TABLES.put(24, new TranslationTableDescriptor(
				24, 
				"Pterobranchia Mitochondrial Code",
				"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSSKVVVVAAAADDEEGGGG",
				"---M---------------M---------------M----------------------------")
			);
		TABLES.put(25, new TranslationTableDescriptor(
				25, 
				"Candidate Division SR1 and Gracilibacteria",
				"FFLLSSSSYY**CCGWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"---M---------------M---------------M----------------------------")
			);
		TABLES.put(26, new TranslationTableDescriptor(
				26, 
				"Pachysolen tannophilus Nuclear",
				"FFLLSSSSYY**CC*WLLLAPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"----------**--*----M---------------M----------------------------")
			);
		
		TABLES.put(27, new TranslationTableDescriptor(
				27, 
				"Karyorelict Nuclear",
				"FFLLSSSSYYQQCCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"--------------*--------------------M----------------------------")
			);

		TABLES.put(28, new TranslationTableDescriptor(
				28, 
				"Condylostoma Nuclear",
				"FFLLSSSSYYQQCCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"----------**--*--------------------M----------------------------")
			);
		TABLES.put(29, new TranslationTableDescriptor(
				29, 
				"Mesodinium Nuclear",
				"FFLLSSSSYYYYCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"--------------*--------------------M----------------------------")
			);
		TABLES.put(30, new TranslationTableDescriptor(
				30, 
				"Peritrich Nuclear",
				"FFLLSSSSYYEECC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"--------------*--------------------M----------------------------")
			);
		TABLES.put(31, new TranslationTableDescriptor(
				31, 
				"Blastocrithidia Nuclear",
				"FFLLSSSSYYEECCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG",
				"----------**-----------------------M----------------------------")
			);
		TABLES.put(33, new TranslationTableDescriptor(
				33, 
				"Cephalodiscidae Mitochondrial UAA-Tyr Code",
				"FFLLSSSSYYY*CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSSKVVVVAAAADDEEGGGG",
				"---M-------*-------M---------------M---------------M------------")
				           
			);
		
	}
	
	private int number;
	private String name;
	private String aminoAcids;
	private String startCodons;	
	
	private TranslationTableDescriptor(int number, String name, 
			String aminoAcids, String startCodons) {
		this.number = number;
		this.name = name;
		this.aminoAcids = aminoAcids;
		this.startCodons = startCodons;
	}
	
	public static TranslationTableDescriptor getDescriptor(int number) {
		return TABLES.get(number);
	}

	public String getAminoAcids() {
		return aminoAcids;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public String getStarts() {
		return startCodons;
	}

}
