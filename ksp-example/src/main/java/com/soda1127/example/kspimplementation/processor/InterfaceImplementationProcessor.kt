package com.soda1127.example.kspimplementation.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.soda1127.example.kspimplementation.processor.extensions.toAppendable
import com.squareup.kotlinpoet.*
import java.util.*

class InterfaceImplementationProcessor : SymbolProcessor {

    private lateinit var codeGenerator: CodeGenerator
    private lateinit var logger: KSPLogger


    companion object {
        private val annotationName = InterfaceImplementation::class.java.canonicalName
        private val filteringKeywords = arrayOf("equals", "hashCode", "toString", "<init>")
    }

    fun init(
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("Processor 시작")

        val symbols = resolver.getSymbolsWithAnnotation(annotationName)

        logger.warn("symbols : $symbols")

        val ret = symbols.filter { !it.validate() }

        symbols
            .filter {
                logger.warn("symbol : $it")
                it is KSClassDeclaration && it.validate()
            }
            .forEach { it.accept(AbstractionVisitor(codeGenerator, logger), Unit) }

        return ret.toList()
    }

    inner class AbstractionVisitor(
        private val codeGenerator: CodeGenerator,
        private val logger: KSPLogger
    ) : KSVisitorVoid() {

        override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
            super.visitAnnotation(annotation, data)
            logger.warn("visitAnnotation = $annotation")
        }

        override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
            super.visitAnnotated(annotated, data)
            logger.warn("visitAnnotated = $annotated")

        }

        @Suppress("DefaultLocale")
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            logger.warn("@${annotationName} -> $classDeclaration 발견")

            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(true, classDeclaration.containingFile!!),
                packageName = packageName,
                fileName = className
            )

            val interfaceName = "I$className"
            val interfaceType = TypeSpec.interfaceBuilder(interfaceName)
                .addFunctions(
                    classDeclaration.getAllFunctions()
                        .filterNot { filteringKeywords.contains(it.simpleName.asString()) }
                        .map { declare ->
                            FunSpec.builder(declare.simpleName.asString())
                                .addParameters(declare.parameters.map {
                                    val build = ParameterSpec.builder(
                                        it.name!!.asString(),
                                        ClassName.bestGuess(it.type.resolve().declaration.qualifiedName?.asString()!!)
                                    )
                                        .build()
                                    build
                                })
                                .returns(ClassName.bestGuess(declare.returnType?.resolve()?.declaration?.qualifiedName?.asString()!!))
                                .addModifiers(KModifier.ABSTRACT)
                                .build()
                        }.toList()
                )
                .build()
            logger.warn("$interfaceName 생성 완료")

            val implementationName = "${className}Impl"
            val implementsType = TypeSpec.classBuilder(implementationName)
                .addSuperinterface(ClassName.bestGuess("${packageName}.${interfaceName}"))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(
                            ParameterSpec.builder(
                                className.lowercase(Locale.getDefault()),
                                ClassName.bestGuess("$packageName.$className")
                            ).build()
                        )
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(
                        className.lowercase(Locale.getDefault()),
                        ClassName.bestGuess("$packageName.$className")
                    )
                        .initializer(className.lowercase(Locale.getDefault()))
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                )
                .addFunctions(
                    classDeclaration.getAllFunctions()
                        .filterNot { filteringKeywords.contains(it.simpleName.asString()) }
                        .map { declare ->
                            val builder = FunSpec.builder(declare.simpleName.asString())
                                .addParameters(declare.parameters.map {
                                    ParameterSpec.builder(
                                        it.name!!.asString(),
                                        ClassName.bestGuess(it.type.resolve().declaration.qualifiedName?.asString()!!)
                                    ).build()
                                })
                                .addModifiers(KModifier.OVERRIDE)

                            val invokeParameters =
                                declare.parameters.joinToString(", ") { it.name!!.asString() }
                            val returnClassName =
                                ClassName.bestGuess(declare.returnType?.resolve()?.declaration?.qualifiedName?.asString()!!)
                            if (returnClassName != Unit::class.asClassName()) {
                                builder.addStatement("return ${className.lowercase(Locale.getDefault())}.${declare.simpleName.asString()}($invokeParameters)")
                                builder.returns(returnClassName)
                            } else {
                                builder.addStatement("${className.lowercase(Locale.getDefault())}.${declare.simpleName.asString()}($invokeParameters)")
                            }
                            builder.build()
                        }.toList()
                )
                .build()
            logger.warn("$implementationName 생성 완료")

            logger.warn("className : $className")
            logger.warn("packageName : $packageName")

            FileSpec.builder(packageName, className)
                .addType(interfaceType)
                .addType(implementsType)
                .build()
                .writeTo(file.toAppendable())
        }
    }

    override fun finish() {
        logger.warn("Processor 끝")
    }

    override fun onError() {
        logger.error("Processor 에러")
    }
}

class AbstractionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return InterfaceImplementationProcessor().apply {
            init(environment.codeGenerator, environment.logger)
        }
    }
}
