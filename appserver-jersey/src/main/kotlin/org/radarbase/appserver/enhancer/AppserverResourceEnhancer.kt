package org.radarbase.appserver.enhancer

import org.radarbase.appserver.config.AppserverConfig
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class AppserverResourceEnhancer(private val config: AppserverConfig) : JerseyResourceEnhancer {

    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.resource",
        )

    override val classes: Array<Class<*>>
        get() = super.classes


}
