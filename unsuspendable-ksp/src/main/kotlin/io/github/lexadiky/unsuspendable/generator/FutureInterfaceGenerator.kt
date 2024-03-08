package io.github.lexadiky.unsuspendable.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.lexadiky.unsuspendable.entity.SuspendableInterface
import io.github.lexadiky.unsuspendable.entity.SuspendableMethod

class FutureInterfaceGenerator {

    fun generate(suspendableInterface: SuspendableInterface): FileSpec {
        return FileSpec.builder(
            suspendableInterface.packageName,
            makeInterfaceName(suspendableInterface)
        )
            .indent("    ")
            .addType(generateInterface(suspendableInterface))
            .addType(generateImplementation(suspendableInterface))
            .addImport("io.github.lexadiky.unsuspendable", "unsuspendable")
            .addFileComment("GENERATED with unsuspendable, please do not modify manually")
            .build()
    }

    private fun generateImplementation(suspendableInterface: SuspendableInterface): TypeSpec {
        return TypeSpec.classBuilder(makeImplementationName(suspendableInterface))
            .addAnnotation(TypeNameLib.generatedAnnotation)
            .primaryConstructor(generateImplementationConstructor(suspendableInterface))
            .addFunctions(suspendableInterface.methods.map(::generateMethod))
            .addProperties(generateImplementationProperties(suspendableInterface))
            .addSuperinterface(ClassName(suspendableInterface.packageName, makeInterfaceName(suspendableInterface)))
            .build()
    }

    private fun generateImplementationConstructor(suspendableInterface: SuspendableInterface): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("scope", TypeNameLib.coroutineScope)
            .addParameter("base", ClassName(suspendableInterface.packageName, suspendableInterface.name))
            .build()
    }

    private fun generateImplementationProperties(suspendableInterface: SuspendableInterface): List<PropertySpec> {
        return buildList {
            add(
                PropertySpec.builder("scope", TypeNameLib.coroutineScope)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("scope")
                    .build()
            )

            add(
                PropertySpec.builder("base", ClassName(suspendableInterface.packageName, suspendableInterface.name))
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("base")
                    .build()
            )
        }
    }


    private fun generateInterface(suspendableInterface: SuspendableInterface): TypeSpec {
        return TypeSpec.interfaceBuilder(makeInterfaceName(suspendableInterface))
            .addAnnotation(TypeNameLib.generatedAnnotation)
            .addFunctions(suspendableInterface.methods.map(::generateAbstractMethod))
            .build()
    }

    private fun generateMethod(method: SuspendableMethod): FunSpec {
        val body = CodeBlock.builder()
        body.beginControlFlow("return unsuspendable(scope) {")

        val innerBody = CodeBlock.builder()

        innerBody.addStatement("base.%L(", method.name)
        innerBody.indent()
        method.parameters.forEach { param ->
            innerBody.addStatement("%L = %L", param.name, param.name)
        }
        innerBody.unindent()
        innerBody.addStatement(")")

        body.add(wrapWithOptionalInvocation(method.returns.typeName.isNullable, innerBody.build()))

        body.endControlFlow()

        return FunSpec.builder(method.name)
            .returns(wrapWithFuture(method.returns.typeName))
            .addCode(body.build())
            .addModifiers(KModifier.OVERRIDE)
            .addParameters(createParameters(method))
            .build()
    }

    private fun generateAbstractMethod(method: SuspendableMethod): FunSpec {
        return FunSpec.builder(method.name)
            .addModifiers(KModifier.ABSTRACT)
            .returns(wrapWithFuture(method.returns.typeName))
            .addParameters(createParameters(method))
            .build()
    }

    private fun createParameters(method: SuspendableMethod) =
        method.parameters.map { param ->
            ParameterSpec.builder(param.name, param.typeDescription.typeName)
                .build()
        }

    private fun makeInterfaceName(suspendableInterface: SuspendableInterface): String {
        return "Future${suspendableInterface.name}"
    }

    private fun makeImplementationName(suspendableInterface: SuspendableInterface): String {
        return "Future${suspendableInterface.name}Impl"
    }

    private fun wrapWithOptionalInvocation(isOptional: Boolean, codeBlock: CodeBlock): CodeBlock {
        if (!isOptional) {
            return codeBlock
        }

        val wrapper = CodeBlock.builder()
        return wrapper.addStatement("Optional.ofNullable(")
            .indent()
            .add(codeBlock)
            .unindent()
            .addStatement(")")
            .build()
    }

    private fun wrapWithFuture(typeName: TypeName): TypeName {
        return TypeNameLib.completableFuture
            .parameterizedBy(wrapWithOption(typeName))
    }

    private fun wrapWithOption(typeName: TypeName): TypeName {
        if (!typeName.isNullable) {
            return typeName
        }

        return TypeNameLib.optional
            .parameterizedBy(typeName.copy(nullable = false))
    }
}