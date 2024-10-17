package vn.tr.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

public class OpenApiConfig {

	private final RouteDefinitionLocator routeDefinitionLocator;

	public OpenApiConfig(RouteDefinitionLocator routeDefinitionLocator) {
		this.routeDefinitionLocator = routeDefinitionLocator;
	}

	@Bean
	public List<GroupedOpenApi> apis() {
		List<GroupedOpenApi> groups = new ArrayList<>();
		List<RouteDefinition> definitions = routeDefinitionLocator.getRouteDefinitions().collectList().block();
		if (definitions != null) {
			definitions.stream().filter(routeDefinition -> routeDefinition.getId().matches(".*-service")).forEach(routeDefinition -> {
				String name = routeDefinition.getId().replace("-service", "");
				groups.add(GroupedOpenApi.builder().pathsToMatch("/" + name + "/**").group(name).build());
			});
		}
		return groups;
	}
}
