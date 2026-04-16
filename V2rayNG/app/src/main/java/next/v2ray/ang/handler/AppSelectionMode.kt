package next.v2ray.ang.handler

import androidx.annotation.StringRes
import next.v2ray.ang.AppConfig
import next.v2ray.ang.R

enum class AppSelectionMode(
    val value: String,
    val enabledKey: String,
    val selectedSetKey: String,
    val invertKey: String,
    @StringRes val titleRes: Int,
    @StringRes val enabledTextRes: Int,
    @StringRes val invertTextRes: Int,
    @StringRes val helpTextRes: Int
) {
    PER_APP_PROXY(
        value = "per_app_proxy",
        enabledKey = AppConfig.PREF_PER_APP_PROXY,
        selectedSetKey = AppConfig.PREF_PER_APP_PROXY_SET,
        invertKey = AppConfig.PREF_BYPASS_APPS,
        titleRes = R.string.per_app_proxy_settings,
        enabledTextRes = R.string.per_app_proxy_settings_enable,
        invertTextRes = R.string.switch_bypass_apps_mode,
        helpTextRes = R.string.summary_pref_per_app_proxy
    ),
    PROXIED_ONLY_APPS(
        value = "proxied_only_apps",
        enabledKey = AppConfig.PREF_PROXIED_ONLY_APPS,
        selectedSetKey = AppConfig.PREF_PROXIED_ONLY_APPS_SET,
        invertKey = AppConfig.PREF_PROXIED_ONLY_APPS_INVERT,
        titleRes = R.string.proxied_only_apps_settings,
        enabledTextRes = R.string.proxied_only_apps_settings_enable,
        invertTextRes = R.string.switch_invert_mode,
        helpTextRes = R.string.summary_proxied_only_apps
    );

    companion object {
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: PER_APP_PROXY
    }
}
