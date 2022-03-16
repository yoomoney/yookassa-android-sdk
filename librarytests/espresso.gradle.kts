/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import org.gradle.kotlin.dsl.support.unzipTo
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.net.URL
import java.nio.channels.Channels
import kotlin.properties.Delegates


val APK_TEST_FOLDER_PATH = "${projectDir.absolutePath}/build/outputs/apk/androidTest/debug"


tasks.register("spoonDebugAndroidTest") {
    group = "ci"
    val apkTestFolder = File(APK_TEST_FOLDER_PATH)
    if (!apkTestFolder.exists()) {
        println("APK_TEST_FOLDER $apkTestFolder is !exists")
        dependsOn("assembleDebugAndroidTest")
    }


    doLast() {
        val PROJECT_NAME = "money-payments-sdk-android"

        mkdir("${rootDir.absolutePath}/espresso")
        var apkPath = getApkDebugName()
        var apkTestPath = getApkTestDebugName()

        }
}


class Device() {
    lateinit var id: String
    var nubmer by Delegates.notNull<Int>()
    private var runParam = mutableListOf<String>()

    constructor(
        idDevices: String,
        numberDevice: Int
    ) : this() {
        this.id = idDevices
        this.nubmer = numberDevice
    }

    fun addParam(param: String) {
        runParam.add(param)
    }

    fun getParamToString(): MutableList<String> {
        return runParam
    }

    fun clearParam() {
        runParam.clear()
    }

    override fun toString(): String {
        return "Devices(id='$id', nubmer='$nubmer')"
    }

    fun killEmu() {
        execCmd("emu kill")
    }

    fun execCmd(command: String = "shell ls -s") {
        var thread = ThreadShellInput("adb -s ${this.id} $command")
        thread.start()
        thread.join()
    }
}


class TinyHelper(project: String) {

    private val projectName = project

    fun getListFullNameBlockedTests(): List<String> {
        var ansCurl = curlReq(listOf("curl", "https://tiny.yooteam.ru/api/test/blockedTests?project=$projectName"))
        var json = groovy.json.JsonSlurper().parseText(ansCurl) as List<Map<String, String>>
        val listFullNameBlockedTest = mutableListOf<String>()
        json.forEach { testCase ->
            testCase["fullName"]?.let { listFullNameBlockedTest.add(it) }
        }
        println("TinyHelper: listFullNameBlockedTest = $listFullNameBlockedTest")
        return listFullNameBlockedTest
    }

    fun getMarathonTextFile(debugPath: String , debugTestPath: String): String {
        var pwd = getCiPWD()
        println("pwd is ${pwd}")
        return curlReq(
            listOf(
                "curl",
                "https://tiny.yamoney.ru/api/test/get-marathon-file?project=$projectName&appNameTestDebug=${debugTestPath}&appNameDebug=${debugPath}&branch=${getCiBuildBranch()}&pwd=${pwd}"
            )
        )
    }

    private fun curlReq(cmdComandList: List<String>): String {
        println("TinyHelper: try  = ${cmdComandList.joinToString(" ")}")
        var ans = ByteArrayOutputStream().use { outputStream ->
            project.exec {
                commandLine(cmdComandList)
                standardOutput = outputStream
            }
            outputStream.toString()
        }
        println("TinyHelper: ans = $ans")
        return ans
    }
}

class Helper {

    private val thread = mutableMapOf<String, ThreadShellInput>()
    fun downloadFile(stringUrl: String, file: File) {
        println("download $stringUrl")
        val website = URL(stringUrl)
        val rbc = Channels.newChannel(website.openStream())
        val fos = FileOutputStream(file.absolutePath)
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)
        println("save to file ${file.absolutePath}")

    }

    fun updateListDevices(): List<Device> {

        println("try getDevices")
        var tempDevices = mutableListOf<Device>()
        var lines = ByteArrayOutputStream().use { outputStream ->
            project.exec {
                commandLine(listOf("adb", "devices"))
                standardOutput = outputStream
            }
            outputStream.toString()
        }.split("\n")

        for (i in 1..lines.size - 3) {
            tempDevices.add(Device(lines[i].replace("\tdevice", ""), i - 1))
        }
        return tempDevices
    }

    fun runEmu(emuList: List<String>): List<Device> {
        if (emuList.size > 4 || emuList.isEmpty()) {
            throw kotlin.Exception("emuList most have size <= 4 and not empty")
        }
        emuList.forEach { emuName ->
            println("try run emu $emuName")
            thread.put(emuName, ThreadShellInput(listOf("emulator", "$emuName", "-read-only").joinToString(" ")))
            thread[emuName]!!.start()
        }
        sleep(20000)
        var devices = updateListDevices()
        var iteratorMarathon = 1234
        devices.forEach { device ->
            device.execCmd("su root setprop marathon.serialno '$iteratorMarathon'")
            iteratorMarathon++
            sleep(5000)
        }
        println("device run: ${devices}")
        return devices
    }

}


class ThreadShellInput() : Thread() {
    var shellCommand = mutableListOf<String>()
    private var output = listOf<String>()

    constructor(cmd: String) : this() {
        var tempList = cmd.split(" ")
        tempList.forEach {
            this.shellCommand.add(it.replace("space_Char", " "))
        }
        println("try $shellCommand")
    }

    override fun run() {
        println("Strart ${this.name}")
        println(this.shellCommand)
        val process = ProcessBuilder(shellCommand).start()
        process.inputStream.reader(Charsets.UTF_8).use {
            output = it.readLines()
        }
        println("End ${this.name}")
    }

    fun getOutPut(): List<String> {
        return output
    }
}

fun getCiBuildBranch(): String {
    return System.getenv("BUILD_BRANCH") ?: "dev"
}

fun getCiPWD(): String {
    return "${rootDir.absolutePath}/espresso"
}


fun getApkDebugName(): File {
    println("get apk debug name file")
    var fileTree = fileTree(APK_TEST_FOLDER_PATH)
    fileTree.forEach {
        println(it.absolutePath)
    }

    var fileFind = fileTree.filter { it.name.contains("-debug-") }.first()
    println("file find ${fileFind.name}")
    return copyFile(File(fileFind.absolutePath), "apkDebug.apk")
}

fun getApkTestDebugName(): File {
    println("get apk debug name file")
    var fileTree = fileTree(APK_TEST_FOLDER_PATH)
    fileTree.forEach {
        println(it.absolutePath)
    }
    var fileFind = fileTree.filter { it.name.contains(".apk") }.first()
    println("file find ${fileFind.name}")
    return copyFile(File(fileFind.absolutePath), "apkTestDebug.apk")
}

fun copyFile(file: File, newName: String): File {
    copy {
        from(file)
        into(File("${rootDir.absolutePath}/espresso"))
        rename {
            newName
        }
    }

    File("${rootDir.absolutePath}/espresso/${file.name}").delete()
    val newFile = File("${rootDir.absolutePath}/espresso/$newName")
    println("after rename $newFile ${newFile.exists()}")
    return newFile
}

