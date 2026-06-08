package in.bawvpl.Authify.config;

import in.bawvpl.Authify.io.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =====================================================
    // RESPONSE STATUS EXCEPTION
    // =====================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleResponseStatusException(

            ResponseStatusException ex,

            HttpServletRequest request
    ) {

        log.error(

                "ResponseStatusException [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage(),

                ex
        );

        return ResponseEntity

                .status(ex.getStatusCode())

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(
                                        ex.getStatusCode().value()
                                )

                                .message(

                                        ex.getReason() != null

                                                ? ex.getReason()

                                                : "Request failed"
                                )

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // 404 NOT FOUND
    // =====================================================

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>>
    handle404(

            NoHandlerFoundException ex,

            HttpServletRequest request
    ) {

        log.error(

                "404 [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage()
        );

        return ResponseEntity

                .status(HttpStatus.NOT_FOUND)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(404)

                                .message("API endpoint not found")

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // 405 METHOD NOT ALLOWED
    // =====================================================

    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ApiResponse<Object>>
    handle405(

            HttpRequestMethodNotSupportedException ex,

            HttpServletRequest request
    ) {

        log.error(

                "405 [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage()
        );

        return ResponseEntity

                .status(HttpStatus.METHOD_NOT_ALLOWED)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(405)

                                .message("Method not allowed")

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // ACCESS DENIED
    // =====================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleAccessDenied(

            AccessDeniedException ex,

            HttpServletRequest request
    ) {

        log.error(

                "AccessDenied [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage(),

                ex
        );

        return ResponseEntity

                .status(HttpStatus.FORBIDDEN)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(403)

                                .message("Access Denied")

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // VALIDATION ERROR
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleValidation(

            MethodArgumentNotValidException ex,

            HttpServletRequest request
    ) {

        log.error(

                "ValidationException [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage(),

                ex
        );

        String message =

                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()

                        .findFirst()

                        .map(field ->

                                field.getField()
                                        + " "
                                        + field.getDefaultMessage()
                        )

                        .orElse("Validation failed");

        return ResponseEntity

                .status(HttpStatus.BAD_REQUEST)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(400)

                                .message(message)

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.<Object>builder()
                                .success(false)
                                .status(400)
                                .message(ex.getMessage())
                                .meta(
                                        buildMeta(
                                                request,
                                                ex.getClass().getSimpleName()
                                        )
                                )
                                .build()
                );
    }

    // =====================================================
    // ILLEGAL ARGUMENT
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleIllegalArgument(

            IllegalArgumentException ex,

            HttpServletRequest request
    ) {

        log.error(

                "IllegalArgument [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage(),

                ex
        );

        return ResponseEntity

                .status(HttpStatus.BAD_REQUEST)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(400)

                                .message(

                                        ex.getMessage() != null

                                                ? ex.getMessage()

                                                : "Invalid request"
                                )

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // GLOBAL EXCEPTION
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>>
    handleException(

            Exception ex,

            HttpServletRequest request
    ) {

        log.error(

                "Unhandled Exception [{} {}]: {}",

                request.getMethod(),

                request.getRequestURI(),

                ex.getMessage(),

                ex
        );

        return ResponseEntity

                .status(HttpStatus.INTERNAL_SERVER_ERROR)

                .body(

                        ApiResponse.<Object>builder()

                                .success(false)

                                .status(500)

                                .message(

                                        ex.getMessage() != null

                                                ? ex.getMessage()

                                                : "Internal Server Error"
                                )

                                .meta(

                                        buildMeta(

                                                request,

                                                ex.getClass()
                                                        .getSimpleName()
                                        )
                                )

                                .build()
                );
    }

    // =====================================================
    // META BUILDER
    // =====================================================

    private Object buildMeta(

            HttpServletRequest request,

            String type
    ) {

        return new ErrorMeta(

                LocalDateTime.now(),

                request.getRequestURI(),

                type
        );
    }

    // =====================================================
    // ERROR META
    // =====================================================

    public record ErrorMeta(

            LocalDateTime timestamp,

            String path,

            String type
    ) {
    }
}