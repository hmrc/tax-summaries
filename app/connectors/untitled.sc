import play.api.libs.json.JsValue
import play.api.mvc.Result
import uk.gov.hmrc.http.HttpResponse

import scala.collection.immutable.Seq
//val list: List[Option[Int]] =
//  List(Some(1),Some(2),Some(3),None,None,None)
//
//list map { OptionNumber => OptionNumber map {
//  number => number + 10 }}
//
//list flatMap { OptionNumber => OptionNumber map {
//  number => number + 10 }}
//
//val simpleList = List(1,2,3,4,5,6)
//simpleList.filter(x => x==1)
//
//val seqEither: Seq[Either[Int, String]] =
//  List(Right("hello"), Left(1), Right("alright"), Left(2))
//
//val x = seqEither.filter(x => x.isRight)
//
//  x map {left => left.toOption.get}
//
//if (seqEither.filter(x => x.isLeft).head == Left(1)) true else false
//
//list map {x => x.get}

val noneList: List[Option[String]] = List()
noneList.headOption

val x : Seq[Either[Int, JsValue]] = Seq(Left(1), Left(1), Left(1), Left(1), Left(1))
x.filter(either => either.isRight) flatMap { right =>
  right.toOption
}