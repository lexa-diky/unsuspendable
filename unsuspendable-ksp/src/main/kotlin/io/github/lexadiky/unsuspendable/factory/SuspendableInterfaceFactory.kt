package io.github.lexadiky.unsuspendable.factory

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.lexadiky.unsuspendable.entity.MethodParameter
import io.github.lexadiky.unsuspendable.entity.SuspendableInterface
import io.github.lexadiky.unsuspendable.entity.SuspendableMethod
import io.github.lexadiky.unsuspendable.entity.TypeDescription

class SuspendableInterfaceFactory {

    fun create(declaration: KSClassDeclaration): SuspendableInterface {
        return SuspendableInterface(
            packageName = declaration.packageName.asString(),
            name = declaration.simpleName.asString(),
            methods = createMethods(declaration)
        )
    }

    private fun createMethods(declaration: KSClassDeclaration): List<SuspendableMethod> {
        return declaration.getDeclaredFunctions()
            .map(::createMethod)
            .toList()
    }

    private fun createMethod(function: KSFunctionDeclaration): SuspendableMethod {
        return SuspendableMethod(
            name = function.simpleName.asString(),
            returns = createTypeDescription(function.returnType),
            parameters = createParameters(function)
        )
    }

    private fun createParameters(function: KSFunctionDeclaration): List<MethodParameter> {
        return function.parameters.map {  param ->
            val name = param.name?.asString() ?: error("only named parameters are supported")
            MethodParameter(name, createTypeDescription(param.type))
        }
    }

    private fun createTypeDescription(returnType: KSTypeReference?): TypeDescription {
        val typeName = returnType?.resolve()?.toTypeName()
            ?: ClassName.bestGuess("kotlin.Unit")
        return TypeDescription(typeName)
    }
}