package com.bonitasoft.challenge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.lang.NonNull;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_id_sequence")
	@SequenceGenerator(initialValue = 2, name = "user_id_sequence", allocationSize = 1)
	@Column(name = "user_id")
	@NonNull
	public Long id;

	@Column(name = "role_type")
	@Enumerated(EnumType.STRING)
	@NonNull
	@NotNull
	private RoleType role;

	@Column(name = "user_name", unique = true)
	@NonNull
	@NotBlank
	private String userName;

	@Column(name = "user_password")
	@NonNull
	@NotBlank
	private String userPassword;

	@Column(name = "user_email", unique = true)
	@NonNull
	@NotBlank
	private String userEmail;

	/**
	 * 
	 */
	public User() {
		super();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the role
	 */
	public RoleType getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(RoleType role) {
		this.role = role;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userPassword
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * @param userPassword the userPassword to set
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * @return the userEmail
	 */
	public String getUserEmail() {
		return userEmail;
	}

	/**
	 * @param userEmail the userEmail to set
	 */
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [id=").append(id).append(", role=").append(role).append(", userName=").append(userName)
				.append(", userPassword=").append(userPassword).append(", userEmail=").append(userEmail).append("]");
		return builder.toString();
	}
}