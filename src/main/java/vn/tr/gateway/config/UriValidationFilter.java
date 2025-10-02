package vn.tr.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2) // Chạy trước hầu hết các filter khác
public class UriValidationFilter implements WebFilter {
	
	private static final Logger log = LoggerFactory.getLogger(UriValidationFilter.class);
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		try {
			// Thử access URI để trigger exception nếu malformed
			String uri = exchange.getRequest().getURI().toString();
			String path = exchange.getRequest().getPath().value();
			
			log.info("Request URI: {}, Path: {}", uri, path);
			
			// Tiếp tục chain nếu URI hợp lệ
			return chain.filter(exchange);
			
		} catch (Exception ex) {
			log.error("Invalid URI detected: {}", ex.getMessage());
			return handleInvalidUri(exchange, ex);
		}
	}
	
	private Mono<Void> handleInvalidUri(ServerWebExchange exchange, Exception ex) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.BAD_REQUEST);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		String errorResponse = String.format(
				"{\"status\":\"error\",\"code\":400,\"message\":\"Invalid URI: %s\"}",
				ex.getMessage().replace("\"", "\\\"")
		                                    );
		
		DataBuffer buffer = response.bufferFactory()
				.wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
		
		return response.writeWith(Mono.just(buffer));
	}
}
