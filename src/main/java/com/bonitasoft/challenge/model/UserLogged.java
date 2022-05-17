package com.bonitasoft.challenge.model;

public class UserLogged extends User {

	private String sessionId;

	/**
	 * Default constructor
	 */
	public UserLogged() {
		super();
	}

	/**
	 * Constructor with father object
	 * 
	 * @param user
	 */
	public UserLogged(User user) {
		this.setId(user.getId());
		this.setRole(user.getRole());
		this.setUserEmail(user.getUserEmail());
		this.setUserName(user.getUserName());
		this.setUserPassword(user.getUserPassword());
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
