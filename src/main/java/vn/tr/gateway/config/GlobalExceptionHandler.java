package vn.tr.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2) // ưu tiên cao, chạy trước default error handler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
		try {
			// Set HTTP status tùy theo exception
			HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
			if (ex instanceof IllegalArgumentException) {
				status = HttpStatus.BAD_REQUEST;
			}
			
			// Build JSON response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("status", status.value());
			errorResponse.put("error", status.getReasonPhrase());
			errorResponse.put("message", ex.getMessage());
			
			byte[] bytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);
			exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
			exchange.getResponse().setStatusCode(status);
			DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
			return exchange.getResponse().writeWith(Mono.just(buffer));
		} catch (Exception handlerEx) {
			return Mono.error(handlerEx);
		}
	}
}
