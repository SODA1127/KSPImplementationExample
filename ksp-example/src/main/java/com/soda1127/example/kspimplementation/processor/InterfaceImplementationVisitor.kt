@file:Suppress("KDocUnresolvedReference")

package com.soda1127.example.kspimplementation.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.soda1127.example.kspimplementation.processor.extensions.toAppendable
import com.squareup.kotlinpoet.*
import java.util.*

/**
 * @author SODA1127
 * [InterfaceImplementationProcessor]에서 사용되는 KSVisitor 클래스.
 */
class InterfaceImplementationVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val annotationName: String,
    private val filteringKeywords: Array<String>
) : KSVisitorVoid() {

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        super.visitAnnotation(annotation, data)
        logger.warn("visitAnnotation = $annotation")
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        super.visitAnnotated(annotated, data)
        logger.warn("visitAnnotated = $annotated")

    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        logger.warn("@${annotationName} -> $classDeclaration 발견")

        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()

        /**
         * [KSClassDeclaration.simpleName]과 동일한 이름으로 파일 생성하여
         * Interface, Implemtation 클래스를 두개 작성하는 파일
         */
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(true, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = className
        )

        /**
         * Interface 정의를 한다.
         * ex) [IExampleRepository]와 같이 `I`키워드가 Prefix로 붙게되어 클래스가 작성된다.
         */
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

        /**
         * Implementation 정의를한다.
         * ex) [ExampleRepositoryImpl]와 같이 `Impl`키워드가 Suffix로 붙게되어 클래스가 작성된다.
         */
        val implementationName = "${className}Impl"
        val implementsType = TypeSpec.classBuilder(implementationName)
            .addSuperinterface(ClassName.bestGuess("${packageName}.${interfaceName}"))
            .primaryConstructor(
                // 생성자에는 구현될 함수가 있는 본래 클래스가 인자로 들어간다.
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
                ).initializer(className.lowercase(Locale.getDefault()))
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
                        /**
                         * 반환타입이 [Unit]인지, 아닌지에 따라 return 키워드를 사용한다.
                         */
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
