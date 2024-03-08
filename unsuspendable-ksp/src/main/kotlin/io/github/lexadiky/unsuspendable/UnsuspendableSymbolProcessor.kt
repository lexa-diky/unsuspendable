package io.github.lexadiky.unsuspendable

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.lexadiky.unsuspendable.factory.SuspendableInterfaceFactory
import io.github.lexadiky.unsuspendable.generator.FutureInterfaceGenerator

class UnsuspendableSymbolProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    private val suspendableInterfaceFactory = SuspendableInterfaceFactory()
    private val generator = FutureInterfaceGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("io.github.lexadiky.unsuspendable.Unsuspendable")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .forEach(::process)
        return emptyList()
    }

    private fun process(declaration: KSClassDeclaration) {
        val suspendableInterface = suspendableInterfaceFactory.create(declaration)
        val file = generator.generate(suspendableInterface)
        file.writeTo(codeGenerator, aggregating = false)
    }
}