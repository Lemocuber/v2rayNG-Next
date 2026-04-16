package next.v2ray.ang.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import next.v2ray.ang.AppConfig
import next.v2ray.ang.AppConfig.ANG_PACKAGE
import next.v2ray.ang.R
import next.v2ray.ang.databinding.ActivityBypassListBinding
import next.v2ray.ang.dto.AppInfo
import next.v2ray.ang.extension.toast
import next.v2ray.ang.extension.toastSuccess
import next.v2ray.ang.extension.v2RayApplication
import next.v2ray.ang.handler.AppSelectionMode
import next.v2ray.ang.handler.AppSelectionStore
import next.v2ray.ang.shizuku.ProxiedOnlyAppsManager
import next.v2ray.ang.util.AppManagerUtil
import next.v2ray.ang.util.HttpUtil
import next.v2ray.ang.util.Utils
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class PerAppProxyActivity : BaseActivity() {
    companion object {
        private const val EXTRA_MODE = "extra_mode"

        fun newIntent(context: Context, mode: AppSelectionMode) =
            Intent(context, PerAppProxyActivity::class.java).putExtra(EXTRA_MODE, mode.value)
    }

    private val binding by lazy { ActivityBypassListBinding.inflate(layoutInflater) }
    private val mode by lazy { AppSelectionMode.fromValue(intent.getStringExtra(EXTRA_MODE)) }
    private val selectionStore by lazy { AppSelectionStore(mode) }

    private var adapter: PerAppProxyAdapter? = null
    private var appsAll: List<AppInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(mode.titleRes))

        addCustomDividerToRecyclerView(binding.recyclerView, this, R.drawable.custom_divider)
        configureHeader()
        initList()
    }

    private fun configureHeader() {
        binding.switchPerAppProxy.text = getString(mode.enabledTextRes)
        binding.switchBypassApps.text = getString(mode.invertTextRes)
        binding.layoutSwitchBypassAppsTips.contentDescription = getString(mode.invertTextRes)

        binding.switchPerAppProxy.isChecked = selectionStore.isEnabled()
        binding.switchBypassApps.isChecked = selectionStore.isInverted()

        binding.switchPerAppProxy.setOnCheckedChangeListener { _, isChecked ->
            if (mode == AppSelectionMode.PROXIED_ONLY_APPS && selectionStore.isEnabled() && !isChecked) {
                ProxiedOnlyAppsManager.handleFeatureDisabled(applicationContext)
            }
            selectionStore.setEnabled(isChecked)
        }
        binding.switchBypassApps.setOnCheckedChangeListener { _, isChecked ->
            selectionStore.setInverted(isChecked)
        }
        binding.layoutSwitchBypassAppsTips.setOnClickListener {
            Toasty.info(this, mode.helpTextRes, Toast.LENGTH_LONG, true).show()
        }
    }

    private fun initList() {
        showLoading()

        lifecycleScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    val appsList = AppManagerUtil.loadNetworkAppList(this@PerAppProxyActivity)
                        .filter { mode.shouldIncludeInSelectionList(it.isSystemApp) }
                    val selectedPackages = selectionStore.getAll()
                    if (selectedPackages.isNotEmpty()) {
                        appsList.forEach { app ->
                            app.isSelected = if (selectedPackages.contains(app.packageName)) 1 else 0
                        }
                        appsList.sortedWith { p1, p2 ->
                            when {
                                p1.isSelected > p2.isSelected -> -1
                                p1.isSelected < p2.isSelected -> 1
                                p1.isSystemApp > p2.isSystemApp -> 1
                                p1.isSystemApp < p2.isSystemApp -> -1
                                p1.appName.lowercase() > p2.appName.lowercase() -> 1
                                p1.appName.lowercase() < p2.appName.lowercase() -> -1
                                p1.packageName > p2.packageName -> 1
                                p1.packageName < p2.packageName -> -1
                                else -> 0
                            }
                        }
                    } else {
                        val collator = Collator.getInstance()
                        appsList.sortedWith(compareBy(collator) { it.appName })
                    }
                }

                appsAll = apps
                adapter = PerAppProxyAdapter(apps, selectionStore)
                binding.recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e(ANG_PACKAGE, "Error loading apps", e)
            } finally {
                hideLoading()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bypass_list, menu)
        if (mode == AppSelectionMode.PROXIED_ONLY_APPS) {
            menu.removeItem(R.id.select_all)
            menu.removeItem(R.id.invert_selection)
            menu.removeItem(R.id.select_proxy_app)
        }

        val searchItem = menu.findItem(R.id.search_view)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterProxyApp(newText.orEmpty())
                    return false
                }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.select_all -> {
            selectAllApp()
            enableCurrentMode()
            true
        }

        R.id.invert_selection -> {
            invertSelection()
            enableCurrentMode()
            true
        }

        R.id.select_proxy_app -> {
            selectProxyAppAuto()
            enableCurrentMode()
            true
        }

        R.id.import_proxy_app -> {
            importProxyApp()
            enableCurrentMode()
            true
        }

        R.id.export_proxy_app -> {
            exportProxyApp()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun selectAllApp() {
        adapter?.let { currentAdapter ->
            val packageNames = currentAdapter.apps.map { it.packageName }
            val allSelected = packageNames.all { selectionStore.contains(it) }

            if (allSelected) {
                selectionStore.removeAll(packageNames)
            } else {
                selectionStore.addAll(packageNames)
            }
            refreshData()
        }
    }

    private fun invertSelection() {
        adapter?.let { currentAdapter ->
            currentAdapter.apps.forEach { app ->
                selectionStore.toggle(app.packageName)
            }
            refreshData()
        }
    }

    private fun selectProxyAppAuto() {
        toast(R.string.msg_downloading_content)
        showLoading()

        val url = AppConfig.ANDROID_PACKAGE_NAME_LIST_URL
        lifecycleScope.launch(Dispatchers.IO) {
            var content = HttpUtil.getUrlContent(url, 5000)
            if (content.isNullOrEmpty()) {
                val httpPort = next.v2ray.ang.handler.SettingsManager.getHttpPort()
                content = HttpUtil.getUrlContent(url, 5000, httpPort) ?: ""
            }
            launch(Dispatchers.Main) {
                selectProxyApp(content, true)
                toastSuccess(R.string.toast_success)
                hideLoading()
            }
        }
    }

    private fun importProxyApp() {
        val content = Utils.getClipboard(applicationContext)
        if (TextUtils.isEmpty(content)) return
        selectProxyApp(content, false)
        toastSuccess(R.string.toast_success)
    }

    private fun exportProxyApp() {
        var lines = selectionStore.isInverted().toString()
        selectionStore.getAll().forEach { pkg ->
            lines += System.lineSeparator() + pkg
        }
        Utils.setClipboard(applicationContext, lines)
        toastSuccess(R.string.toast_success)
    }

    private fun enableCurrentMode() {
        binding.switchPerAppProxy.isChecked = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun selectProxyApp(content: String, force: Boolean): Boolean {
        return try {
            val proxyApps = if (TextUtils.isEmpty(content)) {
                Utils.readTextFromAssets(v2RayApplication, "proxy_package_name")
            } else {
                content
            }
            if (TextUtils.isEmpty(proxyApps)) return false

            selectionStore.clear()

            adapter?.let { currentAdapter ->
                currentAdapter.apps.forEach { app ->
                    val inProxyApps = inProxyApps(proxyApps, app.packageName, force)
                    if (selectionStore.isInverted()) {
                        if (!inProxyApps) selectionStore.add(app.packageName)
                    } else {
                        if (inProxyApps) selectionStore.add(app.packageName)
                    }
                }
            }
            refreshData()
            true
        } catch (e: Exception) {
            Log.e(AppConfig.TAG, "Error selecting proxy app", e)
            false
        }
    }

    private fun inProxyApps(proxyApps: String, packageName: String, force: Boolean): Boolean {
        if (force) {
            if (packageName == "com.google.android.webview") return false
            if (packageName.startsWith("com.google")) return true
        }
        return proxyApps.indexOf(packageName) >= 0
    }

    private fun filterProxyApp(content: String): Boolean {
        val apps = ArrayList<AppInfo>()
        val keyword = content.uppercase()
        if (keyword.isNotEmpty()) {
            appsAll?.forEach {
                if (it.appName.uppercase().indexOf(keyword) >= 0 || it.packageName.uppercase().indexOf(keyword) >= 0) {
                    apps.add(it)
                }
            }
        } else {
            appsAll?.forEach(apps::add)
        }

        adapter = PerAppProxyAdapter(apps, selectionStore)
        binding.recyclerView.adapter = adapter
        refreshData()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        adapter?.notifyDataSetChanged()
    }
}
