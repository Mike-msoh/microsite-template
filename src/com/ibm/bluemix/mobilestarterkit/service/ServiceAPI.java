/*
 * Copyright 2015 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.bluemix.mobilestarterkit.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


@Path("/service")
public class ServiceAPI {

	@Path("/login")
	@POST
	public String checkLogin(String creds) {
		
		System.out.println("checkLogin () ");
		
		String PAGE_ADDRESS = "www.myibmpage.com";
		String IP_ADDRESS = "192.168.22.23";
		String BROWSER = "firefox";
				
		// Insert log into LogTable
		saveLog(PAGE_ADDRESS, IP_ADDRESS, BROWSER);
		
		try {
			JSONObject credentials = new JSONObject(creds);
			String userID = credentials.getString("user_id");
			String password = credentials.getString("password");
			
			if (userID.equals("admin") && password.equals("password")) {
				return "Successful";
			} else {
				return "Failed";
			}

		} catch (JSONException e) {
			e.getStackTrace();
			return "Failed";

		}
	}
	
	private void saveLog(String PAGE_ADDRESS, String IP_ADDRESS, String BROWSER){

		System.out.println("saveLog () ");
		
		/*
		 * Look up DB for dashDB to connect with VCAP_Service
		 * 
		 */
		
		String lookupName = null;
		try {
			com.ibm.json.java.JSONObject vcap = getVcapServices();
			if(vcap.get("dashDB") != null) {
				com.ibm.json.java.JSONObject dashDB0 = (com.ibm.json.java.JSONObject)((com.ibm.json.java.JSONArray)vcap.get("dashDB")).get(0);
				String luName = (String)dashDB0.get("name");
				if(luName != null) {
					lookupName = "jdbc/"+luName;
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DataSource dataSource = null ; 
		
		try  { 
			javax.naming.Context ctx = new InitialContext (); 
            if(lookupName != null) {
            	dataSource = (DataSource )ctx.lookup(lookupName); 
            } else {
            	dataSource = (DataSource )ctx.lookup( "jdbc/dashDB-microsite" ); 
            }
            
        }  catch  (NamingException e)  { 
            e.printStackTrace (); 
        }
		
		
		/*
		 * Check if USER_LOG table exists in DashDB
		 * 
		 */
		
		try {
			Connection conn = dataSource.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT TABNAME,TABSCHEMA FROM SYSCAT.TABLES where TABNAME = 'USER_LOG'");
			ResultSet rs = stmt.executeQuery();
			
			boolean tableExist = false;
			
			if(rs!=null){
				while(rs.next()) {
					System.out.println("table name : " + rs.getString(1));
					if(rs.getString(1).equals("USER_LOG")){
						tableExist = true;
					}
				}
			}else {
				tableExist = false;
			}
			
			/*
			 * If there is no table, create it.
			 * 
			 */
			if(!tableExist){
				System.out.println("table NOT exist ");
				
				Statement crtStatement = conn.createStatement();
				String crtSql = "CREATE TABLE USER_LOG(PAGE_ADDRESS CHAR (80), IP_ADDRESS CHAR (20), BROWSER CHAR (200), ACCESS_TIME TIMESTAMP)" ;
				crtStatement.executeUpdate(crtSql);
				
				System.out.println("Create done!!");
			}
			
			
			/*
			 * Insert Log information
			 * 
			 */
			Statement insertStatement = conn.createStatement();
			String insertSql = "INSERT INTO USER_LOG (PAGE_ADDRESS, IP_ADDRESS, BROWSER, ACCESS_TIME) VALUES ('"
								+ PAGE_ADDRESS + "','" + IP_ADDRESS + "','" + BROWSER + "',  CURRENT TIMESTAMP)";
			insertStatement.executeUpdate(insertSql);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private com.ibm.json.java.JSONObject getVcapServices() {
		String vcap = System.getenv("VCAP_SERVICES");
		if (vcap == null) return null;
		com.ibm.json.java.JSONObject vcapObject = null;
		try {
			vcapObject = com.ibm.json.java.JSONObject.parse(vcap);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vcapObject;
}

	@Path("/searchtips")
	@POST
	public String searchTips(String keyword){

		String QandAList = null;

		try {

			JSONObject keywordObj = new JSONObject(keyword);
			String question = keywordObj.getString("question");
			
			JSONObject responseJSON = new JSONObject();
			
			if( question.equals( "depression" ) ) {
			
				JSONObject questionandasnwer = new JSONObject();
				questionandasnwer.put("question", "What is " + question + "?");
				
				JSONArray answerArray = new JSONArray();
			
				JSONObject answers = new JSONObject();
				answers.put( "text", "you can not sleep or you sleep too much");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you can not concentrate or find that previously easy tasks are now difficult");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you feel hopeless and helpless");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you can not control your negative thoughts, no matter how much you try");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text",  "you have lost your appetite or you can not stop eating");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you are much more irritable, short-tempered, or aggressive than usual");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you are consuming more alcohol than normal or engaging in other reckless behavior");
				answerArray.put( answers );
				
				answers = new JSONObject();
				answers.put( "text", "you have thoughts that life is not worth living (seek help immediately if this is the case");
				answerArray.put( answers );
				
				questionandasnwer.put("answers", answerArray);
				
				JSONArray qanda = new JSONArray();
				qanda.put( questionandasnwer );
				
				responseJSON.put("qa", qanda);
				return responseJSON.toString();
				//System.out.println(responseJSON.toString());
			} else {
				return "Failed";
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return "Failed";
		}
	}

	
}
