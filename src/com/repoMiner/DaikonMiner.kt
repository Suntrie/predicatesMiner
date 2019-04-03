package com.repoMiner

import daikon.*
import daikon.DaikonSimple.SimpleProcessor
import daikon.FileIO.read_data_trace_files
import daikon.inv.Invariant
import daikon.inv.binary.BinaryInvariant
import daikon.inv.unary.UnaryInvariant
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import daikon.PptSlice2
import daikon.PptSlice
import daikon.inv.binary.twoScalar.*
import daikon.util.Pair
import java.lang.reflect.Array
import java.util.logging.Logger
import daikon.tools.InvariantChecker.dtrace_files
import daikon.FileIO
import daikon.Daikon
import daikon.Daikon.proto_invs
import daikon.PrintInvariants
import daikon.PptTopLevel
import daikon.inv.unary.scalar.*
import daikon.inv.unary.string.OneOfString
import java.util.regex.Pattern


class DaikonMiner {

    companion object {
        val daikonDir = "/home/suntrie/daikon-5.7.2"
        val daikonPath = daikonDir + File.separator + "daikon.jar"

        init {
            if (!File("tmp").exists())
                File("tmp").mkdirs()
        }
    }

    var logFile = File("log.txt")

    private fun executeCommand(cmd: List<String>) {

        val builder = ProcessBuilder(cmd)
        val env = builder.environment()
        env.put("DAIKONDIR", daikonDir)
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
        builder.redirectError(ProcessBuilder.Redirect.appendTo(logFile))
        try {
            val process = builder.start()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            throw RuntimeException(e.message)
        }

    }

    private fun getStringTimestamp() =
        SimpleDateFormat.getDateTimeInstance().format(System.currentTimeMillis())

    fun generateDynCompFile(
        mainFile: String,
        classPathFileNames: List<String>,
        args: List<String>,
        dynCompDirectory: String,
        dynCompFilePath: String
    ): File {


        logFile.appendText("Dynamic Comprability file is creating now...${getStringTimestamp()}")

        val dynCompFile = getEmptyFile(dynCompFilePath)

        val cmd = mutableListOf<String>()

        cmd.add("java")
        cmd.add("-cp")
        cmd.add(getFullClassPath(classPathFileNames))
        cmd.add("daikon.DynComp")
        cmd.add("--output-dir=${dynCompDirectory}")
        cmd.add("--decl-file=${dynCompFilePath}")
        cmd.add(mainFile)

        addArguments(args, cmd)

        executeCommand(cmd)


        logFile.appendText("Dynamic Comprability file successfully created. ${getStringTimestamp()}")


        return dynCompFile
    }

    private fun addArguments(
        args: List<String>,
        cmd: MutableList<String>
    ) {
        for (arg in args)
            cmd.add(arg)
    }

    private fun getFullClassPath(classPathFileNames: List<String>) =
        getClientClassPath(classPathFileNames) + daikonPath

    fun generateTraceFile(
        mainFile: String,
        classPathFileNames: List<String>,
        args: List<String>,
        dynCompFilePath: String,
        dataTraceDirectory: String,
        dataTraceFilePath: String
    ) {

        val dataTraceFile = getEmptyFile(dataTraceDirectory + File.separator + dataTraceFilePath)

        val cmd = mutableListOf<String>()

        cmd.add("java")
        cmd.add("-cp")
        cmd.add(getFullClassPath(classPathFileNames))
        cmd.add("daikon.Chicory")
        cmd.add("--comparability-file=$dynCompFilePath")
        cmd.add("--output-dir=$dataTraceDirectory")
        cmd.add("--dtrace-file=$dataTraceFilePath")
        cmd.add(mainFile)
        addArguments(args, cmd)

        executeCommand(cmd)
    }

    fun generateInvariantsFile(
        traceFileName: String,          // can be common - StackArTester*.dtrace.gz, e.g.
        dataTraceDirectory: String,
        invariantsFile: String,
        invariantsDirectory: String
    ) {

        val path = invariantsDirectory + File.separator + invariantsFile

        val invariantsFile = getEmptyFile(path)

        val cmd = mutableListOf<String>()

        cmd.add("java")
        cmd.add("-cp")
        cmd.add(getFullClassPath(listOf()))
        cmd.add("daikon.Daikon")
        cmd.add(dataTraceDirectory + File.separator + traceFileName)
        cmd.add("-o")
        cmd.add("${path}")

        executeCommand(cmd)
    }


    private fun setupProtoInvariants() {

        proto_invs.add(IntGreaterThan.get_proto())
        proto_invs.add(IntEqual.get_proto())
        proto_invs.add(IntLessThan.get_proto())

        proto_invs.add(FloatGreaterThan.get_proto())
        proto_invs.add(FloatEqual.get_proto())
        proto_invs.add(FloatLessThan.get_proto())

        proto_invs.add(LowerBound.get_proto())
        proto_invs.add(UpperBound.get_proto())

        proto_invs.add(LowerBoundFloat.get_proto())
        proto_invs.add(UpperBoundFloat.get_proto())

    }

    fun getDaikonInvariants(
        traceFileName: String,
        dataTraceDirectory: String

    ): Set<Invariant> {

        val all_ppts = PptMap()
        val tracePath = "$dataTraceDirectory${File.separator}$traceFileName"
        setupProtoInvariants();
        //Daikon.ppt_regexp =//TODO

        val invariants = mutableSetOf<Invariant>()

        val processor = SimpleProcessor()

        FileIO.read_data_trace_files(listOf(tracePath), all_ppts, processor, true)

        val t = all_ppts.pptIterator()

        while (t.hasNext()) {
            val ppt = t.next()

            if (ppt.num_samples() == 0 || ppt.is_subexit) {
                continue
            }
            for (inv in ppt.invariants) {

                if (inv.isWorthPrinting)

                    invariants.add(inv)
            }
        }

        return invariants

    }


    private fun getEmptyFile(filePath: String): File {
        val file = File(filePath)

        if (file.exists()) {
            file.delete()
        }

        return file
    }

    private fun getClientClassPath(fileNames: List<String>): String {

        val sb = StringBuilder()

        for (fileName in fileNames) {

            sb.append(fileName).append(File.pathSeparator)

        }

        return sb.toString()
    }
}