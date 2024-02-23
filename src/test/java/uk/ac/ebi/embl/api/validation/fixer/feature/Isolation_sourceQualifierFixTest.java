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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class Isolation_sourceQualifierFixTest {

  private Isolation_sourceQualifierFix check;
  public FeatureFactory featureFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    featureFactory = new FeatureFactory();
    DataRow feature_qualifier_values_row1 =
        new DataRow(
            "lat_lon",
            "N",
            "Y",
            "Y",
            "^\\s*(-{0,1}\\s*\\d{1,6}(\\.\\d{1,6}){0,1})\\s+(S|N)\\s*,{0,1}\\s+(-{0,1}\\s*\\d{1,6}(\\.\\d{1,6}){0,1})\\s+(W|E)\\s*$",
            "22",
            "(null)");
    DataRow feature_qualifier_values_row2 =
        new DataRow(
            "country",
            "N",
            "Y",
            "Y",
            "^\\s*([^:]+)(?:\\s*(:)\\s*([^,]+)(?:\\s*(,)\\s*(.+)){0,1}){0,1}\\s*$",
            "21",
            "(null)");
    DataRow feature_qualifier_values_row3 =
        new DataRow(
            "EC_number",
            "N",
            "Y",
            "Y",
            "^(?:(\\d{0,3}\\-{0,1}\\.)){3}\\d{0,3}\\-{0,1}$",
            "70",
            "(null)");
    DataRow feature_regex_groups_row1 =
        new DataRow(
            "country",
            "1",
            "FALSE",
            "Afghanistan,Albania,Algeria,American Samoa,Andorra,Angola,Anguilla,Antarctica,Antigua and Barbuda,Arctic Ocean,Argentina,Armenia,Aruba,Ashmore and Cartier Islands,Atlantic Ocean,Australia,Austria,Azerbaijan,Bahamas,Bahrain,Baker Island,Bangladesh,Barbados,Bassas da India,Belarus,Belgium,Belgian Congo,Belize,Benin,Bermuda,Bhutan,Bolivia,Bosnia and Herzegovina,Borneo,Botswana,Bouvet Island,Brazil,British Virgin Islands,British Guiana,Brunei,Bulgaria,Burkina Faso,Burma,Burundi,Cambodia,Cameroon,Canada,Cape Verde,Cayman Islands,Central African Republic,Chad,Chile,China,Christmas Island,Clipperton Island,Cocos Islands,Colombia,Comoros,Cook Islands,Coral Sea Islands,Costa Rica,Cote d'Ivoire,Croatia,Cuba,Cyprus,Czech Republic,Czechoslovakia,Democratic Republic of the Congo,Denmark,Djibouti,Dominica,Dominican Republic,East Timor,Ecuador,Egypt,El Salvador,Equatorial Guinea,Eritrea,Estonia,Ethiopia,Europa Island,Falkland Islands (Islas Malvinas),Faroe Islands,Fiji,Finland,Former Yugoslav Republic of Macedonia,France,French Guiana,French Polynesia,French Southern and Antarctic Lands,Gabon,Gambia,Gaza Strip,Georgia,Germany,Ghana,Gibraltar,Glorioso Islands,Greece,Greenland,Grenada,Guadeloupe,Guam,Guatemala,Guernsey,Guinea,Guinea-Bissau,Guyana,Haiti,Heard Island and McDonald Islands,Honduras,Hong Kong,Howland Island,Hungary,Iceland,India,Indian Ocean,Indonesia,Iran,Iraq,Ireland,Isle of Man,Israel,Italy,Jamaica,Jan Mayen,Japan,Jarvis Island,Jersey,Johnston Atoll,Jordan,Juan de Nova Island,Kazakhstan,Kenya,Kerguelen Archipelago,Kingman Reef,Kiribati,Kosovo,Kuwait,Kyrgyzstan,Laos,Latvia,Lebanon,Lesotho,Liberia,Libya,Liechtenstein,Lithuania,Luxembourg,Macau,Macedonia,Madagascar,Malawi,Malaysia,Maldives,Mali,Malta,Marshall Islands,Martinique,Mauritania,Mauritius,Mayotte,Mediterranean Sea,Mexico,Micronesia,Midway Islands,Moldova,Monaco,Mongolia,Montenegro,Montserrat,Morocco,Mozambique,Myanmar,Namibia,Nauru,Navassa Island,Nepal,Netherlands,Netherlands Antilles,New Caledonia,New Zealand,Nicaragua,Niger,Nigeria,Niue,Norfolk Island,North Korea,Northern Mariana Islands,North Sea,Norway,Oman,Pacific Ocean,Pakistan,Palau,Palmyra Atoll,Panama,Papua New Guinea,Paracel Islands,Paraguay,Peru,Philippines,Pitcairn Islands,Poland,Portugal,Puerto Rico,Qatar,Republic of the Congo,Reunion,Romania,Ross Sea,Russia,Rwanda,Saint Helena,Saint Kitts and Nevis,Saint Lucia,Saint Pierre and Miquelon,Saint Vincent and the Grenadines,Samoa,San Marino,Sao Tome and Principe,Saudi Arabia,Senegal,Serbia,Serbia and Montenegro,Seychelles,Siam,Sierra Leone,Singapore,Slovakia,Slovenia,Solomon Islands,Somalia,South Africa,South Georgia and the South Sandwich Islands,South Korea,Southern Ocean,Spain,Spratly Islands,Sri Lanka,Sudan,Suriname,Svalbard,Swaziland,Sweden,Switzerland,Syria,Taiwan,Tajikistan,Tanzania,Tasman Sea,Thailand,Togo,Tokelau,Tonga,Trinidad and Tobago,Tromelin Island,Tunisia,Turkey,Turkmenistan,Turks and Caicos Islands,Tuvalu,USA,USSR,Uganda,Ukraine,United Arab Emirates,United Kingdom,Uruguay,Uzbekistan,Vanuatu,Venezuela,Viet Nam,Virgin Islands,Wake Island,Wallis and Futuna,West Bank,Western Sahara,Yemen,Yugoslavia,Zaire,Zambia,Zimbabwe,Sint Maarten,Curacao,Line Islands,South Sudan");
    DataRow feature_regex_groups_row2 =
        new DataRow("satellite", "1", "TRUE", "microsatellite,minisatellite,satellite");

    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_REGEX_GROUPS,
        feature_regex_groups_row1,
        feature_regex_groups_row2);
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_QUALIFIER_VALUES,
        feature_qualifier_values_row1,
        feature_qualifier_values_row2,
        feature_qualifier_values_row3);
    check = new Isolation_sourceQualifierFix();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_noQualifiers() {

    assertEquals(0, check.check(featureFactory.createFeature("feature")).getMessages().size());
  }

  @Test
  public void testCheck_noIsolationQualifier() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.ANTICODON_QUALIFIER_NAME);
    assertEquals(0, check.check(feature).getMessages().size());
  }

  @Test
  public void testCheck_isolationQualifierwithValidValue() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, "stem tissue");
    assertEquals(0, check.check(feature).getMessages().size());
  }

  @Test
  public void testCheck_isolationQualifierwithCountryValue() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, "Japan:Okayama, Kurashiki");
    ValidationResult result = check.check(feature);
    assertEquals(1, result.getMessages().size());
    assertEquals(1, feature.getQualifiers(Qualifier.COUNTRY_QUALIFIER_NAME).size());
    assertEquals(0, feature.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size());
    assertEquals(1, result.count("Isolation_sourceQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_isolationQualifierwithLat_lonValue() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, "13.59 S 75.3 W");
    ValidationResult result = check.check(feature);
    assertEquals(1, result.getMessages().size());
    assertEquals(1, feature.getQualifiers(Qualifier.LAT_LON_QUALIFIER_NAME).size());
    assertEquals(0, feature.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size());
    assertEquals(1, result.count("Isolation_sourceQualifierFix_2", Severity.FIX));
  }
}
