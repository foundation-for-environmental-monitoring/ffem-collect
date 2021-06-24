package io.ffem.collect.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import io.ffem.collect.android.util.AdjustingViewGlobalLayoutListener
import org.odk.collect.android.R
import org.odk.collect.android.activities.CollectAbstractActivity
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.application.initialization.ExistingProjectMigrator
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.logic.PropertyManager
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ThemeUtils
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.projects.Project.Saved
import javax.inject.Inject

/**
 * Sign in screen shown on app first launch
 */
class SignInActivity : CollectAbstractActivity() {
    var webCredentialsUtils: WebCredentialsUtils? = null

    @JvmField
    @Inject
    var propertyManager: PropertyManager? = null

    @JvmField
    @Inject
    var projectMigrator: ExistingProjectMigrator? = null

    @JvmField
    @Inject
    var currentProjectProvider: CurrentProjectProvider? = null

    @JvmField
    @Inject
    var settingsProvider: SettingsProvider? = null

    private var editText: EditText? = null
    private var editPassword: EditText? = null
    private var layoutUserName: TextInputLayout? = null
    private var layoutPassword: TextInputLayout? = null
    private var isSettings = false
    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)
        isSettings = intent.getBooleanExtra("isSettings", false)
        try {
            currentProjectProvider!!.getCurrentProject()
        } catch (e: Exception) {
            var projectName: String? = ""
            if (webCredentialsUtils != null) {
                projectName = webCredentialsUtils!!.userNameFromPreferences
            }
            projectMigrator!!.run(projectName!!)
        }
        webCredentialsUtils = WebCredentialsUtils(settingsProvider.getGeneralSettings())
        if (webCredentialsUtils!!.passwordFromPreferences.isNotEmpty() && !isSettings) {
            startActivity(Intent(baseContext, MainMenuActivity::class.java))
            finish()
        }
        super.onCreate(savedInstanceState)
        val themeUtils = ThemeUtils(this)
        setTheme(themeUtils.appTheme)
        setContentView(R.layout.activity_signin)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        try {
            setSupportActionBar(toolbar)
        } catch (ignored: Exception) {
        }
        if (isSettings) {
            findViewById<View>(R.id.imageLogo).visibility = View.GONE
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
            setTitle(R.string.server_credentials)
        } else {
            if (toolbar != null) {
                toolbar.visibility = View.GONE
            }
        }
        initialize()
    }

    private fun maskPassword(password: String?): String {
        return if (password != null && password.isNotEmpty()) MASK else ""
    }

    private fun initialize() {
        editText = findViewById(R.id.editUsername)
        editPassword = findViewById(R.id.editPassword)
        findViewById<View>(R.id.textCreateAccount).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(resources.getString(R.string.create_account_link))
            startActivity(intent)
        }
        findViewById<View>(R.id.textForgot).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(resources.getString(R.string.reset_password_link))
            startActivity(intent)
        }
        findViewById<View>(R.id.buttonSignIn).setOnClickListener {
            if (isInputValid) {
                if (editPassword!!.text.toString() != MASK) {
                    webCredentialsUtils!!.saveCredentialsPreferences(editText!!.text.toString()
                        .trim { it <= ' ' },
                        editPassword!!.text.toString().trim { it <= ' ' }, propertyManager
                    )
                }
                var projectName = "Existing"
                if (webCredentialsUtils != null) {
                    projectName = webCredentialsUtils!!.userNameFromPreferences
                }
                val project = Saved(
                    currentProjectProvider!!.getCurrentProjectId()!!,
                    projectName, projectName.substring(0, 1).uppercase(), "#3e9fcc"
                )
                projectMigrator!!.updateProject(project)
                startActivity(Intent(baseContext, MainMenuActivity::class.java))
                finish()
            }
        }
        layoutUserName = findViewById(R.id.layoutUsername)
        layoutPassword = findViewById(R.id.passwordLayout)
        if (webCredentialsUtils!!.passwordFromPreferences.isNotEmpty() || isSettings) {
            editText!!.setText(webCredentialsUtils!!.userNameFromPreferences)
            editPassword!!.setText(maskPassword(webCredentialsUtils!!.passwordFromPreferences))
        }
        editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                layoutUserName!!.error = null
                layoutUserName!!.isErrorEnabled = false
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        editPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                layoutPassword!!.error = null
                layoutPassword!!.isErrorEnabled = false
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        val listen = AdjustingViewGlobalLayoutListener(findViewById(R.id.authLayout))
        window.decorView.rootView.viewTreeObserver.addOnGlobalLayoutListener(listen)
    }

    private val isInputValid: Boolean
        get() {
            val username = editText!!.text.toString().trim { it <= ' ' }
            val password = editPassword!!.text.toString().trim { it <= ' ' }
            if (username.isEmpty()) {
                layoutUserName!!.isErrorEnabled = true
                layoutUserName!!.error = resources.getString(R.string.enter_username)
                return false
            } else {
                layoutUserName!!.isErrorEnabled = false
                layoutUserName!!.error = null
            }
            if (password.isEmpty()) {
                layoutPassword!!.isErrorEnabled = true
                layoutPassword!!.error = resources.getString(R.string.enter_password)
                return false
            } else {
                layoutPassword!!.isErrorEnabled = false
                layoutPassword!!.error = null
            }
            return true
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val MASK = "**********"
    }
}