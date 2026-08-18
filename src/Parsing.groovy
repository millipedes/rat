import io.vavr.control.Either
import java.nio.CharBuffer
import java.util.function.Function

import static ParsingCore.*

static <T> ParseState<T> make_left(CharBuffer cb, String error) {
  def ps = new ParseState<T>(cb)
  ps.data = Either.left(error)
  return ps
}

static <T> ParseState<T> make_right(CharBuffer cb, T data) {
  def ps = new ParseState<T>(cb)
  ps.data = Either.right(data)
  return ps
}

static Function<CharBuffer, ParseState<Unit>> parse_char(char c) {
  { CharBuffer cb ->
    if (cb.get(cb.position()) == c) {
      cb.position(cb.position() + 1)
      return make_right(cb, Unit.Instance)
    } else {
      return make_left(cb, "parse_char: char \'$c\' not matched")
    }
  }
}

static Function<CharBuffer, ParseState<BigInteger>> parse_int() {
  { CharBuffer cb ->
    def cbc = new LocalCharBufferContext(cb)
    if (!cbc.test_against(cb, Character.&isDigit)) {
      return make_left(cb, "parse_int: No integer to be parsed")
    }
    while (cbc.in_bounds() && cbc.test_against(cb, Character.&isDigit)) cbc.i++
    def data = new BigInteger(cbc.snip(cb))
    cb.position(cbc.buf_start + cbc.i)
    return make_right(cb, data)
  }
}

static Function<CharBuffer, ParseState<BigDecimal>> parse_fp() {
  { CharBuffer cb ->
    def cbc = new LocalCharBufferContext(cb)
    def deci = false
    def exp = false
    if (!cbc.test_against(cb, Character.&isDigit)
      && !cbc.test_against(cb, ParsingCore.&is_decimal)) {
      return make_left(cb, "parse_fp: No floating point to be parsed")
    }

    // Pre Decimal
    while (cbc.in_bounds() && cbc.test_against(cb, Character.&isDigit)) cbc.i++

    // Decimal
    if (cbc.in_bounds() && cbc.test_against(cb, ParsingCore.&is_decimal)) {
      deci = true
      cbc.i++
      // Post Decimal
      while (cbc.in_bounds() && cbc.test_against(cb, Character.&isDigit)) cbc.i++
    }

    // E?
    if (cbc.in_bounds() && cbc.test_against(cb, ParsingCore.&is_exp)) {
      // E lhs
      if (cbc.i == 0) {
        return make_left(cb, "parse_fp: No exponent lhs argument")
      }
      exp = true
      cbc.i++
      // E sign?
      if (cbc.in_bounds() && cbc.test_against(cb, ParsingCore.&is_un_op)) cbc.i++

      if (!cbc.in_bounds() || !cbc.test_against(cb, Character.&isDigit)) {
        return make_left(cb, "parse_fp: No exponent rhs argument")
      }

      // E rhs
      while (cbc.in_bounds() && cbc.test_against(cb, Character.&isDigit)) cbc.i++
    }

    if (!deci && !exp) {
        return make_left(cb, "parse_fp: number parsed is an integer")
    }

    def data = new BigDecimal(cbc.snip(cb))
    cb.position(cbc.buf_start + cbc.i)
    return make_right(cb, data)
  }
}

// static Function<CharBuffer, ParseState<String>> parse_string() {
//   { CharBuffer cb ->
//     def cbc = new LocalCharBufferContext(cb)
//     if (!cdc.test_against(cb, ParsingCore.&is_double_quote)) {
//       return make_left("parse_string: no opening quote")
//     }
//   }
// }
