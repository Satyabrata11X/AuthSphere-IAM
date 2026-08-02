package com.authsphere.authsphere_backend.core.common;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ApiResponse<T> {

    private boolean success;

    private ApiStatus status;

    private  String message;

    private T data ;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();



}
