package nl.icsvertex.ktor.processor.visitors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.ClassKind
import nl.icsvertex.server.controllers.annotations.KtorController
import java.io.OutputStream

class KtorModuleVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val ktorControllerClasses: MutableMap<String, KSClassDeclaration> = mutableMapOf(),
    private val ktorServiceClasses: MutableList<KSClassDeclaration> = mutableListOf(),
    private val ktorScheduleClasses: MutableList<KSClassDeclaration> = mutableListOf()
) : KSVisitorVoid() {

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val moduleFunction = function.simpleName.asString()
        val packageName = function.packageName.asString()
        val fileName = "${moduleFunction}Generated"

        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(
                true,
                function.containingFile!!,
                *ktorControllerClasses.map { it.value.containingFile!! }.toTypedArray(),
                *ktorServiceClasses.map { it.containingFile!! }.toTypedArray(),
                *ktorScheduleClasses.map { it.containingFile!! }.toTypedArray()
            ),
            packageName = packageName,
            fileName = "${moduleFunction}Generated"
        )

        val controllerInits = ktorControllerClasses.map { (path, controller) ->
            val fqdn = controller.qualifiedName?.asString() ?: controller.simpleName.asString()
            val invocation = if (controller.classKind == ClassKind.OBJECT) fqdn else "$fqdn()"
            if(path.isBlank()) {
                "this init $invocation"
            } else {
                "this path \"$path\" init $invocation"
            }
        }

        val serviceInits = ktorServiceClasses.map { service ->
            val fqdn = service.qualifiedName?.asString() ?: service.simpleName.asString()
            val invocation = if (service.classKind == ClassKind.OBJECT) fqdn else "$fqdn()"
            "this add $invocation"
        }

        val scheduleInits = ktorScheduleClasses.map { schedule ->
            val fqdn = schedule.qualifiedName?.asString() ?: schedule.simpleName.asString()
            val invocation = if (schedule.classKind == ClassKind.OBJECT) fqdn else "$fqdn()"
            "this schedule $invocation"
        }

        file.write("package $packageName\n\n".toByteArray())
        file.write("import nl.icsvertex.server.controllers.controllers\n".toByteArray())
        file.write("import nl.icsvertex.server.services.services\n".toByteArray())
        file.write("import nl.icsvertex.server.schedules.schedules\n".toByteArray())
        file.write("import nl.icsvertex.server.modules.ktorModule\n".toByteArray())
        file.write("import nl.icsvertex.server.modules.types.KtorModule\n\n".toByteArray())
        file.write(
        """
            fun KM_$moduleFunction(): KtorModule = ktorModule {
                $moduleFunction()
                
                controllers {
                    ${controllerInits.joinToString("\n")}
                }
                
                services {
                    ${serviceInits.joinToString("\n")}
                }
                
                schedules {
                    ${scheduleInits.joinToString("\n")}
                }
            }
        """.trimIndent().toByteArray())
        file.close()
    }
}
