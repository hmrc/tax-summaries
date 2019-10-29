package transformers

import transformers.Descripters._
import transformers.Operation._
import models.{Amount, Liability}
import models.Liability.{CgGainsAfterLosses, CgTotGainsAfterLosses}
import uk.gov.hmrc.play.test.UnitSpec

class ATSDataInterpreterSpec extends UnitSpec {

  "Calling DataInterpreter" should {
    "return an empty amount when passed an empty amount" in {
      val values = Map[String, Amount]()
      ATSDataInterpretor.interpret(Term(""))(values, None) shouldBe Amount.empty
    }

    "should return the value of a term when a list of terms is passed" in {
      val values = Map(1 -> Amount(1.00, "GBP"))
      ATSDataInterpretor.interpret(Term(1))(values, None) shouldBe Amount(1.0, "GBP")
    }

    //    "should return a the value rounded up" in {
    //      val values=Map("Pensions" -> Amount(1.25, "GDP"))
    //      ATSDataInterpretor.interpret(Term("Pensions"))(values, None).roundAmountUp shouldBe Amount(2.00,"GBP")
    //    }

    "should add values when sum is passed with 2 values" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP")
      )

      ATSDataInterpretor.interpret(Sum(
        Term("Pensions"),
        Term("BasicRate")))(values, None) shouldBe Amount(500.0, "GBP")
    }

    "should add values when 4 values to sum is passed in" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP"),
        "Dividends" -> Amount(100.00, "GBP"),
        "Savings" -> Amount(400.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Sum(
          Term("Pensions"),
          Term("BasicRate"),
          List(Term("Dividends"), Term("Savings"))
        )
      )(values, None) shouldBe Amount(1000.00, "GBP")
    }

    "should add values where there are sums inside the sum" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "NationalInsurance" -> Amount(500.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP"),
        "Dividends" -> Amount(100.00, "GBP"),
        "Savings" -> Amount(400.00, "GBP"),
        "ValueToIgnore" -> Amount(600.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Sum(
          Term("Pensions"),
          Sum(
            Term("BasicRate"),
            Term("NationalInsurance")
          ),
          List(Sum(Term("Dividends"), Term("Savings")))
        )
      )(values, None) shouldBe Amount(1500.00, "GBP")
    }


    "should subtract when difference is passed using 2 values" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP")
      )
      ATSDataInterpretor.interpret(
        Difference(
          Term("Pensions"),
          Term("BasicRate"))
      )(values, None) shouldBe Amount(-100.0, "GBP")
    }

    "should subtract when difference is passed using 4 values" in {
      val values = Map(
        "Pensions" -> Amount(1000, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP"),
        "NationalInsurance" -> Amount(500, "GBP"),
        "Dividends" -> Amount(100.00, "GBP"),
        "Savings" -> Amount(200.00, "GBP"),
        "ValueToIgnore" -> Amount(600.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Difference(
          Term("Pensions"),
          Term("BasicRate"),
          List(Term("NationalInsurance"), Term("Dividends"), Term("Savings"))
        ))(values, None) shouldBe Amount(-100.0, "GBP")
    }

    "should subtract values when difference is passed but return zero if result is negative" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP"))

      ATSDataInterpretor.interpret(
        Difference(
          Term("Pensions"),
          Term("BasicRate")
        ).positive)(values, None) shouldBe Amount(0.0, "GBP")
    }

    "should correctly calculate with a mixture of sum and difference" in {
      val values = Map(
        "Pensions" -> Amount(1000, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP"),
        "NationalInsurance" -> Amount(100, "GBP"),
        "Dividends" -> Amount(100.00, "GBP"),
        "Savings" -> Amount(200.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Sum(
          Term("Pensions"),
          Difference(
            Term("BasicRate"),
            Term("NationalInsurance")
          ),
        List(Term("Dividends"),Term("Savings")
        )
      ))(values, None) shouldBe Amount(1500.00,"GBP")
    }

    "should return correct result from filter where a predicate condition is met" in {
      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Sum[String,Int](
          Term("Pensions"),
          Term("BasicRate")
        ).filter {
          case ("Pensions", 20) => true
          case ("Pensions", _)  => false
          case _                => true
        }
      )(values, 20) shouldBe Amount(500.00, "GBP")
    }

    "should return correct result from filter where a predicate condition is not met" in {

      val values = Map(
        "Pensions" -> Amount(200.00, "GBP"),
        "BasicRate" -> Amount(300.00, "GBP")
      )

      ATSDataInterpretor.interpret(
        Sum[String,Int](
          Term("Pensions"),
          Term("BasicRate")
        ).filter {
          case ("Pensions", 20) => true
          case ("Pensions", _)  => false
          case _                => true
        }
      )(values, 200) shouldBe Amount(300.00, "GBP")
    }
  }

}
