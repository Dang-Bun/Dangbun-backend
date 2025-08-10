package com.dangbun.global.docs;

import com.dangbun.global.response.status.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reflections.Reflections;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/docs")
@Tag(name = "999. 응답 코드 문서", description = "당번의 전체 응답 코드 목록을 나타냅니다.",
        extensions = {@Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "999"))})
public class DocsController {

    @Operation(summary = "전체 응답 코드 목록")
    @GetMapping(value = "/error-codes", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getAllErrorCodes() {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        padding: 30px;
                    }
                    h2 {
                        color: #2c3e50;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 20px;
                    }
                    th, td {
                        border: 1px solid #ccc;
                        padding: 10px;
                        text-align: left;
                        font-size: 14px;
                    }
                    th {
                        background-color: #f2f2f2;
                        font-weight: bold;
                    }
                    tr:nth-child(even) {
                        background-color: #fafafa;
                    }
                    tr.domain-divider td {
                        padding: 0;
                        height: 3px;
                        background-color: #999;
                        border: none;
                    }
                </style>
            </head>
            <body>
        """);

        sb.append("<h2>전체 응답 코드 테이블</h2>");
        sb.append("<table>");
        sb.append("<tr><th>도메인</th><th>Code</th><th>Message</th></tr>");

        Reflections reflections = new Reflections("com.dangbun");
        Set<Class<? extends ResponseStatus>> subTypes = reflections.getSubTypesOf(ResponseStatus.class);

        List<Map<String, Object>> errorList = new ArrayList<>();

        for (Class<? extends ResponseStatus> clazz : subTypes) {
            if (!clazz.isEnum()) continue;

            String domain = clazz.getSimpleName().replace("ExceptionResponse", " 에러 코드");

            Object[] constants = clazz.getEnumConstants();
            for (Object constant : constants) {
                ResponseStatus status = (ResponseStatus) constant;
                int code = status.getCode();

                if (code == 20000) {
                    sb.append("<tr>")
                            .append("<td>").append("Base 성공 코드").append("</td>")
                            .append("<td>").append(code).append("</td>")
                            .append("<td>").append(status.getMessage()).append("</td>")
                            .append("</tr>");
                } else {
                    Map<String, Object> row = new HashMap<>();
                    row.put("domain", domain);
                    row.put("code", code);
                    row.put("message", status.getMessage());
                    errorList.add(row);
                }
            }
        }

        errorList.sort(Comparator.comparingInt(row -> (int) row.get("code")));

        String lastDomain = "";
        for (Map<String, Object> row : errorList) {
            String domain = (String) row.get("domain");

            if (!domain.equals(lastDomain)) {
                sb.append("<tr class='domain-divider'><td colspan='3'></td></tr>");
                lastDomain = domain;
            }

            sb.append("<tr>")
                    .append("<td>").append(domain).append("</td>")
                    .append("<td>").append(row.get("code")).append("</td>")
                    .append("<td>").append(row.get("message")).append("</td>")
                    .append("</tr>");
        }

        sb.append("</table></body></html>");
        return ResponseEntity.ok(sb.toString());
    }
}