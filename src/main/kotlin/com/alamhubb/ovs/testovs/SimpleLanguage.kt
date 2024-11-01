package com.alamhubb.ovs.testovs

import com.intellij.lang.Language

class SimpleLanguage private constructor() : Language("Simple") {

    companion object {
        val INSTANCE = SimpleLanguage()
    }
}