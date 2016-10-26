import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("features"),
  glue = Array("steps"),
  tags = Array("~@pending"),
  format = Array("pretty", "html:target/reports/cucumber")
)
class RunCukes
