<%=packageName ? "package ${packageName}\n\n" : ''%>

import grails.transaction.*

@Transactional(readOnly = true)
class ${className}Controller {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond ${className}.list(params), model:[${propertyName}Count: ${className}.count()]
    }

    def show(${className} ${propertyName}) {
        respond ${propertyName}
    }

    def create() {        
        respond new ${className}(params)
    }

    @Transactional
    def save(${className} ${propertyName}) {
        if(${propertyName}.hasErrors()) {
            respond ${propertyName}.errors, view:'create' // STATUS CODE 422
        }
        else {
            ${propertyName}.save flush:true
            withFormat {
                html { 
                    flash.message = message(code: 'default.created.message', args: [message(code: '${propertyName}.label', default: '${className}'), ${propertyName}.id])
                    redirect ${propertyName}
                }
                '*' { render status:201 }
            }
        }
    }

    def edit(${className} ${propertyName}) { 
        respond ${propertyName}  
    }

    @Transactional
    def update(${className} ${propertyName}) {
        if(${propertyName} == null) {
            render status:404
        }
        else if(${propertyName}.hasErrors()) {
            respond ${propertyName}.errors, view:'edit' // STATUS CODE 422
        }
        else {
            ${propertyName}.save flush:true
            withFormat {
                html { 
                    flash.message = message(code: 'default.updated.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                    redirect ${propertyName} 
                }
                '*'{ render status:200 }
            }
        }        
    }

    @Transactional
    def delete(${className} ${propertyName}) {
        if(${propertyName}) {
            ${propertyName}.delete flush:true
            withFormat {
                html { 
                    flash.message = message(code: 'default.deleted.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                    redirect action:"index", method:"GET" 
                }
                '*'{ render status:204 } // NO CONTENT STATUS CODE  
            }                
        }
        else {
            render status:404
        }
    }
}

