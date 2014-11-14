package eu.leads.api.m24;

import java.util.SortedSet;

public abstract class FunctionalityAbst {
	public abstract SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams params);	
}
