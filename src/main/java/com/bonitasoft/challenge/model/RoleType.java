package com.bonitasoft.challenge.model;

public enum RoleType {
	
	ADMIN("ADMIN"), 
	CHEF("CHEF"), 
	USER("USER");
	
    private String value;

    RoleType(String value){
    	this.value = value;
    }

    @Override
    public String toString() {
    	return this.value;
    }
}