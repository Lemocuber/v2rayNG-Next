package com.v2ray.ang.handler

class AppSelectionStore(private val mode: AppSelectionMode) : AppSelectionState {
    private val selectedPackages: MutableSet<String> = MmkvManager.decodeSettingsStringSet(mode.selectedSetKey)?.let {
        HashSet(it)
    } ?: HashSet()

    fun isEnabled(): Boolean = MmkvManager.decodeSettingsBool(mode.enabledKey, false)

    fun setEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(mode.enabledKey, enabled)
        SettingsChangeManager.makeRestartService()
    }

    fun isInverted(): Boolean = MmkvManager.decodeSettingsBool(mode.invertKey, false)

    fun setInverted(inverted: Boolean) {
        MmkvManager.encodeSettings(mode.invertKey, inverted)
        SettingsChangeManager.makeRestartService()
    }

    override fun contains(packageName: String): Boolean = selectedPackages.contains(packageName)

    override fun getAll(): Set<String> = selectedPackages.toSet()

    override fun add(packageName: String): Boolean {
        val changed = selectedPackages.add(packageName)
        if (changed) save()
        return changed
    }

    override fun remove(packageName: String): Boolean {
        val changed = selectedPackages.remove(packageName)
        if (changed) save()
        return changed
    }

    override fun toggle(packageName: String) {
        if (selectedPackages.contains(packageName)) remove(packageName) else add(packageName)
    }

    override fun addAll(packages: Collection<String>) {
        if (selectedPackages.addAll(packages)) save()
    }

    override fun removeAll(packages: Collection<String>) {
        if (selectedPackages.removeAll(packages.toSet())) save()
    }

    override fun clear() {
        if (selectedPackages.isNotEmpty()) {
            selectedPackages.clear()
            save()
        }
    }

    private fun save() {
        MmkvManager.encodeSettings(mode.selectedSetKey, selectedPackages)
        SettingsChangeManager.makeRestartService()
    }
}
