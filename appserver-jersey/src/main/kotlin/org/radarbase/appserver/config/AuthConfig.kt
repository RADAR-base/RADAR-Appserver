package org.radarbase.appserver.config

data class AuthConfig(
    val resourceName: String = "res_Appserver",
    val issuer: String? = null,
    val managementPortalUrl: String? = null,
    val publicKeyUrls: List<String>? = null,
) : Validation {
    override fun validate() {
        check(managementPortalUrl != null || !publicKeyUrls.isNullOrEmpty()) {
            "At least one of auth.publicKeyUrls or auth.managementPortalUrl must be configured"
        }
    }
}
