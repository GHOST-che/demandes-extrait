package backend.exception

import backend.dto.ErrorResponseDTO
import backend.dto.FieldErrorDTO
import grails.converters.JSON
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

import jakarta.servlet.http.HttpServletResponse

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException)
    void handleValidationException(ValidationException e, HttpServletResponse response) {
        log.warn("Erreur de validation: ${e.message}")

        List<FieldErrorDTO> errors = []
        if (e.errors) {
            errors = e.errors.allErrors.collect { error ->
                new FieldErrorDTO(
                        error.field ?: "global",
                        error.defaultMessage ?: "Erreur de validation",
                        error.rejectedValue
                )
            }
        }

        def errorResponse = ErrorResponseDTO.badRequest(e.message ?: "Erreur de validation")
        errorResponse.errors = errors

        response.status = HttpStatus.BAD_REQUEST.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(IllegalStateException)
    void handleIllegalStateException(IllegalStateException e, HttpServletResponse response) {
        log.warn("Action non autorisée: ${e.message}")

        def errorResponse = ErrorResponseDTO.forbidden(e.message ?: "Action non autorisée")

        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(IllegalArgumentException)
    void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        log.warn("Paramètre invalide: ${e.message}")

        def errorResponse = ErrorResponseDTO.badRequest(e.message ?: "Paramètre invalide")

        response.status = HttpStatus.BAD_REQUEST.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(BadCredentialsException)
    void handleBadCredentialsException(BadCredentialsException e, HttpServletResponse response) {
        log.warn("Tentative de connexion échouée: ${e.message}")

        def errorResponse = ErrorResponseDTO.unauthorized("Email ou mot de passe incorrect")

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(AuthenticationException)
    void handleAuthenticationException(AuthenticationException e, HttpServletResponse response) {
        log.warn("Erreur d'authentification: ${e.message}")

        def errorResponse = ErrorResponseDTO.unauthorized("Authentification requise")

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(AccessDeniedException)
    void handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        log.warn("Accès refusé: ${e.message}")

        def errorResponse = ErrorResponseDTO.forbidden("Vous n'êtes pas autorisé à accéder à cette ressource")

        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(ResourceNotFoundException)
    void handleResourceNotFoundException(ResourceNotFoundException e, HttpServletResponse response) {
        log.warn("Ressource non trouvée: ${e.message}")

        def errorResponse = ErrorResponseDTO.notFound(e.message ?: "Ressource non trouvée")

        response.status = HttpStatus.NOT_FOUND.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }

    @ExceptionHandler(Exception)
    void handleGenericException(Exception e, HttpServletResponse response) {
        log.error("Erreur inattendue: ${e.message}", e)

        def errorResponse = ErrorResponseDTO.internalError("Une erreur technique est survenue. Veuillez réessayer plus tard.")

        response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        response.contentType = "application/json"
        response.writer.write(errorResponse.toMap() as JSON)
    }
}

class ResourceNotFoundException extends RuntimeException {
    ResourceNotFoundException(String message) {
        super(message)
    }
}