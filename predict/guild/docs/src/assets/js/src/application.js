// NOTICE!! DO NOT USE ANY OF THIS JAVASCRIPT
// IT'S ALL JUST JUNK FOR OUR DOCS!
// ++++++++++++++++++++++++++++++++++++++++++

/*!
 * JavaScript for Bootstrap's docs (https://getbootstrap.com)
 * Copyright 2011-2016 The Bootstrap Authors
 * Copyright 2011-2016 Twitter, Inc.
 * Licensed under the Creative Commons Attribution 3.0 Unported License. For
 * details, see https://creativecommons.org/licenses/by/3.0/.
 */

/* global Clipboard, anchors */

!function ($) {
    'use strict';

    $(function () {

        // Insert highlighter language class in parent to control
        // background color
        $('code').each(function() {
            var el = $(this);
            el.closest('figure').addClass(el.attr('class'));
        });


        $('[data-toggle="popover"]').popover();

        // Disable empty links in docs examples
        $('.bd-example [href="#"]').click(function (e) {
            e.preventDefault();
        });

        // Add Copy button to with-copy elements
        $('.with-copy').each(function () {
            var btnHtml = '<div class="bd-clipboard"><span class="btn-clipboard" title="Copy to clipboard">Copy</span></div>';
            $(this).before(btnHtml);
            $('.btn-clipboard').tooltip();
        });

        // Clipboard support
        var clipboard = new Clipboard('.btn-clipboard', {
            target: function (trigger) {
                return trigger.parentNode.nextElementSibling;
            }
        });

        clipboard.on('success', function (e) {
            $(e.trigger)
                .attr('title', 'Copied!')
                .tooltip('_fixTitle')
                .tooltip('show')
                .attr('title', 'Copy to clipboard')
                .tooltip('_fixTitle');
            e.clearSelection();
        });

        clipboard.on('error', function (e) {
            var modifierKey = /Mac/i.test(navigator.userAgent) ? '\u2318' : 'Ctrl-';
            var fallbackMsg = 'Press ' + modifierKey + 'C to copy';
            $(e.trigger)
                .attr('title', fallbackMsg)
                .tooltip('_fixTitle')
                .tooltip('show')
                .attr('title', 'Copy to clipboard')
                .tooltip('_fixTitle');
        });

        var positionCallouts = function() {
            $(".popover-callout").each(function(_, callout) {
                var container = $(callout).parent(".callout-container");
                if (container) {
                    var containerHeight = container.height();
                    var containerWidth = container.width();
                    var topRatio = parseFloat(callout.getAttribute("data-top"));
                    var leftRatio = parseFloat(callout.getAttribute("data-left"));
                    callout.style.top = (topRatio * containerHeight) + "px";
                    callout.style.left = (leftRatio * containerWidth) + "px";
                }
            });
        };

        $(window).resize(function(e) {
            positionCallouts();
        });
        positionCallouts();
    });

}(jQuery);
