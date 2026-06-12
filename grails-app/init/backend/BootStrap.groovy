package backend

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j

@Slf4j
class BootStrap {

    def passwordEncoder

    def init = { servletContext ->
        initSecurity()
        initDemandes()
        log.info("BootStrap terminé")
    }

    @Transactional
    void initSecurity() {
        def roleAdmin = Role.findByAuthority('ROLE_ADMIN') ?:
                new Role(authority: 'ROLE_ADMIN').save(flush: true, failOnError: true)

        def roleUser = Role.findByAuthority('ROLE_USER') ?:
                new Role(authority: 'ROLE_USER').save(flush: true, failOnError: true)

        def adminUser = User.findByUsername('admin@demandes.com') ?: new User(
                username: 'admin@demandes.com',
                password: passwordEncoder.encode('admin123'),
                firstName: 'Admin',
                lastName: 'Système',
                enabled: true
        ).save(flush: true, failOnError: true)

        if (!adminUser.authorities.contains(roleAdmin)) {
            UserRole.create(adminUser, roleAdmin, true)
        }

        log.info("Admin créé: admin@demandes.com / admin123")
    }

    @Transactional
    void initDemandes() {
        if (Demande.count() == 0) {
            User admin = User.findByUsername('admin@demandes.com')

            def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")

            // Demande 1 - Pour moi-même (typeDemandeur = MOI_MEME)
            new Demande(
                    user: admin,
                    typeDemandeur: Demande.DEMANDEUR_MOI_MEME,
                    beneficiaireNom: admin.lastName,
                    beneficiairePrenom: admin.firstName,
                    typeExtrait: Demande.TYPE_SIMPLE,
                    genre: "MASCULIN",
                    dateNaissance: sdf.parse("1995-05-15"),
                    villeNaissance: "Abidjan",
                    telephone: "0707070707",
                    nomPere: "Jean Parent",
                    nomMere: "Marie Parent",
                    lieuLivraison: "Cocody",
                    statut: Demande.STATUT_ACCEPTE
            ).save(flush: true, failOnError: true)

            // Demande 2 - Pour un tiers (typeDemandeur = TIERS)
            new Demande(
                    user: admin,
                    typeDemandeur: Demande.DEMANDEUR_TIERS,
                    beneficiaireNom: "Kone",
                    beneficiairePrenom: "Ibrahim",
                    beneficiaireEmail: "ibrahim.kone@example.com",
                    typeExtrait: Demande.TYPE_MULTI_LANGUE,
                    genre: "FEMININ",
                    dateNaissance: sdf.parse("1990-10-20"),
                    villeNaissance: "Bouaké",
                    telephone: "0808080808",
                    nomPere: "Paul Konan",
                    nomMere: "Jeanne Konan",
                    lieuLivraison: "Plateau",
                    statut: Demande.STATUT_EN_TRAITEMENT
            ).save(flush: true, failOnError: true)

            log.info("2 demandes créées")
        }
    }

    def destroy = { }
}