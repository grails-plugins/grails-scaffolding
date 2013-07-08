<%=packageName ? "package ${packageName}\n\n" : ''%>

import grails.transaction.*
import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
class ${className}Controller {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        ${className}.async.task {
            [${propertyName}List: list(params), count: count() ]
        }.then { result ->
            respond result.${propertyName}List, model:[${propertyName}Count: result.count]
        }
    }

    def show(Long id) {
        ${className}.async.get(id).then { ${propertyName}->
            respond ${propertyName} 
        }
    }

    def create() {
        respond new ${className}(params)
    }

    def save(${className} ${propertyName}) {
        ${className}.async.withTransaction {
            if(${propertyName}.hasErrors()) {
                respond ${propertyName}.errors, view:'create' // STATUS CODE 422
            }
            else {
                ${propertyName}.save flush:true
                request.withFormat {
                    form {
                        flash.message = message(code: 'default.created.message', args: [message(code: '${propertyName}.label', default: '${className}'), ${propertyName}.id])
                        redirect ${propertyName}
                    }
                    '*' { respond ${propertyName}, [status: CREATED] }
                }
            }

        }
    }

    def edit(Long id) {
        ${className}.async.get(id).then { ${propertyName}->
            respond ${propertyName} 
        }
    }

    def update(Long id) {

        ${className}.async.withTransaction {
            def ${propertyName}= ${className}.get(id)
            if(${propertyName}== null) {
                render status: NOT_FOUND
                return
            }
            ${propertyName}.properties = params
            if( !${propertyName}.save(flush:true) ) {
                respond ${propertyName}.errors, view:'edit' // STATUS CODE 422
            }
            else {
                request.withFormat {
                    form {
                        flash.message = message(code: 'default.updated.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                        redirect ${propertyName}
                    }
                    '*'{ respond ${propertyName}, [status: OK] }
                }
            }

        }
    }


    def delete(Long id) {
        ${className}.async.withTransaction {
            def ${propertyName}= ${className}.get(id)
            if(${propertyName}) {
                ${propertyName}.delete flush:true
                request.withFormat {
                    form {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                        redirect action:"index", method:"GET"
                    }
                    '*'{ render status: NO_CONTENT } 
                }
            }
            else {
                render status: NOT_FOUND
            }
        }
    }
}

