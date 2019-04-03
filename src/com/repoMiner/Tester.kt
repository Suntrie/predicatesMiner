package com.repoMiner


fun main(args: Array<String>) {

    print("x")

    val daikonMiner = DaikonMiner()
    daikonMiner.generateDynCompFile(

        "com.company.Main",

        listOf("/home/suntrie/IdeaProjects/daikonTester/out/production/daikonTester"),
        listOf(),
        "/home/suntrie/IdeaProjects/predicatesMiner/tmp",
        "dyndynCompComp.decls-DynComp"
    )

    daikonMiner.generateTraceFile(
        "com.company.Main",
        listOf("/home/suntrie/IdeaProjects/daikonTester/out/production/daikonTester"),
        listOf(),
        "/home/suntrie/IdeaProjects/predicatesMiner/tmp/dyndynCompComp.decls-DynComp",
        "/home/suntrie/IdeaProjects/predicatesMiner/tmp",
        "tracerace.dtrace.gz")

    daikonMiner.generateInvariantsFile("tracerace.dtrace.gz", "/home/suntrie/IdeaProjects/predicatesMiner/tmp",
        "invariants.inv.gz", "/home/suntrie/IdeaProjects/predicatesMiner/tmp")

    /*daikonMiner.convertDaikonInvariantsToZ3Input("tracerace.dtrace.gz", "/home/suntrie/IdeaProjects/predicatesMiner/tmp"
        )*/
}