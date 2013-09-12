package com.levelup.http;

public class Header {
	
	private final String name;
	private final String value;

	public Header(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append('{');
		sb.append(name);
		sb.append(':');
		sb.append(value);
		sb.append('}');
		return sb.toString();
	}
	
}