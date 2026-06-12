package backend.dto

class StatistiquesDTO {
    Long brouillon
    Long enTraitement
    Long accepte
    Long refuse
    Long total

    static StatistiquesDTO fromStats(Map stats) {
        return new StatistiquesDTO(
                brouillon: stats.brouillon ?: 0L,
                enTraitement: stats.enTraitement ?: 0L,
                accepte: stats.accepte ?: 0L,
                refuse: stats.refuse ?: 0L,
                total: stats.total ?: 0L
        )
    }

    Map toMap() {
        return [
                brouillon: brouillon,
                enTraitement: enTraitement,
                accepte: accepte,
                refuse: refuse,
                total: total
        ]
    }
}