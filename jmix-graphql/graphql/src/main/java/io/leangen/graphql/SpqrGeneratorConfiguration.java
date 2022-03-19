package io.leangen.graphql;

import io.leangen.graphql.generator.JavaDeprecationMappingConfig;
import io.leangen.graphql.generator.mapping.strategy.InterfaceMappingStrategy;
import io.leangen.graphql.metadata.strategy.type.TypeTransformer;
import io.leangen.graphql.metadata.strategy.value.ScalarDeserializationStrategy;

@SuppressWarnings("WeakerAccess")
public class SpqrGeneratorConfiguration extends GeneratorConfiguration {

    public SpqrGeneratorConfiguration(InterfaceMappingStrategy interfaceMappingStrategy, ScalarDeserializationStrategy scalarDeserializationStrategy,
                                      TypeTransformer typeTransformer, String[] basePackages, JavaDeprecationMappingConfig javaDeprecationConfig) {
        super(interfaceMappingStrategy, scalarDeserializationStrategy, typeTransformer, basePackages, javaDeprecationConfig);
    }
}
