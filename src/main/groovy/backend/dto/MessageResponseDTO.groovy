package backend.dto

import java.text.SimpleDateFormat

class MessageResponseDTO {
    String message
    String type
    String timestamp

    MessageResponseDTO(String message, String type = "SUCCESS") {
        this.message = message
        this.type = type
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
    }

    static MessageResponseDTO success(String message) {
        return new MessageResponseDTO(message, "SUCCESS")
    }

    static MessageResponseDTO error(String message) {
        return new MessageResponseDTO(message, "ERROR")
    }

    static MessageResponseDTO warning(String message) {
        return new MessageResponseDTO(message, "WARNING")
    }

    Map toMap() {
        return [
                message: message,
                type: type,
                timestamp: timestamp
        ]
    }
}