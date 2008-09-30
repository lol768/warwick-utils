require 'redcloth'

class RedClothTextileEngine
  def textileToHtml(textile, hard_breaks)
    r = RedCloth.new( textile )
    r.hard_breaks = hard_breaks
    r.to_html(:textile).to_s
  end
end
RedClothTextileEngine.new