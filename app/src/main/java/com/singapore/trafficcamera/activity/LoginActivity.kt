package com.singapore.trafficcamera.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.ActivityLoginBinding
import com.singapore.trafficcamera.dialog.LoadingDialog
import com.singapore.trafficcamera.dialog.WarningDialog
import com.singapore.trafficcamera.interfaces.WarningDialogListener
import com.singapore.trafficcamera.models.dao.User
import com.singapore.trafficcamera.tools.Preference
import com.singapore.trafficcamera.utils.GeneralUtils
import com.singapore.trafficcamera.utils.KeyboardUtils.hideKeyboard
import com.singapore.trafficcamera.utils.UserManager
import com.singapore.trafficcamera.utils.WifiUtils
import com.singapore.trafficcamera.viewModels.InternetViewModel
import com.singapore.trafficcamera.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), WarningDialogListener {

    private lateinit var binding: ActivityLoginBinding

    private val internetViewModel: InternetViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    //for password hide/show flag
    private var isTogglePassword = false
    private var loadingDialog: LoadingDialog?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initInternetObserver()
        initSharedPrefData()
        initClickEvent()
        initListener()
    }

    override fun onWarningDialogClicked() {
        //reset username & password
        binding.etUsername.text?.clear()
        binding.etPassword.text?.clear()
    }

    private fun performCheckingUserLogic(username: String, password: String) {
        //checking if user exist in db
        userViewModel.checkIfUserExist(username = username).observe(this) { isExist ->
            when {
                isExist -> {
                    //user exist, checking for password
                    performCheckingToLoginUser(username = username, password = password)
                }
                else -> {
                    //user not exist, checking for username & password validation
                    //username & password checking complete, insert to local db
                    performCheckingToCreateUser(username = username, password = password)
                }
            }
        }
    }

    private fun performCheckingToLoginUser(username: String, password: String) {
        //retrieve password from db for checking
        userViewModel.getPasswordFromUsername(username = username).observe(this) { user ->
            if (user != null) {
                when {
                    user.password.equals(password, ignoreCase = false) -> {
                        //password match
                        val currentUser = User(username = username, password = password)
                        saveUserToSharedPref(user = currentUser, username = username, password = password)

                        dismissLoadingDialog()

                        //navigate to main
                        navigateToMainActivity()
                    }
                    else -> {
                        //password not match
                        dismissLoadingDialog()
                        showWarningDialog(content = getString(R.string.warning_login_failed))
                    }
                }
            }
        }
    }

    private fun performCheckingToCreateUser(username: String, password: String) {
        //check username length
        when (username.length) {
            in 4..19 -> {
                //further check password condition
                when {
                    GeneralUtils.isValidPasswordFormat(password = password) -> {
                        //password match scenario
                        //insert it to db
                        val user = User(username = username, password = password)
                        userViewModel.insertUser(user =  user)

                        //save user at sharedPref
                        saveUserToSharedPref(user = user, username = username, password = password)

                        dismissLoadingDialog()

                        //navigate to main
                        navigateToMainActivity()
                    }
                    else -> {
                        //password not match
                        dismissLoadingDialog()
                        showWarningDialog(content = getString(R.string.warning_password_failed_fulfill))
                    }
                }
            }
            else -> {
                //username length not match
                dismissLoadingDialog()
                showWarningDialog(content = getString(R.string.warning_username_length_error))
            }
        }
    }

    private fun validationForLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            username.isEmpty() -> {
                binding.etUsername.requestFocus()
                binding.clWarningUsername.visibility = View.VISIBLE
                return
            }
            password.isEmpty() -> {
                binding.etPassword.requestFocus()
                binding.clWarningUsername.visibility = View.INVISIBLE
                binding.clWarningPassword.visibility = View.VISIBLE
                return
            }
            else -> {
                //show loading dialog
                showLoadingDialog()
                performCheckingUserLogic(username = username, password = password)
            }
        }
    }

    private fun initListener() {
        val selectedColor = ContextCompat.getColor(this, R.color.selected_label_text_color)
        val unselectedColor =  ContextCompat.getColor(this, R.color.default_label_txt_color)

        binding.etUsername.setOnFocusChangeListener { view, _ ->
            when {
                view.hasFocus() -> {
                    binding.tvUsernameLabel.setTextColor(selectedColor)
                }
                else -> {
                    binding.tvUsernameLabel.setTextColor(unselectedColor)
                }
            }
        }

        binding.etPassword.setOnFocusChangeListener { view, _ ->
            when {
                view.hasFocus() -> {
                    binding.tvPasswordLabel.setTextColor(selectedColor)
                    binding.ivTogglePassword.setColorFilter(selectedColor)
                }
                else -> {
                    binding.tvPasswordLabel.setTextColor(unselectedColor)
                    binding.ivTogglePassword.setColorFilter(unselectedColor)
                }
            }
        }

        binding.etPassword.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                binding.root.hideKeyboard()
                validationForLogin()
                true
            } else {
                false
            }
        }

        binding.ivTogglePassword.setOnClickListener {
            isTogglePassword = !isTogglePassword
            transformPassword()
        }
    }

    private fun transformPassword() {
        val drawable: Drawable?
        val passwordTransformationMethod: TransformationMethod

        when {
            !isTogglePassword -> {
                passwordTransformationMethod = PasswordTransformationMethod.getInstance()
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility_off)
            }
            else -> {
                passwordTransformationMethod = HideReturnsTransformationMethod.getInstance()
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility_on)
            }
        }
        binding.etPassword.transformationMethod = passwordTransformationMethod
        binding.ivTogglePassword.setImageDrawable(drawable)
        binding.etPassword.setSelection(binding.etPassword.length())
    }

    private fun initSharedPrefData() {
        val hello = getString(R.string.label_hello) + " !"

        when {
            Preference.prefUsername.isNotEmpty() && Preference.prefPassword.isNotEmpty() -> {
                binding.etUsername.setText(Preference.prefUsername)
                binding.etPassword.setText(Preference.prefPassword)

                val helloWithUserName = getString(R.string.label_hello) + " " + Preference.prefUsername + " !"
                binding.tvWelcomeUser.text = helloWithUserName
            }
            else -> {
                binding.tvWelcomeUser.text = hello
            }
        }
    }

    private fun initClickEvent() {
        binding.btnLogin.setOnClickListener {
            validationForLogin()
        }
    }

    private fun initInternetObserver() {
        internetViewModel.connected.observe(this) { connected ->
            WifiUtils.wifi?.isConnectedInternet = connected
        }
    }

    private fun showWarningDialog(content: String) {
        val warningDialog = WarningDialog(context = this, listener = this,
            title = getString(R.string.label_warning), body = content)
        GeneralUtils.showDialog(dialog = warningDialog, cancelable = false)
    }

    private fun showLoadingDialog() {
        loadingDialog = LoadingDialog(context = this)
        loadingDialog!!.show()
        loadingDialog!!.setCancelable(false)
        loadingDialog!!.setCanceledOnTouchOutside(false)
        loadingDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismiss()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun saveUserToSharedPref(user: User, username: String, password: String) {
        Preference.prefUsername = username
        Preference.prefPassword = password

        //use globally
        UserManager.data?.user = user
    }
}