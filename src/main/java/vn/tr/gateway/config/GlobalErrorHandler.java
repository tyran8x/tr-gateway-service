package vn.tr.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tr.gateway.dto.ApiResponse;

import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
	private final ObjectMapper objectMapper;
	
	public GlobalErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		log.error("Global error handler caught exception: {}", ex.getMessage(), ex);
		
		ServerHttpResponse response = exchange.getResponse();
		
		if (response.isCommitted()) {
			log.warn("Response already committed, cannot handle error");
			return Mono.error(ex);
		}
		
		// Xác định HTTP status và error code
		HttpStatus status = determineHttpStatus(ex);
		Integer errorCode = status.value();
		String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Internal Server Error";
		
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		// Tạo response object
		ApiResponse<?> apiResponse = ApiResponse.error(errorCode, errorMessage);
		
		try {
			String jsonResponse = objectMapper.writeValueAsString(apiResponse);
			DataBuffer buffer = response.bufferFactory()
					.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
			
			return response.writeWith(Mono.just(buffer))
					.doOnError(error -> log.error("Error writing response", error));
			
		} catch (JsonProcessingException e) {
			log.error("Error serializing response", e);
			
			// Fallback response
			String fallback = String.format(
					"{\"code\":%d,\"message\":\"%s\",\"data\":null}",
					errorCode,
					escapeJson(errorMessage)
			                               );
			
			DataBuffer buffer = response.bufferFactory()
					.wrap(fallback.getBytes(StandardCharsets.UTF_8));
			
			return response.writeWith(Mono.just(buffer));
		}
	}
	
	private HttpStatus determineHttpStatus(Throwable ex) {
		if (ex instanceof IllegalArgumentException) {
			return HttpStatus.BAD_REQUEST;
		}
		if (ex instanceof IllegalStateException) {
			return HttpStatus.CONFLICT;
		}
		// Thêm các exception khác
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	private String escapeJson(String str) {
		if (str == null) return "Unknown error";
		return str.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
