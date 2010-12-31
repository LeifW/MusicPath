var _a, _b, _c, _d, edit, l, linkSelector, links, save;
jQuery.fn.originalVal = function(val) {
  return this.data("originalVal", val);
};
edit = function() {
  $(".editable a").click(function(e) {
    return e.preventDefault();
  });
  $(".editable [rel]").find("[href]:not(.blank), [src]:not(.blank), [resource]:not(.blank), [about]:not(.blank)").find("[property]").removeAttr("property");
  $(".editable [property]").each(function() {
    var datatype, property, text;
    text = $(this).text();
    property = $(this).attr("property");
    datatype = $(this).attr("datatype");
    return $(this).replaceWith($("<input/>").attr({
      value: text,
      size: text.length - 1,
      property: property,
      datatype: datatype
    }).originalVal(text).keypress(function() {
      var width;
      width = $(this).val().length - 2;
      return $(this).attr("size") < width ? $(this).attr("size", width) : null;
    }));
  });
  $(".editable [rel]:not([data-cardinality])").each(function() {
    var add_button, list;
    add_button = "<img src='webapp/images/add.png'/>";
    if ($(this).is(linkSelector)) {
      list = $(this).nextAll("[rel='" + $(this).attr("rel") + "']").andSelf();
      list.addClass("target");
      $(this).nextAll("[rel='" + $(this).attr("rel") + "']").andSelf().append("<img src='webapp/images/del.png'/>");
      return $(this).nextAll("[rel='" + $(this).attr("rel") + "']").andSelf().last().after(add_button);
    } else {
      $(this).children().addClass("target").append("<img src='webapp/images/del.png'/>");
      return $(this).append(add_button);
    }
  });
  return $("<button type='submit'>Save</button>").click(save).appendTo(".editable");
};
save = function() {
  return $(this).parent().find("[property]").each(function() {
    var subject, subjectElem;
    if ($(this).val() !== $(this).originalVal()) {
      subjectElem = $(this).closest(linkSelector);
      subject = subjectElem.attr(subjectElem.linkType());
      return console.log(subject + " " + $(this).attr("property") + " " + $(this).val());
    }
  });
};
links = ["href", "src", "resource", "about"];
linkSelector = (function() {
  _a = []; _c = links;
  for (_b = 0, _d = _c.length; _b < _d; _b++) {
    l = _c[_b];
    _a.push("[" + l + "]");
  }
  return _a;
})().join(", ");
jQuery.fn.linkType = function() {
  var self;
  self = this;
  return _.head(_(links).filter(function(l) {
    return self.is('[' + l + ']');
  }));
};