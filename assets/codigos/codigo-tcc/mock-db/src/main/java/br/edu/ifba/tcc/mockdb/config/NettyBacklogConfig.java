package br.edu.ifba.tcc.mockdb.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Aumenta a fila de conexões pendentes do Netty, para o banco simulado não recusar conexões quando a API abre muitas de uma vez
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
