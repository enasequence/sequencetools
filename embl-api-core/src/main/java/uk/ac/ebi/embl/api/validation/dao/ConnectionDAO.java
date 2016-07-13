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
package uk.ac.ebi.embl.api.validation.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ConnectionDAO.
 */
public class ConnectionDAO
{

	private static final String OCI_URL = "jdbc:oracle:oci8:";
	private static final String THIN_URL = "jdbc:oracle:thin:";
	private static final String WINDOWS_WALLET_LOCATION = "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY= c:\\oraclewallet\\wallet)))";
	private static final String TNS_ADMIN_WINDOWS = "c:\\oraclewallet";
	private static final String LINUX_WALLET_LOCATION = "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=%s/wallet)))";
	private static final String TNS_ADMIN_LINUX = "%s" + File.separatorChar + "network" + File.separatorChar + "admin";
	private static final String URL = "jdbc:oracle:thin:@%s";
	private static final String TNS_ADMIN_PROPERTY = "oracle.net.tns_admin";
	private static final String WALLET_LOCATION_PATH = "oracle.net.wallet_location";
	private static final String[] databaseToken={"enapro","enadev","engendev","erapro","enarls"};

	
	/**
	 * Gets a datasource for the dbname.
	 * 
	 * @param dbname
	 *            the database name
	 * @return the connection
	 * @throws SQLException 
	 */
	public static DataSource getDataSource(String dbname) throws SQLException
	{
		dbname = dbname.toLowerCase();
		
		boolean validdb = StringUtils.indexOfAny(dbname,databaseToken)!=-1;
		
		if (!validdb)
		{
			throw new SQLException("invalid database name: " + dbname + ". Valid databases are: ENADEV, ENAPRO, ENARLS, ERAPRO, ENGENDEV");
		}
       
		String home = System.getProperty("user.home");
		java.util.Properties info = new java.util.Properties();


		if (SystemUtils.IS_OS_WINDOWS)// in wimdows OS
		{
			System.setProperty(TNS_ADMIN_PROPERTY, TNS_ADMIN_WINDOWS);
			info.put(WALLET_LOCATION_PATH, WINDOWS_WALLET_LOCATION);
		} else
		{
			String oracleHome = System.getenv("ORACLE_HOME");
			System.setProperty(TNS_ADMIN_PROPERTY, String.format(TNS_ADMIN_LINUX, oracleHome));
			info.put(WALLET_LOCATION_PATH, String.format(LINUX_WALLET_LOCATION, home));
		}

		OracleDataSource ds = new OracleDataSource();
		ds.setURL(String.format(URL, dbname));
		ds.setConnectionProperties(info);
		//ds.setConnectionCachingEnabled(true);

		return ds;
	}
	
	
	/**
	 * Gets the connection.
	 * 
	 * @param dbname
	 *            the database name
	 * @return the connection
	 * @throws SQLException 
	 */
	public static Connection getConnection(String dbname) throws SQLException
	{
		DataSource ds = getDataSource(dbname);
		return ds.getConnection();
	}

	/**
	 * Gets the oCI connection.
	 * 
	 * @param url
	 *            the url
	 * @return the oCI connection
	 */
	public static OracleConnection getOCIConnection(String url)
	{
		OracleConnection dbConnection = null;
		String dbUrl = null;
		try
		{
			Class.forName("oracle.jdbc.OracleDriver");
			DriverManager.registerDriver(new OracleDriver());

			if (url.matches(".*:[0-9]{4}.*"))
			{
				dbUrl = THIN_URL + url;
			} else
			{
				if (url.contains("@"))
				{
					dbUrl = OCI_URL + url;
				} else
				{
					dbUrl = OCI_URL + "@" + url;
				}
			}
			dbConnection = (OracleConnection) DriverManager.getConnection(dbUrl);
			dbConnection.setAutoCommit(false);
			return dbConnection;
		} catch (SQLException e)
		{
			System.err.println("Error connecting to db: " + dbUrl);
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			System.err.println("Error connecting to db: " + dbUrl);
			e.printStackTrace();
		}
		return dbConnection;
	}
	
	
}
