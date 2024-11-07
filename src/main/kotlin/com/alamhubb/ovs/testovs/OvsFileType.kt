package com.alamhubb.ovs.testovs

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class OvsFileType private constructor() : LanguageFileType(OvsLanguage.INSTANCE) {
    override fun getName(): String {
        return "Ovs File"
    }

    override fun getDescription(): String {
        return "Ovs language file"
    }

    override fun getDefaultExtension(): String {
        return "ovs"
    }

    override fun getIcon(): Icon {
        return OvsIcons.FILE
    }

    companion object {
        val INSTANCE: OvsFileType = OvsFileType()
    }
}