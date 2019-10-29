package transformers

import models.Amount

object ATSDataInterpretor {

  def interpret[A, B](op: Operation[A, B])(implicit values: Map[A, Amount], data: B): Amount =
    op match {
      case Empty() => Amount.empty
      case Term(value) => values.getOrElse(value, Amount.empty)
      case Filter(op, pred: ((A, B) => Boolean)) => interpret(interpretFilter(op, pred)(data))
      case Sum(a, b, cs) => { println("there!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        interpret(a) + interpret(b) + cs.map(interpret(_)).foldLeft(Amount.empty)(_ + _)
      }
      case Difference(a, b, cs) => {
        println("HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + a)
        interpret(a) - interpret(b) - cs.map(interpret(_)).foldLeft(Amount.empty)(_ - _)
      }
      case RoundUp(op) => interpret(op).roundAmountUp()
      case Positive(op) =>
        val result = interpret(op)
        if (result < Amount.empty) Amount.empty else result
    }

  def interpretFilter[A, B](op: Operation[A, B], predicate: (A, B) => Boolean)(implicit data: B): Operation[A, B] = {

    def filter(
                ops: List[Operation[A, B]],
                apply: (Operation[A, B], Operation[A, B], List[Operation[A, B]]) => Operation[A, B]): Operation[A, B] = {
      ops.foldLeft(Nil: List[Operation[A, B]]) {
        (acc, cur) =>
          interpretFilter(cur, predicate) match {
            case Empty() => acc
            case other => acc ++ List(other)
          }
      } match {
        case a :: b :: cs => apply(a, b, cs)
        case a :: Nil => a
        case Nil => Empty()
      }
    }

    op match {
      case term@Term(value)
        if predicate(value, data) => term
      case Term(_) => Empty()
      case Empty() => Empty()
      case Filter(op, pred: ((A, B) => Boolean)) => interpretFilter(interpretFilter(op, pred), predicate)
      case Sum(a, b, cs) => filter(a :: b :: cs, Sum[A, B])
      case Difference(a, b, cs) => filter(a :: b :: cs, Difference[A, B])
      case RoundUp(op) => RoundUp(interpretFilter(op, predicate))
      case Positive(op) => Positive(interpretFilter(op, predicate))
    }
  }

}