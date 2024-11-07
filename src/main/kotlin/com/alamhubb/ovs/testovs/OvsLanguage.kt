package com.alamhubb.ovs.testovs

import com.intellij.lang.Language

class OvsLanguage private constructor() : Language("Ovs") {

    companion object {
        val INSTANCE = OvsLanguage()
    }
}