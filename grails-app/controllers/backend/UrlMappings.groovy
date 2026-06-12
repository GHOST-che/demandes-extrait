package backend

class UrlMappings {

    static mappings = {

        // ==================== DEMANDES ====================
        "/api/demandes"(controller: "demande", action: "index", method: "GET")
        "/api/demandes/stats"(controller: "demande", action: "stats", method: "GET")
        "/api/demandes/$id"(controller: "demande", action: "show", method: "GET")
        "/api/demandes"(controller: "demande", action: "save", method: "POST")
        "/api/demandes/$id"(controller: "demande", action: "update", method: "PUT")
        "/api/demandes/$id/statut"(controller: "demande", action: "updateStatut", method: "PATCH")
        "/api/demandes/$id"(controller: "demande", action: "delete", method: "DELETE")
        "/api/register"(controller: "user", action: "register", method: "POST")
        "/api/me"(controller: "user", action: "me", method: "GET")

        // ==================== AUTHENTIFICATION ====================
        "/api/login"(controller: "login", action: "auth", method: "POST")

        // ==================== FALLBACK ====================
        "/api/$controller/$action?/$id?(.$format)?" {
            constraints { }
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}