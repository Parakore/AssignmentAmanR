package com.example.rcp.exception;
import com.example.rcp.dto.ApiModels.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException e){return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(new ResponseInfo(null,"failed"),List.of(new ErrorItem(e.getCode(),e.getMessage()))));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){
        String msg=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponse(new ResponseInfo(null,"failed"),List.of(new ErrorItem("VALIDATION_ERROR",msg))));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> constraint(ConstraintViolationException e){return ResponseEntity.badRequest().body(new ErrorResponse(new ResponseInfo(null,"failed"),List.of(new ErrorItem("VALIDATION_ERROR",e.getMessage()))));}
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> other(Exception e){return ResponseEntity.internalServerError().body(new ErrorResponse(new ResponseInfo(null,"failed"),List.of(new ErrorItem("INTERNAL_ERROR","An unexpected server error occurred"))));}
}
