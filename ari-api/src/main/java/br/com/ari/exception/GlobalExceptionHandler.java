package br.com.ari.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        return resposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos.",
                request,
                fields
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarViolacao(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations()
                .forEach(violation -> fields.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ));

        return resposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos.",
                request,
                fields
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> tratarRequisicaoInvalida(
            Exception exception,
            HttpServletRequest request) {

        String message = exception instanceof HttpMessageNotReadableException
                ? "Corpo da requisição inválido."
                : mensagem(exception);

        return resposta(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                Map.of()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> tratarConflito(
            IllegalStateException exception,
            HttpServletRequest request) {

        return resposta(
                HttpStatus.CONFLICT,
                mensagem(exception),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoAutenticado(
            NaoAutenticadoException exception,
            HttpServletRequest request) {

        return resposta(
                HttpStatus.UNAUTHORIZED,
                mensagem(exception),
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> resposta(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fields) {

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fields
        );

        return ResponseEntity.status(status).body(response);
    }

    private String mensagem(Exception exception) {
        return exception.getMessage() == null
                || exception.getMessage().isBlank()
                ? "Não foi possível processar a requisição."
                : exception.getMessage();
    }
}
