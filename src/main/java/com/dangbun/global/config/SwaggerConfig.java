package com.dangbun.global.config;

import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseErrorResponse;
import com.dangbun.global.response.status.BaseExceptionResponse;
import com.dangbun.global.response.status.ResponseStatus;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dangbun.global.response.status.BaseExceptionResponse.*;

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
    public OpenApiCustomizer addGlobalErrors() {
        return openApi -> {
            List<BaseExceptionResponse> globals = Arrays.asList(
                    BAD_REQUEST,
                    INTERNAL_SERVER_ERROR,
                    AUTH_UNAUTHENTICATED,
                    REQUIRED_FIELD_MISSING,
                    REQUIRED_PARAM_MISSING,
                    INVALID_JWT,
                    INVALID_REFRESH_TOKEN
            );

            if (openApi.getPaths() == null) return;
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        ApiResponses responses = op.getResponses();
                        for (com.dangbun.global.response.status.BaseExceptionResponse g : globals) {
                            String key = String.valueOf(g.getCode());
                            if (responses.containsKey(key)) continue;

                            Example example = new Example()
                                    .summary(((Enum<?>) g).name())
                                    .value(new BaseErrorResponse(g));

                            MediaType mediaType = new MediaType()
                                    .addExamples(((Enum<?>) g).name(), example);

                            Content content = new Content()
                                    .addMediaType("application/json", mediaType);

                            ApiResponse apiResponse = new ApiResponse()
                                    .description(g.getMessage())
                                    .content(content);

                            responses.addApiResponse(key, apiResponse);
                        }
                    })
            );
        };
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
                            .value(new BaseErrorResponse(error));

                    MediaType mediaType = new MediaType()
                            .addExamples(constant.name(), example);

                    Content content = new Content()
                            .addMediaType("application/json", mediaType);

                    ApiResponse apiResponse = new ApiResponse()
                            .description(error.getMessage())
                            .content(content);

                    String key = String.valueOf(error.getCode());
                    if (responses.containsKey(key)) continue; // 이미 있으면 스킵


                    responses.addApiResponse(String.valueOf(error.getCode()), apiResponse);
                }
            }

            return operation;
        };
    }
}
