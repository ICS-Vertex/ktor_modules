package nl.icsvertex.ktor.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import nl.icsvertex.ktor.processor.visitors.KtorModuleVisitor
import nl.icsvertex.server.controllers.annotations.KtorController
import nl.icsvertex.server.modules.annotations.KtorModule
import nl.icsvertex.server.schedules.annotations.KtorSchedule
import nl.icsvertex.server.services.annotations.KtorService

class KtorModuleProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private var ktorModuleFunction: KSFunctionDeclaration? = null
    // Store the path as a String instead of trying to instantiate the KtorController annotation
    private val ktorControllerClasses: MutableMap<String, KSClassDeclaration> = mutableMapOf()
    private val ktorServiceClasses: MutableList<KSClassDeclaration> = mutableListOf()
    private val ktorScheduleClasses: MutableList<KSClassDeclaration> = mutableListOf()

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getNewFiles().forEach { file ->
            file.declarations.forEach { declaration ->
                when {
                    declaration is KSFunctionDeclaration && declaration.isAnnotationPresent(KtorModule::class) -> {
                        ktorModuleFunction = declaration
                    }
                    declaration is KSClassDeclaration && declaration.isAnnotationPresent(KtorController::class) -> {
                        // Extract the path argument safely without getAnnotationsByType
                        val annotation = declaration.annotations.firstOrNull { it.shortName.asString() == "KtorController" }
                        val path = annotation?.arguments?.find { it.name?.asString() == "path" }?.value as? String ?: ""
                        
                        ktorControllerClasses[path] = declaration
                    }
                    declaration is KSClassDeclaration && declaration.isAnnotationPresent(KtorService::class) -> {
                        ktorServiceClasses.add(declaration)
                    }
                    declaration is KSClassDeclaration && declaration.isAnnotationPresent(KtorSchedule::class) -> {
                        ktorScheduleClasses.add(declaration)
                    }
                }
            }
        }
        return emptyList()
    }

    override fun finish() {
        ktorModuleFunction?.accept(KtorModuleVisitor(codeGenerator, logger, ktorControllerClasses, ktorServiceClasses, ktorScheduleClasses), Unit)
    }
}