@Grab('org.apache.commons:commons-csv:1.2')
@Grab("commons-io:commons-io:2.4")
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
import java.nio.file.Paths
import org.apache.commons.io.FileUtils

// processing resultFile
def resultFile = args.length > 0 ? args[0] : 'build/resources-3.5.3082_results.csv'
def resultMap = retrieveResult(resultFile)
println "result file ${resultFile} processed"

// generating report
println "start generating report"
def reportFile = createReport(resultFile, resultMap)
println "report ${reportFile} generated"

// copying static artefacts
def buildDir = extractBaseDirFromFilename(resultFile)
println "copy static artefacts from 'src' to '${buildDir}'"
copyStaticArtefacts("src", buildDir)
println "static artefacts copied"

def retrieveResult(String resultFile) {
    def result = [js:[:],css:[:]]

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
            createResultStructureForAsset(result, recordMap)
            
            if (! result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion].artefacts[recordMap.artefactTitel]) {
                result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion].artefacts[recordMap.artefactTitel] = [:] as TreeMap
            }
            def node = result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion].artefacts[recordMap.artefactTitel]
            if (! node[recordMap.filename]) {
                node[recordMap.filename] = createResultEntry(recordMap)
            }
            addMetric(node[recordMap.filename], recordMap)
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
        "public":"1. public artefacts",
        "private":"2. private artefacts",
        "inputFiles":"3. input files"
    ]

    String[] fileNameParts = e.filename.split("_")
    e.artefactType = fileNameParts[0].trim()
    e.isPrivate = ('private' == e.artefactType)
    e.isPublic = ('public' == e.artefactType) 
    e.isThirdparty = ('thirdparty' == e.artefactType) 
    e.isOutputFile = e.isPrivate || e.isPublic || e.isThirdparty
    e.isInputFile = !e.isOutputFile
    if (e.isInputFile) { e.artefactType = "inputFiles"}
    e.isNonCritical = (e.isPrivate || e.isPublic) && (fileNameParts.length > 2 ) && ('non-critical' == fileNameParts[2])
    e.pageType = (e.isPrivate || e.isPublic) ? fileNameParts[1] : ""
    e.artefactTitel = artefactTypes[e.artefactType]
}

def createResultStructureForAsset(def result, def recordMap) {
    if (! result[recordMap.assetType]) {
        result[recordMap.assetType] = [:] as TreeMap
    }
    if (! result[recordMap.assetType][recordMap.assetVertical]) {
        result[recordMap.assetType][recordMap.assetVertical] = [:] as TreeMap
    }
    if (! result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion]) {
        result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion] = [metrics:[count:0], artefacts:[:] as TreeMap]
    }
    def resultNode = result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion]
    if (recordMap.assetVertical == "all") {
        createResultStructureForVertical(resultNode, recordMap)
    }
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

def createReport(def resultFile, def result) {
    def assetArtefact = resultFile - "_results.csv"
    def reportName = "${assetArtefact}_report.html"
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
                        a(class:"navbar-brand", href:"#", "Asset Metrics Report")
                    }
                    ul(class:"nav navbar-nav") {
                        result.js.each() { assetVertical, resultVerticalNode ->
                            li(class:"dropdown") {
                                a(class:"dropdown-toggle", "data-toggle":"dropdown", href:"#", "${assetVertical}") {
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
                result.each() { assetType, resultTypeNode ->
                    div(class:"container") {
                        h2 "${assetType}"
                        resultTypeNode.each() { assetVertical, resultVerticalNode ->
                            h3(id:"${assetVertical}_${assetType}", style:"padding-top:55px;", "${assetVertical} (${assetType})")
                            ul(class:"nav nav-pills") {
                                resultVerticalNode.each() { assetVersion, resultVersionNode ->
                                    li(class:(assetVersion=="latest" ? "active" : "")) { 
                                        a("data-toggle":"pill", href:"#${assetVertical}_${assetVersion}", "${assetVersion}") 
                                    }
                                }
                            }
                            div(class:"tab-content") {
                                resultVerticalNode.each() { assetVersion, resultVersionNode ->
                                    div(id:"${assetVertical}_${assetVersion}", class:(assetVersion=="latest" ? "tab-pane fade in active" : "tab-pane fade")) {
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
                                                                td("${outputFileNameNode.metrics.loc}")
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
    FileUtils.copyDirectory(new File("$fromDir/js"), new File("$toDir/js"))
    FileUtils.copyDirectory(new File("$fromDir/css"), new File("$toDir/css"))
    FileUtils.copyDirectory(new File("$fromDir/fonts"), new File("$toDir/fonts"))
}