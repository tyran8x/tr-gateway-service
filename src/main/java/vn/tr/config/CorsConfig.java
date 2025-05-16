package vn.tr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
	
	private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, PATCH, *";
	private static final String ALLOWED_ORIGIN = "*";
	private static final String MAX_AGE = "3600";
	
	private static final List<String> ALLOWED_HEADERS = Arrays.asList(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.CONTENT_TYPE,
			"x-token",
			"x-app-code",
			"*"
	                                                                 );
	
	@Bean
	public WebFilter corsFilter() {
		return (ServerWebExchange ctx, WebFilterChain chain) -> {
			ServerHttpRequest request = ctx.getRequest();
			if (CorsUtils.isCorsRequest(request)) {
				ServerHttpResponse response = ctx.getResponse();
				HttpHeaders headers = response.getHeaders();
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN);
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", ALLOWED_METHODS));
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.FALSE.toString());
				headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", ALLOWED_HEADERS));
				//headers.add(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());
			}
			return chain.filter(ctx);
		};
	}
	
}
