package eu.leads.api.m24.model;

import eu.leads.api.m24.FunctionalityAbstResultRow;

public class Functionality1ResultRowAgreed extends FunctionalityAbstResultRow {
	public String shop_name = null;
	public String prod_name = null;
	public String day = null;
	public String prod_price_min = null;
	public String prod_price_max = null;
	public String prod_price_cur = null;
	
	@Override
	public String toString() {
		return "Functionality1ResultRowAgreed [shop_name=" + shop_name
				+ ", prod_name=" + prod_name + ", day=" + day
				+ ", prod_price_min=" + prod_price_min
				+ ", prod_price_max=" + prod_price_max
				+ ", prod_price_cur=" + prod_price_cur + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((prod_name == null) ? 0 : prod_name.hashCode());
		result = prime
				* result
				+ ((prod_price_cur == null) ? 0 : prod_price_cur.hashCode());
		result = prime
				* result
				+ ((prod_price_max == null) ? 0 : prod_price_max.hashCode());
		result = prime
				* result
				+ ((prod_price_min == null) ? 0 : prod_price_min.hashCode());
		result = prime * result
				+ ((shop_name == null) ? 0 : shop_name.hashCode());
		result = prime * result
				+ ((day == null) ? 0 : day.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Functionality1ResultRowAgreed other = (Functionality1ResultRowAgreed) obj;
		if (prod_name == null) {
			if (other.prod_name != null)
				return false;
		} else if (!prod_name.equals(other.prod_name))
			return false;
		if (prod_price_cur == null) {
			if (other.prod_price_cur != null)
				return false;
		} else if (!prod_price_cur.equals(other.prod_price_cur))
			return false;
		if (prod_price_max == null) {
			if (other.prod_price_max != null)
				return false;
		} else if (!prod_price_max.equals(other.prod_price_max))
			return false;
		if (prod_price_min == null) {
			if (other.prod_price_min != null)
				return false;
		} else if (!prod_price_min.equals(other.prod_price_min))
			return false;
		if (shop_name == null) {
			if (other.shop_name != null)
				return false;
		} else if (!shop_name.equals(other.shop_name))
			return false;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		return true;
	}
	
	
}	
