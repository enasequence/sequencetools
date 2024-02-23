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
package uk.ac.ebi.embl.template;

// import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class TemplateFactory {
  private LoadStrategyI loadStrategyI;
  private Map<String, Integer> templateOrders;
  private static final Map<String, List<TemplatePreProcessor>> preProcessors =
      new HashMap<String, List<TemplatePreProcessor>>();
  private static final TemplatePreProcessor ITS_TEMPLATE_PRE_PROCESSOR =
      new ITSTemplatePreProcessor();
  private static final TemplatePreProcessor PHYLO_MARKER_TEMPLATE_PRE_PROCESSOR =
      new PhyloMarkerTemplatePreProcessor();
  private static final TemplatePreProcessor ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR =
      new OrganelleDETemplatePreProcessor();
  private TemplateSet templateSet;

  static {
    preProcessors.put(
        TemplateIDs.ITS_TEMPLATE_ID, Collections.singletonList(ITS_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.PHYLO_MARKER_TEMPLATE_ID,
        Arrays.asList(PHYLO_MARKER_TEMPLATE_PRE_PROCESSOR, ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));

    preProcessors.put(
        TemplateIDs.SINGLE_CDS_MRNA_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.SINGLE_CDS_GENOMIC_DNA_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.NCRNA_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.MOBILE_ELEMENT_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.MULTI_GENE_MARKER_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.MULTI_EXON_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.RIBOSOMAL_RNA_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.SATELLITE_DNA_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.PROMOTER_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.IGS_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.GENE_INTRON_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.ISR_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.TSA_UNANNOTATED_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
    preProcessors.put(
        TemplateIDs.TSA_ANNOTATED_TEMPLATE_ID,
        Collections.singletonList(ORGANELLE_DE_TEMPLATE_PRE_PROCESSOR));
  }
  /*
      @Autowired
      public void setLoadStrategyI(LoadStrategyI loadStrategyI) throws TemplateException {
          this.loadStrategyI = loadStrategyI;
          loadTemplates();
      }
  */
  public void loadTemplates() throws TemplateException {
    loadStrategyI.loadTemplates();
    this.templateSet = loadStrategyI.getTemplates();
    this.templateOrders = loadStrategyI.getOrders();
  }

  /**
   * returns the most recent version of all templates
   *
   * @return
   */
  public List<TemplateInfo> getAllLatestTemplates() throws TemplateException {
    List<TemplateInfo> latestTemplates = new ArrayList<TemplateInfo>();
    for (String templateId : templateSet.getTemplateIds()) {
      TemplateInfo templateInfo = getLatestTemplateInfo(templateId);
      if (templateInfo.isIncluded()) latestTemplates.add(templateInfo);
    }
    Collections.sort(latestTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return latestTemplates;
  }

  public List<TemplateInfo> getBarcodeCompliantLatestIncludedTemplates() {
    List<TemplateInfo> barcodeTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.BARCODE_COMPLIANT_TEMPLATE_IDs);
    Collections.sort(barcodeTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return barcodeTemplates;
  }

  public List<TemplateInfo> getFrequentlyUsedLatestIncludedTemplates() {
    List<TemplateInfo> frequentlyUsedTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.FREQUENTLY_USED_CHECKLISTS);
    Collections.sort(
        frequentlyUsedTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return frequentlyUsedTemplates;
  }

  public List<TemplateInfo> getMarkerSequenceLatestIncludedTemplates() {
    List<TemplateInfo> markerSequenceTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.MARKER_SEQUENCE_CHECKLISTS);
    Collections.sort(
        markerSequenceTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return markerSequenceTemplates;
  }

  public List<TemplateInfo> getVirusSpecificLatestIncludedTemplates() {
    List<TemplateInfo> virusSpecificTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.VIRUS_SPECIFIC_CHECKLISTS);
    Collections.sort(
        virusSpecificTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return virusSpecificTemplates;
  }

  public List<TemplateInfo> getLargeScaleLatestIncludedTemplates() {
    List<TemplateInfo> largeScaleTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.LARGE_SCALE_DATA_CHECKLISTS);
    Collections.sort(
        largeScaleTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return largeScaleTemplates;
  }

  private List<TemplateInfo> getLatestIncludedTemplatesById(List<String> templateIDs) {
    ArrayList<TemplateInfo> latestTemplates = new ArrayList<TemplateInfo>(templateIDs.size());
    try {
      for (String templateId : templateIDs) {
        TemplateInfo templateInfo = getLatestTemplateInfo(templateId);
        if (templateInfo.isIncluded()) latestTemplates.add(templateInfo);
      }
    } catch (TemplateException e) {
      e.printStackTrace();
    }
    return latestTemplates;
  }

  public boolean isNewTemplate(String templateId) throws TemplateException {
    return TemplateIDs.NEW_TEMPLATE_IDS.contains(templateId);
  }

  public TemplateInfo getLatestFTUploadTemplate() throws TemplateException {
    return getLatestTemplateInfo(TemplateIDs.FEATURE_TABLE_TEMPLATE_ID);
  }

  public List<TemplateInfo> getMimarksCompliantLatestIncludedTemplates() {
    List<TemplateInfo> mimarksTemplates =
        getLatestIncludedTemplatesById(TemplateIDs.MIMARKS_COMPLIANT_TEMPLATE_IDs);
    Collections.sort(mimarksTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
    return mimarksTemplates;
  }

  public List<TemplatePreProcessor> getPreProcessors(String id) {
    return preProcessors.get(id);
  }

  /*
    public List<TemplateInfo> getRNALatestIncludedTemplates() {
      List<TemplateInfo> rnaTemplates = getLatestIncludedTemplatesById(TemplateIDs.RNA_TEMPLATE_IDs);
      Collections.sort(rnaTemplates, new TemplateOrderComparator<TemplateInfo>(templateOrders));
      return rnaTemplates;
    }
  */
  public TemplateInfo getTemplateInfo(String templateId, Integer templateVersion)
      throws TemplateException {
    if (templateSet.containsTemplateId(templateId)) {
      TemplateVersions versionMap = templateSet.getTemplateVersions(templateId);
      if (versionMap.containsVersion(templateVersion))
        return versionMap.getTemplate(templateVersion);
      throw new TemplateException(
          "No template found with VERSION : " + templateVersion + " and ID: " + templateId);
    }
    throw new TemplateException("No template found with ID : " + templateId);
  }

  public boolean isMIENSTemplate(String templateId) {
    return TemplateIDs.MIENS_TEMPLATE_IDs.contains(templateId);
  }

  public TemplateInfo getLatestTemplateInfo(String selectedTemplateId) throws TemplateException {
    return templateSet.getLatestTemplateInfo(selectedTemplateId);
  }

  private static class TemplateOrderComparator<T> implements Comparator<TemplateInfo> {
    private Map<String, Integer> orders = new HashMap<String, Integer>();

    public TemplateOrderComparator(Map<String, Integer> orders) {
      this.orders = orders;
    }

    public int compare(TemplateInfo template1, TemplateInfo template2) {
      String template1Id = template1.getId();
      String template2Id = template2.getId();
      if (orders.containsKey(template1Id) && orders.containsKey(template2Id))
        return orders.get(template1Id).compareTo(orders.get(template2Id));
      return 0; // just dont sort it otherwise
    }
  }

  public static Map<String, List<TemplatePreProcessor>> getPreProcessors() {
    return Collections.unmodifiableMap(preProcessors);
  }
}
