package vn.tr.gateway.config;

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;

@Configuration
public class CustomCodecConfiguration implements CodecCustomizer {
	
	@Override
	public void customize(CodecConfigurer configurer) {
		configurer.defaultCodecs().enableLoggingRequestDetails(true);
	}
}
