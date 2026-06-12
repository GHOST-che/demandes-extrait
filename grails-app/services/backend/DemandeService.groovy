package backend

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j

@Slf4j
@Transactional
class DemandeService {

    List<Demande> findAllByUser(User user) {
        Demande.findAllByUser(user, [sort: 'dateCreated', order: 'desc'])
    }

    List<Demande> findAll() {
        Demande.list(sort: 'dateCreated', order: 'desc')
    }

    List<Demande> findAllByStatut(String statut) {
        Demande.findAllByStatut(statut, [sort: 'dateCreated', order: 'desc'])
    }

    Demande findById(Long id, User user = null) {
        if (user) {
            // Un utilisateur ne peut voir que SES demandes
            return Demande.findWhere(id: id, user: user)
        }
        return Demande.get(id)
    }

    Demande save(Demande demande) {
        // Par défaut, une nouvelle demande est créée avec le statut "BROUILLON"
        demande.save(flush: true, failOnError: true)
        return demande
    }

    Demande update(Long id, Map params, User user) {
        // Récupérer la demande en s'assurant que l'utilisateur est propriétaire
        Demande demande = findById(id, user)
        if (!demande) {
            return null
        }

        // Vérifier si modifiable (seulement en brouillon)
        if (!demande.isModifiable()) {
            throw new IllegalStateException("Cette demande n'est plus modifiable (statut: ${demande.displayStatut})")
        }

        // Mettre à jour les propriétés de la demande
        demande.properties = params
        demande.save(flush: true, failOnError: true)
        return demande
    }

    Demande updateStatut(Long id, String nouveauStatut, User admin) {
        // Seul l'admin peut changer le statut
        Demande demande = Demande.get(id)
        if (!demande) {
            return null
        }

        // Vérifier que le nouveau statut est valide
        demande.statut = nouveauStatut
        demande.save(flush: true, failOnError: true)
        log.info("Demande ${id} changée de statut vers ${nouveauStatut} par ${admin.username}")
        return demande
    }

    // Méthode pour trouver une demande par ID en s'assurant que l'utilisateur est propriétaire
    Demande findByIdAndUser(Long id, User user) {
        Demande.findWhere(id: id, user: user)
    }

    // Seule une demande au statut "BROUILLON" peut être supprimée, et seulement par son propriétaire ou un admin
    boolean delete(Long id, User user = null) {
        Demande demande = findById(id, user)
        if (!demande) {
            return false
        }

        // Seul l'admin ou l'utilisateur propriétaire (si brouillon) peut supprimer
        if (user && !user.authorities*.authority.contains('ROLE_ADMIN') && demande.statut != Demande.STATUT_BROUILLON) {
            throw new IllegalStateException("Impossible de supprimer une demande qui n'est plus au brouillon")
        }

        demande.delete(flush: true)
        log.info("Demande ${id} supprimée par ${user?.username ?: 'admin'}")
        return true
    }

    Long countByStatut(String statut) {
        Demande.countByStatut(statut)
    }

    Long countByUserAndStatut(User user, String statut) {
        Demande.countByUserAndStatut(user, statut)
    }
}