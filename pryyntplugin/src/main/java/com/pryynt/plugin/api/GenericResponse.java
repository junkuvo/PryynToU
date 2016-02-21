package com.pryynt.plugin.api;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "status_code", "error_message", "data" })
class GenericResponse {
	
	public static final String R_RESULT_SUCCESS = "1";
	public static final String R_MEDIA_MATCH_BY_CHECKSUM = "2";
	

	@JsonProperty("status_code")
	private Integer statusCode;
	@JsonProperty("error_message")
	private String errorMessage;
	@JsonProperty("data")
	private String data;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The statusCode
	 */
	@JsonProperty("status_code")
	public Integer getStatusCode() {
		return statusCode;
	}

	/**
	 * 
	 * @param statusCode
	 *            The status_code
	 */
	@JsonProperty("status_code")
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * 
	 * @return The errorMessage
	 */
	@JsonProperty("error_message")
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * 
	 * @param errorMessage
	 *            The error_message
	 */
	@JsonProperty("error_message")
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 
	 * @return The data
	 */
	@JsonProperty("data")
	public String getData() {
		return data;
	}

	/**
	 * 
	 * @param data
	 *            The data
	 */
	@JsonProperty("data")
	public void setData(String data) {
		this.data = data;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
