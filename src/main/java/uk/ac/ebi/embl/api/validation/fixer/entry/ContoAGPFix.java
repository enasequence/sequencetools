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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryContigsCheck;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Description("")
public class ContoAGPFix extends EntryValidationCheck
{
	static final HashMap<String, String> gapType= new HashMap<String, String>();
	static final HashMap<String, String> linkageEvidence= new HashMap<String, String>();
	static
	{
		
		gapType.put("within scaffold","scaffold");
		gapType.put("between scaffolds","contig");
		gapType.put("centromere","centromere");
		gapType.put("short arm","short_arm");
		gapType.put("heterochromatin","heterochromatin");
		gapType.put("telomere","telomere");
		gapType.put("repeat within scaffold","repeat");
		gapType.put("unknown","unknown");
		gapType.put("repeat between scaffolds","repeat");
		linkageEvidence.put("unspecified","na");
		linkageEvidence.put("paired-ends","paired-ends");
		linkageEvidence.put("align genus","align_genus");
		linkageEvidence.put("align xgenus","align_xgenus");
		linkageEvidence.put("align trnscpt","align_trnscpt");
		linkageEvidence.put("within clone","within_clone");
		linkageEvidence.put("clone contig","clone_contig");
		linkageEvidence.put("map","map");
		linkageEvidence.put("strobe","strobe");
		linkageEvidence.put("unspecified","unspecified");
		linkageEvidence.put("pcr","pcr");
    }
	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		try{
		result = new ValidationResult();
	    List<AgpRow> agpRows= new ArrayList<AgpRow>();
	    EntryContigsCheck check= new EntryContigsCheck();
	    int partNumber=0;
		Long objectStart=0l;
		Long objectEnd=0l;

		if (entry == null||getEntryDAOUtils()==null||(entry.getSequence()!=null&&entry.getSequence().getContigs().size()==0)||entry.getSequence().getAgpRows().size()!=0)
		{
			return result;
		}
		if(!check.check(entry).isValid())
			return result;
		
		for(Location contig : entry.getSequence().getContigs())
		{
			AgpRow agpRow= new AgpRow();
			partNumber++;
			agpRow.setObject(entry.getSubmitterAccession());
			agpRow.setObject_acc(entry.getPrimaryAccession()+"."+entry.getSequence().getVersion());
			if(agpRows.isEmpty())
			{
				objectStart=1l;
				if(!(contig instanceof Gap))
				{
				objectEnd= (Long) (contig.getEndPosition()-contig.getBeginPosition()+1);	
				}
				else
				{
					objectEnd= (Long) contig.getLength();
					agpRow.setGap_type(((Gap) contig).isUnknownLength()?"U":"N");
				}
			}
			else
			{
				objectStart= objectEnd+1;
				if(contig instanceof Gap)
				{
					objectEnd = (Long) (objectEnd+contig.getLength());
					agpRow.setGap_type(((Gap) contig).isUnknownLength()?"U":"N");
				}
				else
				{
					objectEnd= (Long) (objectEnd+(contig.getEndPosition()-contig.getBeginPosition()+1));
				}
			}
			
			agpRow.setObject_beg(objectStart);
			agpRow.setObject_end(objectEnd);
			agpRow.setPart_number(partNumber);
			
			if(contig instanceof Gap)//GAP
			{
				List<Feature> assemblyGapFeatures=SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
				assemblyGapFeatures=getSortedAssemblyGapFeatures(assemblyGapFeatures);
				for(Feature assemblyGapFeature:assemblyGapFeatures)
				{
					if(assemblyGapFeature.getLocations().getMinPosition()==agpRow.getObject_beg()&&assemblyGapFeature.getLocations().getMaxPosition()==agpRow.getObject_end())
					{
						agpRows.add(generateGapAgpRow(assemblyGapFeature,agpRow));
					}
					}
			}
			else if(contig instanceof RemoteLocation)
			{
				RemoteLocation remoteLocation = (RemoteLocation) contig;
				String accession = remoteLocation.getAccession() + (remoteLocation.getVersion() == null ? "" : "." + remoteLocation.getVersion());
				agpRow.setComponent_acc(accession);
				Entry contigEntry=getEntryDAOUtils().getEntryInfo(((RemoteLocation) contig).getAccession().split("\\.")[0]);
				if(contigEntry==null)
				{
					throw new ValidationEngineException("invalid accession in CO line: " + accession);
				}
				agpRows.add(generateComponentAgpRow(contig,agpRow,contigEntry));
			}
 	}
		entry.getSequence().addAgpRows(agpRows);
		}
		catch(Exception e)
		{
		  throw new ValidationEngineException(e);
		}
        return result;
	}

	 AgpRow generateGapAgpRow(Feature gapFeature,AgpRow gapRow) 
	{
	  List<Qualifier> qualifiers=gapFeature.getQualifiers();
	  List<String> linkageEvidences= new ArrayList<String>();
		
		for(int i=0;i<qualifiers.size();i++)
		{
			Qualifier qualifier=qualifiers.get(i);
		  			
			if(qualifier.getName().equals(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME))
			{
				linkageEvidences.add(linkageEvidence.get(qualifier.getValue()));
			}
			
			if(qualifier.getName().equals(Qualifier.GAP_TYPE_QUALIFIER_NAME))
			{
				gapRow.setGap_type(gapType.get(qualifier.getValue()));
			
			}
			
			if(qualifier.getName().equals(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME))
			{
				if("unknown".equals(qualifier.getValue()))
				{
					gapRow.setComponent_type_id("U");
				}
				else
				{
					gapRow.setComponent_type_id("N");
					gapRow.setGap_length(new Long(qualifier.getValue()));
				}
			}
		}
		gapRow.setLinkageevidence(linkageEvidences);

	
		return gapRow;
	}
	 
	 private AgpRow generateComponentAgpRow(Location component,AgpRow componentRow,Entry contigEntry)
		{	
			componentRow.setComponent_beg(component.getBeginPosition().longValue());
			componentRow.setComponent_end(component.getEndPosition().longValue());
			componentRow.setComponent_id(contigEntry.getSubmitterAccession());
			String componentType=Utils.getComponentTypeId(contigEntry);
			componentRow.setComponent_type_id(componentType);
			if(component.isComplement())
			{
				componentRow.setOrientation("-");
			}
			else
			{
				componentRow.setOrientation("+");
			}

			return componentRow;
			
		}

		public List<Feature> getSortedAssemblyGapFeatures(List<Feature> assemblyGapFeature)
		{
			Collections.sort(assemblyGapFeature,(feature1,feature2)->(feature1.getLocations().getMinPosition() < feature2.getLocations().getMinPosition()) ? -1 : 1);
			return assemblyGapFeature;
		}
}
