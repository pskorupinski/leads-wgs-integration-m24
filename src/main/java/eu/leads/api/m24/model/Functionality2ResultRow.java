package eu.leads.api.m24.model;

import eu.leads.api.m24.FunctionalityAbstResultRow;

public class Functionality2ResultRow extends FunctionalityAbstResultRow implements Comparable<Functionality2ResultRow> {
	public String site = null;
	public String keywords = null;
	public Long   week = null;
	public Long   mentions_no = null;
	public Double avg_sentiment = null;
	
	@Override
	public int compareTo(Functionality2ResultRow other) {
		if(keywords.equals(other.keywords)) {
			if(site.equals(other.site)) {
				if(week.equals(other.week)) {
					return 0;
				}
				else return week.compareTo(other.week);
			}
			else return site.compareTo(other.site);
		}
		else return keywords.compareTo(other.keywords);
	}

	@Override
	public String toString() {
		return "Functionality2ResultRow [url=" + site + ", keywords="
				+ keywords + ", week=" + week + ", mentions_no="
				+ mentions_no + ", avg_sentiment=" + avg_sentiment + "]";
	}
}
