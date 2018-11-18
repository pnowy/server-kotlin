package com.github.pnowy.starter.exceptions

class EntityNotFoundException(definition: ErrorDefinition) : CustomParametrizedException(definition) {

    constructor(entityName: String, entityId: Any):
            this(ErrorDefinition.ENTITY_NOT_FOUND
                    .withTitle("Entity type $entityName with $entityId doesn't exist!")
                    .withIdParam(entityId))

    constructor(message: String):
            this(ErrorDefinition.ENTITY_NOT_FOUND.withTitle(message))

}
