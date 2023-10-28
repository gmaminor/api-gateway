package com.association.apigateway.config;

import com.association.apigateway.Dto.UserDto;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private WebClient.Builder webClientBuilder;

    //    public AuthFilter(WebClient.Builder webClientBuilder) {
//        this.webClientBuilder = webClientBuilder;
//    }
    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;

    }

    @Override
    public GatewayFilter apply(Config config) {
//        HttpHeaders httpHeaders = new HttpHeaders();

        return (exchange, chain) -> {
            System.out.println("filter started");
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Missing Auth token");
            }
            String authToken = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authToken != null) {
                System.out.println("verifying token");
            }

            return webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/validate")
                    .header("Authorization", authToken)
                    .retrieve().bodyToMono(UserDto.class)
                    .map(authResponse -> {
                        exchange.getRequest()
                                .mutate()
                                .header("username", authResponse.username())
                                .header("isAuthenticated", authResponse.isAuthenticated()).build();

                        return exchange;
                    }).flatMap(chain::filter);

        };
    }
    public static class Config {
    }

}
