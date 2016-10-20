require 'redcloth'
require 'java'

java_import "uk.ac.warwick.util.content.textile2.jruby.TextileService"

class RedClothTextileEngine
  include TextileService
  def textileToHtml(textile, hard_breaks)
    r = RedCloth.new( textile )
    r.hard_breaks = hard_breaks
    r.to_html(:textile).to_s
  end
end
RedClothTextileEngine.new