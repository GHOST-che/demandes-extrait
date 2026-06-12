// grails-app/domain/backend/Demande.groovy
package backend

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@GrailsCompileStatic
@EqualsAndHashCode(includes='id')
@ToString(includes=['id', 'typeExtrait', 'statut'], includeNames=true)
class Demande {

    // === Types d'extrait ===
    static final String TYPE_SIMPLE = "SIMPLE"
    static final String TYPE_MULTI_LANGUE = "MULTI_LANGUE"
    static final String TYPE_INTEGRALE = "INTEGRALE"

    // === Statuts ===
    static final String STATUT_BROUILLON = "BROUILLON"
    static final String STATUT_EN_TRAITEMENT = "EN_TRAITEMENT"
    static final String STATUT_ACCEPTE = "ACCEPTE"
    static final String STATUT_REFUSE = "REFUSE"

    // === Types de demandeur ===
    static final String DEMANDEUR_MOI_MEME = "MOI_MEME"
    static final String DEMANDEUR_TIERS = "TIERS"

    // === Relations ===
    User user

    // === Informations demande ===
    String typeExtrait
    String genre
    Date dateNaissance
    String villeNaissance
    String telephone
    String nomPere
    String nomMere
    String lieuLivraison

    // === Type de demandeur ===
    String typeDemandeur

    // === Demande pour un tiers ===
    String beneficiaireNom
    String beneficiairePrenom
    String beneficiaireEmail

    String statut = STATUT_BROUILLON

    Date dateCreated
    Date lastUpdated

    static constraints = {
        user nullable: false

        typeExtrait nullable: false, blank: false, inList: [TYPE_SIMPLE, TYPE_MULTI_LANGUE, TYPE_INTEGRALE]
        genre nullable: false, blank: false, inList: ["MASCULIN", "FEMININ"]
        dateNaissance nullable: false
        villeNaissance nullable: false, blank: false
        telephone nullable: false, blank: false, matches: /^[0-9]{10}$/, message: "Le numéro doit contenir 10 chiffres"
        nomPere nullable: true, blank: true, maxSize: 100
        nomMere nullable: true, blank: true, maxSize: 100
        lieuLivraison nullable: false, blank: false, maxSize: 200

        statut nullable: false, blank: false, inList: [STATUT_BROUILLON, STATUT_EN_TRAITEMENT, STATUT_ACCEPTE, STATUT_REFUSE]

        typeDemandeur nullable: false, blank: false, inList: [DEMANDEUR_MOI_MEME, DEMANDEUR_TIERS]

        beneficiaireNom nullable: true, blank: true, maxSize: 100   // Peut être null si MOI_MEME
        beneficiairePrenom nullable: true, blank: true, maxSize: 100
        beneficiaireEmail nullable: true, email: true
    }

    static mapping = {
        table 'demande'
        version false
        user column: 'user_id'

        typeExtrait column: 'type_extrait'
        dateNaissance column: 'date_naissance'
        villeNaissance column: 'ville_naissance'
        telephone column: 'telephone'
        nomPere column: 'nom_pere'
        nomMere column: 'nom_mere'
        lieuLivraison column: 'lieu_livraison'

        dateCreated column: 'date_creation'
        lastUpdated column: 'date_modification'

        typeDemandeur column: 'type_demandeur'
        beneficiaireNom column: 'beneficiaire_nom'
        beneficiairePrenom column: 'beneficiaire_prenom'
        beneficiaireEmail column: 'beneficiaire_email'

        sort statut: 'asc'
    }

    boolean isModifiable() {
        return statut == STATUT_BROUILLON
    }

    String getDisplayType() {
        switch(typeExtrait) {
            case TYPE_SIMPLE: return "Extrait simple"
            case TYPE_MULTI_LANGUE: return "Extrait multi-langue"
            case TYPE_INTEGRALE: return "Extrait intégrale"
            default: return typeExtrait
        }
    }

    String getDisplayStatut() {
        switch(statut) {
            case STATUT_BROUILLON: return "Brouillon"
            case STATUT_EN_TRAITEMENT: return "En traitement"
            case STATUT_ACCEPTE: return "Accepté"
            case STATUT_REFUSE: return "Refusé"
            default: return statut
        }
    }

    String getDisplayTypeDemandeur() {
        return typeDemandeur == DEMANDEUR_MOI_MEME ? "Pour moi-même" : "Pour un tiers"
    }
}