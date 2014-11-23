import java.util.List;

import org.json.JSONObject;

import eu.leads.infext.datastore.datastruct.UrlTimestamp;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import eu.leads.processor.web.QueryResults;


public class LeadsStorageQuery {

	public static void main(String[] args) {

		LeadsDataStore.initialize("http://clu25.softnet.tuc.gr", 8080);
		
		String query1 = "SELECT uri AS uri, ts AS ts\n"
				+ "FROM keywords\n"
				+ "WHERE ts>=0 AND ts <=1416501600106\n"
				+ "AND partid = 'ecom_prod_name:000'";
		
		QueryResults rs = LeadsDataStore.send_query_and_wait(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				System.out.println(row);
			}
		}
		
	}
}
