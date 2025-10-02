package vn.tr.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tr.gateway.dto.ApiResponse;

import java.nio.charset.StandardCharsets;

@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
	private final ObjectMapper objectMapper;
	
	public GlobalErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		log.error("Global error handler - Type: {}, Message: {}",
				ex.getClass().getSimpleName(), ex.getMessage());
		
		ServerHttpResponse response = exchange.getResponse();
		
		if (response.isCommitted()) {
			log.warn("Response already committed");
			return Mono.error(ex);
		}
		
		HttpStatus status = determineHttpStatus(ex);
		Integer errorCode = status.value();
		String errorMessage = extractMessage(ex);
		
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		ApiResponse<?> apiResponse = ApiResponse.error(errorCode, errorMessage);
		
		try {
			String jsonResponse = objectMapper.writeValueAsString(apiResponse);
			DataBuffer buffer = response.bufferFactory()
					.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
			
			return response.writeWith(Mono.just(buffer))
					.doOnSuccess(v -> log.debug("Error response sent successfully"))
					.doOnError(error -> log.error("Error writing response", error));
			
		} catch (JsonProcessingException e) {
			log.error("Error serializing response", e);
			return writeFallbackResponse(response, errorCode, errorMessage);
		}
	}
	
	private HttpStatus determineHttpStatus(Throwable ex) {
		if (ex instanceof ResponseStatusException rse) {
			return (HttpStatus) rse.getStatusCode();
		}
		if (ex instanceof IllegalArgumentException) {
			return HttpStatus.BAD_REQUEST;
		}
		if (ex instanceof IllegalStateException) {
			return HttpStatus.CONFLICT;
		}
		if (ex.getMessage() != null && ex.getMessage().contains("Malformed")) {
			return HttpStatus.BAD_REQUEST;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	private String extractMessage(Throwable ex) {
		String message = ex.getMessage();
		
		// Xử lý các exception đặc biệt
		if (message == null || message.isEmpty()) {
			return "Internal Server Error";
		}
		
		// Rút gọn message nếu quá dài
		if (message.length() > 200) {
			message = message.substring(0, 197) + "...";
		}
		
		return message;
	}
	
	private Mono<Void> writeFallbackResponse(ServerHttpResponse response,
			Integer code,
			String message) {
		String fallback = String.format(
				"{\"code\":%d,\"message\":\"%s\",\"data\":null}",
				code,
				escapeJson(message)
		                               );
		
		DataBuffer buffer = response.bufferFactory()
				.wrap(fallback.getBytes(StandardCharsets.UTF_8));
		
		return response.writeWith(Mono.just(buffer));
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
