package com.example.identityservice.dto.response;
import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ErrorResponse <T>{
    private LocalDateTime timestamp;
    private T error;
    private String path;
}
