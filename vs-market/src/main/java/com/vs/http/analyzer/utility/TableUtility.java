package com.vs.http.analyzer.utility;

public class TableUtility {
	public static String getTableContent(String content, int position) {
		String startString = "<table";
		String endString = "</table>";
		if (position < 0) {
			return "";
		}
		int tableStart = content.substring(0, position).lastIndexOf(startString);
		int tableEnd = content.substring(position).indexOf(endString);

		String tableContent = content.substring(tableStart, tableEnd + position + endString.length());
		// System.out.println(tableContent);
		return tableContent;
	}
}
