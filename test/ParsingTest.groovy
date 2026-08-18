import spock.lang.Specification

import java.nio.CharBuffer

import static ParsingCore.*
import static Parsing.*

class ParsingSpec extends Specification {
  def test_bind() {
    when:
    def input_char_buf = CharBuffer.wrap(input_string)
    def result = bind(parse_char(target_char_one),
                      { Unit a -> parse_char(target_char_two) }).apply(input_char_buf)

    then:
    result.data.isRight() == should_succeed
    if (should_succeed) {
      result.data.get() == success_value
    } else {
      result.data.getLeft() == error_value
    }
    result.stream.toString() == stream_remaining

    where:
    input_string | target_char_one | target_char_two || should_succeed | success_value | error_value                          | stream_remaining
    "ab"         | "a" as char     | "b" as char     || true           | Unit.Instance | null                                 | ""
    "ax"         | "a" as char     | "b" as char     || false          | null          | "parse_char: char \'b\' not matched" | "x"
    "abc"        | "a" as char     | "b" as char     || true           | Unit.Instance | null                                 | "c"
  }

  def test_parse_char() {
    when:
    def input_char_buf = CharBuffer.wrap(input_string)
    def result = parse_char(targetChar).apply(input_char_buf)

    then:
    result.data.isRight() == should_succeed
    if (should_succeed) {
      result.data.get() == success_value
    } else {
      result.data.getLeft() == error_value
    }
    result.stream.toString() == stream_remaining

    where:
    input_string | targetChar  || should_succeed | success_value  | error_value                          | stream_remaining
    "a"          | "a" as char || true           | Unit.Instance  | null                                 | ""
    "ab"         | "a" as char || true           | Unit.Instance  | null                                 | "b"
    "x"          | "a" as char || false          | null           | "parse_char: char \'a\' not matched" | "x"
  }

  def test_parse_int() {
    when:
    def input_char_buf = CharBuffer.wrap(input_string)
    def result = parse_int().apply(input_char_buf)

    then:
    result.data.isRight() == should_succeed
    if (should_succeed) {
      result.data.get() == success_value
    } else {
      result.data.getLeft() == error_value
    }
    result.stream.toString() == stream_remaining

    where:
    input_string || should_succeed | success_value | error_value                          | stream_remaining
    "1"          || true           | 1             | null                                 | ""
    "123"        || true           | 123           | null                                 | ""
    "123.123"    || true           | 123           | null                                 | ".123"
    "a123"       || false          | null          | "parse_int: No integer to be parsed" | "a123"
    "+123"       || false          | null          | "parse_int: No integer to be parsed" | "+123"
    "-123"       || false          | null          | "parse_int: No integer to be parsed" | "-123"
  }

  def test_parse_fp() {
    when:
    def input_char_buf = CharBuffer.wrap(input_string)
    def result = parse_fp().apply(input_char_buf)

    then:
    result.data.isRight() == should_succeed
    if (should_succeed) {
      result.data.get() == success_value
    } else {
      result.data.getLeft() == error_value
    }
    result.stream.toString() == stream_remaining

    where:
    input_string || should_succeed | success_value | error_value                                | stream_remaining
    "1."         || true           | 1.0           | null                                       | ""
    ".1"         || true           | 0.1           | null                                       | ""
    "123.123"    || true           | 123.123       | null                                       | ""
    "1e123"      || true           | 1e123         | null                                       | ""
    "1.e123"     || true           | 1e123         | null                                       | ""
    ".1e123"     || true           | .1e123        | null                                       | ""
    "1.e+123"    || true           | 1e+123        | null                                       | ""
    ".1e+123"    || true           | .1e+123       | null                                       | ""
    "1.e-123"    || true           | 1e-123        | null                                       | ""
    ".1e-123"    || true           | .1e-123       | null                                       | ""
    "1.E123"     || true           | 1e123         | null                                       | ""
    ".1E123"     || true           | .1e123        | null                                       | ""
    "1.E+123"    || true           | 1e+123        | null                                       | ""
    ".1E+123"    || true           | .1e+123       | null                                       | ""
    "1.E-123"    || true           | 1e-123        | null                                       | ""
    ".1E-123"    || true           | .1e-123       | null                                       | ""
    "1"          || false          | null          | "parse_fp: number parsed is an integer"    | "1"
    "e1"         || false          | null          | "parse_fp: No exponent lhs argument"       | "e1"
    "1e"         || false          | null          | "parse_fp: No exponent rhs argument"       | "1e"
    "1eabc"      || false          | null          | "parse_fp: No exponent rhs argument"       | "1eabc"
    "eabc"       || false          | null          | "parse_fp: No floating point to be parsed" | "eabc"
  }
}
