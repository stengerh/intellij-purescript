package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSImportDeclarationImplTest : BasePlatformTestCase() {

    fun `test resolve to module in root directory`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!! as PSModule

        TestCase.assertEquals("Foo", psModule.name)
    }

    fun `test dont crash if module not found`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ) as PSFile

        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()
        TestCase.assertNull(psModule)
    }


    fun `test resolve to module in subdirectory`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Bar/Foo.purs",
            """
            module Bar.Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Bar.Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!! as PSModule

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test resolve to module with correct module name when there is competing files`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Bar/Foo.purs",
            """
            module Bar.Foo where
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Bar.Foo")!!

        val resolve = psImportDeclaration.reference.resolve()
        val psModule = resolve!! as PSModule

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test knows about imported names`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo hiding (x)
            import Bar
            import Buz (x)
            import Fuz (hiding)
            """.trimIndent()
        ) as PSFile

        val foo = mainFile.module.getImportDeclarationByName("Foo")!!
        assertTrue(foo.isHiding)
        assertContainsElements(foo.namedImports, "x")

        val bar = mainFile.module.getImportDeclarationByName("Bar")!!
        assertFalse(bar.isHiding)
        assertDoesntContain(bar.namedImports, "x")

        val buz = mainFile.module.getImportDeclarationByName("Buz")!!
        assertFalse(buz.isHiding)
        assertContainsElements(buz.namedImports, "x")

        val fuz = mainFile.module.getImportDeclarationByName("Fuz")!!
        assertFalse(fuz.isHiding)
        assertContainsElements(fuz.namedImports, "hiding")
    }

}