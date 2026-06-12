// grails-app/controllers/backend/DemandeController.groovy
package backend

import backend.dto.DemandeDTO
import backend.dto.ErrorResponseDTO
import backend.dto.MessageResponseDTO
import backend.dto.StatistiquesDTO
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional

import java.text.SimpleDateFormat

class DemandeController {

    DemandeService demandeService
    def springSecurityService

    private User getCurrentUser() {
        return springSecurityService.currentUser
    }

    /**
     * GET /api/demandes
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def index() {
        User currentUser = getCurrentUser()
        boolean isAdmin = currentUser.authorities*.authority.contains('ROLE_ADMIN')

        List<Demande> demandes
        if (isAdmin) {
            demandes = demandeService.findAll()
        } else {
            demandes = demandeService.findAllByUser(currentUser)
        }

        render(status: 200, text: [
                demandes: demandes.collect { DemandeDTO.fromDemande(it)?.toMap() },
                count: demandes.size()
        ] as JSON)
    }

    /**
     * GET /api/demandes/stats
     */
    @Secured(['ROLE_ADMIN'])
    def stats() {
        def stats = [
                brouillon: Demande.countByStatut("BROUILLON"),
                enTraitement: Demande.countByStatut("EN_TRAITEMENT"),
                accepte: Demande.countByStatut("ACCEPTE"),
                refuse: Demande.countByStatut("REFUSE"),
                total: Demande.count()
        ]
        render(status: 200, text: StatistiquesDTO.fromStats(stats).toMap() as JSON)
    }

    /**
     * GET /api/demandes/{id}
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show() {
        Long id = params.id as Long
        User currentUser = getCurrentUser()
        boolean isAdmin = currentUser.authorities*.authority.contains('ROLE_ADMIN')

        Demande demande
        if (isAdmin) {
            demande = demandeService.findById(id)
        } else {
            demande = demandeService.findByIdAndUser(id, currentUser)
        }

        if (!demande) {
            render(status: 404, text: ErrorResponseDTO.notFound("Demande non trouvée").toMap() as JSON)
            return
        }

        def demandeDTO = DemandeDTO.fromDemande(demande)
        if (!demandeDTO) {
            render(status: 500, text: ErrorResponseDTO.internalError("Erreur lors de la création du DTO").toMap() as JSON)
            return
        }

        render(status: 200, text: demandeDTO.toMap() as JSON)
    }

    /**
     * POST /api/demandes
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    @Transactional
    def save() {
        User currentUser = getCurrentUser()
        def data = request.JSON

        Demande demande = new Demande()
        demande.user = currentUser
        demande.typeExtrait = data.typeExtrait
        demande.genre = data.genre

        // Conversion de la date avec gestion d'erreur
        if (data.dateNaissance) {
            try {
                demande.dateNaissance = new SimpleDateFormat("yyyy-MM-dd").parse(data.dateNaissance)
            } catch (Exception e) {
                render(status: 400, text: ErrorResponseDTO.badRequest("Format de date invalide. Utilisez le format yyyy-MM-dd (exemple: 1995-05-15)").toMap() as JSON)
                return
            }
        } else {
            render(status: 400, text: ErrorResponseDTO.badRequest("La date de naissance est obligatoire").toMap() as JSON)
            return
        }

        demande.villeNaissance = data.villeNaissance
        demande.telephone = data.telephone
        demande.nomPere = data.nomPere
        demande.nomMere = data.nomMere
        demande.lieuLivraison = data.lieuLivraison
        demande.typeDemandeur = data.typeDemandeur
        demande.beneficiaireNom = data.beneficiaireNom
        demande.beneficiairePrenom = data.beneficiairePrenom
        demande.beneficiaireEmail = data.beneficiaireEmail

        if (demande.typeDemandeur == "MOI_MEME") {
            demande.beneficiaireNom = currentUser.lastName
            demande.beneficiairePrenom = currentUser.firstName
            demande.beneficiaireEmail = currentUser.username
        }

        if (!demande.validate()) {
            render(status: 400, text: ErrorResponseDTO.badRequest(demande.errors.allErrors.collect { it.defaultMessage }.join(", ")).toMap() as JSON)
            return
        }

        demande.save(flush: true, failOnError: true)
        render(status: 200, text: MessageResponseDTO.success("Demande créée avec succès").toMap() as JSON)
    }

    /**
     * PUT /api/demandes/{id}
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    @Transactional
    def update() {
        Long id = params.id as Long
        User currentUser = getCurrentUser()
        boolean isAdmin = currentUser.authorities*.authority.contains('ROLE_ADMIN')
        def data = request.JSON

        Demande demande
        if (isAdmin) {
            demande = demandeService.findById(id)
        } else {
            demande = demandeService.findByIdAndUser(id, currentUser)
        }

        if (!demande) {
            render(status: 404, text: ErrorResponseDTO.notFound("Demande non trouvée").toMap() as JSON)
            return
        }

        if (!demande.modifiable) {
            render(status: 403, text: ErrorResponseDTO.forbidden("Cette demande n'est plus modifiable (statut: ${demande.statut})").toMap() as JSON)
            return
        }

        demande.properties = data

        // Conversion de la date avec gestion d'erreur
        if (data.dateNaissance && data.dateNaissance instanceof String) {
            try {
                demande.dateNaissance = new SimpleDateFormat("yyyy-MM-dd").parse(data.dateNaissance)
            } catch (Exception e) {
                render(status: 400, text: ErrorResponseDTO.badRequest("Format de date invalide. Utilisez le format yyyy-MM-dd (exemple: 1995-05-15)").toMap() as JSON)
                return
            }
        }

        if (!demande.validate()) {
            render(status: 400, text: ErrorResponseDTO.badRequest(demande.errors.allErrors.collect { it.defaultMessage }.join(", ")).toMap() as JSON)
            return
        }

        demande.save(flush: true, failOnError: true)
        render(status: 200, text: MessageResponseDTO.success("Demande mise à jour avec succès").toMap() as JSON)
    }

    /**
     * PATCH /api/demandes/{id}/statut
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def updateStatut() {
        Long id = params.id as Long
        String nouveauStatut = request.JSON?.statut

        if (!nouveauStatut) {
            render(status: 400, text: ErrorResponseDTO.badRequest("Le champ 'statut' est obligatoire").toMap() as JSON)
            return
        }

        Demande demande = demandeService.findById(id)
        if (!demande) {
            render(status: 404, text: ErrorResponseDTO.notFound("Demande non trouvée").toMap() as JSON)
            return
        }

        demande.statut = nouveauStatut
        demande.save(flush: true, failOnError: true)
        render(status: 200, text: MessageResponseDTO.success("Statut mis à jour avec succès").toMap() as JSON)
    }

    /**
     * DELETE /api/demandes/{id}
     */
    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    @Transactional
    def delete() {
        Long id = params.id as Long
        User currentUser = getCurrentUser()
        boolean isAdmin = currentUser.authorities*.authority.contains('ROLE_ADMIN')

        Demande demande
        if (isAdmin) {
            demande = demandeService.findById(id)
        } else {
            demande = demandeService.findByIdAndUser(id, currentUser)
        }

        if (!demande) {
            render(status: 404, text: ErrorResponseDTO.notFound("Demande non trouvée").toMap() as JSON)
            return
        }

        if (!isAdmin && !demande.modifiable) {
            render(status: 403, text: ErrorResponseDTO.forbidden("Impossible de supprimer une demande qui n'est plus au brouillon").toMap() as JSON)
            return
        }

        demande.delete(flush: true)
        render(status: 200, text: MessageResponseDTO.success("Demande supprimée avec succès").toMap() as JSON)
    }
}