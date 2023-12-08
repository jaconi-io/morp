//package io.jaconi.morp.tenant;
//
//import static io.jaconi.morp.tenant.TenantExtractor.KEY;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//
//import io.micrometer.core.instrument.Tags;
//
//@Component
//@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
//public class TenantGatewayTagsProvider implements DefaultServerRequestObservationConvention {
//
//    @Override
//    public Tags apply(ServerWebExchange exchange) {
//        return TenantExtractor.extractTenant(exchange.getAttributes())
//                .map(t -> Tags.of(KEY, t))
//                .orElse(Tags.empty());
//    }
//}
