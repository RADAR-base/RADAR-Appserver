package org.radarbase.appserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.mappers.LicenseMapper;
import springfox.documentation.swagger2.mappers.LicenseMapperImpl;
import springfox.documentation.swagger2.mappers.ModelMapper;
import springfox.documentation.swagger2.mappers.ModelMapperImpl;
import springfox.documentation.swagger2.mappers.ParameterMapper;
import springfox.documentation.swagger2.mappers.ParameterMapperImpl;
import springfox.documentation.swagger2.mappers.SecurityMapper;
import springfox.documentation.swagger2.mappers.SecurityMapperImpl;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;
import springfox.documentation.swagger2.mappers.VendorExtensionsMapper;
import springfox.documentation.swagger2.mappers.VendorExtensionsMapperImpl;

@Configuration
@EnableSwagger2
public class SwaggerSpringFoxConfig {
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build();
  }

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapperImpl();
  }

  @Bean
  public ParameterMapper parameterMapper() {
    return new ParameterMapperImpl();
  }

  @Bean
  public SecurityMapper securityMapper() {
    return new SecurityMapperImpl();
  }

  @Bean
  public LicenseMapper licenseMapper() {
    return new LicenseMapperImpl();
  }

  @Bean
  public VendorExtensionsMapper vendorExtensionsMapper() {
    return new VendorExtensionsMapperImpl();
  }

  @Bean
  public ServiceModelToSwagger2Mapper serviceModelToSwagger2Mapper() {
    return new ServiceModelToSwagger2MapperImpl();
  }
}
