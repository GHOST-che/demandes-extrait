package backend.dto

import backend.User

class UserDTO {
    Long id
    String username
    String firstName
    String lastName
    String fullName
    List<String> roles
    Boolean enabled

    static UserDTO fromUser(User user) {
        if (!user) return null

        // Récupération sécurisée des rôles
        List<String> roleNames = []
        try {
            def authorities = user.authorities
            if (authorities) {
                roleNames = authorities.collect { it.authority }
            }
        } catch (Exception e) {
            roleNames = []
        }

        return new UserDTO(
                id: user.id,
                username: user.username,
                firstName: user.firstName,
                lastName: user.lastName,
                fullName: user.fullName,
                roles: roleNames,
                enabled: user.enabled
        )
    }

    Map toMap() {
        return [
                id: id,
                username: username,
                firstName: firstName,
                lastName: lastName,
                fullName: fullName,
                roles: roles,
                enabled: enabled
        ]
    }
}