package com.bonitasoft.challenge.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoleType {

	ADMIN("ADMIN"), CHEF("CHEF"), USER("USER");

	private String roleType;

	private RoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getRoleType() {
		return roleType;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static RoleType forValue(@JsonProperty("role") String roleType) {
		return Arrays.stream(RoleType.values()).filter(r -> r.getRoleType().equals(roleType)).findFirst()
				.orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", roleType)));

	}
}