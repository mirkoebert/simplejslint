# simplejslint
Simple JS Lint and JS Preprocessor

## JS Lint
Simple BASH tool to check JavaScript (JS) from the command line (BASH). This simple tool helps to build an faster WWW by reducing JS and CSS.

Check following rules set:
- Bad JS Code
- JS Speed Brakes
- Simple Metrics for JS and CSS

Bad Code
- eval
- return null
- document.write

JS Speed Brake
- $ (uses of JQuery)
- with
- new

Metric
- Lines of Code
- Bytes of Code


### Use
#### Check JavaScript
Input: Diretory with JS files or one single JS file.
Output: Output is also written to an log file (CSV format).
```
./simplejslint.sh 20160420/private_product_critical_min.js 
Fr 22 Apr 2016 19:21:25 CEST, "Count eval",        1, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count $",      458, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count with",        3, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count new",      145, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count Pattern for\s+in",        0, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count Pattern return\s+null",       22, 20160420/private_product_critical_min.js
Fr 22 Apr 2016 19:21:25 CEST, "Count Bytes of Code",   270457, 20160420/private_product_critical_min.js
```


#### Check CSS
Input: Diretory with css files or one single css file.
Output: Count all CSS warnings and CSS errors that CSS Lint have produce. Output is also written to an log file (CSV format). For a detailed CSS analysis you have to use CSS Lint directly.
```
src/checkCSS.sh 20160420/
Fr 22 Apr 2016 15:35:56 CEST, "CSS Warnings Count",     1659, 20160420/
Fr 22 Apr 2016 15:35:56 CEST, "CSS Errors Count",       39, 20160420/
```

### Rules
- See [JavaScript Coding Standards and Best Practices](https://github.com/stevekwan/best-practices/blob/master/javascript/best-practices.md)
- [document.write ](http://www.stevesouders.com/blog/2012/04/10/dont-docwrite-scripts/)
- Google [AMP Speed up](https://www.ampproject.org/docs/get_started/technical_overview.html)

### Dependencies
- BASH
- grep
- wc
- [CSS Lint](https://github.com/CSSLint/csslint/wiki)i


## JS Preprocessor
Simple BASH tool to build different JS files from one JS source file. Best use for AB tests. Easy to integrate into build pipelines and easy to use for developers.

### Code Ingegration
```
//#ifdef Atest
aaa=1;
//#endif
```
Start block with name Atest with `//#ifdef Atest`. Close block with `//#endif`. Every ifdef need a closing endif.
Could also work with Java code but untestet.

### Use
```
./simplejspreprocessor.sh big_source.js Atest
```

### Dependencies
- BASH
- grep
- wc

