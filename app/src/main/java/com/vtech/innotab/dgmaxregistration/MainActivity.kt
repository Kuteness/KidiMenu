package com.vtech.innotab.title.m57_126805_000_336


import android.app.Activity
import android.content.Intent
import android.app.AlertDialog
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*

class MainActivity : Activity() {

    private lateinit var gridView: GridView
    private lateinit var searchBar: EditText
    private lateinit var pm: PackageManager
    private lateinit var apps: List<ApplicationInfo>
    private val filteredApps: MutableList<ApplicationInfo> = ArrayList()
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.setPadding(20, 20, 20, 20)

        val title = TextView(this)
        title.text = "KidiMenu"
        title.textSize = 20f
        title.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val menuButton = ImageButton(this)
        menuButton.setImageResource(android.R.drawable.ic_menu_more)
        menuButton.setBackgroundColor(0x00000000)

        topBar.addView(title)
        topBar.addView(menuButton)

        searchBar = EditText(this)
        searchBar.hint = "Search apps..."
        searchBar.clearFocus()

        gridView = GridView(this)
        gridView.numColumns = 3
        gridView.verticalSpacing = 20
        gridView.horizontalSpacing = 20
        gridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH

        rootLayout.addView(topBar)
        rootLayout.addView(searchBar)
        rootLayout.addView(gridView)

        setContentView(rootLayout)

        pm = packageManager

        apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            }

        filteredApps.addAll(apps)

        adapter = AppAdapter()
        gridView.adapter = adapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        menuButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Credits")

            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Credits") {
                    showCredits()
                }
                true
            }

            popup.show()
        }

        gridView.setOnItemClickListener { _, _, position, _ ->
            val app = filteredApps[position]
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) startActivity(launchIntent)
        }

        gridView.setOnItemLongClickListener { _, _, position, _ ->
            uninstallApp(filteredApps[position])
            true
        }
    }

    private fun showCredits() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Credits")
        dialog.setMessage("KidiMenu\nCoded by Kuteness\nThanks to jojo09 to had created the\nCertifiGate exploit")
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun uninstallApp(app: ApplicationInfo) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:" + app.packageName)
        startActivity(intent)
    }

    private fun filterApps(query: String) {
        filteredApps.clear()
        for (app in apps) {
            val name = pm.getApplicationLabel(app).toString().lowercase()
            if (name.contains(query.lowercase())) {
                filteredApps.add(app)
            }
        }
        adapter.notifyDataSetChanged()
    }

    inner class AppAdapter : BaseAdapter() {
        override fun getCount(): Int = filteredApps.size
        override fun getItem(position: Int): Any = filteredApps[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity)
                .inflate(android.R.layout.activity_list_item, parent, false)

            val icon = view.findViewById<ImageView>(android.R.id.icon)
            val text = view.findViewById<TextView>(android.R.id.text1)

            val app = filteredApps[position]
            val appIcon: Drawable = pm.getApplicationIcon(app)

            icon.setImageDrawable(appIcon)
            text.text = pm.getApplicationLabel(app)

            return view
        }
    }
}

