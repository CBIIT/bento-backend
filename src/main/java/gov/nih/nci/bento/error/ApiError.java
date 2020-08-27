package gov.nih.nci.bento.error;

import com.google.gson.GsonBuilder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ApiError extends Throwable {

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private HttpStatus status;
	private String message;
	private List<String> errors;
	private String time;

	public static String jsonApiError(HttpStatus status, String message){
		return jsonApiError(new ApiError(status, message));
	}

	public static String jsonApiError(HttpStatus status, String message, String error){
		return jsonApiError(new ApiError(status, message, error));
	}

	public static String jsonApiError(HttpStatus status, String message, List<String> errors){
		return jsonApiError(new ApiError(status, message, errors));
	}

	public static String jsonApiError(ApiError error){
		return new GsonBuilder().create().toJson(new ApiErrorWrapper(error));
	}

	public ApiError(HttpStatus status, String message, List<String> errors) {
		this.status = status;
		this.message = message;
		this.errors = errors;
		this.time = formatter.format(LocalDateTime.now());
	}

	public ApiError(HttpStatus status, String message, String error) {
		this(status, message, Arrays.asList(error));
	}

	public ApiError(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
		this.time = formatter.format(LocalDateTime.now());
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getTime() {
		return time;
	}
}