package vn.tr.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private Integer code;
	private String message;
	private T data;
	
	// Success response
	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder()
				.code(200)
				.message("Success")
				.data(data)
				.build();
	}
	
	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
				.code(200)
				.message(message)
				.data(data)
				.build();
	}
	
	public static <T> ApiResponse<T> error(String message) {
		return error(500, message);
	}
	
	// Error response
	public static <T> ApiResponse<T> error(Integer code, String message) {
		return ApiResponse.<T>builder()
				.code(code)
				.message(message)
				.build();
	}
}
