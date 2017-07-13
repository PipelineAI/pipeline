module Filters

  def format_cmd_desc(input)
    input
      .gsub(/ ([A-Z]{2,})/, " `\\1`")
      .gsub(/(--[A-Za-z0-9\-]{2,})/, "`\\1`")
      .gsub(/'(.+?)'/, "`\\1`")
      .gsub(/!!\ *(.+?) {2,}(.+?)$/,
            "<div class=\"cmd-desc-item\">" \
            "<span class=\"cmd-desc-item-name\">\\1</span>" \
            "<span class=\"cmd-desc-item-val\">\\2</span></div>")
  end
end

Liquid::Template.register_filter(Filters)

class Note < Liquid::Block

  def initialize(tag_name, title, tokens)
    super
    title.strip!
    if title == ""
      @title = "Note"
    else
      @title = title
    end
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    output = converter.convert(super(context))
    "<div class=\"bd-callout bd-callout-note\"><h4>#{@title}</h4>#{output}</div>"
  end
end

Liquid::Template.register_tag('note', Note)

class Insight < Liquid::Block

  def initialize(tag_name, title, tokens)
    super
    title.strip!
    if title == ""
      @title = "Insight"
    else
      @title = title
    end
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    output = converter.convert(super(context))
    "<div class=\"bd-callout bd-callout-warning\"><h4>#{@title}</h4>#{output}</div>"
  end
end

Liquid::Template.register_tag('insight', Insight)

class Next < Liquid::Block

  def initialize(tag_name, link, tokens)
    super
    @link = link.strip
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    output = converter.convert(super(context))
    "<p class=\"next-link\">" \
    "<a href=\"#{@link}\" " \
    "class=\"btn btn-bs btn-outline\">#{super(context)} " \
    "<i class=\"fa fa-fw fa-caret-right\"></i></a></p>"
  end
end

Liquid::Template.register_tag('next', Next)

class Ref < Liquid::Tag

  def initialize(tag_name, target, tokens)
    super
    @target = target.strip
  end

  def render(context)
    case @target
    when "guild-project-file"
      "[Guild project file](/project-reference/guild-project-file/)"
    when "github-issues"
      "<a href=\"https://github.com/guildai/guild/issues\" " \
      "target=\"_blank\">Guild's GitHub issues list " \
      "<i class=\"ext-link-icon fa fa-external-link\"></i></a>"
    when "mnist-example"
      "<a href=\"https://github.com/guildai/guild-examples/tree/master/mnist\"" \
      ">MNIST example</a>"
    when "mnist-example-guild"
      "<a href=\"https://github.com/guildai/guild-examples/blob/master/mnist/Guild\"" \
      ">MNIST example Guild file</a>"
    else
      if @target.start_with?("cmd:")
        cmd = @target[4..-1]
        "[command reference](/command-reference/#{cmd}/)"
      else
        "!!! UNKNOWN REF #{@target} !!!"
      end
    end
  end
end

Liquid::Template.register_tag('ref', Ref)

class Link < Liquid::Block

  def initialize(tag_name, link, tokens)
    super
    @link = link.strip
  end

  def render(context)
    "<a href=\"#{@link}\" target=\"_blank\">#{super(context)} " \
    "<i class=\"ext-link-icon fa fa-external-link\"></i></a>"
  end
end

Liquid::Template.register_tag('link', Link)

class Term < Jekyll::Tags::HighlightBlock

  def initialize(tag_name, arg, tokens)
    if arg.strip == "long"
      super(tag_name, "commands-long", tokens)
    else
      super(tag_name, "commands", tokens)
    end
  end

end

Liquid::Template.register_tag('term', Term)

class Code < Jekyll::Tags::HighlightBlock

  def initialize(tag_name, lang, tokens)
    if lang.strip == ""
      super(tag_name, "none", tokens)
    else
      super(tag_name, lang, tokens)
    end
  end

end

Liquid::Template.register_tag('code', Code)

class Screen < Liquid::Tag

  def initialize(tag_name, image, tokens)
    super
    @image = image.strip
  end

  def render(context)
    "<div class=\"bd-screen\">" \
    "<figure class=\"figure\">" \
    "<img class=\"figure-img screenshot border\" src=\"/assets/img/#{@image}\">" \
    "</figure></div>"
  end

end

Liquid::Template.register_tag('screen', Screen)

class ScreenCap < Liquid::Block

  def initialize(tag_name, image, tokens)
    super
    @image = image.strip
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    caption = converter.convert(super(context))
    "<div class=\"bd-callout bd-callout-imagecap\">" \
    "<figure class=\"figure\">" \
    "<img class=\"figure-img screenshot\" src=\"/assets/img/#{@image}\">" \
    "<figcaption class=\"figure-caption\">#{caption}</figcaption>" \
    "</figure></div>"
  end

end

Liquid::Template.register_tag('screencap', ScreenCap)

class Todo < Liquid::Block

  def render(context)
  end

end

Liquid::Template.register_tag('todo', Todo)

class CalloutTag < Liquid::Block

  def initialize(tag_name, type, tokens)
    super
    @type = type.strip!
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    output = converter.convert(super(context))
    if @type != ""
      "<div class=\"bd-callout bd-callout-#{@type}\">#{output}</div>"
    else
      "<div class=\"bd-callout\">#{output}</div>"
    end
  end
end

Liquid::Template.register_tag('callout', CalloutTag)

class InsightTag < Liquid::Block

  def initialize(tag_name, title, tokens)
    super
    title.strip!
    if title == ""
      @title = "Insight"
    else
      @title = title
    end
  end

  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    output = converter.convert(super(context))
    "<div class=\"bd-callout bd-callout-warning\"><h4>#{@title}</h4>#{output}</div>"
  end
end

Liquid::Template.register_tag('insight', InsightTag)

class MarkdownBlock < Liquid::Block
  alias_method :render_block, :render

  def initialize(tag_name, markup, tokens)
    super
  end

  # Uses the default Jekyll markdown parser to
  # parse the contents of this block
  #
  def render(context)
    site = context.registers[:site]
    converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
    converter.convert(render_block(context))
  end
end

Liquid::Template.register_tag('markdown', MarkdownBlock)
