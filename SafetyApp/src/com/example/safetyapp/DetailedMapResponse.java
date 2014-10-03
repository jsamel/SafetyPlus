package com.example.safetyapp;

import java.util.List;

public class DetailedMapResponse {
	public List<String> html_attributions;
	public DetailResult result;
	public String status;
	
	public String toString() {
		return "MapsResponse: " + html_attributions + "\t" + result + "\t" + status;
	}
}
