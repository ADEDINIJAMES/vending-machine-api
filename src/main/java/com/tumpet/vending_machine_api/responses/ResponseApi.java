package com.tumpet.vending_machine_api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApi <T> {
    private HttpStatusCode status;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ResponseApi(HttpStatusCode status, String message, T data) {
        this.status =status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();

    }

    public ResponseApi(HttpStatusCode status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


    public  static <T> ResponseApi<T> of (HttpStatusCode status, String message, T data){
        return new ResponseApi<>(status, message,data);
    }
    public  static <T> ResponseApi<T> of (HttpStatusCode status, String message){
        return new ResponseApi<>(status, message);
    }

}
