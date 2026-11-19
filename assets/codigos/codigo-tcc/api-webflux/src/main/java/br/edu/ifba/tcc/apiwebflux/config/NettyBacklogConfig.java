package br.edu.ifba.tcc.apiwebflux.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Iguala a fila de conexões pendentes do Netty à do Tomcat (accept-count=8192), para a variante webflux aceitar as mesmas rajadas de conexões que as outras
@Configuration
public class NettyBacklogConfig {

    /**
     * @param backlog Tamanho da fila de conexões pendentes, 8192 por padrão
     * @return Ajuste aplicado ao servidor Netty quando o Spring cria ele
     */
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyBacklogCustomizer(
            @Value("${server.netty.backlog:8192}") int backlog) {
        return factory -> factory.addServerCustomizers(server -> server.option(ChannelOption.SO_BACKLOG, backlog));
    }
}
