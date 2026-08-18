import io.vavr.control.Either
import java.nio.CharBuffer
import java.util.function.Function

enum Unit { Instance }

class ParseState<T> {
  Either<String, T> data
  CharBuffer stream

  ParseState(String seed) {
    stream = CharBuffer.wrap(seed)
  }

  ParseState(CharBuffer s) {
    stream = s
  }
}

/**
 * This is just a little helper as we want to avoid calling position()/length()
 * several times per parsing function, and these are requisite for most
 * primitive parsing functions.
 */
class LocalCharBufferContext {
  int i
  int buf_start
  int buf_length

  LocalCharBufferContext(CharBuffer cb) {
    i = 0
    buf_start = cb.position()
    buf_length = cb.length()
  }

  Boolean in_bounds() {
    i + buf_start < buf_length
  }

  Boolean test_against(CharBuffer cb, Function<Character, Boolean> comp) {
    comp(cb.get(buf_start + i))
  }

  String snip(CharBuffer cb) {
    cb.subSequence(buf_start, i + buf_start).toString()
  }
}

static <T, U> Function<CharBuffer, ParseState<U>> \
bind(Function<CharBuffer, ParseState<T>> f,
     Function<T, Function<CharBuffer, ParseState<U>>> g) {
  { CharBuffer cb ->
    def a = f(cb)
    if (a.data.isRight()) {
      g(a.data.get())(a.stream)
    } else {
      def ps = new ParseState<U>(a.stream)
      ps.data = Either.left(a.data.getLeft())
      ps
    }
  }
}

static Boolean is_exp(char c) {
  (c == "e" || c == "E")
}

static Boolean is_decimal(char c) {
  c == "."
}

static Boolean is_un_op(char c) {
  c == "-" || c == "+"
}

static Boolean is_double_quote(char c) {
  c == "\""
}
