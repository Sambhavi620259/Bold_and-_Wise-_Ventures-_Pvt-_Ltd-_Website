package in.bawvpl.Authify.io;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApiResponse<T> {

    private boolean success;

    private T data;

    private String message;

    private String error;

    // =====================================================
    // SUCCESS WITH DATA
    // =====================================================

    public static <T> AdminApiResponse<T> success(
            T data
    ) {

        return AdminApiResponse.<T>builder()

                .success(true)

                .data(data)

                .message("Success")

                .build();
    }

    // =====================================================
    // SUCCESS MESSAGE
    // =====================================================

    public static <T> AdminApiResponse<T> successMessage(
            String message
    ) {

        return AdminApiResponse.<T>builder()

                .success(true)

                .message(message)

                .build();
    }

    // =====================================================
    // ERROR
    // =====================================================

    public static <T> AdminApiResponse<T> error(
            String error
    ) {

        return AdminApiResponse.<T>builder()

                .success(false)

                .error(error)

                .build();
    }
}