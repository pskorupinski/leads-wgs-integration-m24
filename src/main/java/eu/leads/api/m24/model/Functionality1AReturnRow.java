package eu.leads.api.m24.model;

import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;

///////////////////////////////////////////////////////////////////

public class Functionality1AReturnRow extends FunctionalityAbstResultRow implements Comparable<Functionality1AReturnRow> {
	public String keyword = null;
	public String country_code = null;
	public String shop_name = null;
	public Long week = null;
	public Long products_no = null;
	
	@Override
	public String toString() {
		return "Functionality1AReturnRow [keyword=" + keyword
				+ ", country_code=" + country_code + ", shop_name="
				+ shop_name + ", week=" + week + ", products_no="
				+ products_no + "]";
	}
	
	@Override
	public int compareTo(Functionality1AReturnRow other) {
		if(keyword.equals(other.keyword)) {
			if(country_code.equals(other.country_code)) {
				if(shop_name.equals(other.shop_name)) {
					if(week.equals(other.week)) {
						return 0;
					}
					else return week.compareTo(other.week);
				}
				else return shop_name.compareTo(other.shop_name);
			}
			else return country_code.compareTo(other.country_code);
		}
		else return keyword.compareTo(other.keyword);
	}
}