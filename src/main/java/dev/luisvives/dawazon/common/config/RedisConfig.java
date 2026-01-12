package dev.luisvives.dawazon.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Configuración de caché con Redis.
 * <p>
 * Define la configuración de serialización JSON para el almacenamiento en
 * caché.
 * </p>
 */
@Configuration
public class RedisConfig {

        /**
         * Configura la serialización de caché para Redis.
         *
         * @return Configuración de caché con serialización JSON
         */
        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                return RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
        }
}