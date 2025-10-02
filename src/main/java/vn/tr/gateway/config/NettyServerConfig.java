package vn.tr.gateway.config;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class NettyServerConfig {
	
	private static final Logger log = LoggerFactory.getLogger(NettyServerConfig.class);
	
	@Bean
	public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
		NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
		factory.addServerCustomizers(nettyServerCustomizer());
		return factory;
	}
	
	@Bean
	public NettyServerCustomizer nettyServerCustomizer() {
		return httpServer -> httpServer
				// Thêm custom handler vào pipeline
				.doOnConnection(connection -> {
					log.debug("New connection established, adding custom handlers");
					
					// Thêm handler sau HttpServerCodec để bắt lỗi decode
					connection.addHandlerLast("malformedUriHandler", new MalformedUriHandler());
				})
				// Configure timeout
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.idleTimeout(Duration.ofSeconds(60))
				// Log configuration
				.doOnBound(disposableServer -> {
					log.info("Netty server bound on: {}", disposableServer.address());
				})
				.doOnChannelInit((observer, channel, remoteAddress) -> {
					log.debug("Channel initialized for remote address: {}", remoteAddress);
				});
	}
}
