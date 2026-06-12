package backend.dto

import backend.Demande
import java.text.SimpleDateFormat

class DemandeDTO {
    Long id
    Long userId
    String userFullName
    String typeExtrait
    String statut
    String beneficiaireNomComplet
    String dateCreation
    String dateModification
    String dateNaissance
    Boolean modifiable

    static DemandeDTO fromDemande(Demande demande) {
        if (!demande) return null

        def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        def sdfDate = new SimpleDateFormat("yyyy-MM-dd")

        // Construction sécurisée du nom complet du bénéficiaire
        String beneficiaireComplet = ""
        if (demande.beneficiaireNom || demande.beneficiairePrenom) {
            beneficiaireComplet = "${demande.beneficiairePrenom ?: ''} ${demande.beneficiaireNom ?: ''}".trim()
        } else {
            beneficiaireComplet = demande.user?.fullName ?: ""
        }

        return new DemandeDTO(
                id: demande.id,
                userId: demande.user?.id,
                userFullName: demande.user?.fullName ?: "",
                typeExtrait: demande.typeExtrait,
                statut: demande.statut,
                beneficiaireNomComplet: beneficiaireComplet,
                dateCreation: demande.dateCreated ? sdf.format(demande.dateCreated) : null,
                dateModification: demande.lastUpdated ? sdf.format(demande.lastUpdated) : null,
                dateNaissance: demande.dateNaissance ? sdfDate.format(demande.dateNaissance) : null,
                modifiable: demande.modifiable
        )
    }

    Map toMap() {
        return [
                id: id,
                userId: userId,
                userFullName: userFullName,
                typeExtrait: typeExtrait,
                statut: statut,
                beneficiaireNomComplet: beneficiaireNomComplet,
                dateCreation: dateCreation,
                dateModification: dateModification,
                dateNaissance: dateNaissance,
                modifiable: modifiable
        ]
    }
}