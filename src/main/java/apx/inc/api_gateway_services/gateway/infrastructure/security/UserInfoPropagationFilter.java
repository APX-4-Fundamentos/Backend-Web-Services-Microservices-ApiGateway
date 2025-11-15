package apx.inc.api_gateway_services.gateway.infrastructure.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class UserInfoPropagationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 🔍 Obtener el header Authorization del cliente
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .header("X-Gateway-Request", "true")
                .header("X-Request-Path", path);

        // 📤 PROPAGAR el token al IAM Service
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            requestBuilder.header("Authorization", authHeader);
            System.out.println("🔐 [GATEWAY] Propagating token to IAM");
        } else {
            System.out.println("⚠️ [GATEWAY] No Authorization header found");
        }

        System.out.println("🚀 [GATEWAY] Routing to: " + path);

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}