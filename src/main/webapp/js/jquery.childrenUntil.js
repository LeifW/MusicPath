/**

The MIT License

Copyright (c) 2010 Joshua Gitlin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
(function($) {
	$.fn.childrenUntil = function(selector) {
		// BFS until we find a tier with matched nodes
		var matches = [];
		if (selector && selector != '') {
			var queue = [];
			queue.push(this);
      if (this.is(selector)) matches.push(this[0]); // include the current element in the search, not just children
			while(matches.length == 0 && queue.length > 0) {
				var node = queue.shift();
				$.each(node.children(), function(i,e) {
					var child = $(e);
					queue.push(child);
					if (child.is(selector)) matches.push(e);
				});
			}
		}
		return $(matches);
	};
})(jQuery);
