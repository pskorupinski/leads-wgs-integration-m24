package eu.leads.infext.datastore.impl;

import java.io.IOException;

import eu.leads.processor.web.QueryResults;
import eu.leads.processor.web.QueryStatus;
import eu.leads.processor.web.WebServiceClient;
import static java.lang.Thread.sleep;

public class LeadsDataStore {

    public static QueryResults send_query_and_wait(String sql) {
    	QueryResults res = null;
    	
    	try {
	        QueryStatus currentStatus = WebServiceClient.submitQuery("adidas",sql);
	        while(!currentStatus.getStatus().equals("COMPLETED") && !currentStatus.getStatus().equals("FAILED")) {
	            sleep(3000);
	            currentStatus = WebServiceClient.getQueryStatus(currentStatus.getId());
	//            System.out.print("s: " + currentStatus.toString());
	//            System.out.println(", o: " + currentStatus.toString());
	            System.out.println("The query with id " + currentStatus.getId() + " is " + currentStatus.getStatus());
	
	        }  //currentStatus.getStatus()!= QueryState.COMPLETED
	        System.out.println("The query with id " + currentStatus.getId() + " " + currentStatus.getStatus());
	        if(currentStatus.getStatus().equals("COMPLETED")) {
	            System.out.println("Wait while we fetching your result...");
	            res = WebServiceClient.getQueryResults(currentStatus.getId(), 0, -1);
	        }
	        else{
	            System.out.println("because " + currentStatus.getErrorMessage());
	        }
        
    	} catch (IOException | InterruptedException e) {
    		e.printStackTrace();
    	}
        
        return res;

    }
	
}
