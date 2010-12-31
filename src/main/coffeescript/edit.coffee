# Convenience wrapper for saving the original value using $.fn.data()
jQuery.fn.originalVal = (val)-> this.data("originalVal", val)

edit = ->
  $(".editable a").click (e) -> e.preventDefault()

  $(".editable [rel]").find("[href]:not(.blank), [src]:not(.blank), [resource]:not(.blank), [about]:not(.blank)").find("[property]").removeAttr("property")
  $(".editable [property]").each ->
    text = $(this).text()
    property = $(this).attr "property"
    datatype = $(this).attr "datatype"
    $(this).replaceWith $("<input/>")
      .attr({value: text, size: text.length - 1, property: property, datatype: datatype})
      .originalVal(text)
      .keypress(->    # Auto-expand the input box as they type.
        width = $(this).val().length - 2
        if $(this).attr("size") < width
          $(this).attr("size", width)
      )
  #split this up into two selects
  # only the last on the list gets an add button
  # Add some buttons
  $(".editable [rel]:not([data-cardinality])").each ->
    #$(this).children().append("<a href='javascript:remove'>remove</a>")
    # If there's not a containing element for the list of linked items, put the add button at the end of the list, instead of as the last
    #$(this)[if $(this).is(linkSelector) then "after" else "append"]("<img src='webapp/images/add.png'/>")
    # if this is linkSelector:
    # else childrenUntil linkSelector
    # find out which link type we have
    # nextAll(that link type).last().after(<add button/>)
    #$(this).childrenUntil(linkSelector) nextAll(that link type).last().after(<add button/>)
    # Include the current element in the possible 'rel' targets.
    #target = if $(this).is(linkSelector) then $(this) else $(this).children.first()
    add_button = "<img src='webapp/images/add.png'/>"
    #target = if $(this).is(linkSelector) then $(this) else $(this).find(linkSelector).first()
    if $(this).is(linkSelector)
      # The add button goes at the end of the list of these kind of rels.
      list = $(this).nextAll("[rel='"+$(this).attr("rel")+"']").andSelf()
      list.addClass("target")
      $(this).nextAll("[rel='"+$(this).attr("rel")+"']").andSelf().append("<img src='webapp/images/del.png'/>")
      $(this).nextAll("[rel='"+$(this).attr("rel")+"']").andSelf().last().after(add_button)
    else
      # Put it at the bottom of this container
      $(this).children().addClass("target").append("<img src='webapp/images/del.png'/>")
      $(this).append(add_button)
    #console.log(target[0])
    #target.nextAll('['+target.linkType()+']').andSelf().last().after("<img src='webapp/images/add.png'/>")
    #target.nextAll().andSelf().last().after("<img src='webapp/images/add.png'/>")
    #while (!type) type = 

  $("<button type='submit'>Save</button>")
    .click(save)
    .appendTo(".editable")
    
save = ->
  $(this).parent().find("[property]").each(->
    if $(this).val() != $(this).originalVal()
      subjectElem = $(this).closest(linkSelector)
      #subject = _.head _.compact(subjectElem.attr(link) for link in links)
      subject = subjectElem.attr(subjectElem.linkType())
      console.log(
       subject + " " +  $(this).attr("property") + " " + $(this).val()
      )
  )

links = ["href", "src", "resource", "about"]
linkSelector = ("["+l+"]" for l in links).join(", ")
jQuery.fn.linkType = ->
  self = this
  _.head _(links).filter((l) -> self.is('['+l+']'))
