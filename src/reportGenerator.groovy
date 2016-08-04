@Grab('org.apache.commons:commons-csv:1.2')
@Grab('commons-cli:commons-cli:1.3.1')
@Grab("commons-io:commons-io:2.4")
@Grab('io.dropwizard.metrics:metrics-core:3.1.0')
@Grab('io.dropwizard.metrics:metrics-graphite:3.1.0')

import org.apache.commons.cli.Option
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
import org.apache.commons.io.FileUtils

import java.nio.file.Paths
import groovy.json.JsonOutput
import com.codahale.metrics.graphite.Graphite

// processing parameters
def cli = new CliBuilder(
    usage: 'groovy reportGenerator [options] -- <inputFiles>',
    header: '\nAvailable options (use -h for help):\n',
    footer: '\nUse with care'
)
cli.with {
    a(longOpt:'assetArtefact', 'asset artefact name (resources-3.5.3082)', args:1, optionalArg:true, required:false)
    e(longOpt:'environment', 'assets target environment name', args:1, required:false)
    t(longOpt:'timestamp', 'asset artefact timestamp', args:1, required:false)
    r(longOpt:'report', 'html report file name', args:1, optionalArg:true, required:false)
    j(longOpt:'json', 'json output file name', args:1, optionalArg:true, required:false)
    m(longOpt:'metrics', 'generate graphite metrics', args:0, required:false)
    _(longOpt:'input', 'input csv files with asset metrics', args:Option.UNLIMITED_VALUES, required:true)
}
def opt = cli.parse(args)
if (!opt) return
if (opt.h) cli.usage()

def resultFileNames = opt.inputs ? opt.inputs - "--" : ['build/resources-3.5.3082_results.csv']
String assetArtefact = opt.a ?: resultFileName[0] - "_results.csv"
String htmlReportFileName = opt.r ?: "${assetArtefact}_report.html"
String jsonOutputFileName = opt.j ?: htmlReportFileName - "html" + "json"
String environment = opt.e ?: "unknown"
long timestamp = opt.t ?: ((long) (new Date()).time / 1000)
println "resultFileNames = ${resultFileNames}"
println "assetArtefact = ${assetArtefact}"
println "htmlReportFileName = ${htmlReportFileName}"
println "jsonOutputFileName = ${jsonOutputFileName}"
println "environment = ${environment}"
println "timestamp=$timestamp"

// processing resultFile
def resultMap = ["environment":environment,"assetArchive":assetArtefact, "timestamp":timestamp, "assets":[:]]
resultFileNames.each() { resultFileName ->
    println "next result file : ${resultFileName}"
    resultMap = retrieveResult(resultFileName, assetArtefact, environment, timestamp, resultMap)
    println "result file ${resultFileName} processed"
}

if (opt.report) {
    // generating html report
    println "start generating html report"
    def htmlReportFile = createHtmlReport(resultMap, htmlReportFileName)
    println "report ${htmlReportFile} generated"

    // copying static artefacts
    def buildDir = extractBaseDirFromFilename(htmlReportFileName)
    println "copy static artefacts from 'src' to '${buildDir}'"
    copyStaticArtefacts("src", buildDir)
    println "static artefacts copied"
}

if (opt.json) {
    // generate json output
    println "start generating json output in ${jsonOutputFileName}"
    def jsonOutputFile = createJsonOutput(resultMap, jsonOutputFileName)
    println "json output ${jsonOutputFile} generated"
}

if (opt.metrics) {
    // send metrics to graphite
    sendMetrics2Graphite(resultMap)
    println "sending graphite metrics completed"
}

def retrieveResult(String resultFile, String assetArtefact, String environment, long timestamp, Map result) {
    Paths.get(resultFile).withReader { reader ->
        CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

        csv.iterator().each() { record ->
            def recordMap = record.toMap()
            // Pfadbestandteile ermitteln
            computePathElements(recordMap)
            if (recordMap.assetVertical=="fonts") { 
                // do not handle font css files
                return 
            }
            if (recordMap.assetVersion ==~ /[0-9a-f]{16}/) {
                //println "handle productive version ${recordMap.assetVersion}"
            } else { 
                // all productive artefacts have a hashcode String with 16 characters as version 
                //println "do not handle development and test only versions: ${recordMap.assetVersion}"
                return 
            } 

            // Dateinamensbestandteile ermitteln und verarbeiten
            computeFileNameElements(recordMap)
            // create structure within result for new asset
            def assetVersionNode = createResultStructureForAsset(result["assets"], recordMap)
            
            if (! assetVersionNode.artefacts[recordMap.artefactTitel]) {
                assetVersionNode.artefacts[recordMap.artefactTitel] = [:] as TreeMap
                if (recordMap.artefactTitel == "1. output artefact") {
                    assetVersionNode.outputArtefactName = recordMap.filename
                }
            }
            def artefactTitleNode = assetVersionNode.artefacts[recordMap.artefactTitel]
            if (! artefactTitleNode[recordMap.filename]) {
                artefactTitleNode[recordMap.filename] = createResultEntry(recordMap)
            }
            addMetric(artefactTitleNode[recordMap.filename], recordMap)
        }
    }
    return result
}

def computePathElements(def recordMap) {
    String[] assetBasePathParts = recordMap.basePath.split("/")
    recordMap.baseDir = assetBasePathParts.length > 0 ? assetBasePathParts[0] : "unknownBaseDir"
    recordMap.assetVertical = assetBasePathParts.length > 1 ? assetBasePathParts[1] : "unknownAssetVertical"
    recordMap.assetType = assetBasePathParts.length > 2 ? assetBasePathParts[2] : "unknownAssetType"
    recordMap.assetVersion = assetBasePathParts.length > 3 ? assetBasePathParts[3] : "unknownAssetVersion"
}

def computeFileNameElements(def e) {
    def artefactTypes = [
        "outputFile":"1. output artefact",
        "inputFiles":"2. input files"
    ]
    String fileNameExtension = ""
    String fileNameBase = e.filename
    if (e.filename.contains(".")) {
        def parts = e.filename.split("[.]") as List
        fileNameExtension = parts.last()
        parts.pop()
        fileNameBase = parts.join(".")
    }
    //println "filename=${e.filename} : fileNameBase=$fileNameBase , fileNameExtension=$fileNameExtension"
    e.shortenedFileName = fileNameBase.size() > 35 ? fileNameBase.take(30)+'...' : fileNameBase
    String[] fileNameParts = fileNameBase.split("_")
    e.artefactGroup = fileNameParts[0].trim()
    //e.artefactType = ['private','public'].contains(e.artefactGroup) ? 'outputFile' : 'inputFiles'
    String lastPart = fileNameParts.last()
    e.artefactType = lastPart == 'min' ? 'outputFile' : 'inputFiles'
    e.artefactTitel = artefactTypes[e.artefactType]
}

def createResultStructureForAsset(def resultMap, def recordMap) {
    String vertical = recordMap.assetVertical
    if (! resultMap[vertical]) {
        resultMap[vertical] = [
            "assetVertical": vertical,
            "shortenedVerticalName": vertical.size() > 12 ? vertical.take(10)+'..' : vertical,
            "assets" : [:] as TreeMap
        ]
    }
    if (! resultMap[vertical]["assets"][recordMap.assetType]) {
        resultMap[vertical]["assets"][recordMap.assetType] = [:] as TreeMap
    }
    if (! resultMap[vertical]["assets"][recordMap.assetType][recordMap.assetVersion]) {
        resultMap[vertical]["assets"][recordMap.assetType][recordMap.assetVersion] = [artefacts:[:] as TreeMap]
    }
    def resultNode = resultMap[recordMap.assetVertical]["assets"][recordMap.assetType][recordMap.assetVersion]
    return resultNode
}

def createResultStructureForVertical(def resultNode, def recordMap) {
    if (!resultNode.verticals) {
        resultNode.verticals = [:]
    }
    if (!resultNode.verticals[recordMap.vertical]) {
        resultNode.verticals[recordMap.vertical] = [ metrics:[count:0]]//,inputFiles:[:] as TreeMap ]
    }
}

def createResultEntry(def recordMap) {
    resultEntry = [:]
    resultEntry.asset = recordMap.asset
    resultEntry.filename = recordMap.filename
    resultEntry.shortenedFileName = recordMap.shortenedFileName
    resultEntry.metrics = [:]
    resultEntry.assetVertical = recordMap.assetVertical
    resultEntry.assetCategory = recordMap.filename.split('_')[0]
    return resultEntry
}

def addMetric(def node, def recordMap) {
    node.metrics[recordMap.metric.trim()] = recordMap.count
}

def createHtmlReport(def result, def reportName) {
    def assetArtefact = result["assetArchive"].split("/").last()
    def environment = result.environment
    def resultMap = result["assets"]
    def writer = new FileWriter(reportName)
    writer.println("<!DOCTYPE html>")
    def html   = new groovy.xml.MarkupBuilder(writer)
    //def helper = new groovy.xml.MarkupBuilderHelper(html)
    html.setExpandEmptyElements(true)
    html.html(lang:"en") {
        head {
            meta(charset:"utf-8")
            meta("http-equiv":"X-UA-Compatible", content:"IE=edge")
            meta(name:"viewport", content:"width=device-width, initial-scale=1")
            title 'Asset Metrics Report'
            link(href:"./css/bootstrap.min.css", rel:"stylesheet")
            link(href:"./css/assetMetricsReport.css", rel:"stylesheet")
        }
        body(class:"container-fluid") {
            nav(class:"navbar navbar-default navbar-fixed-top") {
                div(class:"container-fluid") {
                    div(class:"navbar-header") {
                        a(class:"navbar-brand", href:"#") {
                            img(src:"./images/scale_performance.png", height:"46")
                        }
                        span(class:"nav navbar-nav") {
                            div(class:"nav","${environment}")
                            div(class:"nav","${assetArtefact}")
                        } 
                    }
                    ul(class:"nav navbar-nav") {
                        resultMap.sort {a,b -> 
                                a.key == "all" ? -1 : a.key<=>b.key
                            }.each() { assetVertical, resultVerticalNode ->
                            li(class:"dropdown") {
                                a(class:"dropdown-toggle", "data-toggle":"dropdown", "data-hover":"tooltip", title:"${assetVertical}", href:"#", "${assetVertical == 'all' ? 'public' : resultVerticalNode.shortenedVerticalName}") {
                                    span(class:"caret")
                                } 
                                ul(class:"dropdown-menu") {
                                    li { a(href:"#${assetVertical}_js", "js") }
                                    li { a(href:"#${assetVertical}_css", "css") }
                                }
                            }
                        }
                    }
                }
            }
            div(id:"metricsReport", class:"container") {
                br(style:"margin-top:50px;")
                h1 "Asset Metrics Report for ${assetArtefact}.tar"
                span "Environment:${environment}"
                br()
                resultMap.sort {a,b -> 
                        a.key == "all" ? -1 : a.key<=>b.key
                    }.each() { assetVertical, resultVerticalNode ->
                        div(class:"container") {
                        h2(id:"${assetVertical}_css", "${assetVertical == 'all' ? 'public all' : 'private ' + assetVertical}")
                        resultVerticalNode.assets.each() { assetType, resultTypeNode ->
                            if (assetType == "js") {
                                div(id:"${assetVertical}_js", style:"padding-top: 50px;")
                            }
                            h3("${assetType}")
                            ul(class:"nav nav-pills") {
                                resultTypeNode.sort{ a,b ->
                                        a.value.outputArtefactName <=> b.value.outputArtefactName 
                                    }.eachWithIndex { assetVersion, resultVersionNode, index ->
                                    li(class:(index==0 ? "active" : "")) { 
                                        a("data-toggle":"pill", href:"#${assetVertical}_${assetVersion}", "${resultVersionNode.outputArtefactName}") 
                                    }
                                }
                            }
                            div(class:"tab-content") {
                                resultTypeNode.sort{ a,b ->
                                        a.value.outputArtefactName <=> b.value.outputArtefactName 
                                    }.eachWithIndex { assetVersion, resultVersionNode, index ->
                                    div(id:"${assetVertical}_${assetVersion}", class:(index==0 ? "tab-pane fade in active" : "tab-pane fade")) {
                                        resultVersionNode.artefacts.each() { artefactTitle, artefactsNode ->
                                            if (artefactTitle != "metrics") {
                                                h4 "${artefactTitle}"
                                                table(class:"table table-striped table-bordered table-hover sortable") {
                                                    thead {
                                                        tr(class:"info") {
                                                            th('class':'alignLeft', "artefact")
                                                            th {
                                                                mkp.yieldUnescaped("Lines&nbsp;of Code")
                                                            }
                                                            th("Size")
                                                            th("Min size")
                                                            th("Min gzip size")
                                                            if (assetType == "js") {
                                                                th("Count eval")
                                                                th("Count new")
                                                                th("Count with")
                                                                th("jQuery \$( LocatorCalls")
                                                                th("jQuery \$. FunctionCalls")
                                                                th("document.write")
                                                                th("Count for..in")
                                                                th {
                                                                    mkp.yieldUnescaped('Count return&nbsp;null')
                                                                }
                                                            }
                                                            if (assetType == "css") {
                                                                //th{"metrics"}
                                                                th("Warnings")
                                                                th("Errors")
                                                                th("Media Query rules")
                                                                th("Breakpunkt M rules")
                                                                th("Breakpunkt L rules")
                                                                th("Breakpunkt XL rules")
                                                                th("Media Query bytes")
                                                                th("Breakpunkt M bytes")
                                                                th("Breakpunkt L bytes")
                                                                th("Breakpunkt XL bytes")
                                                            }
                                                        }
                                                    }
                                                    tbody {
                                                        artefactsNode.each() { outputFileName, outputFileNameNode ->
                                                            tr {
                                                                //println outputFileName
                                                                td('class':'alignLeft','data-toggle':'tooltip',title:"${outputFileName}","${outputFileNameNode.shortenedFileName}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics?.loc as Integer)}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics?.bytes as Integer)}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics?.minBytes as Integer)}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics?.minBytesGzip as Integer)}")
                                                                if (assetType == "js") {
                                                                    td("${outputFileNameNode.metrics?.evalCount}")
                                                                    td("${outputFileNameNode.metrics?.newCount}")
                                                                    td("${outputFileNameNode.metrics?.withCount}")
                                                                    td("${outputFileNameNode.metrics?.jQueryLocatorCalls}")
                                                                    td("${outputFileNameNode.metrics?.jQueryFunctionCalls}")
                                                                    td("${outputFileNameNode.metrics?.documentWriteCount}")
                                                                    td("${outputFileNameNode.metrics?.forInCount}")
                                                                    td("${outputFileNameNode.metrics?.returnNullCount}")
                                                                }
                                                                if (assetType == "css") {
                                                                    //td("${outputFileNameNode.metrics}")
                                                                    td("${outputFileNameNode.metrics?.cssWarnings}")
                                                                    td("${outputFileNameNode.metrics?.cssErrors}")
                                                                    td("${outputFileNameNode.metrics?.mediaQueryCount}")
                                                                    td("${outputFileNameNode.metrics?.breakpointMCount}")
                                                                    td("${outputFileNameNode.metrics?.breakpointLCount}")
                                                                    td("${outputFileNameNode.metrics?.breakpointXLCount}")
                                                                    td("${sprintf('%,d',outputFileNameNode.metrics?.mediaQueryBytes as Integer)}")
                                                                    td("${sprintf('%,d',outputFileNameNode.metrics?.breakpointMBytes as Integer)}")
                                                                    td("${sprintf('%,d',outputFileNameNode.metrics?.breakpointLBytes as Integer)}")
                                                                    td("${sprintf('%,d',outputFileNameNode.metrics?.breakpointXLBytes as Integer)}")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            script(src:"./js/jquery.min.js") { 
                mkp.comment("jQuery (necessary for Bootstrap's JavaScript plugins)")
            }
            script(src:"./js/bootstrap.min.js") { 
                mkp.comment("Include all compiled plugins (below), or include individual files as needed")
            }
            script(src:"./js/tether.min.js") { 
                mkp.comment("Tether (necessary for Bootstrap tooltip plugin)")
            }
            script(src:"./js/sorttable.js") { 
                mkp.comment("Sorttable (http://www.kryogenix.org/code/browser/sorttable/)")
            }
        }
    }
    return reportName
}

def extractBaseDirFromFilename(String filename) {
    def pathElements = filename.split("/") as List
    if ( pathElements.size() <= 1 ) {
        return "."
    }
    pathElements.pop()
    return pathElements.join("/")
}

def copyStaticArtefacts(String fromDir, String toDir) {
    ["js","css","fonts","images"].each() { String subdir ->
        println "  $subdir"
        FileUtils.copyDirectory(new File("$fromDir/$subdir"), new File("$toDir/$subdir"))
    }
}

def createJsonOutput(def result, def jsonOutputFileName) {
    new File(jsonOutputFileName).withWriter { writer ->
        String jsonString = JsonOutput.toJson(result)
        String prettyJsonString = JsonOutput.prettyPrint(jsonString)
        writer.write(prettyJsonString)
    }
    return jsonOutputFileName
}

def sendMetrics2Graphite(def result) {
    def environment = result.environment
    def assetArtefact = result.assetArchive
    long timestamp = result.timestamp
    def resultMap = result.assets
    println "connecting to graphite"
    def graphite = connectToGraphite()
    println "start pushing metrics to graphite"
    resultMap.each() { assetVertical, resultVerticalNode ->
        // assetVertical: all (public) or aftersales | global-pattern | global-resources | order | ... (private)
        resultVerticalNode.assets.each() { assetType, resultTypeNode ->
            // assetType: js|css
            resultTypeNode.each() { assetVersion, resultVersionNode ->
                // assetVersion: 50f1d1150badf3d1 | ...
                resultVersionNode.artefacts["1. output artefact"].each() { outputFileName, outputFileNameNode ->
                    // outputFileName: private_aftersales_non-critical_min.js | ... 
                    String assetName = outputFileName.replace('.','_')
                    String assetCategory = outputFileNameNode.assetCategory
                    outputFileNameNode.metrics.each() { metricName, metricValue ->
                        graphite.send("verticals.scale.assets.${environment}.${assetCategory}.${assetVertical}.${assetType}.${assetName}.${metricName}", metricValue, timestamp)
                    }
                    def inputFileMap = resultVersionNode.artefacts["2. input files"] ?: [:]
                    graphite.send(
                        "verticals.scale.assets.${environment}.${assetCategory}.${assetVertical}.${assetType}.${assetName}.inputFiles.count", 
                        "${inputFileMap.size()}",
                        timestamp)
                }
            }
        }
    }
}

def connectToGraphite() {
    Graphite graphite = new Graphite("carbon-relay.otto.easynet.de", 2003)
//    Graphite graphite = new Graphite("localhost", 2003)
    graphite.connect()
    return graphite
}
