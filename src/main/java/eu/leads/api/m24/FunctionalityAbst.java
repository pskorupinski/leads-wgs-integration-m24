package eu.leads.api.m24;

import java.util.SortedSet;

public interface FunctionalityAbst {
	public SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams params);	
}
