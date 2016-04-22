# simplejslint
Simple JS Lint

Simple tool to check JavaScript (JS) from the command line (BASH).

Check following rules set:
- Bad JS Code
- JS Speed Brakes
- Simple Metrics for JS and CSS

Bad Code
- eval
- return null

JS Speed Brake
- $ (uses of JQuery)
- with
- new

Metric
- Lines of Code
- Bytes of Code


## Use
### Check JavaScript

### Check CSS
```
src/checkCSS.sh 20160420/
Fr 22 Apr 2016 15:35:56 CEST, "CSS Warnings Count",     1659, 20160420/
Fr 22 Apr 2016 15:35:56 CEST, "CSS Errors Count",       39, 20160420/
```
Count all CSS warnings and CSS errors that CSS Lint will produce. For a detailed CSS analysis you have to use CSS Lint directly.

## Rules


## Dependencies
- BASH
- grep
- wc
- [CSS Lint](https://github.com/CSSLint/csslint/wiki)
