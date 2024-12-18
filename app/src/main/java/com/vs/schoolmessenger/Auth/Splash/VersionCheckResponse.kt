package com.vs.schoolmessenger.Auth.Splash

data class VersionCheckResponse(
    val playstoreMarketId: String,
    val playStoreLink: String,
    val versionAlertTitle: String,
    val isAppUninstall: String,
    val resendOTPTimer: String,
    val imageSize: String,
    val pdfSize: String,
    val fileContent: String,
    val videoJson: String,
    val videoSizeLimit: String,
    val videoSizeLimitAlert: String,
    val profileTitle: String,
    val uploadProfileTitle: String,
    val awsAccessKey: String,
    val awsSecreteKey: String,
    val alertTitle: String,
    val offerslink: String,
    val alertContent: String,
    val awsMasterBucketName: String,
    val awsMasterBucketRegion: String,
    val awsTransBucketName: String,
    val awsTransBucketRegion: String,
    val awsTransCognitoPoolid: String,
    val awsMasterCognitoPoolid: String,
    val onlineTextBookLink: String,
    val versionAlertContent: String,
    val newProductLink: String,
    val helplineURL: String,
    val reportsLink: String,
    val feePaymentGateway: String,
    val profileLink: String,
    val isAlertAvailable: String,
    val adTimerInterval: String,
    val otpDialInbound: String,
    val newVersion: String,
    val inAppUpdate: String,
    val newUpdates: String,
    val languages: List<String>,
    val updateAvailable: String,
    val forceUpdate: String
)
