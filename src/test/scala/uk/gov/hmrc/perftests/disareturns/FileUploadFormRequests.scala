/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.perftests.disareturns

import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType

trait FileUploadFormRequests extends BaseRequest {

  private val fileUploadAmazonUrlPattern    = """action="(.*?)""""
  private val callBackUrPattern             = """name="x-amz-meta-callback-url" value="(.*?)""""
  private val amzDatePattern                = """name="x-amz-date" value="(.*?)""""
  private val amzCredentialPattern          = """name="x-amz-credential" value="(.*?)""""
  private val UpscanInitiateResponsePattern = """name="x-amz-meta-upscan-initiate-response" value="(.*?)""""
  private val UpscanInitiateReceivedPattern = """name="x-amz-meta-upscan-initiate-received" value="(.*?)""""
  private val amzMetaRequestIdPattern       = """name="x-amz-meta-request-id" value="(.*?)""""
  private val amzMetaOriginalFilename       = """name="x-amz-meta-original-filename" value="(.*?)""""
  private val amzAlgorithmPattern           = """name="x-amz-algorithm" value="(.*?)""""
  private val keyPattern                    = """name="key" value="(.*?)""""
  private val aclPattern                    = """name="acl" value="(.*?)""""
  private val amzSignaturePattern           = """name="x-amz-signature" value="(.*?)""""
  private val sessionIdPattern              = """name="x-amz-meta-session-id" value="(.*?)""""
  private val consumingServicePattern       = """name="x-amz-meta-consuming-service" value="(.*?)""""
  private val policyPattern                 = """name="policy" value="(.*?)""""
  private val referencePattern              = """data-reference="(.*?)""""
  private val fileTypePattern               = """data-filetype="(.*?)""""

  def saveFileUploadurl: CheckBuilder[RegexCheckType, String] =
    bodyCheck(fileUploadAmazonUrlPattern).saveAs("fileUploadAmazonUrl")

  def saveCallBack: CheckBuilder[RegexCheckType, String] =
    bodyCheck(callBackUrPattern).saveAs("callBack")

  def saveReference: CheckBuilder[RegexCheckType, String] =
    bodyCheck(referencePattern).saveAs("reference")

  def saveFileType: CheckBuilder[RegexCheckType, String] =
    bodyCheck(fileTypePattern).saveAs("fileType")

  def saveAmazonDate: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzDatePattern).saveAs("amazonDate")

  def saveAmazonCredential: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzCredentialPattern).saveAs("amazonCredential")

  def saveUpscanIniateResponse: CheckBuilder[RegexCheckType, String] =
    bodyCheck(UpscanInitiateResponsePattern).saveAs("upscanInitiateResponse")

  def saveUpscanInitiateRecieved: CheckBuilder[RegexCheckType, String] =
    bodyCheck(UpscanInitiateReceivedPattern).saveAs("upscanInitiateReceived")

  def saveAmazonMetaOriginalFileName: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzMetaOriginalFilename).saveAs("amazonMetaOriginalFileName")

  def saveAmazonMetaRequestId: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzMetaRequestIdPattern).saveAs("metaRequestId")

  def saveAmazonAlgorithm: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzAlgorithmPattern).saveAs("amazonAlgorithm")

  def saveKey: CheckBuilder[RegexCheckType, String] =
    bodyCheck(keyPattern).saveAs("key")

  def saveAcl: CheckBuilder[RegexCheckType, String] =
    bodyCheck(aclPattern).saveAs("acl")

  def saveMetaSessionId: CheckBuilder[RegexCheckType, String] =
    bodyCheck(sessionIdPattern).saveAs("metaSessionId")

  def saveConsumingService: CheckBuilder[RegexCheckType, String] =
    bodyCheck(consumingServicePattern).saveAs("consumingService")

  def saveQAmazonSignature: CheckBuilder[RegexCheckType, String] =
    bodyCheck(amzSignaturePattern).saveAs("amazonSignature")

  def savePolicy: CheckBuilder[RegexCheckType, String] =
    bodyCheck(policyPattern).saveAs("policy")

}
