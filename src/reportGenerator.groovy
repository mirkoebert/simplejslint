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
        if (recordMap.isOutputFile) {
            //println "found outputFileMetric : $recordMap with assetType=$assetType, assetVertical=$assetVertical and outputFileType=$outputFileType"
            if (! result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion][recordMap.outputFileType]) {
                result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion][recordMap.outputFileType] = [:]
            }
            def node = result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion][recordMap.outputFileType]
            if (! node[recordMap.filename]) {
                node[recordMap.filename] = createResultEntry(recordMap)
            }
            addMetric(node[recordMap.filename], recordMap)
        } else {
            //println "found inputFile : $recordMap with fileNameParts[0]='${fileNameParts[0]}'"
            def node = result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion]
            if (! node.inputFiles[recordMap.filename]) {
                node.inputFiles[recordMap.filename] = createResultEntry(recordMap)
            } 
            node.metrics.count++
            addMetric(node.inputFiles[recordMap.filename], recordMap)
            /*
            if ("all" == recordMap.assetVertical) {
                String inputFileRegion = recordMap.outputFileType
                String vertical = recordMap.outputFileType.isNumber() ? "assets" : recordMap.outputFileType
            }
            */
        }
    }
    println result
    createReport(resultFile, result)
}

def computePathElements(def recordMap) {
    String[] assetBasePathParts = recordMap.basePath.split("/")
    recordMap.baseDir = assetBasePathParts[0]
    recordMap.assetVertical = assetBasePathParts[1]
    recordMap.assetType = assetBasePathParts[2]
    recordMap.assetVersion = assetBasePathParts.length > 3 ? assetBasePathParts[3] : ""
}

def computeFileNameElements(def e) {
    String[] fileNameParts = e.filename.split("_")
    e.outputFileType = fileNameParts[0].trim()
    e.isPrivate = ('private' == e.outputFileType)
    e.isPublic = ('public' == e.outputFileType) 
    e.isThirdparty = ('thirdparty' == e.outputFileType) 
    e.isOutputFile = e.isPrivate || e.isPublic || e.isThirdparty
    e.isInputFile = !e.isOutputFile
    e.isNonCritical = (e.isPrivate || e.isPublic) && (fileNameParts.length > 2 ) && ('non-critical' == fileNameParts[2])
    e.pageType = (e.isPrivate || e.isPublic) ? fileNameParts[1] : ""
}

def createResultStructureForAsset(def result, def recordMap) {
    if (! result[recordMap.assetType]) {
        result[recordMap.assetType] = [:]
    }
    if (! result[recordMap.assetType][recordMap.assetVertical]) {
        result[recordMap.assetType][recordMap.assetVertical] = [:]
    }
    if (! result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion]) {
        result[recordMap.assetType][recordMap.assetVertical][recordMap.assetVersion] = [metrics:[count:0],inputFiles:[:]]
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
        resultNode.verticals[recordMap.vertical] = [metrics:[count:0],inputFiles:[:]]
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
    def writer = new FileWriter('report.html')
    def html   = new groovy.xml.MarkupBuilder(writer)
    //def helper = new groovy.xml.MarkupBuilderHelper(html)
    html.mkp.yieldUnescaped """<!DOCTYPE html>
    """
    html.html(lang:"en") {
        head {
            meta(charset:"utf-8")
            meta("http-equiv":"X-UA-Compatible", content:"IE=edge")
            meta(name:"viewport", content:"width=device-width, initial-scale=1")
            title 'Asset Metrics Report'
            link(href:"src/css/bootstrap.min.css", rel:"stylesheet")
        }
        body(class:"container-fluid") {
            h1 "Asset Metrics Report for ${resultFile}"
            br()
            result.each() { assetType, resultTypeNode ->
                h2 "${assetType}"
                resultTypeNode.each() { assetVertical, resultVerticalNode ->
                    resultVerticalNode.each() { assetVersion, resultVersionNode ->
                        h3 "${assetVertical} - version:${assetVersion}" 
                        resultVersionNode.each() { outputFileType, resultOutputFileTypeNode ->
                            if (outputFileType != "metrics") {
                                h4 "${outputFileType}"
                                table(class:"table table-striped table-bordered table-hover") {
                                    thead {
                                        tr(class:"active") {
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
                                        resultOutputFileTypeNode.each() { outputFileName, outputFileNameNode ->
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
                                                td "${outputFileNameNode.metrics['document.write']}"
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
            mkp.comment("jQuery (necessary for Bootstrap's JavaScript plugins)")
            script(src:"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js")
            mkp.comment("Include all compiled plugins (below), or include individual files as needed")
            script(src:"src/js/bootstrap.min.js")
 
        }
    }
}