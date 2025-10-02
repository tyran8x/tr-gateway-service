package vn.tr.gateway.config;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MalformedUriHandler extends ChannelDuplexHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MalformedUriHandler.class);
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("Exception caught in Netty pipeline: {}", cause.getMessage());
		
		// Kiểm tra nếu là lỗi URI malformed
		if (cause.getMessage() != null &&
				(cause.getMessage().contains("Malformed escape pair") ||
						cause.getMessage().contains("Invalid URI") ||
						cause instanceof IllegalArgumentException)) {
			
			String errorMessage = String.format(
					"{\"code\":400,\"message\":\"Invalid URI format: %s\",\"data\":null}",
					escapeJson(cause.getMessage())
			                                   );
			
			FullHttpResponse response = new DefaultFullHttpResponse(
					HttpVersion.HTTP_1_1,
					HttpResponseStatus.BAD_REQUEST,
					Unpooled.copiedBuffer(errorMessage, CharsetUtil.UTF_8)
			);
			
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
			
			ctx.writeAndFlush(response).addListener(future -> {
				log.debug("Error response sent, closing connection");
				ctx.close();
			});
			
			return;
		}
		
		// Các exception khác tiếp tục propagate
		ctx.fireExceptionCaught(cause);
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
