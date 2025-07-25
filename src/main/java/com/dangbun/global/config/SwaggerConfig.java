package com.dangbun.global.config;

import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseErrorResponse;
import com.dangbun.global.response.status.ResponseStatus;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dangbun API")
                        .version("0.0.1")
                        .description("<h3>당번 API 문서</h3>"));
    }

    @Bean
    public OperationCustomizer customizeErrorExamples() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            DocumentedApiErrors annotation = handlerMethod.getMethodAnnotation(DocumentedApiErrors.class);
            if (annotation == null) return operation;

            ApiResponses responses = operation.getResponses();
            Set<String> includes = new HashSet<>(Arrays.asList(annotation.includes()));

            for (Class<? extends Enum<? extends ResponseStatus>> errorEnumClass : annotation.value()) {
                for (Enum<?> constant : errorEnumClass.getEnumConstants()) {
                    if (!includes.isEmpty() && !includes.contains(constant.name())) continue;

                    ResponseStatus error = (ResponseStatus) constant;

                    Example example = new Example()
                            .summary(constant.name())
                            .value(new BaseErrorResponse(error)); // JSON 예시 객체

                    MediaType mediaType = new MediaType()
                            .addExamples(constant.name(), example);

                    Content content = new Content()
                            .addMediaType("application/json", mediaType);

                    ApiResponse apiResponse = new ApiResponse()
                            .description(error.getMessage())
                            .content(content);

                    responses.addApiResponse(String.valueOf(error.getCode()), apiResponse);
                }
            }

            return operation;
        };
    }
}
