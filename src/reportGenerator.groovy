@Grab('org.apache.commons:commons-csv:1.2')
@Grab("commons-io:commons-io:2.4")
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import groovy.json.JsonOutput

// processing resultFile
def resultFileName = args.length > 0 ? args[0] : 'build/resources-3.5.3082_results.csv'
String assetArtefact = resultFileName - "_results.csv"
String htmlReportFileName = args.length > 1 ? args[1] : "${assetArtefact}_report.html"
String jsonOutputFileName = args.length > 1 ? args[1] - "html" + "json" : "${assetArtefact}_report.json"
def resultMap = retrieveResult(resultFileName)
println "result file ${resultFileName} processed"

// generating html report
println "start generating html report"
def htmlReportFile = createHtmlReport(resultFileName, resultMap, assetArtefact, htmlReportFileName)
println "report ${htmlReportFile} generated"

// copying static artefacts
def buildDir = extractBaseDirFromFilename(htmlReportFileName)
println "copy static artefacts from 'src' to '${buildDir}'"
copyStaticArtefacts("src", buildDir)
println "static artefacts copied"

// generate json output
println "start generating json output in ${jsonOutputFileName}"
def jsonOutputFile = createJsonOutput(resultFileName, resultMap, assetArtefact, jsonOutputFileName)
println "json output ${jsonOutputFile} generated"

def retrieveResult(String resultFile) {
    def result = [:]

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
            def assetVersionNode = createResultStructureForAsset(result, recordMap)
            
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
    String[] fileNameParts = e.filename.split("_")
    e.artefactGroup = fileNameParts[0].trim()
    e.artefactType = ['private','public'].contains(e.artefactGroup) ? 'outputFile' : 'inputFiles'
    e.artefactTitel = artefactTypes[e.artefactType]
}

def createResultStructureForAsset(def result, def recordMap) {
    if (! result[recordMap.assetVertical]) {
        result[recordMap.assetVertical] = [:] as TreeMap
    }
    if (! result[recordMap.assetVertical][recordMap.assetType]) {
        result[recordMap.assetVertical][recordMap.assetType] = [:] as TreeMap
    }
    if (! result[recordMap.assetVertical][recordMap.assetType][recordMap.assetVersion]) {
        result[recordMap.assetVertical][recordMap.assetType][recordMap.assetVersion] = [artefacts:[:] as TreeMap]
    }
    def resultNode = result[recordMap.assetVertical][recordMap.assetType][recordMap.assetVersion]
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
    resultEntry.metrics = [:]
    resultEntry.assetVertical = recordMap.assetVertical
    return resultEntry
}

def addMetric(def node, def recordMap) {
    node.metrics[recordMap.metric.trim()] = recordMap.count
}

def createHtmlReport(def resultFile, def result, def assetArtefact, def reportName) {
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
                    }
                    ul(class:"nav navbar-nav") {
                        result.sort {a,b -> 
                                a.key == "all" ? -1 : a.key<=>b.key
                            }.each() { assetVertical, resultVerticalNode ->
                            li(class:"dropdown") {
                                a(class:"dropdown-toggle", "data-toggle":"dropdown", href:"#", "${assetVertical == 'all' ? 'public' : assetVertical}") {
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
            div(class:"container",style:"margin-top:50px;") {
                h1 "Asset Metrics Report for ${assetArtefact}.tar"
                br()
                result.sort {a,b -> 
                        a.key == "all" ? -1 : a.key<=>b.key
                    }.each() { assetVertical, resultVerticalNode ->
                        div(class:"container") {
                        h2(id:"${assetVertical}_css", "${assetVertical == 'all' ? 'public all' : 'private ' + assetVertical}")
                        resultVerticalNode.each() { assetType, resultTypeNode ->
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
                                resultTypeNode.eachWithIndex { assetVersion, resultVersionNode, index ->
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
                                                                td('class':'alignLeft','data-toggle':'tooltip',title:"${outputFileName}","${outputFileName.size() > 43 ? outputFileName.take(40)+'...' : outputFileName}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics.loc as Integer)}")
                                                                td("${sprintf('%,d',outputFileNameNode.metrics.bytes as Integer)}")
                                                                if (assetType == "js") {
                                                                    td("${outputFileNameNode.metrics.evalCount}")
                                                                    td("${outputFileNameNode.metrics.newCount}")
                                                                    td("${outputFileNameNode.metrics.withCount}")
                                                                    td("${outputFileNameNode.metrics.jQueryLocatorCalls}")
                                                                    td("${outputFileNameNode.metrics.jQueryFunctionCalls}")
                                                                    td("${outputFileNameNode.metrics.documentWriteCount}")
                                                                    td("${outputFileNameNode.metrics.forInCount}")
                                                                    td("${outputFileNameNode.metrics.returnNullCount}")
                                                                }
                                                                if (assetType == "css") {
                                                                    //td("${outputFileNameNode.metrics}")
                                                                    td("${outputFileNameNode.metrics.cssWarnings}")
                                                                    td("${outputFileNameNode.metrics.cssErrors}")
                                                                    td("${outputFileNameNode.metrics.mediaQueryCount}")
                                                                    td("${outputFileNameNode.metrics.breakpointMCount}")
                                                                    td("${outputFileNameNode.metrics.breakpointLCount}")
                                                                    td("${outputFileNameNode.metrics.breakpointXLCount}")
                                                                    td("${outputFileNameNode.metrics.mediaQueryBytes}")
                                                                    td("${outputFileNameNode.metrics.breakpointMBytes}")
                                                                    td("${outputFileNameNode.metrics.breakpointLBytes}")
                                                                    td("${outputFileNameNode.metrics.breakpointXLBytes}")
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

def createJsonOutput(def resultFileName, def result, def assetArtefact, def jsonOutputFileName) {
    new File(jsonOutputFileName).withWriter { writer ->
        String jsonString = JsonOutput.toJson(result)
        String prettyJsonString = JsonOutput.prettyPrint(jsonString)
        writer.write(prettyJsonString)
    }
    return jsonOutputFileName
}
