package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageDto {

    private Long id;

    private String senderType;

    private String message;

    private LocalDateTime createdAt;
}
