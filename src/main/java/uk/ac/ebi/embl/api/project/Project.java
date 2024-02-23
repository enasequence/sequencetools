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
package uk.ac.ebi.embl.api.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.ac.ebi.embl.api.taxonomy.HasTaxon;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

public class Project implements HasTaxon, Serializable {

  private static final long serialVersionUID = 1L;

  private String projectAccession;
  private Taxon taxon = (new TaxonFactory()).createTaxon();
  private String projectType;
  private String projectName;
  private String projectURL;
  private List<Center> centers;
  /** List of WGS accessions and ranges. */
  private List<String> wgsAccessions;
  /** List of CON accessions and ranges. */
  private List<String> conAccessions;
  /** List of STD accessions and ranges. */
  private List<String> stdAccessions;

  public Project() {
    wgsAccessions = new ArrayList<String>();
    conAccessions = new ArrayList<String>();
    stdAccessions = new ArrayList<String>();
    centers = new ArrayList<Center>();
  }

  public String getProjectAccession() {
    return projectAccession;
  }

  public void setProjectAccession(String projectAccession) {
    this.projectAccession = projectAccession;
  }

  public Taxon getTaxon() {
    return taxon;
  }

  public void setTaxon(Taxon taxon) {
    this.taxon = taxon;
  }

  public String getProjectType() {
    return projectType;
  }

  public void setProjectType(String projectType) {
    this.projectType = projectType;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectURL() {
    return projectURL;
  }

  public void setProjectURL(String projectURL) {
    this.projectURL = projectURL;
  }

  public List<Center> getCenters() {
    return Collections.unmodifiableList(centers);
  }

  public void setCenters(List<Center> centers) {
    this.centers = centers;
  }

  public void addCenter(Center center) {
    this.centers.add(center);
  }

  public List<String> getWgsAccessions() {
    return Collections.unmodifiableList(wgsAccessions);
  }

  public void setWgsAccessions(List<String> wgsAccessions) {
    this.wgsAccessions = wgsAccessions;
  }

  public void addWgsAccession(String wgsAccession) {
    this.wgsAccessions.add(wgsAccession);
  }

  public List<String> getConAccessions() {
    return Collections.unmodifiableList(conAccessions);
  }

  public void setConAccessions(List<String> conAccessions) {
    this.conAccessions = conAccessions;
  }

  public void addConAccession(String conAccession) {
    this.conAccessions.add(conAccession);
  }

  public List<String> getStdAccessions() {
    return Collections.unmodifiableList(stdAccessions);
  }

  public void setStdAccessions(List<String> stdAccessions) {
    this.stdAccessions = stdAccessions;
  }

  public void addStdAccession(String stdAccession) {
    this.stdAccessions.add(stdAccession);
  }
}
