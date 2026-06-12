package backend

import backend.dto.ErrorResponseDTO
import backend.dto.MessageResponseDTO
import backend.dto.UserDTO
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import grails.converters.JSON
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.HttpStatus.UNAUTHORIZED

import grails.gorm.transactions.Transactional

class UserController {

    UserService userService
    def passwordEncoder
    def springSecurityService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    // ==================== CRUD EXISTANTS ====================

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def users = userService.list(params)
        render(status: OK, text: [
                users: users.collect { UserDTO.fromUser(it)?.toMap() },
                count: userService.count()
        ] as JSON)
    }

    def show(Long id) {
        def user = userService.get(id)
        if (!user) {
            render(status: NOT_FOUND, text: ErrorResponseDTO.notFound("Utilisateur non trouvé").toMap() as JSON)
            return
        }
        def userDTO = UserDTO.fromUser(user)
        if (!userDTO) {
            render(status: INTERNAL_SERVER_ERROR, text: ErrorResponseDTO.internalError("Erreur lors de la création du DTO").toMap() as JSON)
            return
        }
        render(status: OK, text: userDTO.toMap() as JSON)
    }

    @Transactional
    def save(User user) {
        if (user == null) {
            render(status: NOT_FOUND, text: ErrorResponseDTO.notFound().toMap() as JSON)
            return
        }
        if (user.hasErrors()) {
            transactionStatus.setRollbackOnly()
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest(user.errors.allErrors.collect { it.defaultMessage }.join(", ")).toMap() as JSON)
            return
        }

        userService.save(user)

        def userDTO = UserDTO.fromUser(user)
        if (!userDTO) {
            render(status: INTERNAL_SERVER_ERROR, text: ErrorResponseDTO.internalError("Erreur lors de la création du DTO").toMap() as JSON)
            return
        }
        render(status: CREATED, text: userDTO.toMap() as JSON)
    }

    @Transactional
    def update(User user) {
        if (user == null) {
            render(status: NOT_FOUND, text: ErrorResponseDTO.notFound("Utilisateur non trouvé").toMap() as JSON)
            return
        }
        if (user.hasErrors()) {
            transactionStatus.setRollbackOnly()
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest(user.errors.allErrors.collect { it.defaultMessage }.join(", ")).toMap() as JSON)
            return
        }

        userService.save(user)

        def userDTO = UserDTO.fromUser(user)
        if (!userDTO) {
            render(status: INTERNAL_SERVER_ERROR, text: ErrorResponseDTO.internalError("Erreur lors de la création du DTO").toMap() as JSON)
            return
        }
        render(status: OK, text: userDTO.toMap() as JSON)
    }

    @Transactional
    def delete(Long id) {
        if (id == null || userService.delete(id) == null) {
            render(status: NOT_FOUND, text: ErrorResponseDTO.notFound("Utilisateur non trouvé").toMap() as JSON)
            return
        }

        render(status: NO_CONTENT, text: MessageResponseDTO.success("Utilisateur supprimé avec succès").toMap() as JSON)
    }

    // ==================== NOUVEAUX ENDPOINTS ====================

    /**
     * POST /api/register
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    def register() {
        def data = request.JSON

        if (!data.username || !data.password || !data.confirmPassword || !data.firstName || !data.lastName) {
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest("Tous les champs sont obligatoires").toMap() as JSON)
            return
        }

        if (data.password != data.confirmPassword) {
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest("Les mots de passe ne correspondent pas").toMap() as JSON)
            return
        }

        def existingUser = User.findByUsername(data.username)
        if (existingUser) {
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest("Cet email est déjà utilisé").toMap() as JSON)
            return
        }

        def user = new User(
                username: data.username,
                password: passwordEncoder.encode(data.password),
                firstName: data.firstName,
                lastName: data.lastName,
                enabled: true
        )

        if (!user.save(flush: true)) {
            render(status: BAD_REQUEST, text: ErrorResponseDTO.badRequest("Erreur lors de la création: ${user.errors.allErrors.collect { it.defaultMessage }}").toMap() as JSON)
            return
        }

        def userRole = Role.findByAuthority('ROLE_USER')
        if (userRole) {
            UserRole.create(user, userRole, true)
        }

        render(status: CREATED, text: MessageResponseDTO.success("Compte créé avec succès. Vous pouvez maintenant vous connecter.").toMap() as JSON)
    }

    /**
     * GET /api/me
     * Récupère les informations de l'utilisateur connecté
     */
    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def me() {
        def user = springSecurityService.currentUser
        if (!user) {
            render(status: UNAUTHORIZED, text: ErrorResponseDTO.unauthorized("Non authentifié").toMap() as JSON)
            return
        }

        def userDTO = UserDTO.fromUser(user)
        if (!userDTO) {
            render(status: INTERNAL_SERVER_ERROR, text: ErrorResponseDTO.internalError("Erreur lors de la création").toMap() as JSON)
            return
        }

        render(status: OK, text: userDTO.toMap() as JSON)
    }
}