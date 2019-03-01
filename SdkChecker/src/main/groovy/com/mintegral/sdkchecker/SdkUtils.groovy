package com.mintegral.sdkchecker

import groovy.json.JsonSlurper


class SdkUtils {
    static Map<String, ArrayList<String>> initSdkTypeMap() {
        def inputStream = SdkUtils.getClassLoader().getResourceAsStream("sdkConfigure.json")
        def jsonStr = inputStream.text

        def jsonSlurper = new JsonSlurper()
        def sdkTypeMap = jsonSlurper.parseText(jsonStr)

        return sdkTypeMap
    }
}