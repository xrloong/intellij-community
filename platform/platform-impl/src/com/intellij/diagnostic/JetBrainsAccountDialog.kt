/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.diagnostic

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.BrowserUtil
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.dialog
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ClickListener
import com.intellij.ui.layout.*
import com.intellij.ui.layout.LCFlags.*
import com.intellij.util.net.HttpConfigurable
import java.awt.Component
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.*

fun showJetBrainsAccountDialog(parent: Component, project: Project? = null): DialogBuilder {
  val panel = panel(fillX) {
    label(DiagnosticBundle.message("diagnostic.error.report.description"))
    link(DiagnosticBundle.message("diagnostic.error.report.proxy.setup")) {
      HttpConfigurable.editConfigurable(this)
    }
  }

  return dialog(
      title = DiagnosticBundle.message("error.report.title"),
      centerPanel = panel,
      project = project,
      parent = if (parent.isShowing) parent else null)
}

class JetBrainsAccountDialog : DialogWrapper(null) {
  private val myItnLoginTextField: JTextField? = null
  private val myPasswordText: JPasswordField? = null
  private val myRememberCheckBox: JCheckBox? = null

  var myMainPanel: JPanel? = null
  var mySendingSettingsLabel: JLabel? = null
  private val myCreateAccountLabel: JLabel? = null

  override fun getDimensionServiceKey(): String? {
    return "#com.intellij.diagnostic.AbstractSendErrorDialog"
  }

  override fun getPreferredFocusedComponent(): JComponent? {
    return myItnLoginTextField
  }

  override fun init() {
    contentPane.add(myMainPanel)

    object : ClickListener() {
      override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
        HttpConfigurable.editConfigurable(myMainPanel)
        return true
      }
    }.installOn(mySendingSettingsLabel!!)

    mySendingSettingsLabel!!.cursor = Cursor(Cursor.HAND_CURSOR)

    val credentials = ErrorReportConfigurable.getCredentials()
    val userName = credentials?.userName
    myItnLoginTextField!!.text = userName
    val password = credentials?.getPasswordAsString()
    myPasswordText!!.text = password
    // if no user name - never stored and so, defaults to remember. if user name set, but no password, so, previously was stored without password
    myRememberCheckBox!!.isSelected = StringUtil.isEmpty(userName) || !StringUtil.isEmpty(password)

    object : ClickListener() {
      override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
        BrowserUtil.browse("http://account.jetbrains.com")
        return true
      }
    }.installOn(myCreateAccountLabel!!)
    myCreateAccountLabel.cursor = Cursor(Cursor.HAND_CURSOR)

    super.init()
  }

  override fun doOKAction() {
    val userName = myItnLoginTextField!!.text
    if (!StringUtil.isEmpty(userName)) {
      PasswordSafe.getInstance().set(CredentialAttributes(ErrorReportConfigurable.SERVICE_NAME, userName),
          Credentials(userName, if (myRememberCheckBox!!.isSelected) myPasswordText!!.password else null))
    }
    super.doOKAction()
  }

  override fun createCenterPanel(): JComponent? {
    return myMainPanel
  }
}
