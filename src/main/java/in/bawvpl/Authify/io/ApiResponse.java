package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // =====================================================
    // STATUS
    //
    // IMPORTANT:
    //
    // success=true for all successful APIs
    // =====================================================

    @Builder.Default
    private boolean success = true;

    private int status;

    private String message;

    private T data;

    private Object meta;

    // =====================================================
    // SUCCESS WITH DATA
    // =====================================================

    public static <T> ApiResponse<T> success(
            T data
    ) {

        return ApiResponse.<T>builder()

                .success(true)

                .status(200)

                .message("Success")

                .data(data)

                .build();
    }

    // =====================================================
    // SUCCESS MESSAGE
    // =====================================================

    public static <T> ApiResponse<T> successMessage(
            String message
    ) {

        return ApiResponse.<T>builder()

                .success(true)

                .status(200)

                .message(message)

                .build();
    }

    // =====================================================
    // SUCCESS WITH META
    // =====================================================

    public static <T> ApiResponse<T> success(
            T data,

            Object meta
    ) {

        return ApiResponse.<T>builder()

                .success(true)

                .status(200)

                .message("Success")

                .data(data)

                .meta(meta)

                .build();
    }

    // =====================================================
    // CUSTOM SUCCESS
    // =====================================================

    public static <T> ApiResponse<T> success(
            String message,

            T data
    ) {

        return ApiResponse.<T>builder()

                .success(true)

                .status(200)

                .message(message)

                .data(data)

                .build();
    }

    // =====================================================
    // ERROR RESPONSE
    // =====================================================

    public static <T> ApiResponse<T> error(

            int status,

            String message
    ) {

        return ApiResponse.<T>builder()

                .success(false)

                .status(status)

                .message(message)

                .build();
    }

    // =====================================================
    // ERROR RESPONSE WITH DATA
    // =====================================================

    public static <T> ApiResponse<T> error(

            int status,

            String message,

            T data
    ) {

        return ApiResponse.<T>builder()

                .success(false)

                .status(status)

                .message(message)

                .data(data)

                .build();
    }
}