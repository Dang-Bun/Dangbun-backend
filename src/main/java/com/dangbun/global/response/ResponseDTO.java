package com.dangbun.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO<T> {

    private int status;
    private String message;
    private T data;

    public ResponseDTO(int status, String message){
        this.status = status;
        this.message = message;
        this.data = null;
    }

}
