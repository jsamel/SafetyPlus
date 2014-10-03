package com.example.safetyapp;

import java.util.List;

public class MapsResponse {
	public List<String> html_attributions;
	public List<Result> results;
	
	public String toString() {
		return "MapsResponse: " + html_attributions + "\t" + results;
	}
}
