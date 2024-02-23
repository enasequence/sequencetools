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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.storage.tsv.TSVReader;

public class TemplateLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateLoader.class);

  public static String TEMPLATE_ORDERS_FILE = "/con_vocab/template_orders.csv";

  private final Environment environment;

  public TemplateLoader() {
    this.environment = Environment.DEV;
  }

  public TemplateLoader(Environment environment) {
    this.environment = environment;
  }

  protected TemplateSet loadTemplatesFromClassPath(List<String> template_class_paths)
      throws TemplateException {

    TemplateSet results = new TemplateSet();

    for (String template_class_path : template_class_paths) {
      loadTemplateFromClassPath(template_class_path, results);
    }
    return results;
  }

  private void loadTemplateFromClassPath(String templatePath, TemplateSet results)
      throws TemplateException {

    InputStream stream = this.getClass().getResourceAsStream(templatePath);
    LOGGER.debug("Loading template from classpath:" + templatePath);
    if (stream == null) {
      throw new TemplateException("Failed to load stream for resource: " + templatePath);
    }

    MutableTemplateInfo template = parsseTemplate(stream);
    addTemplate(results, template);
  }

  private void addTemplate(TemplateSet results, MutableTemplateInfo template)
      throws TemplateException {

    if (results.containsTemplateId(template.id)) {
      TemplateVersions versionMap = results.getTemplateVersions(template.id);
      if (versionMap.containsVersion(template.version)) {
        String message =
            "Template version " + template.version + " duplicated for template " + template.id;

        String duplicateName = versionMap.getTemplate(template.version).getName();
        message += " name " + duplicateName + " and " + template.name;
        throw new TemplateException(message);
      }
      versionMap.addTemplate(template.version, new TemplateInfo(template));
    } else {
      TemplateVersions versions = new TemplateVersions();
      versions.addTemplate(template.version, new TemplateInfo(template));
      results.addTemplateVersions(template.id, versions);
    }
  }

  protected Map<String, Integer> parseTemplateOrderFile(InputStream stream)
      throws TemplateException {

    Map<String, Integer> results = new HashMap<String, Integer>();

    try {
      if (stream != null) { // just bail if it is null - can go without ordering
        TSVReader reader = new TSVReader(",", "#");
        DataSet dataSet = reader.readDataSetAsStream(stream);

        /**
         * this will fail with an index out of bounds exception if the required elements are not
         * present - just let it fail at runtime as this is loaded at server startup and will get
         * picked up. The order is determined by the order of the templates in the file.
         */
        int rowNumber = 0;
        for (DataRow dataRow : dataSet.getRows()) {
          results.put(dataRow.getString(0), rowNumber);
          rowNumber++;
        }
      }
    } catch (IOException e) {
      throw new TemplateException(e);
    }

    return results;
  }

  protected TemplateSet loadTemplatesFromDirectory(String templateDir) throws TemplateException {

    TemplateSet results = new TemplateSet();

    File file = new File(templateDir);

    if (!file.exists()) {
      throw new TemplateException("Failed to load file for resource: " + templateDir);
    }

    if (!file.isDirectory()) {
      throw new TemplateException("File is not a directory: " + templateDir);
    }

    addTemplatesInDir(file, results);

    loadArchivedTemplates(templateDir, results);

    if (!environment.equals(Environment.PROD)) {
      loadprototypeTemplates(templateDir, results);
    }

    return results;
  }

  private void loadprototypeTemplates(String templateDir, TemplateSet results)
      throws TemplateException {

    File prototypeFile = new File(templateDir + File.separator + "prototype");
    if (!prototypeFile.exists()) {
      throw new TemplateException("Failed to load file for resource: " + prototypeFile.getPath());
    }
    if (!prototypeFile.isDirectory()) {
      throw new TemplateException("File is not a directory: " + prototypeFile.getPath());
    }
    addTemplatesInDir(prototypeFile, results);
  }

  private void loadArchivedTemplates(String templateDir, TemplateSet results)
      throws TemplateException {

    File archiveFile = new File(templateDir + File.separator + "archive");
    if (!archiveFile.exists()) {
      throw new TemplateException("Failed to load file for resource: " + archiveFile.getPath());
    }
    if (!archiveFile.isDirectory()) {
      throw new TemplateException("File is not a directory: " + archiveFile.getPath());
    }
    addTemplatesInDir(archiveFile, results);
  }

  private void addTemplatesInDir(File file, TemplateSet results) throws TemplateException {
    try {
      for (String templateFileName : file.list(new TemplateFileFilter())) {
        FileInputStream stream =
            new FileInputStream(file.getPath() + File.separator + templateFileName);
        MutableTemplateInfo template = parsseTemplate(stream);
        template.filePath = (file.getPath() + File.separator + templateFileName);
        addTemplate(results, template);
      }
    } catch (FileNotFoundException e) {
      throw new TemplateException(e);
    }
  }

  public TemplateInfo loadTemplateFromFile(File templateFile) throws TemplateException {

    try {
      if (!templateFile.exists()) {
        throw new TemplateException("Failed to load file for resource: " + templateFile.getName());
      }
      LOGGER.info("Loading: " + templateFile.getName());
      FileInputStream stream = new FileInputStream(templateFile);
      MutableTemplateInfo template = parsseTemplate(stream);
      template.filePath = (templateFile.getPath());

      return new TemplateInfo(template);
    } catch (FileNotFoundException e) {
      throw new TemplateException(e);
    }
  }

  public TemplateInfo loadTemplateFromString(String template) throws TemplateException {
    try {
      if (template == null || template.isEmpty())
        throw new TemplateException("Template provided from dB is NULL");
      InputStream stream = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
      MutableTemplateInfo mutableTemplateInfo = parsseTemplate(stream);
      return new TemplateInfo(mutableTemplateInfo);
    } catch (TemplateException e) {
      throw new TemplateException(e);
    }
  }

  private MutableTemplateInfo parsseTemplate(InputStream stream) throws TemplateException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();

      SAXParser saxParser = factory.newSAXParser();
      TemplateXMLHandler XMLHandler = new TemplateXMLHandler();
      saxParser.parse(stream, XMLHandler);
      MutableTemplateInfo template = XMLHandler.getTemplate();

      checkDuplicateTokens(template);

      processGroups(template);

      setTokenAndGroupOrder(template);

      template.newTemplate = TemplateIDs.NEW_TEMPLATE_IDS.contains(template.id);

      return template;
    } catch (ParserConfigurationException e) {
      throw new TemplateException(e);
    } catch (SAXException e) {
      throw new TemplateException(e);
    } catch (IOException e) {
      throw new TemplateException(e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new TemplateException(e);
    }
  }

  /**
   * throws error if a token name appears twice - will bail as soon as one duplicate is hit
   *
   * @param templateInfo
   * @throws TemplateException
   */
  private void checkDuplicateTokens(MutableTemplateInfo templateInfo) throws TemplateException {
    List<String> allTokenNames = new ArrayList<String>();
    for (TemplateTokenInfo tokenInfo : templateInfo.tokenInfos) {
      if (allTokenNames.contains(tokenInfo.getName())) {
        throw new TemplateException(
            "Token name '"
                + tokenInfo.getName()
                + "' duplicated in template '"
                + templateInfo.name
                + "'.");
      } else {
        allTokenNames.add(tokenInfo.getName());
      }
    }
  }

  /**
   * creates a group containing all tokens not contained in any other sections - an "all others"
   * group
   *
   * @param template -
   */
  private void processGroups(MutableTemplateInfo template) throws TemplateException {

    List<TemplateTokenGroupInfo> groupInfos = template.groupInfo;
    List<String> allGroupTokens = new ArrayList<String>();

    for (TemplateTokenGroupInfo groupInfo : groupInfos) {
      for (String newToken : groupInfo.getContainsString()) {
        if (allGroupTokens.contains(newToken)) {
          throw new TemplateException(
              "Token "
                  + newToken
                  + " in template group : '"
                  + groupInfo.getName()
                  + "' already exists in another group");
        } else {
          allGroupTokens.add(newToken);
        }
      }
    }

    List<String> containsList = new ArrayList<String>();
    for (TemplateTokenInfo tokenInfo : template.tokenInfos) {
      if (!allGroupTokens.contains(
          tokenInfo.getName())) { // if its not already in another group, add
        containsList.add(tokenInfo.getName());
      }
    }

    if (containsList.size() != 0) {
      TemplateTokenGroupInfo allOthersGroup =
          new TemplateTokenGroupInfo(null, containsList, "", true);
      // this will have a group order of '0' by default and will appear at the top when sorted
      template.groupInfo.add(allOthersGroup);
    }

    for (TemplateTokenGroupInfo groupInfo : template.groupInfo) {
      groupInfo.setParentGroups(template.tokenInfos);
    }
  }

  private void setTokenAndGroupOrder(MutableTemplateInfo templateInfo) {
    List<TemplateTokenGroupInfo> groupInfos = templateInfo.groupInfo;
    int groupOrder = 0;

    /**
     * for the tokens, first set the order for all tokens based on that which they are declared in
     * the file
     */
    int tokenOrder = 0;
    for (TemplateTokenInfo tokenInfo : templateInfo.tokenInfos) {
      tokenInfo.setOrder(tokenOrder);
      tokenOrder++;
    }

    /** for the groups - set the order to be that in which they are defined in the template file */
    for (TemplateTokenGroupInfo groupInfo : groupInfos) {
      groupInfo.setOrder(groupOrder);
      groupOrder++;

      /**
       * take any tokens defined in a group and reset their groupOrder to that as defined in the
       * token group
       */
      tokenOrder = 0;
      for (String tokenName : groupInfo.getContainsString()) {
        TemplateTokenInfo tokenInfo = getToken(templateInfo.tokenInfos, tokenName);
        if (tokenInfo == null) {
          LOGGER.warn(
              "Token "
                  + tokenName
                  + " defined in token group "
                  + groupInfo.getName()
                  + " is not defined in template "
                  + templateInfo.name);
          continue;
        }
        tokenInfo.setGroupOrder(tokenOrder);
        tokenOrder++;
      }
    }
  }

  private TemplateTokenInfo getToken(List<TemplateTokenInfo> tokenInfos, String tokenName) {
    for (TemplateTokenInfo tokenInfo : tokenInfos) {
      if (tokenInfo.getName().equals(tokenName)) {
        return tokenInfo;
      }
    }
    return null;
  }

  public TemplateInfo parseTemplate(InputStream stream) throws TemplateException {
    return new TemplateInfo(parsseTemplate(stream));
  }

  public static class StringIntComparator<T> implements Comparator<String> {

    public int compare(String o1, String o2) {
      return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
    }
  }

  private static class TemplateFileFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
      return !name.startsWith(".") && !name.startsWith("~") && name.endsWith(".xml");
    }
  }
}
