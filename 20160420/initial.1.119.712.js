/* global o_global, o_cr, o_util, version */
/*eslint-disable no-var, prefer-template, prefer-arrow-callback*/

var stylesheet = document.createElement('link'),
    productId = document.getElementById('cr_js_topReviews').getAttribute('data-product-id'),
    topReviews = o_util.dom.getElementsByClassname(document.getElementById('cr_js_topReviews'), 'cr_js_reviewList__review--top'),
    i;

stylesheet.setAttribute('rel', 'stylesheet');
stylesheet.setAttribute('type', 'text/css');
stylesheet.setAttribute('href', '/product-customerreview/css/product-customerreview' + version + '.min.css');
document.getElementsByTagName('head')[0].appendChild(stylesheet);

// Check whether user has already rating for the top reviews
for (i = 0; i < topReviews.length; i++) {
  var reviewId = topReviews[i].getAttribute('data-review-id'), //eslint-disable-line one-var, vars-on-top
      ratedReviewIds = window.localStorage.getItem('cr_RatedReviews_' + productId),
      hasBeenRated = !!ratedReviewIds && ratedReviewIds.indexOf(reviewId) >= 0;

  if (hasBeenRated) {
    var wrapper = o_global.helper.isIE8() ? topReviews[i].nextSibling : topReviews[i].nextElementSibling, //eslint-disable-line one-var, vars-on-top
        helpFulRating = o_util.dom.getElementsByClassname(wrapper, 'cr_helpfulRating');

    helpFulRating[0].innerHTML = 'Vielen Dank für Ihr Feedback!';
  }
}

o_global.eventLoader.onLoad(0, function () { //eslint-disable-line func-names
  'use strict';
  //TopReviews Breakpoint abhängig öffnen bzw. schließen
  if (o_global.breakpoint.getCurrentBreakpoint() === 's' || o_global.breakpoint.getCurrentBreakpoint() === 'm') {
    for (var x = 1; x < topReviews.length; x++) { //eslint-disable-line vars-on-top
      o_util.dom.removeClass(topReviews[x], 'p_accordion__header--open');
    }
  }

  o_util.ajax.getScript('/product-customerreview/js/product-customerreview' + version + '.min.js', true, function (xhr) { //eslint-disable-line func-names
    if (xhr.status === 200) {
      var customerReviewListWidget = new o_cr.widgets.ProductReviewListWidget(document.getElementById('cr_js_topReviews')); //eslint-disable-line vars-on-top
      customerReviewListWidget.init();
      document.getElementById('cr_js_topReviews').setAttribute('data-js-loaded', 'true');
    }
  });
});
