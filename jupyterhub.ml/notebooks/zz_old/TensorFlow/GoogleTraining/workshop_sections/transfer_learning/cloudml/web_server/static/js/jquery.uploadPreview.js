// The MIT License (MIT)

// Copyright (c) 2013 Opoloo GbR

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

// from: http://opoloo.github.io/jquery_upload_preview/

(function ($) {
    $.extend({
	    uploadPreview : function (options) {

		// Options + Defaults
		var settings = $.extend({
			input_field: ".image-input",
			preview_box: ".image-preview",
			label_field: ".image-label",
			label_default: "Choose File",
			label_selected: "Change File",
			no_label: false
		    }, options);

		// Check if FileReader is available
		if (window.File && window.FileList && window.FileReader) {
		    if (typeof($(settings.input_field)) !== 'undefined' && $(settings.input_field) !== null) {
			$(settings.input_field).change(function() {
				var files = event.target.files;

				if (files.length > 0) {
				    var file = files[0];
				    var reader = new FileReader();

				    // Load file
				    reader.addEventListener("load",function(event) {
					    var loadedFile = event.target;

					    // Check format
					    if (file.type.match('image')) {
						// Image
						$(settings.preview_box).css("background-image", "url("
							+ loadedFile.result + ")");
						$(settings.preview_box).css("background-size", "cover");
						$(settings.preview_box).css("background-position", "center center");
					    } else if (file.type.match('audio')) {
						// Audio
						$(settings.preview_box).html("<audio controls><source src='"
							+ loadedFile.result	+ "' type='" + file.type
							+ "' />Your browser does not support the audio element.</audio>");
					    } else {
						alert("This file type is not supported yet.");
					    }
					});

				    if (settings.no_label == false) {
					// Change label
					$(settings.label_field).html(settings.label_selected);
				    }

				    // Read the file
				    reader.readAsDataURL(file);
				} else {
				    if (settings.no_label == false) {
					// Change label
					$(settings.label_field).html(settings.label_default);
				    }

				    // Clear background
				    $(settings.preview_box).css("background-image", "none");

				    // Remove Audio
				    $(settings.preview_box + " audio").remove();
				}
			    });
		    }
		} else {
		    alert("You need a browser with file reader support, to use this form properly.");
		    return false;
		}
	    }
	});
})(jQuery);
