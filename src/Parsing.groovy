import io.vavr.control.Either
import java.nio.CharBuffer
import java.util.function.Function

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

enum Unit { Instance }

static <T, U> Function<CharBuffer, ParseState<U>> \
bind(Function<CharBuffer, ParseState<T>> f,
     Function<T, Function<CharBuffer, ParseState<U>>> g) {
  { CharBuffer s ->
    def a = f(s)
    if (a.data.isRight()) {
      g(a.data.get())(a.stream)
    } else {
      def ps = new ParseState<U>(a.stream)
      ps.data = Either.left(a.data.getLeft())
      ps
    }
  }
}

static Function<CharBuffer, ParseState<Unit>> parse_char(char c) {
  { CharBuffer s ->
    if (s.get(s.position()) == c) {
      s.position(s.position() + 1)
      def ps = new ParseState<Unit>(s)
      ps.data = Either.right(Unit.Instance)
      ps
    } else {
      def ps = new ParseState<Unit>(s)
      ps.data = Either.left("parse_char: char \'$c\' not matched")
      ps
    }
  }
}

// // cb.subSequence(start, end).toString().toBigBigInteger | toDouble | toFloat
// static Function<CharBuffer, ParseState<BigInteger>> parse_int() {
// }
