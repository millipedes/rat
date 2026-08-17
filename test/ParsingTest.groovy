import spock.lang.Specification

import java.nio.CharBuffer

import static Parsing.bind
import static Parsing.parse_char

class ParsingSpec extends Specification {
  def test_parse_char_0() {
    when:
    def inputCharBuffer = CharBuffer.wrap(inputString)
    def result = parse_char(targetChar).apply(inputCharBuffer)

    then:
    result.data.isRight() == shouldSucceed
    if (shouldSucceed) {
      result.data.get() == successValue
    } else {
      result.data.getLeft() == errorValue
    }
    result.stream.toString() == streamRemaining

    where:
    inputString | targetChar  || shouldSucceed | successValue  | errorValue                           | streamRemaining
    "a"         | "a" as char || true          | Unit.Instance | null                                 | ""
    "ab"        | "a" as char || true          | Unit.Instance | null                                 | "b"
    "x"         | "a" as char || false         | null          | "parse_char: char \'a\' not matched" | "x"
  }

  def test_parse_char_1() {
    when:
    def inputCharBuffer = CharBuffer.wrap(inputString)
    def result = bind(parse_char(targetCharOne),
                      { Unit a -> parse_char(targetCharTwo) }).apply(inputCharBuffer)

    then:
    result.data.isRight() == shouldSucceed
    if (shouldSucceed) {
      result.data.get() == successValue
    } else {
      result.data.getLeft() == errorValue
    }
    result.stream.toString() == streamRemaining

    where:
    inputString | targetCharOne | targetCharTwo || shouldSucceed | successValue  | errorValue                           | streamRemaining
    "ab"        | "a" as char   | "b" as char   || true          | Unit.Instance | null                                 | ""
    "ax"        | "a" as char   | "b" as char   || false         | null          | "parse_char: char \'b\' not matched" | "x"
    "abc"       | "a" as char   | "b" as char   || true          | Unit.Instance | null                                 | "c"
  }
}
