package backend.dto

import java.text.SimpleDateFormat

class ErrorResponseDTO {
    String error
    String message
    Integer status
    String timestamp
    List<FieldErrorDTO> errors

    ErrorResponseDTO(String error, String message, Integer status) {
        this.error = error
        this.message = message
        this.status = status
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
        this.errors = []
    }

    static ErrorResponseDTO badRequest(String message) {
        return new ErrorResponseDTO("BAD_REQUEST", message, 400)
    }

    static ErrorResponseDTO unauthorized(String message) {
        return new ErrorResponseDTO("UNAUTHORIZED", message ?: "Non authentifié", 401)
    }

    static ErrorResponseDTO forbidden(String message) {
        return new ErrorResponseDTO("FORBIDDEN", message ?: "Accès interdit", 403)
    }

    static ErrorResponseDTO notFound(String message) {
        return new ErrorResponseDTO("NOT_FOUND", message ?: "Ressource non trouvée", 404)
    }

    static ErrorResponseDTO internalError(String message) {
        return new ErrorResponseDTO("INTERNAL_ERROR", message ?: "Erreur interne", 500)
    }

    Map toMap() {
        return [
                error: error,
                message: message,
                status: status,
                timestamp: timestamp,
                errors: errors*.toMap()
        ]
    }
}

class FieldErrorDTO {
    String field
    String message
    Object rejectedValue

    FieldErrorDTO(String field, String message, Object rejectedValue) {
        this.field = field
        this.message = message
        this.rejectedValue = rejectedValue
    }

    Map toMap() {
        return [
                field: field,
                message: message,
                rejectedValue: rejectedValue
        ]
    }
}
