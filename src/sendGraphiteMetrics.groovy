@Grab('io.dropwizard.metrics:metrics-core:3.1.0')
@Grab('io.dropwizard.metrics:metrics-graphite:3.1.0')
@Grab('commons-cli:commons-cli:1.3.1')

import com.codahale.metrics.graphite.Graphite
import org.apache.commons.cli.Option

// processing parameters
def cli = new CliBuilder(
    usage: 'groovy sendGraphiteMetris [options] --metrics <metricsInputFile>',
    header: '\nAvailable options (use -h for help):\n',
    footer: '\nUse with care'
)
cli.with {
    s(longOpt:'server', 'Graphite Server name', args:1, required:false)
    p(longOpt:'port', 'Graphite Server port', args:1, required:false)
    t(longOpt:'timestamp', 'metrics reporting timestamp', args:1, required:false)
    m(longOpt:'metrics', 'graphite metrics files', args:Option.UNLIMITED_VALUES, required:true)
}
def opt = cli.parse(args)
if (!opt) return
if (opt.h) cli.usage()

def metricsInputFileNames = opt.ms - "--"
String graphiteServerName = opt.s ?: "localhost"
int graphiteServerPort = opt.p ?: 2003
long timestamp = opt.t ?: ((long) (new Date()).time / 1000)
println "metricsInputFileNames = ${metricsInputFileNames}"
println "graphiteServerName = ${graphiteServerName}"
println "graphiteServerPort = ${graphiteServerPort}"
println "timestamp=$timestamp"

println "connecting to graphite"
def graphite = connectToGraphite(graphiteServerName, graphiteServerPort)
metricsInputFileNames.each() { metricsInputFileName ->
    println "reading metrics input from ${metricsInputFileName}"
    String metricsInput = (new File(metricsInputFileName)).text
    println "start pushing metrics to graphite"
    int count = 0
    metricsInput.eachLine() { line ->
        String[] lineParts = line.split(" ")
        if (lineParts.size() < 2) {
            // to short line ==> drop it
            return
        }
        String metric = lineParts[0]
        String value = lineParts[1]
        graphite.send(metric, value, timestamp)
        count++
    }
    println "done pushing ${count} metrics to graphite"
}
println "sending graphite metrics finished"

def connectToGraphite(String graphiteServerName, int graphiteServerPort) {
    Graphite graphite = new Graphite(graphiteServerName, graphiteServerPort)
//    Graphite graphite = new Graphite("localhost", 2003)
    graphite.connect()
    return graphite
}
