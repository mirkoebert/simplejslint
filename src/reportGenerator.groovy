@Grab('org.apache.commons:commons-csv:1.2')
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
import java.nio.file.Paths

def result = [js:[:],css:[:]]

def resultFile = args.length > 0 ? args[0] : 'resources-3.5.3082_results.csv'
Paths.get(resultFile).withReader { reader ->
    CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

    csv.iterator().each() { record ->
        def recordMap = record.toMap()
        if ("css" == recordMap.extension) return
        // Pfadbestandteile ermitteln
        computePathElements(recordMap)
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
    println "result file ${resultFile} processed"
    println "start generating report"
    def reportFile = createReport(resultFile, result)
    println "report ${reportFile} generated"
}

def computePathElements(def recordMap) {
    String[] assetBasePathParts = recordMap.basePath.split("/")
    recordMap.baseDir = assetBasePathParts[0]
    recordMap.assetVertical = assetBasePathParts[1]
    recordMap.assetType = assetBasePathParts[2]
    recordMap.assetVersion = assetBasePathParts.length > 3 ? assetBasePathParts[3] : ""
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
    html.html(lang:"en") {
        head {
            meta(charset:"utf-8")
            meta("http-equiv":"X-UA-Compatible", content:"IE=edge")
            meta(name:"viewport", content:"width=device-width, initial-scale=1")
            title 'Asset Metrics Report'
            link(href:"src/css/bootstrap.min.css", rel:"stylesheet")
        }
        body(class:"container-fluid") {
            h1 "Asset Metrics Report for ${assetArtefact}.tar"
            br()
            result.each() { assetType, resultTypeNode ->
                div(class:"container") {
                    h2 "${assetType}"
                    resultTypeNode.each() { assetVertical, resultVerticalNode ->
                        h3 "${assetVertical}"
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
                                            table(class:"table table-striped table-bordered table-hover") {
                                                thead {
                                                    tr(class:"info") {
                                                        //th "node"
                                                        //th "metrics"
                                                        th "artefact"
                                                        th "LoC"
                                                        th "Size"
                                                        th "Count eval"
                                                        th "Count new"
                                                        th "Count with"
                                                        th "jQuery Calls \$("
                                                        th "jQuery Function Calls \$."
                                                        th "document.write"
                                                        th "Count Pattern for\\s+in"
                                                        th "Count Pattern return\\s+null"
                                                    }
                                                }
                                                tbody {
                                                    artefactsNode.each() { outputFileName, outputFileNameNode ->
                                                        tr {
                                                            //td "${outputFileNameNode}"
                                                            //td "${outputFileNameNode.metrics}"
                                                            td "${outputFileName}"
                                                            td "${outputFileNameNode.metrics['Count Lines of Code']}"
                                                            td "${outputFileNameNode.metrics['Count Bytes of Code']}"
                                                            td "${outputFileNameNode.metrics['Count eval']}"
                                                            td "${outputFileNameNode.metrics['Count new']}"
                                                            td "${outputFileNameNode.metrics['Count with']}"
                                                            td "${outputFileNameNode.metrics['Count $(']}"
                                                            td "${outputFileNameNode.metrics['Count $.']}"
                                                            td "${outputFileNameNode.metrics['Count document.write']}"
                                                            td "${outputFileNameNode.metrics['Count Pattern for\\s+in']}"
                                                            td "${outputFileNameNode.metrics['Count Pattern return\\s+null']}"
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
            script(src:"https://ajax.googleapis.com/ajax/libs/jquery/2.0.2/jquery.min.js") { 
                mkp.comment("jQuery (necessary for Bootstrap's JavaScript plugins)")
            }
            script(src:"src/js/bootstrap.min.js") { 
                mkp.comment("Include all compiled plugins (below), or include individual files as needed")
            }
 
        }
    }
    return reportName
}