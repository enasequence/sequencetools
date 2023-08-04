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
package uk.ac.ebi.embl.template;

import java.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TemplateXMLHandler extends DefaultHandler {
  private MutableTemplateInfo template;
  /**
   * keep a local copy of the tokens despite their being stored in the template - speeds up access
   * where needed here
   */
  private Map<String, TemplateTokenInfo> tokens = new HashMap<String, TemplateTokenInfo>();

  String currentString;

  public TemplateXMLHandler() {
    this.template = new MutableTemplateInfo();
    currentString = "";
  }

  public MutableTemplateInfo getTemplate() {
    return template;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equalsIgnoreCase("name")) {
      template.name = currentString;
    } else if (qName.equals("description")) {
      template.description = currentString;
    } else if (qName.equals("template_string")) {
      template.templateString = currentString;
    } else if (qName.equals("comment")) {
      template.comment = currentString;
    } else if (qName.equals("example_string")) {
      currentString = currentString.trim();
      template.example = currentString;
    }
    currentString = ""; // clear the current string
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (qName.equalsIgnoreCase("template")) {
      String id = attributes.getValue("id");
      String version = attributes.getValue("version");
      template.id = id;
      template.version = Integer.valueOf(version);

    } else if (qName.equalsIgnoreCase("token")) {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type");
      String mand = attributes.getValue("mandatory");
      String displayName = attributes.getValue("display_name");
      if (displayName != null && displayName.contains(","))
        throw new SAXException("Token display name must not contain a comma - " + displayName);
      String description = attributes.getValue("description");
      if (description != null && description.equals("")) description = null;
      String tip = attributes.getValue("tip");
      if (tip != null && tip.equals("")) tip = null;
      String cvName = attributes.getValue("cv_name");
      if (cvName != null && cvName.equals("")) {
        cvName = null;
      }

      String variableOnly = attributes.getValue("variable_only");
      if (variableOnly != null && variableOnly.equals("")) {
        variableOnly = "false";
      }

      try {
        TemplateTokenType tokenType = TemplateTokenType.valueOf(type);
        TemplateTokenInfo tokenInfo =
            new TemplateTokenInfo(
                name,
                tokenType,
                (mand != null && mand.equals("true")),
                displayName,
                description,
                tip);

        if (cvName != null) {
          tokenInfo.setCvName(cvName);
        }

        if (variableOnly != null) { // dont care what the value is - just that it is there
          tokenInfo.setVariableOnly(true);
        }

        template.tokenInfos.add(tokenInfo);
        tokens.put(name, tokenInfo);
      } catch (IllegalArgumentException e) {
        throw new SAXException("Token type not recognized?! " + type);
      }
    } else if (qName.equalsIgnoreCase("section")) {
      String name = attributes.getValue("name");
      String depends = attributes.getValue("mandatory");

      List<String> dependsList = Arrays.asList(depends.split(","));
      List<TemplateTokenInfo> dependsTokens = new ArrayList<TemplateTokenInfo>();
      for (String tokenName : dependsList) {
        TemplateTokenInfo tokenInfo = tokens.get(tokenName);
        if (tokenInfo != null) {
          dependsTokens.add(tokenInfo);
        } else {
          throw new SAXException("Section depends on token that does not exist?! " + tokenName);
        }
      }

      template.sectionInfos.add(new TemplateSectionInfo(name, dependsTokens));
    } else if (qName.equalsIgnoreCase("token_group")) {
      String name = attributes.getValue("name");
      String contains = attributes.getValue("contains");
      String description = attributes.getValue("description");
      String mandatoryAttribute = attributes.getValue("mandatory");
      if (description != null && description.equals("")) {
        description = null;
      }

      boolean mandatory = true; // default
      if (mandatoryAttribute != null && mandatoryAttribute.equals("false")) {
        mandatory = false;
      }

      List<String> containsStrings = Arrays.asList(contains.split(","));
      template.groupInfo.add(
          new TemplateTokenGroupInfo(name, containsStrings, description, mandatory));

    } else if (qName.equalsIgnoreCase("name")
        || qName.equals("description")
        || qName.equals("comment")
        || qName.equals("id")
        || qName.equals("template_string")) {
      currentString = ""; // clear the current string (will have accumulated new line characters)
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    StringBuffer buffer = new StringBuffer();
    for (int i = start; i < start + length; i++) {
      buffer.append(ch[i]);
    }
    this.currentString = currentString.concat(buffer.toString());
  }
}
