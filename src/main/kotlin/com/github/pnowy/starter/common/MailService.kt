package com.github.pnowy.starter.common

import com.github.pnowy.starter.config.AppProperties
import com.github.pnowy.starter.account.User
import mu.KotlinLogging
import org.springframework.context.MessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.nio.charset.StandardCharsets
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class MailService(val javaMailSender: JavaMailSender,
                  val messageSource: MessageSource,
                  val templateEngine: SpringTemplateEngine,
                  val appProperties: AppProperties) {

    companion object {
        private const val USER = "user"
        private const val BASE_URL = "baseUrl"
    }

    @Async
    fun sendEmail(to: String, subject: String, content: String, isMultipart: Boolean, isHtml: Boolean) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content)

        // Prepare message using a Spring helper
        val mimeMessage = javaMailSender.createMimeMessage()
        try {
            val message = MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name())
            message.setTo(to)
            message.setFrom(appProperties.system.emailFrom)
            message.setSubject(subject)
            message.setText(content, isHtml)
            javaMailSender.send(mimeMessage)
            log.debug("Sent email to User '{}'", to)
        } catch (e: Exception) {
            if (log.isDebugEnabled) {
                log.warn("Email could not be sent to user '{}'", to, e)
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.message)
            }
        }
    }

    @Async
    fun sendEmailFromTemplate(user: User, templateName: String, titleKey: String) {
        val locale = Locale.forLanguageTag(user.locale)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(BASE_URL, appProperties.system.baseUrl)
        val content = templateEngine.process(templateName, context)
        val subject = messageSource.getMessage(titleKey, null, locale)
        sendEmail(user.email, subject, content, false, true)
    }

    @Async
    fun sendActivationEmail(user: User) {
        log.debug("Sending activation email to '{}'", user.email)
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title")
    }

    @Async
    fun sendPasswordResetMail(user: User) {
        log.debug("Sending password reset email to '{}'", user.email)
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title")
    }

}
