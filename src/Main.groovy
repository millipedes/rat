import static Parsing.bind
import static Parsing.parse_char

static void main(String[] args) {
  def file = new File("resources/test.txt")
  def ps = new ParseState<Integer>(file.text)

  def x = bind(parse_char('T' as char),
               { Unit a -> parse_char('h' as char) })(ps.stream)
  println x.data
  println x.stream
}
