package lgbt.princess.spaghetti

import org.junit.Test
import org.junit.Assert._

class SpaghettiTest {

  @Test(expected = classOf[IllegalArgumentException])
  def duplicateLabel(): Unit =
    unitSpaghetti {
      label("a") {}
      label("a") {}
    }

  @Test(expected = classOf[IllegalArgumentException])
  def gotoNonexistent(): Unit =
    unitSpaghetti {
      label("a") {
        goto("b")
      }
    }

  @Test(expected = classOf[IllegalStateException])
  def nestedLabels(): Unit =
    unitSpaghetti {
      label("a") {
        label("b") {}
      }
    }

  @Test(expected = classOf[IllegalStateException])
  def missingReturn(): Unit =
    spaghettiWithReturn[Int] {}

  @Test
  def whileEquivalent1(): Unit =
    def _while(cond: => Boolean)(body: => Unit): Unit =
      unitSpaghetti {
        label("loop") {
          if cond then
            body
            goto("loop")
        }
      }

    var i = 0

    _while(i < 10)(i += 1)
    assertEquals(10, i)

    _while(i < 10)(i += 1)
    assertEquals(10, i)

  @Test
  def whileEquivalent2(): Unit =
    def _while(cond: => Boolean)(body: => Unit): Unit =
      unitSpaghetti {
        label("loop") {
          if !cond then goto("end")
          body
          goto("loop")
        }
        label("end") {}
      }

    var i = 0

    _while(i < 10)(i += 1)
    assertEquals(10, i)

    _while(i < 10)(i += 1)
    assertEquals(10, i)

  @Test
  def doWhileEquivalent(): Unit =
    def doWhile(body: => Unit)(cond: => Boolean): Unit =
      unitSpaghetti {
        label("loop") {
          body
          if cond then goto("loop")
        }
      }

    var i = 0

    doWhile(i += 1)(i < 10)
    assertEquals(10, i)

    doWhile(i += 1)(i < 10)
    assertEquals(11, i)

  @Test
  def terrible1(): Unit =
    def bad(a: Int, b: Int): Int =
      var x = a
      var y = b
      spaghettiWithReturn[Int] {
        label("1") {
          if x > y then goto("3")
        }
        label("2") {
          x += 8
          if x < y then goto("4")
        }
        label("3") {
          if x + y > 100 then goto("4")
          y += 10
          goto("1")
        }
        label("4") {
          ret(x + y)
        }
      }

    assertEquals(98, bad(0, 0))
    assertEquals(18, bad(0, 10))
    assertEquals(118, bad(10, 0))
    assertEquals(110, bad(60, 50))

  end terrible1

  @Test
  def terrible2(): Unit =
    var sum = 0
    unitSpaghetti {
      // aaaaaaaaaaaa
      for (i <- 1 to 10)
        do label(s"$i") { sum += i }
    }
    assertEquals(55, sum)

  @Test
  def terrible3(): Unit =
    var sum = 0
    unitSpaghetti {
      label("") {
        goto("6")
      }
      // aaaaaaaaaaaa
      for (i <- 1 to 10)
        do label(s"$i") { sum += i }
    }
    assertEquals(40, sum)

  @Test
  def duffsDevice(): Unit =
    class Output:
      private var sum: Int     = 0
      def :=(value: Int): Unit = sum += value
      def read: Int            = sum

    def send(to: Output, from: Array[Int], count: Int): Unit =
      require(count <= from.length)
      unitSpaghetti {
        var idx = 0
        var n   = (count + 7) / 8
        label("") {
          goto(s"${count % 8}")
        }
        for (i <- 8 to 1 by -1)
          do
            label(s"${i % 8}") {
              to := from(idx)
              idx += 1
            }
        label("end") {
          n -= 1
          if n > 0 then goto("0")
        }
      }

    val ones = Array.fill[Int](100)(1)

    def check(count: Int): Unit =
      val out = new Output
      send(out, ones, count)
      assertEquals(count, out.read)

    check(1)
    check(7)
    check(8)
    check(10)
    check(31)
    check(32)
    check(50)
    check(100)

  end duffsDevice
}
