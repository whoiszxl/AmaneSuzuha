package com.whoiszxl.blockchain.bo;
/**
 * 前端响应对象
 * @author whoiszxl
 *
 */
public class Result {

	private int code;
	private String message;
	private Object data;
	
	
	public static Result Success(String message) {
		return new Result(200, message, null);
	}
	
	public static Result Success(String message, Object data) {
		return new Result(200, message, data);
	}
	
	public static Result Mistake(String message) {
		return new Result(-1, message, null);
	}

	
	public Result(int code, String message, Object data) {
		super();
		this.code = code;
		this.message = message;
		this.data = data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
	
}
