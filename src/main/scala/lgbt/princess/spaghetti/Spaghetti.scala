package lgbt.princess.spaghetti

import scala.annotation.tailrec
import scala.collection.mutable.{LinkedHashMap, SeqMap}
import scala.util.control.ControlThrowable

/* control flow exceptions */
private final case class Jump(label: String) extends ControlThrowable(label)
private final case class Return[A](value: A) extends ControlThrowable

/* trampoline states */
private sealed trait DoNext
private object FromStart                          extends DoNext
private final case class FromLabel(label: String) extends DoNext
private object NoReturn                           extends DoNext
private final case class ReturnValue[A](value: A) extends DoNext

/** A scope for writing and executing spaghetti code that uses `goto`s. */
final class Spaghetti[A] private[spaghetti] (
    expectsReturn: Boolean,
    labels: SeqMap[String, () => Unit] = LinkedHashMap.empty,
    private[this] var frozen: Boolean = false
):
  private[spaghetti] def addLabel(label: String, block: () => Unit): Unit =
    if frozen then throw new IllegalStateException("cannot nest labels")
    else if labels.contains(label) then throw new IllegalArgumentException(s"already defined label: $label")
    else labels(label) = block

  private[spaghetti] def jump(label: String): Nothing =
    if labels.contains(label) then throw Jump(label)
    else throw new IllegalArgumentException(s"goto nonexistent label: $label")

  private[spaghetti] def ret(value: A): Nothing = throw new Return(value)

  /** Execute the code starting from the first label. */
  private[spaghetti] def eat(): A =
    frozen = true
    slurp(FromStart)

  /** Execute the next action, recursively. */
  @tailrec
  private[this] def slurp(next: DoNext): A =
    next match
      case FromStart                        => slurp(bite(labels.iterator))
      case FromLabel(label)                 => slurp(bite(labels.iterator.dropWhile(_._1 != label)))
      case ReturnValue(value: A @unchecked) => value
      case NoReturn =>
        if expectsReturn then throw new IllegalStateException("no value returned; must call `ret` method")
        else ().asInstanceOf[A]

  /** Execute all of the given blocks of code. */
  private[this] def bite(it: Iterator[(String, () => Unit)]): DoNext =
    try
      while it.hasNext do it.next()._2()
      NoReturn
    catch
      case Jump(label)                 => FromLabel(label)
      case Return(value: A @unchecked) => ReturnValue(value)

end Spaghetti

/** Context available within the body of a label, needed for [[`goto`]] and [[`ret`]]. */
final class LabelBody[A] private[spaghetti] (private[spaghetti] val s: Spaghetti[A])

/**
 * Execute (spaghetti) code that uses `goto`s and returns a value.
 *
 * The value must be returned explicitly by calling [[`ret`]].
 *
 * It is strongly recommended that all code to be executed be within [[`label`]]s; doing otherwise may not behave as
 * desired.
 *
 * @param init
 *   the body of the code, containing calls to [[`label`]]
 * @tparam A
 *   the type of value returned
 * @throws IllegalStateException
 *   if no value is explicitly returned
 * @return
 *   the value returned by the code
 */
@throws[IllegalStateException]
def spaghettiWithReturn[A](init: Spaghetti[A] ?=> Unit): A =
  given s: Spaghetti[A] = new Spaghetti(expectsReturn = true)
  init
  s.eat()

/**
 * Execute (spaghetti) code that uses `goto`s and does not return anything.
 *
 * It is strongly recommended that all code to be executed be within [[`label`]]s; doing otherwise may not behave as
 * desired.
 *
 * @param init
 *   the body of the code, containing calls to [[`label`]]
 */
def unitSpaghetti(init: Spaghetti[Unit] ?=> Unit): Unit =
  given s: Spaghetti[Unit] = new Spaghetti(expectsReturn = false)
  init
  s.eat()

/**
 * Create a labelled block of code that can be jumped to.
 *
 * Must be called from within the body of [[`spaghettiWithReturn`]] or [[`unitSpaghetti`]].
 *
 * @param name
 *   the name of the label
 * @param block
 *   the code following that label
 */
def label[A](name: String)(block: LabelBody[A] ?=> Unit)(using s: Spaghetti[A]): Unit =
  given body: LabelBody[A] = new LabelBody(s)
  s.addLabel(name, () => block)

/**
 * Jump to the specified label.
 *
 * Must be called from within the body of a label.
 */
def goto(label: String)(using body: LabelBody[?]): Nothing = body.s.jump(label)

/**
 * Return a value from [[`spaghettiWithReturn`]].
 *
 * Must be called from within the body of a label.
 *
 * @param value
 *   the value to return
 * @tparam A
 *   the type of value returned
 */
def ret[A](value: A)(using body: LabelBody[A]): Nothing = body.s.ret(value)
