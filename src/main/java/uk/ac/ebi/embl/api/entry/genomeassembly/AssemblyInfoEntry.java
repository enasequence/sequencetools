/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.entry.genomeassembly;

import java.util.Date;

public class AssemblyInfoEntry extends GCSEntry {
  private String submissionId;
  private String name;
  private String setId;
  private String projectId;
  private String wgsId;
  private String masterId;
  private String sampleId;
  private String submissionAccountId;
  private String coverage;
  private Integer minGapLength;
  private Integer contigCount;
  private Integer scaffoldCount;
  private Integer chromosomeCount;
  private String contigAccRange;
  private String scaffoldAccRange;
  private String chromosomeAccRange;
  private String biosampleId;
  private Integer minContigCount;
  private Integer minScaffoldCount;
  private String gcId;
  private String program;
  private String platform;
  private String moleculeType;
  private String studyId;
  private boolean tpa;
  private String assemblyType;
  private String organism;
  private String authors;
  private String address;
  private Date date;
  private String distribute;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getAuthors() {
    return authors;
  }

  public void setAuthors(String authors) {
    this.authors = authors;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getOrganism() {
    return organism;
  }

  public void setOrganism(String organism) {
    this.organism = organism;
  }

  public String getAssemblyType() {
    return assemblyType;
  }

  public void setAssemblyType(String assemblyType) {
    this.assemblyType = assemblyType;
  }

  public boolean isTpa() {
    return tpa;
  }

  public void setTpa(boolean tpa) {
    this.tpa = tpa;
  }

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getGcId() {
    return gcId;
  }

  public void setGcId(String gcId) {
    this.gcId = gcId;
  }

  public Integer getMinContigCount() {
    return minContigCount;
  }

  public void setMinContigCount(Integer minContigCount) {
    this.minContigCount = minContigCount;
  }

  public Integer getMinScaffoldCount() {
    return minScaffoldCount;
  }

  public void setMinScaffoldCount(Integer minScaffoldCount) {
    this.minScaffoldCount = minScaffoldCount;
  }

  public String getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(String submissionId) {
    this.submissionId = submissionId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSetId() {
    return setId;
  }

  public void setSetId(String setId) {
    this.setId = setId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getWgsId() {
    return wgsId;
  }

  public void setWgsId(String wgsId) {
    this.wgsId = wgsId;
  }

  public String getMasterId() {
    return masterId;
  }

  public void setMasterId(String masterId) {
    this.masterId = masterId;
  }

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getSubmissionAccountId() {
    return submissionAccountId;
  }

  public void setSubmissionAccountId(String submissionAccountId) {
    this.submissionAccountId = submissionAccountId;
  }

  public String getCoverage() {
    return coverage;
  }

  public void setCoverage(String coverage) {
    this.coverage = coverage;
  }

  public Integer getMinGapLength() {
    return minGapLength;
  }

  public void setMinGapLength(Integer minGapLength) {
    this.minGapLength = minGapLength;
  }

  public Integer getContigCount() {
    return contigCount;
  }

  public void setContigCount(Integer contigCount) {
    this.contigCount = contigCount;
  }

  public Integer getScaffoldCount() {
    return scaffoldCount;
  }

  public void setScaffoldCount(Integer scaffoldCount) {
    this.scaffoldCount = scaffoldCount;
  }

  public Integer getChromosomeCount() {
    return chromosomeCount;
  }

  public void setChromosomeCount(Integer chromosomeCount) {
    this.chromosomeCount = chromosomeCount;
  }

  public String getContigAccRange() {
    return contigAccRange;
  }

  public void setContigAccRange(String contigAccRange) {
    this.contigAccRange = contigAccRange;
  }

  public String getScaffoldAccRange() {
    return scaffoldAccRange;
  }

  public void setScaffoldAccRange(String scaffoldAccRange) {
    this.scaffoldAccRange = scaffoldAccRange;
  }

  public String getChromosomeAccRange() {
    return chromosomeAccRange;
  }

  public void setChromosomeAccRange(String chromosomeAccRange) {
    this.chromosomeAccRange = chromosomeAccRange;
  }

  public String getBiosampleId() {
    return biosampleId;
  }

  public void setBiosampleId(String biosampleId) {
    this.biosampleId = biosampleId;
  }

  public void setMoleculeType(String moleculeType) {
    this.moleculeType = moleculeType;
  }

  public String getMoleculeType() {
    return moleculeType;
  }

  public String getDistribute() {
    return distribute;
  }

  public void setDistribute(String distribute) {
    this.distribute = distribute;
  }
}
